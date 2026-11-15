package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.FamilyDataGateway
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyViewModel(
    private val gateway: FamilyDataGateway,
    val currentUserId: String,
    val currentUserEmail: String
) : ViewModel() {

    private val _actionStatus = MutableStateFlow<String?>(null)
    val actionStatus: StateFlow<String?> = _actionStatus.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)

    val userProfileState: StateFlow<UiState<UserProfile?>> = gateway.observeUserProfile(currentUserId)
        .toUiState("Gagal memuat profil pengguna")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val activeFamilyIdFlow: Flow<String?> = userProfileState.map { state ->
        (state as? UiState.Success)?.data?.familyId?.takeIf { it.isNotBlank() }
    }

    val familyState: StateFlow<Family?> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(null) else gateway.observeFamily(famId).catch { emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val membersState: StateFlow<List<FamilyMember>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeMembers(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val groupsState: StateFlow<List<FamilyGroup>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeGroups(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val privateChatsState: StateFlow<List<PrivateChat>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observePrivateChats(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeMessagesState: StateFlow<List<ChatMessage>> = _activeRoomId
        .flatMapLatest { roomId ->
            val famId = (userProfileState.value as? UiState.Success)?.data?.familyId
            if (roomId.isNullOrBlank() || famId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                gateway.observeMessages(famId, roomId).catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val momentsState: StateFlow<List<FamilyMoment>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeMoments(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val callsState: StateFlow<List<FamilyCall>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeCalls(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val treeNodesState: StateFlow<List<TreeNode>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeTreeNodes(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val calendarEventsState: StateFlow<List<CalendarEvent>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeCalendarEvents(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val galleryAlbumsState: StateFlow<List<GalleryAlbum>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeGalleryAlbums(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val fundTransactionsState: StateFlow<List<FundTransaction>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeFundTransactions(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val safetyAlertsState: StateFlow<List<SafetyAlert>> = activeFamilyIdFlow
        .flatMapLatest { famId ->
            if (famId == null) flowOf(emptyList()) else gateway.observeSafetyAlerts(famId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedMomentId = MutableStateFlow<String?>(null)
    val momentCommentsState: StateFlow<List<MomentComment>> = _selectedMomentId
        .flatMapLatest { momId ->
            val famId = (userProfileState.value as? UiState.Success)?.data?.familyId
            if (momId.isNullOrBlank() || famId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                gateway.observeMomentComments(famId, momId).catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun clearStatus() {
        _actionStatus.value = null
    }

    fun selectChatRoom(roomId: String?) {
        _activeRoomId.value = roomId
    }

    fun selectMomentForComments(momentId: String?) {
        _selectedMomentId.value = momentId
    }

    fun currentMember(): FamilyMember? =
        membersState.value.firstOrNull { it.userId == currentUserId }

    fun familyMemberUids(): List<String> =
        familyState.value?.memberUids?.ifEmpty { listOf(currentUserId) } ?: listOf(currentUserId)

    fun syncAccessIfAdmin() {
        val fam = familyState.value ?: return
        val member = currentMember()
        if (member?.isAdmin == true || fam.adminUid == currentUserId) {
            viewModelScope.launch {
                gateway.syncFamilyMemberAccess(fam.id, fam.memberUids)
            }
        }
    }

    fun createFamily(
        familyName: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String
    ) = runAction("Keluarga berhasil dibuat! Anda menjadi Admin keluarga.") {
        gateway.createFamilyAndAdminProfile(
            familyName = familyName,
            displayName = displayName,
            relationship = relationship,
            phone = phone,
            showPhone = showPhone,
            birthday = birthday,
            email = currentUserEmail
        )
    }

    fun joinFamily(
        inviteCode: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String
    ) = runAction("Berhasil bergabung ke keluarga!") {
        gateway.joinFamilyWithInviteCode(
            inviteCodeInput = inviteCode,
            displayName = displayName,
            relationship = relationship,
            phone = phone,
            showPhone = showPhone,
            birthday = birthday,
            email = currentUserEmail
        )
    }

    fun updateProfile(
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        locationSharingEnabled: Boolean
    ) {
        val famId = familyState.value?.id ?: return
        runAction("Profil berhasil diperbarui") {
            gateway.updateOwnProfile(
                familyId = famId,
                displayName = displayName,
                relationship = relationship,
                phone = phone,
                showPhone = showPhone,
                birthday = birthday,
                locationSharingEnabled = locationSharingEnabled
            )
        }
    }

    fun updateOwnGpsLocation(
        latitude: Double?,
        longitude: Double?,
        locationSharingEnabled: Boolean,
        silent: Boolean = false
    ) {
        val famId = familyState.value?.id ?: return
        viewModelScope.launch {
            val result = gateway.updateOwnGpsLocation(famId, latitude, longitude, locationSharingEnabled)
            if (!silent) {
                result.onSuccess {
                    _actionStatus.value = if (locationSharingEnabled && latitude != null) {
                        "Lokasi GPS Anda berhasil diperbarui"
                    } else if (!locationSharingEnabled) {
                        "Berbagi lokasi GPS dimatikan"
                    } else {
                        "Status lokasi GPS diperbarui"
                    }
                }.onFailure { err ->
                    _actionStatus.value = err.localizedMessage ?: "Gagal memperbarui lokasi GPS."
                }
            }
        }
    }

    fun shareGpsToGroup(
        group: FamilyGroup,
        latitude: Double,
        longitude: Double,
        addressLabel: String?
    ) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Anggota"
        val coordsText = String.format(java.util.Locale.US, "%.6f, %.6f", latitude, longitude)
        val addressPart = addressLabel?.takeIf { it.isNotBlank() }?.let { "\nAlamat: $it" } ?: ""
        val messageText = "Lokasi GPS saya saat ini: $coordsText$addressPart\nhttps://maps.google.com/?q=$latitude,$longitude"
        runAction("Lokasi GPS dibagikan ke grup ${group.name}") {
            gateway.sendMessage(
                familyId = famId,
                roomId = group.id,
                roomType = "group",
                senderName = myName,
                text = messageText,
                messageType = "text",
                mediaLabel = "GPS Pin",
                memberUids = group.memberUids
            )
        }
    }

    fun createGroup(name: String, description: String, category: String, selectedUids: List<String>) {
        val famId = familyState.value?.id ?: return
        runAction("Grup keluarga berhasil dibuat") {
            gateway.createGroup(famId, name, description, category, selectedUids)
        }
    }

    fun updateGroup(groupId: String, name: String, description: String, category: String, memberUids: List<String>) {
        val famId = familyState.value?.id ?: return
        runAction("Info grup diperbarui") {
            gateway.updateGroupDetails(famId, groupId, name, description, category, memberUids)
        }
    }

    fun deleteGroup(groupId: String) {
        val famId = familyState.value?.id ?: return
        runAction("Grup berhasil dihapus") {
            gateway.deleteGroup(famId, groupId)
        }
    }

    fun updateMemberRole(targetUid: String, role: String, fundManager: Boolean) {
        val famId = familyState.value?.id ?: return
        runAction("Peran anggota diperbarui") {
            gateway.updateMemberRoles(famId, targetUid, role, fundManager)
        }
    }

    fun removeMember(targetUid: String) {
        val famId = familyState.value?.id ?: return
        runAction("Anggota dikeluarkan dari keluarga") {
            gateway.removeMember(famId, targetUid)
        }
    }

    fun updateFamilyName(newName: String) {
        val famId = familyState.value?.id ?: return
        runAction("Nama keluarga diperbarui") {
            gateway.updateFamilyName(famId, newName)
        }
    }

    fun regenerateInviteCode() {
        val fam = familyState.value ?: return
        runAction("Kode undangan baru berhasil dibuat") {
            gateway.regenerateInviteCode(fam.id, fam.name, fam.inviteCode)
        }
    }

    fun openOrCreatePrivateChat(otherMember: FamilyMember, onReady: (String) -> Unit) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Saya"
        viewModelScope.launch {
            _isBusy.value = true
            val res = gateway.getOrCreatePrivateChat(famId, otherMember.userId, myName, otherMember.displayName)
            _isBusy.value = false
            res.onSuccess { chatId -> onReady(chatId) }
                .onFailure { _actionStatus.value = it.localizedMessage ?: "Gagal membuka chat" }
        }
    }

    fun sendMessage(
        roomId: String,
        roomType: String,
        text: String,
        messageType: String = "text",
        mediaLabel: String = "",
        replyToText: String = "",
        replyToSender: String = "",
        forwarded: Boolean = false,
        roomMemberUids: List<String>
    ) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Anggota"
        viewModelScope.launch {
            val res = gateway.sendMessage(
                familyId = famId,
                roomId = roomId,
                roomType = roomType,
                senderName = myName,
                text = text,
                messageType = messageType,
                mediaLabel = mediaLabel,
                replyToText = replyToText,
                replyToSender = replyToSender,
                forwarded = forwarded,
                memberUids = roomMemberUids
            )
            res.onFailure { _actionStatus.value = it.localizedMessage ?: "Gagal mengirim pesan" }
        }
    }

    fun markRoomMessagesRead(messages: List<ChatMessage>) {
        val famId = familyState.value?.id ?: return
        viewModelScope.launch {
            gateway.markMessagesAsRead(famId, messages)
        }
    }

    fun createMoment(caption: String, momentType: String, mediaLabel: String) {
        val famId = familyState.value?.id ?: return
        val me = currentMember()
        runAction("Momen keluarga dibagikan") {
            gateway.createMoment(
                familyId = famId,
                authorName = me?.displayName ?: "Anggota",
                authorRoleLabel = me?.relationship ?: "Keluarga",
                caption = caption,
                momentType = momentType,
                mediaLabel = mediaLabel,
                memberUids = familyMemberUids()
            )
        }
    }

    fun toggleMomentLike(moment: FamilyMoment) {
        val famId = familyState.value?.id ?: return
        viewModelScope.launch {
            gateway.toggleMomentLike(famId, moment)
        }
    }

    fun addMomentComment(momentId: String, text: String) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Anggota"
        runAction("Komentar ditambahkan") {
            gateway.addMomentComment(famId, momentId, myName, text, familyMemberUids())
        }
    }

    fun startCall(callType: String, targetName: String, onCreated: (String) -> Unit = {}) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Anggota"
        viewModelScope.launch {
            val res = gateway.startCallRoom(famId, myName, callType, targetName, familyMemberUids())
            res.onSuccess { onCreated(it) }
                .onFailure { _actionStatus.value = it.localizedMessage ?: "Gagal memulai panggilan" }
        }
    }

    fun updateCallStatus(callId: String, status: String, join: Boolean = false) {
        val famId = familyState.value?.id ?: return
        viewModelScope.launch {
            gateway.updateCallStatus(famId, callId, status, join)
        }
    }

    fun addTreeNode(fullName: String, relationTitle: String, generation: Long, parentName: String, spouseName: String, notes: String) {
        val famId = familyState.value?.id ?: return
        runAction("Anggota silsilah ditambahkan") {
            gateway.addTreeNode(famId, fullName, relationTitle, generation, parentName, spouseName, notes, familyMemberUids())
        }
    }

    fun addCalendarEvent(title: String, category: String, eventDate: String, description: String, reminderEnabled: Boolean) {
        val famId = familyState.value?.id ?: return
        runAction("Acara keluarga disimpan") {
            gateway.addCalendarEvent(famId, title, category, eventDate, description, reminderEnabled, familyMemberUids())
        }
    }

    fun createGalleryAlbum(title: String, eventName: String, year: Long, mediaType: String, accessLevel: String, initialItems: List<String>) {
        val famId = familyState.value?.id ?: return
        runAction("Album galeri berhasil dibuat") {
            gateway.createGalleryAlbum(famId, title, eventName, year, mediaType, accessLevel, initialItems, familyMemberUids())
        }
    }

    fun addMediaToAlbum(albumId: String, mediaLabel: String) {
        val famId = familyState.value?.id ?: return
        runAction("Media ditambahkan ke album") {
            gateway.addMediaToAlbum(famId, albumId, mediaLabel)
        }
    }

    fun addFundTransaction(title: String, txType: String, amountRupiah: Long, category: String, note: String) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Pengelola"
        runAction("Transaksi kas keluarga dicatat") {
            gateway.addFundTransaction(famId, title, txType, amountRupiah, category, note, myName, familyMemberUids())
        }
    }

    fun deleteFundTransaction(txId: String) {
        val famId = familyState.value?.id ?: return
        runAction("Transaksi kas dihapus") {
            gateway.deleteFundTransaction(famId, txId)
        }
    }

    fun sendSafetyAlert(alertType: String, message: String, lat: Double?, lng: Double?, consented: Boolean) {
        val famId = familyState.value?.id ?: return
        val myName = currentMember()?.displayName ?: "Anggota"
        runAction(if (alertType == "sos") "Sinyal Darurat SOS terkirim ke seluruh keluarga!" else "Pembaruan keamanan terkirim") {
            gateway.sendSafetyAlert(famId, myName, alertType, message, lat, lng, consented, familyMemberUids())
        }
    }

    fun resolveSafetyAlert(alertId: String) {
        val famId = familyState.value?.id ?: return
        runAction("Status darurat ditandai aman") {
            gateway.resolveSafetyAlert(famId, alertId)
        }
    }

    private fun <T> runAction(successMessage: String, block: suspend () -> Result<T>) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = block()
            _isBusy.value = false
            result.onSuccess {
                _actionStatus.value = successMessage
            }.onFailure { err ->
                _actionStatus.value = err.localizedMessage ?: "Terjadi kesalahan saat memproses permintaan."
            }
        }
    }

    private fun <T> Flow<T>.toUiState(fallbackError: String): Flow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w("FamilyViewModel", fallbackError, rootError)
                emit(UiState.Error(rootError.message ?: fallbackError))
            }
}
