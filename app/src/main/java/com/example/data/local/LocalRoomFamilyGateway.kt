package com.example.data.local

import android.util.Patterns
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
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import java.util.UUID

class LocalRoomFamilyGateway(
    private val dao: FamilyLocalDao,
    private val activeUid: String
) : FamilyDataGateway {

    companion object {
        const val DEMO_FAMILY_ID = "fam_demo_harmoni"
        const val DEMO_ADMIN_UID = "local_admin_ayah"
        const val DEMO_TREASURER_UID = "local_bendahara_ibu"
        const val DEMO_MEMBER_UID = "local_member_budi"
        const val DEMO_GRANDPA_UID = "local_member_kakek"
        const val DEMO_ADMIN_EMAIL = "ayah@keluargaharmoni.id"
        const val DEMO_TREASURER_EMAIL = "ibu@keluargaharmoni.id"
        const val DEMO_MEMBER_EMAIL = "budi@keluargaharmoni.id"
        const val DEMO_DEFAULT_PASSWORD = "keluarga123"

        private fun generateSalt(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private fun hashPassword(password: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        private fun isValidEmail(email: String): Boolean {
            val trimmed = email.trim()
            return trimmed.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
        }

        suspend fun ensureDemoAccounts(dao: FamilyLocalDao) {
            val demoAccounts = listOf(
                DEMO_ADMIN_EMAIL to DEMO_ADMIN_UID,
                DEMO_TREASURER_EMAIL to DEMO_TREASURER_UID,
                DEMO_MEMBER_EMAIL to DEMO_MEMBER_UID
            )
            val now = System.currentTimeMillis()
            for ((email, uid) in demoAccounts) {
                if (dao.getAccountByEmail(email) == null) {
                    val salt = generateSalt()
                    dao.upsertAccount(
                        LocalAccountEntity(
                            email = email,
                            userId = uid,
                            passwordSalt = salt,
                            passwordHash = hashPassword(DEMO_DEFAULT_PASSWORD, salt),
                            createdAtMs = now
                        )
                    )
                }
            }
        }

        suspend fun authenticateLocalAccount(
            dao: FamilyLocalDao,
            rawEmail: String,
            password: String
        ): Result<LocalAccountEntity> = runCatching {
            val email = rawEmail.trim().lowercase()
            require(isValidEmail(email)) { "Masukkan alamat email yang valid." }
            require(password.isNotEmpty()) { "Kata sandi tidak boleh kosong." }

            LocalRoomFamilyGateway(dao, DEMO_ADMIN_UID).ensureDemoSeeded()
            ensureDemoAccounts(dao)

            val account = dao.getAccountByEmail(email)
                ?: error("Email belum terdaftar. Silakan tekan tombol Daftar untuk membuat akun.")
            val computed = hashPassword(password, account.passwordSalt)
            require(computed == account.passwordHash) { "Kata sandi yang Anda masukkan salah." }
            account
        }

        suspend fun registerLocalAccount(
            dao: FamilyLocalDao,
            rawEmail: String,
            password: String,
            userId: String
        ): Result<LocalAccountEntity> = runCatching {
            val email = rawEmail.trim().lowercase()
            require(isValidEmail(email)) { "Format email tidak valid." }
            require(password.length >= 6) { "Kata sandi minimal 6 karakter." }

            ensureDemoAccounts(dao)
            require(dao.getAccountByEmail(email) == null) {
                "Email sudah terdaftar. Silakan masuk menggunakan kata sandi Anda."
            }

            val salt = generateSalt()
            val entity = LocalAccountEntity(
                email = email,
                userId = userId,
                passwordSalt = salt,
                passwordHash = hashPassword(password, salt),
                createdAtMs = System.currentTimeMillis()
            )
            dao.upsertAccount(entity)
            entity
        }
    }

    private fun Long.toTimestamp(): Timestamp = Timestamp(Date(this))

    private suspend fun isUserAdmin(familyId: String, uid: String): Boolean {
        val fam = dao.getFamily(familyId)
        if (fam?.adminUid == uid) return true
        return dao.getMember(familyId, uid)?.role == "admin"
    }

    private suspend fun isUserFundManager(familyId: String, uid: String): Boolean {
        if (isUserAdmin(familyId, uid)) return true
        return dao.getMember(familyId, uid)?.fundManager == true
    }

    suspend fun ensureDemoSeeded() {
        if (dao.getFamily(DEMO_FAMILY_ID) != null) return
        val now = System.currentTimeMillis()
        val allUids = listOf(DEMO_ADMIN_UID, DEMO_TREASURER_UID, DEMO_GRANDPA_UID, DEMO_MEMBER_UID)
        val allUidsJson = encodeStringList(allUids)

        dao.upsertFamily(
            LocalFamilyEntity(
                id = DEMO_FAMILY_ID,
                name = "Keluarga Harmoni",
                slogan = "One Family, One Connection, One Love.",
                adminUid = DEMO_ADMIN_UID,
                inviteCode = "KELHARMONI",
                memberUidsJson = allUidsJson,
                createdAtMs = now - 86400000L * 5,
                updatedAtMs = now
            )
        )

        val demoMembers = listOf(
            LocalMemberEntity(
                id = "${DEMO_FAMILY_ID}_$DEMO_ADMIN_UID",
                userId = DEMO_ADMIN_UID,
                familyId = DEMO_FAMILY_ID,
                displayName = "Ayah Hendra",
                relationship = "Ayah",
                role = "admin",
                fundManager = true,
                phone = "081234567801",
                showPhone = true,
                birthday = "12 Mei 1974",
                avatarColorIndex = 0L,
                locationSharingEnabled = true,
                lastLat = null,
                lastLng = null,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 500000L,
                updatedAtMs = now
            ),
            LocalMemberEntity(
                id = "${DEMO_FAMILY_ID}_$DEMO_TREASURER_UID",
                userId = DEMO_TREASURER_UID,
                familyId = DEMO_FAMILY_ID,
                displayName = "Ibu Sari",
                relationship = "Ibu",
                role = "member",
                fundManager = true,
                phone = "081234567802",
                showPhone = true,
                birthday = "24 Agu 1978",
                avatarColorIndex = 1L,
                locationSharingEnabled = true,
                lastLat = null,
                lastLng = null,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 400000L,
                updatedAtMs = now
            ),
            LocalMemberEntity(
                id = "${DEMO_FAMILY_ID}_$DEMO_GRANDPA_UID",
                userId = DEMO_GRANDPA_UID,
                familyId = DEMO_FAMILY_ID,
                displayName = "Kakek Hadi",
                relationship = "Kakek",
                role = "member",
                fundManager = false,
                phone = "081234567803",
                showPhone = true,
                birthday = "03 Jan 1948",
                avatarColorIndex = 2L,
                locationSharingEnabled = false,
                lastLat = null,
                lastLng = null,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 300000L,
                updatedAtMs = now
            ),
            LocalMemberEntity(
                id = "${DEMO_FAMILY_ID}_$DEMO_MEMBER_UID",
                userId = DEMO_MEMBER_UID,
                familyId = DEMO_FAMILY_ID,
                displayName = "Budi Pratama",
                relationship = "Anak",
                role = "member",
                fundManager = false,
                phone = "081234567804",
                showPhone = false,
                birthday = "17 Sep 2002",
                avatarColorIndex = 3L,
                locationSharingEnabled = false,
                lastLat = null,
                lastLng = null,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 200000L,
                updatedAtMs = now
            )
        )
        demoMembers.forEach { m ->
            val mappedEmail = when (m.userId) {
                DEMO_ADMIN_UID -> DEMO_ADMIN_EMAIL
                DEMO_TREASURER_UID -> DEMO_TREASURER_EMAIL
                DEMO_MEMBER_UID -> DEMO_MEMBER_EMAIL
                else -> "${m.userId}@keluargaharmoni.id"
            }
            dao.upsertMember(m)
            dao.upsertUserProfile(
                LocalUserProfileEntity(
                    userId = m.userId,
                    displayName = m.displayName,
                    email = mappedEmail,
                    familyId = DEMO_FAMILY_ID,
                    relationship = m.relationship,
                    phone = m.phone,
                    showPhone = m.showPhone,
                    birthday = m.birthday,
                    createdAtMs = m.createdAtMs,
                    updatedAtMs = m.updatedAtMs
                )
            )
        }
        ensureDemoAccounts(dao)

        dao.upsertGroup(
            LocalGroupEntity(
                id = "grp_demo_besar",
                familyId = DEMO_FAMILY_ID,
                name = "Keluarga Besar Harmoni",
                description = "Grup utama untuk kabar harian, doa bersama, dan informasi keluarga.",
                category = "Keluarga Besar",
                createdBy = DEMO_ADMIN_UID,
                memberUidsJson = allUidsJson,
                lastMessageText = "Jangan lupa kumpul makan malam hari Minggu jam 18.00 ya!",
                lastMessageSender = "Ibu Sari",
                createdAtMs = now - 300000L,
                updatedAtMs = now - 60000L
            )
        )
        dao.upsertGroup(
            LocalGroupEntity(
                id = "grp_demo_liburan",
                familyId = DEMO_FAMILY_ID,
                name = "Rencana Liburan Keluarga",
                description = "Koordinasi jadwal, penginapan, dan transportasi liburan.",
                category = "Liburan",
                createdBy = DEMO_ADMIN_UID,
                memberUidsJson = allUidsJson,
                lastMessageText = "Villa di Bandung sudah dicek kapasitas 10 orang.",
                lastMessageSender = "Ayah Hendra",
                createdAtMs = now - 250000L,
                updatedAtMs = now - 120000L
            )
        )

        dao.upsertMessage(
            LocalMessageEntity(
                id = "msg_demo_1",
                familyId = DEMO_FAMILY_ID,
                roomId = "grp_demo_besar",
                roomType = "group",
                senderId = DEMO_ADMIN_UID,
                senderName = "Ayah Hendra",
                text = "Selamat datang di My Family komunikasi! Semoga komunikasi keluarga makin hangat.",
                messageType = "text",
                mediaLabel = "",
                replyToText = "",
                replyToSender = "",
                forwarded = false,
                status = "read",
                readByUidsJson = allUidsJson,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 180000L,
                updatedAtMs = now - 180000L
            )
        )
        dao.upsertMessage(
            LocalMessageEntity(
                id = "msg_demo_2",
                familyId = DEMO_FAMILY_ID,
                roomId = "grp_demo_besar",
                roomType = "group",
                senderId = DEMO_TREASURER_UID,
                senderName = "Ibu Sari",
                text = "Jangan lupa kumpul makan malam hari Minggu jam 18.00 ya!",
                messageType = "text",
                mediaLabel = "",
                replyToText = "",
                replyToSender = "",
                forwarded = false,
                status = "delivered",
                readByUidsJson = encodeStringList(listOf(DEMO_TREASURER_UID, DEMO_ADMIN_UID)),
                memberUidsJson = allUidsJson,
                createdAtMs = now - 60000L,
                updatedAtMs = now - 60000L
            )
        )

        dao.upsertMoment(
            LocalMomentEntity(
                id = "mom_demo_1",
                familyId = DEMO_FAMILY_ID,
                authorId = DEMO_TREASURER_UID,
                authorName = "Ibu Sari",
                authorRoleLabel = "Ibu",
                caption = "Alhamdulillah panen mangga di halaman rumah Kakek hari ini! Siap dibagikan ke seluruh keluarga.",
                momentType = "text",
                mediaLabel = "",
                likedByUidsJson = encodeStringList(listOf(DEMO_ADMIN_UID, DEMO_MEMBER_UID)),
                commentCount = 1L,
                memberUidsJson = allUidsJson,
                createdAtMs = now - 7200000L,
                updatedAtMs = now - 7200000L
            )
        )
        dao.upsertMomentComment(
            LocalMomentCommentEntity(
                id = "cmt_demo_1",
                momentId = "mom_demo_1",
                authorId = DEMO_MEMBER_UID,
                authorName = "Budi Pratama",
                text = "Asyik! Budi pulang kuliah langsung mampir ke rumah Kakek ya Bu.",
                memberUidsJson = allUidsJson,
                createdAtMs = now - 3600000L
            )
        )

        listOf(
            LocalTreeNodeEntity("tree_demo_1", DEMO_FAMILY_ID, "Hadi Wijaya", "Kakek", 1L, "", "Sri Rahayu", "Tetua Keluarga (Solo)", DEMO_ADMIN_UID, allUidsJson, now - 500L, now),
            LocalTreeNodeEntity("tree_demo_2", DEMO_FAMILY_ID, "Sri Rahayu", "Nenek", 1L, "", "Hadi Wijaya", "Hobi merajut & masak", DEMO_ADMIN_UID, allUidsJson, now - 400L, now),
            LocalTreeNodeEntity("tree_demo_3", DEMO_FAMILY_ID, "Hendra Wijaya", "Ayah", 2L, "Hadi Wijaya", "Sari Kusuma", "Admin Keluarga", DEMO_ADMIN_UID, allUidsJson, now - 300L, now),
            LocalTreeNodeEntity("tree_demo_4", DEMO_FAMILY_ID, "Sari Kusuma", "Ibu", 2L, "", "Hendra Wijaya", "Pengelola Kas", DEMO_ADMIN_UID, allUidsJson, now - 200L, now),
            LocalTreeNodeEntity("tree_demo_5", DEMO_FAMILY_ID, "Budi Pratama", "Anak", 3L, "Hendra Wijaya", "", "Mahasiswa Teknik", DEMO_ADMIN_UID, allUidsJson, now - 100L, now)
        ).forEach { dao.upsertTreeNode(it) }

        dao.upsertCalendarEvent(
            LocalCalendarEventEntity(
                id = "evt_demo_1",
                familyId = DEMO_FAMILY_ID,
                title = "Ulang Tahun Kakek Hadi ke-78",
                category = "birthday",
                eventDate = "03 Jan 2027",
                description = "Syukuran makan siang bersama di rumah utama",
                reminderEnabled = true,
                createdBy = DEMO_ADMIN_UID,
                memberUidsJson = allUidsJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        dao.upsertCalendarEvent(
            LocalCalendarEventEntity(
                id = "evt_demo_2",
                familyId = DEMO_FAMILY_ID,
                title = "Arisan & Kumpul Keluarga Besar",
                category = "gathering",
                eventDate = "28 Nov 2026",
                description = "Silaturahmi bulanan seluruh paman, bibi, dan cucu",
                reminderEnabled = true,
                createdBy = DEMO_ADMIN_UID,
                memberUidsJson = allUidsJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )

        dao.upsertGalleryAlbum(
            LocalGalleryAlbumEntity(
                id = "alb_demo_1",
                familyId = DEMO_FAMILY_ID,
                title = "Lebaran Keluarga Harmoni",
                eventName = "Idul Fitri",
                year = 2026L,
                mediaType = "photo",
                accessLevel = "all_family",
                itemLabelsJson = encodeStringList(listOf("Foto bersama di teras Kakek", "Sungkeman pagi hari")),
                createdBy = DEMO_ADMIN_UID,
                memberUidsJson = allUidsJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )

        dao.upsertFundTransaction(
            LocalFundTransactionEntity(
                id = "tx_demo_1",
                familyId = DEMO_FAMILY_ID,
                title = "Iuran Kas Keluarga Bulan Ini",
                txType = "income",
                amountRupiah = 1500000L,
                category = "Iuran",
                note = "Setoran rutin keluarga besar",
                recordedBy = DEMO_TREASURER_UID,
                recordedByName = "Ibu Sari",
                memberUidsJson = allUidsJson,
                createdAtMs = now - 50000L,
                updatedAtMs = now
            )
        )
        dao.upsertFundTransaction(
            LocalFundTransactionEntity(
                id = "tx_demo_2",
                familyId = DEMO_FAMILY_ID,
                title = "Konsumsi Arisan Keluarga",
                txType = "expense",
                amountRupiah = 350000L,
                category = "Acara",
                note = "Snack & buah-buahan",
                recordedBy = DEMO_TREASURER_UID,
                recordedByName = "Ibu Sari",
                memberUidsJson = allUidsJson,
                createdAtMs = now - 20000L,
                updatedAtMs = now
            )
        )

        dao.upsertSafetyAlert(
            LocalSafetyAlertEntity(
                id = "sos_demo_1",
                familyId = DEMO_FAMILY_ID,
                senderId = DEMO_ADMIN_UID,
                senderName = "Ayah Hendra",
                alertType = "checkin",
                message = "Sudah tiba dengan selamat di kantor.",
                latitude = null,
                longitude = null,
                locationConsented = false,
                status = "resolved",
                memberUidsJson = allUidsJson,
                createdAtMs = now - 100000L,
                updatedAtMs = now
            )
        )
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        dao.observeUserProfile(userId).map { e ->
            e?.let {
                UserProfile(
                    id = it.userId,
                    userId = it.userId,
                    displayName = it.displayName,
                    email = it.email,
                    familyId = it.familyId,
                    relationship = it.relationship,
                    phone = it.phone,
                    showPhone = it.showPhone,
                    birthday = it.birthday,
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeFamily(familyId: String): Flow<Family?> =
        dao.observeFamily(familyId).map { e ->
            e?.let {
                Family(
                    id = it.id,
                    name = it.name,
                    slogan = it.slogan,
                    adminUid = it.adminUid,
                    inviteCode = it.inviteCode,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeMembers(familyId: String): Flow<List<FamilyMember>> =
        dao.observeMembers(familyId).map { list ->
            list.map {
                FamilyMember(
                    id = it.userId,
                    userId = it.userId,
                    familyId = it.familyId,
                    displayName = it.displayName,
                    relationship = it.relationship,
                    role = it.role,
                    fundManager = it.fundManager,
                    phone = it.phone,
                    showPhone = it.showPhone,
                    birthday = it.birthday,
                    avatarColorIndex = it.avatarColorIndex,
                    locationSharingEnabled = it.locationSharingEnabled,
                    lastLat = it.lastLat,
                    lastLng = it.lastLng,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeGroups(familyId: String): Flow<List<FamilyGroup>> =
        dao.observeGroups(familyId).map { list ->
            list.map {
                FamilyGroup(
                    id = it.id,
                    familyId = it.familyId,
                    name = it.name,
                    description = it.description,
                    category = it.category,
                    createdBy = it.createdBy,
                    memberUids = decodeStringList(it.memberUidsJson),
                    lastMessageText = it.lastMessageText,
                    lastMessageSender = it.lastMessageSender,
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observePrivateChats(familyId: String): Flow<List<PrivateChat>> =
        dao.observePrivateChats(familyId).map { list ->
            list.filter { decodeStringList(it.memberUidsJson).contains(activeUid) }.map {
                PrivateChat(
                    id = it.id,
                    familyId = it.familyId,
                    participantNames = decodeStringList(it.participantNamesJson),
                    memberUids = decodeStringList(it.memberUidsJson),
                    lastMessageText = it.lastMessageText,
                    lastMessageSender = it.lastMessageSender,
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeMessages(familyId: String, roomId: String): Flow<List<ChatMessage>> =
        dao.observeMessages(familyId, roomId).map { list ->
            list.map {
                ChatMessage(
                    id = it.id,
                    familyId = it.familyId,
                    roomId = it.roomId,
                    roomType = it.roomType,
                    senderId = it.senderId,
                    senderName = it.senderName,
                    text = it.text,
                    messageType = it.messageType,
                    mediaLabel = it.mediaLabel,
                    replyToText = it.replyToText,
                    replyToSender = it.replyToSender,
                    forwarded = it.forwarded,
                    status = it.status,
                    readByUids = decodeStringList(it.readByUidsJson),
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeMoments(familyId: String): Flow<List<FamilyMoment>> =
        dao.observeMoments(familyId).map { list ->
            list.map {
                FamilyMoment(
                    id = it.id,
                    familyId = it.familyId,
                    authorId = it.authorId,
                    authorName = it.authorName,
                    authorRoleLabel = it.authorRoleLabel,
                    caption = it.caption,
                    momentType = it.momentType,
                    mediaLabel = it.mediaLabel,
                    likedByUids = decodeStringList(it.likedByUidsJson),
                    commentCount = it.commentCount,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeMomentComments(familyId: String, momentId: String): Flow<List<MomentComment>> =
        dao.observeMomentComments(momentId).map { list ->
            list.map {
                MomentComment(
                    id = it.id,
                    momentId = it.momentId,
                    authorId = it.authorId,
                    authorName = it.authorName,
                    text = it.text,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp()
                )
            }
        }

    override fun observeCalls(familyId: String): Flow<List<FamilyCall>> =
        dao.observeCalls(familyId).map { list ->
            list.map {
                FamilyCall(
                    id = it.id,
                    familyId = it.familyId,
                    callerId = it.callerId,
                    callerName = it.callerName,
                    callType = it.callType,
                    targetName = it.targetName,
                    status = it.status,
                    participantUids = decodeStringList(it.participantUidsJson),
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeTreeNodes(familyId: String): Flow<List<TreeNode>> =
        dao.observeTreeNodes(familyId).map { list ->
            list.map {
                TreeNode(
                    id = it.id,
                    familyId = it.familyId,
                    fullName = it.fullName,
                    relationTitle = it.relationTitle,
                    generation = it.generation,
                    parentName = it.parentName,
                    spouseName = it.spouseName,
                    notes = it.notes,
                    createdBy = it.createdBy,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeCalendarEvents(familyId: String): Flow<List<CalendarEvent>> =
        dao.observeCalendarEvents(familyId).map { list ->
            list.map {
                CalendarEvent(
                    id = it.id,
                    familyId = it.familyId,
                    title = it.title,
                    category = it.category,
                    eventDate = it.eventDate,
                    description = it.description,
                    reminderEnabled = it.reminderEnabled,
                    createdBy = it.createdBy,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeGalleryAlbums(familyId: String): Flow<List<GalleryAlbum>> =
        dao.observeGalleryAlbums(familyId).map { list ->
            list.map {
                GalleryAlbum(
                    id = it.id,
                    familyId = it.familyId,
                    title = it.title,
                    eventName = it.eventName,
                    year = it.year,
                    mediaType = it.mediaType,
                    accessLevel = it.accessLevel,
                    itemLabels = decodeStringList(it.itemLabelsJson),
                    createdBy = it.createdBy,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeFundTransactions(familyId: String): Flow<List<FundTransaction>> =
        dao.observeFundTransactions(familyId).map { list ->
            list.map {
                FundTransaction(
                    id = it.id,
                    familyId = it.familyId,
                    title = it.title,
                    txType = it.txType,
                    amountRupiah = it.amountRupiah,
                    category = it.category,
                    note = it.note,
                    recordedBy = it.recordedBy,
                    recordedByName = it.recordedByName,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override fun observeSafetyAlerts(familyId: String): Flow<List<SafetyAlert>> =
        dao.observeSafetyAlerts(familyId).map { list ->
            list.map {
                SafetyAlert(
                    id = it.id,
                    familyId = it.familyId,
                    senderId = it.senderId,
                    senderName = it.senderName,
                    alertType = it.alertType,
                    message = it.message,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    locationConsented = it.locationConsented,
                    status = it.status,
                    memberUids = decodeStringList(it.memberUidsJson),
                    createdAt = it.createdAtMs.toTimestamp(),
                    updatedAt = it.updatedAtMs.toTimestamp()
                )
            }
        }

    override suspend fun createFamilyAndAdminProfile(
        familyName: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String> {
        val now = System.currentTimeMillis()
        val familyId = "fam_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val inviteCode = "KEL${UUID.randomUUID().toString().replace("-", "").take(6).uppercase()}"
        val memberUidsJson = encodeStringList(listOf(activeUid))

        dao.upsertFamily(
            LocalFamilyEntity(
                id = familyId,
                name = familyName.trim(),
                slogan = "One Family, One Connection, One Love.",
                adminUid = activeUid,
                inviteCode = inviteCode,
                memberUidsJson = memberUidsJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        dao.upsertMember(
            LocalMemberEntity(
                id = "${familyId}_$activeUid",
                userId = activeUid,
                familyId = familyId,
                displayName = displayName.trim(),
                relationship = relationship.trim(),
                role = "admin",
                fundManager = true,
                phone = phone.trim(),
                showPhone = showPhone,
                birthday = birthday.trim(),
                avatarColorIndex = 0L,
                locationSharingEnabled = false,
                lastLat = null,
                lastLng = null,
                memberUidsJson = memberUidsJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        dao.upsertUserProfile(
            LocalUserProfileEntity(
                userId = activeUid,
                displayName = displayName.trim(),
                email = email.ifBlank { "uji-coba@lokal" },
                familyId = familyId,
                relationship = relationship.trim(),
                phone = phone.trim(),
                showPhone = showPhone,
                birthday = birthday.trim(),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        dao.upsertGroup(
            LocalGroupEntity(
                id = "grp_${UUID.randomUUID().toString().replace("-", "").take(10)}",
                familyId = familyId,
                name = "Keluarga Besar ${familyName.trim()}",
                description = "Grup komunikasi utama seluruh anggota keluarga.",
                category = "Keluarga Besar",
                createdBy = activeUid,
                memberUidsJson = memberUidsJson,
                lastMessageText = "Selamat datang di ${familyName.trim()}!",
                lastMessageSender = displayName.trim(),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(familyId)
    }

    override suspend fun joinFamilyWithInviteCode(
        inviteCodeInput: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        email: String
    ): Result<String> {
        val code = inviteCodeInput.trim().uppercase()
        val fam = dao.getFamilyByInviteCode(code)
            ?: return Result.failure(IllegalArgumentException("Kode undangan tidak ditemukan di database lokal (coba: KELHARMONI)."))
        val now = System.currentTimeMillis()
        val updatedMembers = (decodeStringList(fam.memberUidsJson) + activeUid).distinct()
        val updatedJson = encodeStringList(updatedMembers)

        dao.upsertFamily(fam.copy(memberUidsJson = updatedJson, updatedAtMs = now))
        dao.upsertMember(
            LocalMemberEntity(
                id = "${fam.id}_$activeUid",
                userId = activeUid,
                familyId = fam.id,
                displayName = displayName.trim(),
                relationship = relationship.trim(),
                role = "member",
                fundManager = false,
                phone = phone.trim(),
                showPhone = showPhone,
                birthday = birthday.trim(),
                avatarColorIndex = (updatedMembers.size % 6).toLong(),
                locationSharingEnabled = false,
                lastLat = null,
                lastLng = null,
                memberUidsJson = updatedJson,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        dao.upsertUserProfile(
            LocalUserProfileEntity(
                userId = activeUid,
                displayName = displayName.trim(),
                email = email.ifBlank { "uji-coba@lokal" },
                familyId = fam.id,
                relationship = relationship.trim(),
                phone = phone.trim(),
                showPhone = showPhone,
                birthday = birthday.trim(),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(fam.id)
    }

    override suspend fun updateOwnProfile(
        familyId: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String,
        locationSharingEnabled: Boolean
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val existingMember = dao.getMember(familyId, activeUid)
            ?: return Result.failure(IllegalStateException("Anggota tidak ditemukan."))
        dao.upsertMember(
            existingMember.copy(
                displayName = displayName.trim(),
                relationship = relationship.trim(),
                phone = phone.trim(),
                showPhone = showPhone,
                birthday = birthday.trim(),
                locationSharingEnabled = locationSharingEnabled,
                updatedAtMs = now
            )
        )
        val existingUser = dao.getUserProfile(activeUid)
        if (existingUser != null) {
            dao.upsertUserProfile(
                existingUser.copy(
                    displayName = displayName.trim(),
                    relationship = relationship.trim(),
                    phone = phone.trim(),
                    showPhone = showPhone,
                    birthday = birthday.trim(),
                    updatedAtMs = now
                )
            )
        }
        return Result.success(Unit)
    }

    override suspend fun updateOwnGpsLocation(
        familyId: String,
        latitude: Double?,
        longitude: Double?,
        locationSharingEnabled: Boolean
    ): Result<Unit> {
        val existingMember = dao.getMember(familyId, activeUid)
            ?: return Result.failure(IllegalStateException("Anggota tidak ditemukan."))
        dao.upsertMember(
            existingMember.copy(
                locationSharingEnabled = locationSharingEnabled,
                lastLat = if (locationSharingEnabled) (latitude ?: existingMember.lastLat) else null,
                lastLng = if (locationSharingEnabled) (longitude ?: existingMember.lastLng) else null,
                updatedAtMs = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun createGroup(
        familyId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<String> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Pembuatan grup hanya boleh dilakukan oleh Admin!"))
        }
        val now = System.currentTimeMillis()
        val groupId = "grp_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertGroup(
            LocalGroupEntity(
                id = groupId,
                familyId = familyId,
                name = name.trim(),
                description = description.trim(),
                category = category.trim(),
                createdBy = activeUid,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                lastMessageText = "Grup dibuat oleh Admin",
                lastMessageSender = "Sistem",
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(groupId)
    }

    override suspend fun updateGroupDetails(
        familyId: String,
        groupId: String,
        name: String,
        description: String,
        category: String,
        memberUids: List<String>
    ): Result<Unit> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin yang dapat mengubah grup!"))
        }
        val existing = dao.getGroup(groupId)
            ?: return Result.failure(IllegalArgumentException("Grup tidak ditemukan."))
        dao.upsertGroup(
            existing.copy(
                name = name.trim(),
                description = description.trim(),
                category = category.trim(),
                memberUidsJson = encodeStringList(memberUids.distinct()),
                updatedAtMs = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun deleteGroup(familyId: String, groupId: String): Result<Unit> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin yang dapat menghapus grup!"))
        }
        dao.deleteGroup(familyId, groupId)
        return Result.success(Unit)
    }

    override suspend fun updateMemberRoles(
        familyId: String,
        targetMemberUid: String,
        role: String,
        fundManager: Boolean
    ): Result<Unit> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Anggota biasa tidak dapat mengubah peran!"))
        }
        val target = dao.getMember(familyId, targetMemberUid)
            ?: return Result.failure(IllegalArgumentException("Anggota tidak ditemukan."))
        dao.upsertMember(
            target.copy(
                role = role,
                fundManager = fundManager,
                updatedAtMs = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun removeMember(familyId: String, targetMemberUid: String): Result<Unit> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin yang dapat mengeluarkan anggota!"))
        }
        dao.deleteMember(familyId, targetMemberUid)
        val fam = dao.getFamily(familyId)
        if (fam != null) {
            val remaining = decodeStringList(fam.memberUidsJson).filter { it != targetMemberUid }
            dao.upsertFamily(fam.copy(memberUidsJson = encodeStringList(remaining), updatedAtMs = System.currentTimeMillis()))
        }
        return Result.success(Unit)
    }

    override suspend fun updateFamilyName(familyId: String, newName: String): Result<Unit> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin yang dapat mengubah nama keluarga!"))
        }
        val fam = dao.getFamily(familyId) ?: return Result.failure(IllegalArgumentException("Keluarga tidak ditemukan."))
        dao.upsertFamily(fam.copy(name = newName.trim(), updatedAtMs = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    override suspend fun regenerateInviteCode(familyId: String, familyName: String, oldCode: String): Result<String> {
        if (!isUserAdmin(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin yang dapat membuat kode undangan baru!"))
        }
        val newCode = "KEL${UUID.randomUUID().toString().replace("-", "").take(6).uppercase()}"
        val fam = dao.getFamily(familyId) ?: return Result.failure(IllegalArgumentException("Keluarga tidak ditemukan."))
        dao.upsertFamily(fam.copy(inviteCode = newCode, updatedAtMs = System.currentTimeMillis()))
        return Result.success(newCode)
    }

    override suspend fun syncFamilyMemberAccess(familyId: String, memberUids: List<String>) {
        // Not needed locally since Room queries by familyId
    }

    override suspend fun getOrCreatePrivateChat(
        familyId: String,
        otherUid: String,
        currentName: String,
        otherName: String
    ): Result<String> {
        val sortedUids = listOf(activeUid, otherUid).sorted()
        val chatId = "dm_${sortedUids[0].take(14)}_${sortedUids[1].take(14)}"
        if (dao.getPrivateChat(chatId) == null) {
            val now = System.currentTimeMillis()
            val names = if (sortedUids[0] == activeUid) listOf(currentName, otherName) else listOf(otherName, currentName)
            dao.upsertPrivateChat(
                LocalPrivateChatEntity(
                    id = chatId,
                    familyId = familyId,
                    participantNamesJson = encodeStringList(names),
                    memberUidsJson = encodeStringList(sortedUids),
                    lastMessageText = "Mulai percakapan keluarga",
                    lastMessageSender = currentName,
                    createdAtMs = now,
                    updatedAtMs = now
                )
            )
        }
        return Result.success(chatId)
    }

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
    ): Result<String> {
        val now = System.currentTimeMillis()
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val finalMembers = encodeStringList((memberUids + activeUid).distinct())
        dao.upsertMessage(
            LocalMessageEntity(
                id = msgId,
                familyId = familyId,
                roomId = roomId,
                roomType = roomType,
                senderId = activeUid,
                senderName = senderName,
                text = text,
                messageType = messageType,
                mediaLabel = mediaLabel,
                replyToText = replyToText,
                replyToSender = replyToSender,
                forwarded = forwarded,
                status = "delivered",
                readByUidsJson = encodeStringList(listOf(activeUid)),
                memberUidsJson = finalMembers,
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        val preview = when (messageType) {
            "image" -> "[Foto] ${mediaLabel.ifBlank { text }}"
            "video" -> "[Video] ${mediaLabel.ifBlank { text }}"
            "document" -> "[Dokumen] ${mediaLabel.ifBlank { text }}"
            "voice" -> "[Pesan Suara] ${mediaLabel.ifBlank { text }}"
            else -> text
        }.take(120)

        if (roomType == "group") {
            dao.getGroup(roomId)?.let { grp ->
                dao.upsertGroup(grp.copy(lastMessageText = preview, lastMessageSender = senderName, updatedAtMs = now))
            }
        } else {
            dao.getPrivateChat(roomId)?.let { chat ->
                dao.upsertPrivateChat(chat.copy(lastMessageText = preview, lastMessageSender = senderName, updatedAtMs = now))
            }
        }
        return Result.success(msgId)
    }

    override suspend fun markMessagesAsRead(familyId: String, messages: List<ChatMessage>) {
        val now = System.currentTimeMillis()
        messages.filter { it.senderId != activeUid && !it.readByUids.contains(activeUid) && it.id.isNotBlank() }
            .take(20)
            .forEach { msg ->
                dao.getMessage(msg.id)?.let { entity ->
                    val updatedReaders = (decodeStringList(entity.readByUidsJson) + activeUid).distinct()
                    dao.upsertMessage(entity.copy(status = "read", readByUidsJson = encodeStringList(updatedReaders), updatedAtMs = now))
                }
            }
    }

    override suspend fun createMoment(
        familyId: String,
        authorName: String,
        authorRoleLabel: String,
        caption: String,
        momentType: String,
        mediaLabel: String,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "mom_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertMoment(
            LocalMomentEntity(
                id = id,
                familyId = familyId,
                authorId = activeUid,
                authorName = authorName,
                authorRoleLabel = authorRoleLabel,
                caption = caption.trim(),
                momentType = momentType,
                mediaLabel = mediaLabel,
                likedByUidsJson = encodeStringList(emptyList()),
                commentCount = 0L,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun toggleMomentLike(familyId: String, moment: FamilyMoment): Result<Unit> {
        val existing = dao.getMoment(moment.id) ?: return Result.success(Unit)
        val currentLikes = decodeStringList(existing.likedByUidsJson)
        val updated = if (currentLikes.contains(activeUid)) currentLikes - activeUid else currentLikes + activeUid
        dao.upsertMoment(existing.copy(likedByUidsJson = encodeStringList(updated), updatedAtMs = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    override suspend fun addMomentComment(
        familyId: String,
        momentId: String,
        authorName: String,
        text: String,
        memberUids: List<String>
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        dao.upsertMomentComment(
            LocalMomentCommentEntity(
                id = "cmt_${UUID.randomUUID().toString().replace("-", "").take(10)}",
                momentId = momentId,
                authorId = activeUid,
                authorName = authorName,
                text = text.trim(),
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now
            )
        )
        dao.getMoment(momentId)?.let { mom ->
            dao.upsertMoment(mom.copy(commentCount = mom.commentCount + 1L, updatedAtMs = now))
        }
        return Result.success(Unit)
    }

    override suspend fun startCallRoom(
        familyId: String,
        callerName: String,
        callType: String,
        targetName: String,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "call_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertCall(
            LocalCallEntity(
                id = id,
                familyId = familyId,
                callerId = activeUid,
                callerName = callerName,
                callType = callType,
                targetName = targetName,
                status = "ringing",
                participantUidsJson = encodeStringList(listOf(activeUid)),
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun updateCallStatus(
        familyId: String,
        callId: String,
        status: String,
        joinAsParticipant: Boolean
    ): Result<Unit> {
        val call = dao.getCall(callId) ?: return Result.success(Unit)
        val participants = decodeStringList(call.participantUidsJson).let {
            if (joinAsParticipant) (it + activeUid).distinct() else it
        }
        dao.upsertCall(
            call.copy(
                status = status,
                participantUidsJson = encodeStringList(participants),
                updatedAtMs = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun addTreeNode(
        familyId: String,
        fullName: String,
        relationTitle: String,
        generation: Long,
        parentName: String,
        spouseName: String,
        notes: String,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "tree_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertTreeNode(
            LocalTreeNodeEntity(
                id = id,
                familyId = familyId,
                fullName = fullName.trim(),
                relationTitle = relationTitle.trim(),
                generation = generation,
                parentName = parentName.trim(),
                spouseName = spouseName.trim(),
                notes = notes.trim(),
                createdBy = activeUid,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun addCalendarEvent(
        familyId: String,
        title: String,
        category: String,
        eventDate: String,
        description: String,
        reminderEnabled: Boolean,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "evt_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertCalendarEvent(
            LocalCalendarEventEntity(
                id = id,
                familyId = familyId,
                title = title.trim(),
                category = category,
                eventDate = eventDate.trim(),
                description = description.trim(),
                reminderEnabled = reminderEnabled,
                createdBy = activeUid,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun createGalleryAlbum(
        familyId: String,
        title: String,
        eventName: String,
        year: Long,
        mediaType: String,
        accessLevel: String,
        itemLabels: List<String>,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "alb_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertGalleryAlbum(
            LocalGalleryAlbumEntity(
                id = id,
                familyId = familyId,
                title = title.trim(),
                eventName = eventName.trim(),
                year = year,
                mediaType = mediaType,
                accessLevel = accessLevel,
                itemLabelsJson = encodeStringList(itemLabels.take(30)),
                createdBy = activeUid,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun addMediaToAlbum(familyId: String, albumId: String, mediaLabel: String): Result<Unit> {
        val album = dao.getGalleryAlbum(albumId) ?: return Result.success(Unit)
        val items = (decodeStringList(album.itemLabelsJson) + mediaLabel).distinct()
        dao.upsertGalleryAlbum(album.copy(itemLabelsJson = encodeStringList(items), updatedAtMs = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    override suspend fun addFundTransaction(
        familyId: String,
        title: String,
        txType: String,
        amountRupiah: Long,
        category: String,
        note: String,
        recordedByName: String,
        memberUids: List<String>
    ): Result<String> {
        if (!isUserFundManager(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin atau Pengelola Dana yang boleh mencatat kas!"))
        }
        val now = System.currentTimeMillis()
        val id = "tx_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertFundTransaction(
            LocalFundTransactionEntity(
                id = id,
                familyId = familyId,
                title = title.trim(),
                txType = txType,
                amountRupiah = amountRupiah,
                category = category.trim(),
                note = note.trim(),
                recordedBy = activeUid,
                recordedByName = recordedByName,
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun deleteFundTransaction(familyId: String, txId: String): Result<Unit> {
        if (!isUserFundManager(familyId, activeUid)) {
            return Result.failure(SecurityException("PERMISSION_DENIED: Hanya Admin atau Pengelola Dana yang boleh menghapus transaksi kas!"))
        }
        dao.deleteFundTransaction(familyId, txId)
        return Result.success(Unit)
    }

    override suspend fun sendSafetyAlert(
        familyId: String,
        senderName: String,
        alertType: String,
        message: String,
        latitude: Double?,
        longitude: Double?,
        locationConsented: Boolean,
        memberUids: List<String>
    ): Result<String> {
        val now = System.currentTimeMillis()
        val id = "sos_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        dao.upsertSafetyAlert(
            LocalSafetyAlertEntity(
                id = id,
                familyId = familyId,
                senderId = activeUid,
                senderName = senderName,
                alertType = alertType,
                message = message.trim(),
                latitude = if (locationConsented) latitude else null,
                longitude = if (locationConsented) longitude else null,
                locationConsented = locationConsented,
                status = "active",
                memberUidsJson = encodeStringList((memberUids + activeUid).distinct()),
                createdAtMs = now,
                updatedAtMs = now
            )
        )
        return Result.success(id)
    }

    override suspend fun resolveSafetyAlert(familyId: String, alertId: String): Result<Unit> {
        val alert = dao.getSafetyAlert(alertId) ?: return Result.success(Unit)
        dao.upsertSafetyAlert(alert.copy(status = "resolved", updatedAtMs = System.currentTimeMillis()))
        return Result.success(Unit)
    }
}
