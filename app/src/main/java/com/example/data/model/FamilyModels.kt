package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val familyId: String = "",
    val relationship: String = "Anggota",
    val phone: String = "",
    val showPhone: Boolean = false,
    val birthday: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Family(
    @DocumentId val id: String = "",
    val name: String = "",
    val slogan: String = "One Family, One Connection, One Love.",
    val adminUid: String = "",
    val inviteCode: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FamilyInvite(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val createdBy: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FamilyMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val familyId: String = "",
    val displayName: String = "",
    val relationship: String = "Anggota",
    val role: String = "member", // "admin" | "member"
    val fundManager: Boolean = false,
    val phone: String = "",
    val showPhone: Boolean = false,
    val birthday: String = "",
    val avatarColorIndex: Long = 0L,
    val locationSharingEnabled: Boolean = false,
    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isAdmin: Boolean get() = role == "admin"
}

@IgnoreExtraProperties
data class FamilyGroup(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "Umum",
    val createdBy: String = "",
    val memberUids: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageSender: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PrivateChat(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val participantNames: List<String> = emptyList(),
    val memberUids: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageSender: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val roomId: String = "",
    val roomType: String = "private", // "private" | "group"
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val messageType: String = "text", // "text" | "image" | "video" | "document" | "voice" | "sticker"
    val mediaLabel: String = "",
    val replyToText: String = "",
    val replyToSender: String = "",
    val forwarded: Boolean = false,
    val status: String = "sent", // "sent" | "delivered" | "read"
    val readByUids: List<String> = emptyList(),
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FamilyMoment(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRoleLabel: String = "",
    val caption: String = "",
    val momentType: String = "text", // "text" | "photo" | "video"
    val mediaLabel: String = "",
    val likedByUids: List<String> = emptyList(),
    val commentCount: Long = 0L,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MomentComment(
    @DocumentId val id: String = "",
    val momentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FamilyCall(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callType: String = "voice", // "voice" | "video" | "group"
    val targetName: String = "",
    val status: String = "ringing", // "ringing" | "active" | "ended" | "missed"
    val participantUids: List<String> = emptyList(),
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TreeNode(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val fullName: String = "",
    val relationTitle: String = "",
    val generation: Long = 2L,
    val parentName: String = "",
    val spouseName: String = "",
    val notes: String = "",
    val createdBy: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CalendarEvent(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val category: String = "gathering", // "birthday" | "wedding" | "gathering" | "reminder"
    val eventDate: String = "",
    val description: String = "",
    val reminderEnabled: Boolean = true,
    val createdBy: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GalleryAlbum(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val eventName: String = "",
    val year: Long = 2026L,
    val mediaType: String = "photo", // "photo" | "video" | "mixed"
    val accessLevel: String = "all_family", // "all_family" | "adults_only" | "admin_only"
    val itemLabels: List<String> = emptyList(),
    val createdBy: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FundTransaction(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val txType: String = "income", // "income" | "expense"
    val amountRupiah: Long = 0L,
    val category: String = "Iuran",
    val note: String = "",
    val recordedBy: String = "",
    val recordedByName: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SafetyAlert(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val alertType: String = "sos", // "sos" | "checkin" | "location"
    val message: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationConsented: Boolean = false,
    val status: String = "active", // "active" | "resolved"
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
