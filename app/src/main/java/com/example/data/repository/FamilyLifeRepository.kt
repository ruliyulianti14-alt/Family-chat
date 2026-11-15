package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CalendarEvent
import com.example.data.model.FundTransaction
import com.example.data.model.GalleryAlbum
import com.example.data.model.SafetyAlert
import com.example.data.model.TreeNode
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

class FamilyLifeRepository(
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
            ?: throw IllegalStateException("Pengguna harus login sebelum mengakses fitur keluarga.")

    // --- FAMILY TREE ---
    fun observeTreeNodes(familyId: String): Flow<List<TreeNode>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/tree_nodes"
        emitAll(
            db.collection("families").document(familyId).collection("tree_nodes")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(TreeNode::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<TreeNode> { it.generation }.thenBy { it.fullName })
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addTreeNode(
        familyId: String,
        fullName: String,
        relationTitle: String,
        generation: Long,
        parentName: String,
        spouseName: String,
        notes: String,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val nodeId = "tree_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("tree_nodes").document(nodeId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "fullName" to fullName.trim(),
                "relationTitle" to relationTitle.trim(),
                "generation" to generation,
                "createdBy" to uid,
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (parentName.isNotBlank()) payload["parentName"] = parentName.trim()
            if (spouseName.isNotBlank()) payload["spouseName"] = spouseName.trim()
            if (notes.isNotBlank()) payload["notes"] = notes.trim()
            ref.set(payload).await()
            Result.success(nodeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    // --- FAMILY CALENDAR ---
    fun observeCalendarEvents(familyId: String): Flow<List<CalendarEvent>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/calendar_events"
        emitAll(
            db.collection("families").document(familyId).collection("calendar_events")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CalendarEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.eventDate }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addCalendarEvent(
        familyId: String,
        title: String,
        category: String,
        eventDate: String,
        description: String,
        reminderEnabled: Boolean,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val eventId = "evt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("calendar_events").document(eventId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "title" to title.trim(),
                "category" to category,
                "eventDate" to eventDate.trim(),
                "reminderEnabled" to reminderEnabled,
                "createdBy" to uid,
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (description.isNotBlank()) payload["description"] = description.trim()
            ref.set(payload).await()
            Result.success(eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    // --- FAMILY GALLERY ---
    fun observeGalleryAlbums(familyId: String): Flow<List<GalleryAlbum>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/gallery_albums"
        emitAll(
            db.collection("families").document(familyId).collection("gallery_albums")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(GalleryAlbum::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.year }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createGalleryAlbum(
        familyId: String,
        title: String,
        eventName: String,
        year: Long,
        mediaType: String,
        accessLevel: String,
        itemLabels: List<String>,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val albumId = "alb_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("gallery_albums").document(albumId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            ref.set(
                mapOf(
                    "familyId" to familyId,
                    "title" to title.trim(),
                    "eventName" to eventName.trim(),
                    "year" to year,
                    "mediaType" to mediaType,
                    "accessLevel" to accessLevel,
                    "itemLabels" to itemLabels.take(30),
                    "createdBy" to uid,
                    "memberUids" to finalMembers,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(albumId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun addMediaToAlbum(familyId: String, albumId: String, mediaLabel: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("families").document(familyId).collection("gallery_albums").document(albumId)
        return try {
            ref.update(
                mapOf(
                    "itemLabels" to FieldValue.arrayUnion(mediaLabel),
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

    // --- FAMILY FUND (TREASURER / ADMIN RBAC!) ---
    fun observeFundTransactions(familyId: String): Flow<List<FundTransaction>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/fund_transactions"
        emitAll(
            db.collection("families").document(familyId).collection("fund_transactions")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FundTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addFundTransaction(
        familyId: String,
        title: String,
        txType: String,
        amountRupiah: Long,
        category: String,
        note: String,
        recordedByName: String,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val txId = "tx_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("fund_transactions").document(txId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "title" to title.trim(),
                "txType" to txType,
                "amountRupiah" to amountRupiah,
                "category" to category.trim(),
                "recordedBy" to uid,
                "recordedByName" to recordedByName,
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (note.isNotBlank()) payload["note"] = note.trim()
            ref.set(payload).await()
            Result.success(txId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteFundTransaction(familyId: String, txId: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("families").document(familyId).collection("fund_transactions").document(txId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    // --- FAMILY SAFETY ---
    fun observeSafetyAlerts(familyId: String): Flow<List<SafetyAlert>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/safety_alerts"
        emitAll(
            db.collection("families").document(familyId).collection("safety_alerts")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(SafetyAlert::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun sendSafetyAlert(
        familyId: String,
        senderName: String,
        alertType: String,
        message: String,
        latitude: Double?,
        longitude: Double?,
        locationConsented: Boolean,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val alertId = "sos_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val ref = db.collection("families").document(familyId).collection("safety_alerts").document(alertId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            val payload = mutableMapOf<String, Any>(
                "familyId" to familyId,
                "senderId" to uid,
                "senderName" to senderName,
                "alertType" to alertType,
                "message" to message.trim(),
                "locationConsented" to locationConsented,
                "status" to "active",
                "memberUids" to finalMembers,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (locationConsented && latitude != null && longitude != null) {
                payload["latitude"] = latitude
                payload["longitude"] = longitude
            }
            ref.set(payload).await()
            Result.success(alertId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun resolveSafetyAlert(familyId: String, alertId: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("families").document(familyId).collection("safety_alerts").document(alertId)
        return try {
            ref.update(
                mapOf(
                    "status" to "resolved",
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
