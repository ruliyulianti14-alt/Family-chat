package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

internal fun encodeStringList(list: List<String>): String = JSONArray(list).toString()

internal fun decodeStringList(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    return try {
        val arr = JSONArray(raw)
        List(arr.length()) { idx -> arr.optString(idx) }.filter { it.isNotEmpty() }
    } catch (_: Exception) {
        emptyList()
    }
}

@Entity(tableName = "local_user_profiles")
data class LocalUserProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String,
    val familyId: String,
    val relationship: String,
    val phone: String,
    val showPhone: Boolean,
    val birthday: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_families")
data class LocalFamilyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slogan: String,
    val adminUid: String,
    val inviteCode: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_members")
data class LocalMemberEntity(
    @PrimaryKey val id: String, // "${familyId}_${userId}"
    val userId: String,
    val familyId: String,
    val displayName: String,
    val relationship: String,
    val role: String,
    val fundManager: Boolean,
    val phone: String,
    val showPhone: Boolean,
    val birthday: String,
    val avatarColorIndex: Long,
    val locationSharingEnabled: Boolean,
    val lastLat: Double?,
    val lastLng: Double?,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_groups")
data class LocalGroupEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val name: String,
    val description: String,
    val category: String,
    val createdBy: String,
    val memberUidsJson: String,
    val lastMessageText: String,
    val lastMessageSender: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_private_chats")
data class LocalPrivateChatEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val participantNamesJson: String,
    val memberUidsJson: String,
    val lastMessageText: String,
    val lastMessageSender: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_messages")
data class LocalMessageEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val roomId: String,
    val roomType: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val messageType: String,
    val mediaLabel: String,
    val replyToText: String,
    val replyToSender: String,
    val forwarded: Boolean,
    val status: String,
    val readByUidsJson: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_moments")
data class LocalMomentEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val authorId: String,
    val authorName: String,
    val authorRoleLabel: String,
    val caption: String,
    val momentType: String,
    val mediaLabel: String,
    val likedByUidsJson: String,
    val commentCount: Long,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_moment_comments")
data class LocalMomentCommentEntity(
    @PrimaryKey val id: String,
    val momentId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val memberUidsJson: String,
    val createdAtMs: Long
)

@Entity(tableName = "local_calls")
data class LocalCallEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val callerId: String,
    val callerName: String,
    val callType: String,
    val targetName: String,
    val status: String,
    val participantUidsJson: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_tree_nodes")
data class LocalTreeNodeEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val fullName: String,
    val relationTitle: String,
    val generation: Long,
    val parentName: String,
    val spouseName: String,
    val notes: String,
    val createdBy: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_calendar_events")
data class LocalCalendarEventEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val title: String,
    val category: String,
    val eventDate: String,
    val description: String,
    val reminderEnabled: Boolean,
    val createdBy: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_gallery_albums")
data class LocalGalleryAlbumEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val title: String,
    val eventName: String,
    val year: Long,
    val mediaType: String,
    val accessLevel: String,
    val itemLabelsJson: String,
    val createdBy: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_fund_transactions")
data class LocalFundTransactionEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val title: String,
    val txType: String,
    val amountRupiah: Long,
    val category: String,
    val note: String,
    val recordedBy: String,
    val recordedByName: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_safety_alerts")
data class LocalSafetyAlertEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val senderId: String,
    val senderName: String,
    val alertType: String,
    val message: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationConsented: Boolean,
    val status: String,
    val memberUidsJson: String,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Entity(tableName = "local_accounts")
data class LocalAccountEntity(
    @PrimaryKey val email: String,
    val userId: String,
    val passwordSalt: String,
    val passwordHash: String,
    val createdAtMs: Long
)
