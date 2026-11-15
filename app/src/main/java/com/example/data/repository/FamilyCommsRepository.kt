package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyCall
import com.example.data.model.FamilyMoment
import com.example.data.model.MomentComment
import com.example.data.model.PrivateChat
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FamilyCommsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus login sebelum mengakses komunikasi keluarga.")

    fun observePrivateChats(familyId: String): Flow<List<PrivateChat>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/chats"
        emitAll(
            db.collection("families").document(familyId).collection("chats")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PrivateChat::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMessages(familyId: String, roomId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/messages"
        emitAll(
            db.collection("families").document(familyId).collection("messages")
                .whereEqualTo("roomId", roomId)
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun getOrCreatePrivateChat(
        familyId: String,
        otherUid: String,
        currentName: String,
        otherName: String
    ): Result<String> {
        val uid = requireUserId()
        val sortedUids = listOf(uid, otherUid).sorted()
        val chatId = "dm_${sortedUids[0].take(16)}_${sortedUids[1].take(16)}"
        val chatRef = db.collection("families").document(familyId).collection("chats").document(chatId)
        return try {
            val snap = chatRef.get().await()
            if (!snap.exists()) {
                val names = if (sortedUids[0] == uid) listOf(currentName, otherName) else listOf(otherName, currentName)
                chatRef.set(
                    mapOf(
                        "familyId" to familyId,
                        "participantNames" to names,
                        "memberUids" to sortedUids,
                        "lastMessageText" to "Mulai percakapan keluarga",
                        "lastMessageSender" to currentName,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(chatId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, chatRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        familyId: String,
        roomId: String,
        roomType: String,
        senderName: String,
        text: String,
        messageType: String = "text",
        mediaLabel: String = "",
        replyToText: String = "",
        replyToSender: String = "",
        forwarded: Boolean = false,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val familyRef = db.collection("families").document(familyId)
        val msgRef = familyRef.collection("messages").document(msgId)
        val roomRef = if (roomType == "group") {
            familyRef.collection("groups").document(roomId)
        } else {
            familyRef.collection("chats").document(roomId)
        }
        val finalMembers = (memberUids + uid).distinct()
        val preview = when (messageType) {
            "image" -> "[Foto] ${mediaLabel.ifBlank { text }}"
            "video" -> "[Video] ${mediaLabel.ifBlank { text }}"
            "document" -> "[Dokumen] ${mediaLabel.ifBlank { text }}"
            "voice" -> "[Pesan Suara] ${mediaLabel.ifBlank { text }}"
            "sticker" -> text
            else -> text
        }.take(120)

        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "roomId" to roomId,
                "roomType" to roomType,
                "senderId" to uid,
                "senderName" to senderName,
                "text" to text,
                "messageType" to messageType,
                "forwarded" to forwarded,
                "status" to "delivered",
                "readByUids" to listOf(uid),
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (mediaLabel.isNotBlank()) payload["mediaLabel"] = mediaLabel.take(280)
            if (replyToText.isNotBlank()) payload["replyToText"] = replyToText.take(280)
            if (replyToSender.isNotBlank()) payload["replyToSender"] = replyToSender.take(75)

            val batch = db.batch()
            batch.set(msgRef, payload)
            batch.update(
                roomRef,
                mapOf(
                    "lastMessageText" to preview,
                    "lastMessageSender" to senderName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(msgId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(familyId: String, messages: List<ChatMessage>) {
        val uid = auth.currentUser?.uid ?: return
        val unread = messages.filter { it.senderId != uid && !it.readByUids.contains(uid) && it.id.isNotBlank() }
        if (unread.isEmpty()) return
        try {
            val batch = db.batch()
            unread.take(20).forEach { msg ->
                val ref = db.collection("families").document(familyId).collection("messages").document(msg.id)
                batch.update(
                    ref,
                    mapOf(
                        "status" to "read",
                        "readByUids" to FieldValue.arrayUnion(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
        } catch (_: Exception) {
            // Non-fatal read receipt update
        }
    }

    fun observeMoments(familyId: String): Flow<List<FamilyMoment>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/moments"
        emitAll(
            db.collection("families").document(familyId).collection("moments")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FamilyMoment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMomentComments(familyId: String, momentId: String): Flow<List<MomentComment>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/moments/$momentId/comments"
        emitAll(
            db.collection("families").document(familyId).collection("moments")
                .document(momentId).collection("comments")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(MomentComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createMoment(
        familyId: String,
        authorName: String,
        authorRoleLabel: String,
        caption: String,
        momentType: String,
        mediaLabel: String,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val momentId = "mom_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("moments").document(momentId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "authorId" to uid,
                "authorName" to authorName,
                "authorRoleLabel" to authorRoleLabel,
                "caption" to caption.trim(),
                "momentType" to momentType,
                "likedByUids" to emptyList<String>(),
                "commentCount" to 0L,
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (mediaLabel.isNotBlank()) payload["mediaLabel"] = mediaLabel.take(280)
            ref.set(payload).await()
            Result.success(momentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun toggleMomentLike(familyId: String, moment: FamilyMoment): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("families").document(familyId).collection("moments").document(moment.id)
        val alreadyLiked = moment.likedByUids.contains(uid)
        return try {
            ref.update(
                mapOf(
                    "likedByUids" to if (alreadyLiked) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun addMomentComment(
        familyId: String,
        momentId: String,
        authorName: String,
        text: String,
        memberUids: List<String>
    ): Result<Unit> {
        val uid = requireUserId()
        val commentId = "cmt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val momentRef = db.collection("families").document(familyId).collection("moments").document(momentId)
        val commentRef = momentRef.collection("comments").document(commentId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val batch = db.batch()
            batch.set(
                commentRef,
                mapOf(
                    "momentId" to momentId,
                    "authorId" to uid,
                    "authorName" to authorName,
                    "text" to text.trim(),
                    "memberUids" to finalMembers,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                momentRef,
                mapOf(
                    "commentCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    fun observeCalls(familyId: String): Flow<List<FamilyCall>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/calls"
        emitAll(
            db.collection("families").document(familyId).collection("calls")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FamilyCall::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun startCallRoom(
        familyId: String,
        callerName: String,
        callType: String,
        targetName: String,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("calls").document(callId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            ref.set(
                mapOf(
                    "familyId" to familyId,
                    "callerId" to uid,
                    "callerName" to callerName,
                    "callType" to callType,
                    "targetName" to targetName,
                    "status" to "ringing",
                    "participantUids" to listOf(uid),
                    "memberUids" to finalMembers,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(callId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun updateCallStatus(familyId: String, callId: String, status: String, joinAsParticipant: Boolean = false): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("families").document(familyId).collection("calls").document(callId)
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (joinAsParticipant) {
                updates["participantUids"] = FieldValue.arrayUnion(uid)
            }
            ref.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    private fun logStreamError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
