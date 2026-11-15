package com.example.data.repository

import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.Family
import com.example.data.model.FamilyCall
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyMoment
import com.example.data.model.FundTransaction
import com.example.data.model.GalleryAlbum
import com.example.data.model.MomentComment
import com.example.data.model.PrivateChat
import com.example.data.model.SafetyAlert
import com.example.data.model.TreeNode
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FamilyDataGateway {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun observeFamily(familyId: String): Flow<Family?>
    fun observeMembers(familyId: String): Flow<List<FamilyMember>>
    fun observeGroups(familyId: String): Flow<List<FamilyGroup>>
    fun observePrivateChats(familyId: String): Flow<List<PrivateChat>>
    fun observeMessages(familyId: String, roomId: String): Flow<List<ChatMessage>>
    fun observeMoments(familyId: String): Flow<List<FamilyMoment>>
    fun observeMomentComments(familyId: String, momentId: String): Flow<List<MomentComment>>
    fun observeCalls(familyId: String): Flow<List<FamilyCall>>
    fun observeTreeNodes(familyId: String): Flow<List<TreeNode>>
    fun observeCalendarEvents(familyId: String): Flow<List<CalendarEvent>>
    fun observeGalleryAlbums(familyId: String): Flow<List<GalleryAlbum>>
    fun observeFundTransactions(familyId: String): Flow<List<FundTransaction>>
    fun observeSafetyAlerts(familyId: String): Flow<List<SafetyAlert>>

    suspend fun createFamilyAndAdminProfile(
        familyName: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String>

    suspend fun joinFamilyWithInviteCode(
        inviteCodeInput: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String>

    suspend fun updateOwnProfile(
        familyId: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        locationSharingEnabled: Boolean
    ): Result<Unit>

    suspend fun updateOwnGpsLocation(
        familyId: String,
        latitude: Double?,
        longitude: Double?,
        locationSharingEnabled: Boolean
    ): Result<Unit>

    suspend fun createGroup(
        familyId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<String>

    suspend fun updateGroupDetails(
        familyId: String,
        groupId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<Unit>

    suspend fun deleteGroup(familyId: String, groupId: String): Result<Unit>

    suspend fun updateMemberRoles(
        familyId: String,
        targetMemberUid: String,
        role: String,
        fundManager: Boolean
    ): Result<Unit>

    suspend fun removeMember(familyId: String, targetMemberUid: String): Result<Unit>

    suspend fun updateFamilyName(familyId: String, newName: String): Result<Unit>

    suspend fun regenerateInviteCode(familyId: String, familyName: String, oldCode: String): Result<String>

    suspend fun syncFamilyMemberAccess(familyId: String, memberUids: List<String>)

    suspend fun getOrCreatePrivateChat(
        familyId: String,
        otherUid: String,
        currentName: String,
        otherName: String
    ): Result<String>

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
    ): Result<String>

    suspend fun markMessagesAsRead(familyId: String, messages: List<ChatMessage>)

    suspend fun createMoment(
        familyId: String,
        authorName: String,
        authorRoleLabel: String,
        caption: String,
        momentType: String,
        mediaLabel: String,
        memberUids: List<String>
    ): Result<String>

    suspend fun toggleMomentLike(familyId: String, moment: FamilyMoment): Result<Unit>

    suspend fun addMomentComment(
        familyId: String,
        momentId: String,
        authorName: String,
        text: String,
        memberUids: List<String>
    ): Result<Unit>

    suspend fun startCallRoom(
        familyId: String,
        callerName: String,
        callType: String,
        targetName: String,
        memberUids: List<String>
    ): Result<String>

    suspend fun updateCallStatus(
        familyId: String,
        callId: String,
        status: String,
        joinAsParticipant: Boolean = false
    ): Result<Unit>

    suspend fun addTreeNode(
        familyId: String,
        fullName: String,
        relationTitle: String,
        generation: Long,
        parentName: String,
        spouseName: String,
        notes: String,
        memberUids: List<String>
    ): Result<String>

    suspend fun addCalendarEvent(
        familyId: String,
        title: String,
        category: String,
        eventDate: String,
        description: String,
        reminderEnabled: Boolean,
        memberUids: List<String>
    ): Result<String>

    suspend fun createGalleryAlbum(
        familyId: String,
        title: String,
        eventName: String,
        year: Long,
        mediaType: String,
        accessLevel: String,
        itemLabels: List<String>,
        memberUids: List<String>
    ): Result<String>

    suspend fun addMediaToAlbum(familyId: String, albumId: String, mediaLabel: String): Result<Unit>

    suspend fun addFundTransaction(
        familyId: String,
        title: String,
        txType: String,
        amountRupiah: Long,
        category: String,
        note: String,
        recordedByName: String,
        memberUids: List<String>
    ): Result<String>

    suspend fun deleteFundTransaction(familyId: String, txId: String): Result<Unit>

    suspend fun sendSafetyAlert(
        familyId: String,
        senderName: String,
        alertType: String,
        message: String,
        latitude: Double?,
        longitude: Double?,
        locationConsented: Boolean,
        memberUids: List<String>
    ): Result<String>

    suspend fun resolveSafetyAlert(familyId: String, alertId: String): Result<Unit>
}

class FirestoreFamilyGateway(
    private val coreRepo: FamilyCoreRepository,
    private val commsRepo: FamilyCommsRepository,
    private val lifeRepo: FamilyLifeRepository
) : FamilyDataGateway {
    override fun observeUserProfile(userId: String) = coreRepo.observeUserProfile(userId)
    override fun observeFamily(familyId: String) = coreRepo.observeFamily(familyId)
    override fun observeMembers(familyId: String) = coreRepo.observeMembers(familyId)
    override fun observeGroups(familyId: String) = coreRepo.observeGroups(familyId)
    override fun observePrivateChats(familyId: String) = commsRepo.observePrivateChats(familyId)
    override fun observeMessages(familyId: String, roomId: String) = commsRepo.observeMessages(familyId, roomId)
    override fun observeMoments(familyId: String) = commsRepo.observeMoments(familyId)
    override fun observeMomentComments(familyId: String, momentId: String) = commsRepo.observeMomentComments(familyId, momentId)
    override fun observeCalls(familyId: String) = commsRepo.observeCalls(familyId)
    override fun observeTreeNodes(familyId: String) = lifeRepo.observeTreeNodes(familyId)
    override fun observeCalendarEvents(familyId: String) = lifeRepo.observeCalendarEvents(familyId)
    override fun observeGalleryAlbums(familyId: String) = lifeRepo.observeGalleryAlbums(familyId)
    override fun observeFundTransactions(familyId: String) = lifeRepo.observeFundTransactions(familyId)
    override fun observeSafetyAlerts(familyId: String) = lifeRepo.observeSafetyAlerts(familyId)

    override suspend fun createFamilyAndAdminProfile(
        familyName: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ) = coreRepo.createFamilyAndAdminProfile(familyName, displayName, relationship, phone, showPhone, birthday, email)

    override suspend fun joinFamilyWithInviteCode(
        inviteCodeInput: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ) = coreRepo.joinFamilyWithInviteCode(inviteCodeInput, displayName, relationship, phone, showPhone, birthday, email)

    override suspend fun updateOwnProfile(
        familyId: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        locationSharingEnabled: Boolean
    ) = coreRepo.updateOwnProfile(familyId, displayName, relationship, phone, showPhone, birthday, locationSharingEnabled)

    override suspend fun updateOwnGpsLocation(
        familyId: String,
        latitude: Double?,
        longitude: Double?,
        locationSharingEnabled: Boolean
    ) = coreRepo.updateOwnGpsLocation(familyId, latitude, longitude, locationSharingEnabled)

    override suspend fun createGroup(
        familyId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ) = coreRepo.createGroup(familyId, name, description, category, memberUids)

    override suspend fun updateGroupDetails(
        familyId: String,
        groupId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ) = coreRepo.updateGroupDetails(familyId, groupId, name, description, category, memberUids)

    override suspend fun deleteGroup(familyId: String, groupId: String) = coreRepo.deleteGroup(familyId, groupId)

    override suspend fun updateMemberRoles(
        familyId: String,
        targetMemberUid: String,
        role: String,
        fundManager: Boolean
    ) = coreRepo.updateMemberRoles(familyId, targetMemberUid, role, fundManager)

    override suspend fun removeMember(familyId: String, targetMemberUid: String) = coreRepo.removeMember(familyId, targetMemberUid)

    override suspend fun updateFamilyName(familyId: String, newName: String) = coreRepo.updateFamilyName(familyId, newName)

    override suspend fun regenerateInviteCode(familyId: String, familyName: String, oldCode: String) =
        coreRepo.regenerateInviteCode(familyId, familyName, oldCode)

    override suspend fun syncFamilyMemberAccess(familyId: String, memberUids: List<String>) =
        coreRepo.syncFamilyMemberAccess(familyId, memberUids)

    override suspend fun getOrCreatePrivateChat(
        familyId: String,
        otherUid: String,
        currentName: String,
        otherName: String
    ) = commsRepo.getOrCreatePrivateChat(familyId, otherUid, currentName, otherName)

    override suspend fun sendMessage(
        familyId: String,
        roomId: String,
        roomType: String,
        senderName: String,
        text: String,
        messageType: String,
        mediaLabel: String,
        replyToText: String,
        replyToSender: String,
        forwarded: Boolean,
        memberUids: List<String>
    ) = commsRepo.sendMessage(
        familyId, roomId, roomType, senderName, text, messageType, mediaLabel, replyToText, replyToSender, forwarded, memberUids
    )

    override suspend fun markMessagesAsRead(familyId: String, messages: List<ChatMessage>) =
        commsRepo.markMessagesAsRead(familyId, messages)

    override suspend fun createMoment(
        familyId: String,
        authorName: String,
        authorRoleLabel: String,
        caption: String,
        momentType: String,
        mediaLabel: String,
        memberUids: List<String>
    ) = commsRepo.createMoment(familyId, authorName, authorRoleLabel, caption, momentType, mediaLabel, memberUids)

    override suspend fun toggleMomentLike(familyId: String, moment: FamilyMoment) =
        commsRepo.toggleMomentLike(familyId, moment)

    override suspend fun addMomentComment(
        familyId: String,
        momentId: String,
        authorName: String,
        text: String,
        memberUids: List<String>
    ) = commsRepo.addMomentComment(familyId, momentId, authorName, text, memberUids)

    override suspend fun startCallRoom(
        familyId: String,
        callerName: String,
        callType: String,
        targetName: String,
        memberUids: List<String>
    ) = commsRepo.startCallRoom(familyId, callerName, callType, targetName, memberUids)

    override suspend fun updateCallStatus(
        familyId: String,
        callId: String,
        status: String,
        joinAsParticipant: Boolean
    ) = commsRepo.updateCallStatus(familyId, callId, status, joinAsParticipant)

    override suspend fun addTreeNode(
        familyId: String,
        fullName: String,
        relationTitle: String,
        generation: Long,
        parentName: String,
        spouseName: String,
        notes: String,
        memberUids: List<String>
    ) = lifeRepo.addTreeNode(familyId, fullName, relationTitle, generation, parentName, spouseName, notes, memberUids)

    override suspend fun addCalendarEvent(
        familyId: String,
        title: String,
        category: String,
        eventDate: String,
        description: String,
        reminderEnabled: Boolean,
        memberUids: List<String>
    ) = lifeRepo.addCalendarEvent(familyId, title, category, eventDate, description, reminderEnabled, memberUids)

    override suspend fun createGalleryAlbum(
        familyId: String,
        title: String,
        eventName: String,
        year: Long,
        mediaType: String,
        accessLevel: String,
        itemLabels: List<String>,
        memberUids: List<String>
    ) = lifeRepo.createGalleryAlbum(familyId, title, eventName, year, mediaType, accessLevel, itemLabels, memberUids)

    override suspend fun addMediaToAlbum(familyId: String, albumId: String, mediaLabel: String) =
        lifeRepo.addMediaToAlbum(familyId, albumId, mediaLabel)

    override suspend fun addFundTransaction(
        familyId: String,
        title: String,
        txType: String,
        amountRupiah: Long,
        category: String,
        note: String,
        recordedByName: String,
        memberUids: List<String>
    ) = lifeRepo.addFundTransaction(familyId, title, txType, amountRupiah, category, note, recordedByName, memberUids)

    override suspend fun deleteFundTransaction(familyId: String, txId: String) =
        lifeRepo.deleteFundTransaction(familyId, txId)

    override suspend fun sendSafetyAlert(
        familyId: String,
        senderName: String,
        alertType: String,
        message: String,
        latitude: Double?,
        longitude: Double?,
        locationConsented: Boolean,
        memberUids: List<String>
    ) = lifeRepo.sendSafetyAlert(familyId, senderName, alertType, message, latitude, longitude, locationConsented, memberUids)

    override suspend fun resolveSafetyAlert(familyId: String, alertId: String) =
        lifeRepo.resolveSafetyAlert(familyId, alertId)
}
