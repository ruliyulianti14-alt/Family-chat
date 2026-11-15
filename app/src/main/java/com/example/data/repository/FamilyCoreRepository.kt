package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Family
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.UserProfile
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

class FamilyCoreRepository(
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
            ?: throw IllegalStateException("Pengguna harus login dengan Google sebelum mengakses data keluarga.")

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    logStreamError(error, OperationType.GET, docRef.path, userId)
                    throw error
                }
        )
    }

    fun observeFamily(familyId: String): Flow<Family?> = flow {
        val uid = requireUserId()
        val docRef = db.collection("families").document(familyId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(Family::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    logStreamError(error, OperationType.GET, docRef.path, uid)
                    throw error
                }
        )
    }

    fun observeMembers(familyId: String): Flow<List<FamilyMember>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/members"
        emitAll(
            db.collection("families").document(familyId).collection("members")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FamilyMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeGroups(familyId: String): Flow<List<FamilyGroup>> = flow {
        val uid = requireUserId()
        val path = "families/$familyId/groups"
        emitAll(
            db.collection("families").document(familyId).collection("groups")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FamilyGroup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createFamilyAndAdminProfile(
        familyName: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String> {
        val uid = requireUserId()
        val familyId = "fam_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val inviteCode = "KEL${UUID.randomUUID().toString().replace("-", "").take(6).uppercase()}"

        val familyRef = db.collection("families").document(familyId)
        val inviteRef = db.collection("family_invites").document(inviteCode)
        val memberRef = familyRef.collection("members").document(uid)
        val userRef = db.collection("users").document(uid)
        val defaultGroupRef = familyRef.collection("groups").document("grp_utama_${familyId.takeLast(6)}")

        return try {
            val batch = db.batch()
            batch.set(
                familyRef,
                mapOf(
                    "name" to familyName.trim(),
                    "slogan" to "One Family, One Connection, One Love.",
                    "adminUid" to uid,
                    "inviteCode" to inviteCode,
                    "memberUids" to listOf(uid),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                inviteRef,
                mapOf(
                    "familyId" to familyId,
                    "familyName" to familyName.trim(),
                    "createdBy" to uid,
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                memberRef,
                mapOf(
                    "userId" to uid,
                    "familyId" to familyId,
                    "displayName" to displayName.trim(),
                    "relationship" to relationship.trim(),
                    "role" to "admin",
                    "fundManager" to true,
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "avatarColorIndex" to 0L,
                    "locationSharingEnabled" to false,
                    "memberUids" to listOf(uid),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                userRef,
                mapOf(
                    "userId" to uid,
                    "displayName" to displayName.trim(),
                    "email" to email,
                    "familyId" to familyId,
                    "relationship" to relationship.trim(),
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            // Admin creates the default Family Group after admin member doc is persisted
            defaultGroupRef.set(
                mapOf(
                    "familyId" to familyId,
                    "name" to "Keluarga Besar ${familyName.trim()}",
                    "description" to "Grup komunikasi utama seluruh anggota keluarga.",
                    "category" to "Keluarga Besar",
                    "createdBy" to uid,
                    "memberUids" to listOf(uid),
                    "lastMessageText" to "Selamat datang di ${familyName.trim()}!",
                    "lastMessageSender" to displayName.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(familyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, familyRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinFamilyWithInviteCode(
        inviteCodeInput: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String> {
        val uid = requireUserId()
        val code = inviteCodeInput.trim().uppercase()
        val inviteRef = db.collection("family_invites").document(code)

        return try {
            val inviteSnap = inviteRef.get().await()
            if (!inviteSnap.exists() || inviteSnap.getBoolean("active") != true) {
                return Result.failure(IllegalArgumentException("Kode undangan tidak ditemukan atau tidak aktif."))
            }
            val familyId = inviteSnap.getString("familyId")
                ?: return Result.failure(IllegalStateException("Data undangan tidak lengkap."))
            val familyRef = db.collection("families").document(familyId)

            // Add self to family.memberUids (allowed by self-join rule)
            familyRef.update(
                mapOf(
                    "memberUids" to FieldValue.arrayUnion(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val updatedFamilySnap = familyRef.get().await()
            val memberUids = (updatedFamilySnap.get("memberUids") as? List<*>)
                ?.filterIsInstance<String>()
                ?.distinct()
                ?: listOf(uid)

            val memberRef = familyRef.collection("members").document(uid)
            val userRef = db.collection("users").document(uid)
            val batch = db.batch()
            batch.set(
                memberRef,
                mapOf(
                    "userId" to uid,
                    "familyId" to familyId,
                    "displayName" to displayName.trim(),
                    "relationship" to relationship.trim(),
                    "role" to "member",
                    "fundManager" to false,
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "avatarColorIndex" to (memberUids.size % 6).toLong(),
                    "locationSharingEnabled" to false,
                    "memberUids" to memberUids,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                userRef,
                mapOf(
                    "userId" to uid,
                    "displayName" to displayName.trim(),
                    "email" to email,
                    "familyId" to familyId,
                    "relationship" to relationship.trim(),
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(familyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, inviteRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateOwnProfile(
        familyId: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        locationSharingEnabled: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val memberRef = db.collection("families").document(familyId).collection("members").document(uid)
        val userRef = db.collection("users").document(uid)
        return try {
            val batch = db.batch()
            batch.update(
                memberRef,
                mapOf(
                    "displayName" to displayName.trim(),
                    "relationship" to relationship.trim(),
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "locationSharingEnabled" to locationSharingEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userRef,
                mapOf(
                    "displayName" to displayName.trim(),
                    "relationship" to relationship.trim(),
                    "phone" to phone.trim(),
                    "showPhone" to showPhone,
                    "birthday" to birthday.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, memberRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateOwnGpsLocation(
        familyId: String,
        latitude: Double?,
        longitude: Double?,
        locationSharingEnabled: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val memberRef = db.collection("families").document(familyId).collection("members").document(uid)
        return try {
            val updates = mutableMapOf<String, Any>(
                "locationSharingEnabled" to locationSharingEnabled,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (locationSharingEnabled && latitude != null && longitude != null) {
                updates["lastLat"] = latitude
                updates["lastLng"] = longitude
            }
            memberRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, memberRef.path)
            Result.failure(e)
        }
    }

    // ADMIN-ONLY ACTIONS (Enforced by firestore.rules!)
    suspend fun createGroup(
        familyId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val groupId = "grp_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val groupRef = db.collection("families").document(familyId).collection("groups").document(groupId)
        val finalMembers = (memberUids + uid).distinct()
        return try {
            groupRef.set(
                mapOf(
                    "familyId" to familyId,
                    "name" to name.trim(),
                    "description" to description.trim(),
                    "category" to category.trim(),
                    "createdBy" to uid,
                    "memberUids" to finalMembers,
                    "lastMessageText" to "Grup dibuat oleh Admin",
                    "lastMessageSender" to "Sistem",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(groupId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, groupRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateGroupDetails(
        familyId: String,
        groupId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<Unit> {
        requireUserId()
        val groupRef = db.collection("families").document(familyId).collection("groups").document(groupId)
        return try {
            groupRef.update(
                mapOf(
                    "name" to name.trim(),
                    "description" to description.trim(),
                    "category" to category.trim(),
                    "memberUids" to memberUids.distinct(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, groupRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(familyId: String, groupId: String): Result<Unit> {
        requireUserId()
        val groupRef = db.collection("families").document(familyId).collection("groups").document(groupId)
        return try {
            groupRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, groupRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMemberRoles(
        familyId: String,
        targetMemberUid: String,
        role: String,
        fundManager: Boolean
    ): Result<Unit> {
        requireUserId()
        val memberRef = db.collection("families").document(familyId).collection("members").document(targetMemberUid)
        return try {
            memberRef.update(
                mapOf(
                    "role" to role,
                    "fundManager" to fundManager,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, memberRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeMember(familyId: String, targetMemberUid: String): Result<Unit> {
        requireUserId()
        val familyRef = db.collection("families").document(familyId)
        val memberRef = familyRef.collection("members").document(targetMemberUid)
        return try {
            val batch = db.batch()
            batch.delete(memberRef)
            batch.update(
                familyRef,
                mapOf(
                    "memberUids" to FieldValue.arrayRemove(targetMemberUid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, memberRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateFamilyName(familyId: String, newName: String): Result<Unit> {
        requireUserId()
        val familyRef = db.collection("families").document(familyId)
        return try {
            familyRef.update(
                mapOf(
                    "name" to newName.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, familyRef.path)
            Result.failure(e)
        }
    }

    suspend fun regenerateInviteCode(familyId: String, familyName: String, oldCode: String): Result<String> {
        val uid = requireUserId()
        val newCode = "KEL${UUID.randomUUID().toString().replace("-", "").take(6).uppercase()}"
        val familyRef = db.collection("families").document(familyId)
        val newInviteRef = db.collection("family_invites").document(newCode)
        val oldInviteRef = db.collection("family_invites").document(oldCode)
        return try {
            val batch = db.batch()
            batch.set(
                newInviteRef,
                mapOf(
                    "familyId" to familyId,
                    "familyName" to familyName,
                    "createdBy" to uid,
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                familyRef,
                mapOf(
                    "inviteCode" to newCode,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (oldCode.isNotBlank() && oldCode != newCode) {
                batch.delete(oldInviteRef)
            }
            batch.commit().await()
            Result.success(newCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, familyRef.path)
            Result.failure(e)
        }
    }

    /**
     * Syncs newly joined member UIDs across family subcollections when an admin is active,
     * ensuring newly joined members can list shared family records without get() in allow list.
     */
    suspend fun syncFamilyMemberAccess(familyId: String, memberUids: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        if (memberUids.isEmpty()) return
        val familyDoc = db.collection("families").document(familyId)
        val collectionsToSync = listOf(
            "members", "moments", "calls", "tree_nodes",
            "calendar_events", "gallery_albums", "fund_transactions", "safety_alerts"
        )
        try {
            val batch = db.batch()
            var ops = 0
            for (col in collectionsToSync) {
                val snap = familyDoc.collection(col)
                    .whereArrayContains("memberUids", uid)
                    .get()
                    .await()
                for (doc in snap.documents) {
                    val existingUids = (doc.get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    if (existingUids.toSet() != memberUids.toSet()) {
                        batch.update(
                            doc.reference,
                            mapOf(
                                "memberUids" to memberUids,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        ops++
                    }
                }
            }
            if (ops > 0) {
                batch.commit().await()
            }
        } catch (_: Exception) {
            // Best-effort background sync by admin; non-fatal if offline or non-admin
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
