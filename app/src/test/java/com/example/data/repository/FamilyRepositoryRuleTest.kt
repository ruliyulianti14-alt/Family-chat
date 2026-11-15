package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FamilyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun adminCreatesFamilyAndGroup_memberCannotCreateGroup_butCanJoinAndSendMessage() = runBlocking {
        val adminEmail = "admin_${UUID.randomUUID().toString().take(6)}@test.com"
        val memberEmail = "member_${UUID.randomUUID().toString().take(6)}@test.com"

        // 1. Admin creates family and default group
        val adminUid = signInTestUser(adminEmail)
        val adminCoreRepo = FamilyCoreRepository(firestore, auth)
        val adminCommsRepo = FamilyCommsRepository(firestore, auth)
        val adminLifeRepo = FamilyLifeRepository(firestore, auth)

        val familyId = withTimeout(TIMEOUT_MS) {
            adminCoreRepo.createFamilyAndAdminProfile(
                familyName = "Keluarga Cemara",
                displayName = "Ayah",
                relationship = "Ayah",
                phone = "0812345678",
                showPhone = true,
                birthday = "1975-05-12",
                email = adminEmail
            ).getOrThrow()
        }
        assertTrue(familyId.isNotBlank())

        val family = withTimeout(TIMEOUT_MS) {
            adminCoreRepo.observeFamily(familyId).first { it != null }!!
        }

        // Admin creates an extra group -> succeeds
        val adminGroupResult = withTimeout(TIMEOUT_MS) {
            adminCoreRepo.createGroup(
                familyId = familyId,
                name = "Liburan Bali",
                description = "Koordinasi liburan keluarga",
                category = "Liburan",
                memberUids = listOf(adminUid)
            )
        }
        assertTrue(adminGroupResult.isSuccess)
        val adminGroupId = adminGroupResult.getOrThrow()

        // Admin records a Family Fund transaction -> succeeds
        val adminFundResult = withTimeout(TIMEOUT_MS) {
            adminLifeRepo.addFundTransaction(
                familyId = familyId,
                title = "Saldo Awal Kas",
                txType = "income",
                amountRupiah = 500000L,
                category = "Iuran",
                note = "Setoran awal",
                recordedByName = "Ayah",
                memberUids = listOf(adminUid)
            )
        }
        assertTrue(adminFundResult.isSuccess)

        // 2. Member joins family via invite code
        val memberUid = signInTestUser(memberEmail)
        val memberCoreRepo = FamilyCoreRepository(firestore, auth)
        val memberLifeRepo = FamilyLifeRepository(firestore, auth)

        val joinResult = withTimeout(TIMEOUT_MS) {
            memberCoreRepo.joinFamilyWithInviteCode(
                inviteCodeInput = family.inviteCode,
                displayName = "Budi",
                relationship = "Anak",
                phone = "0898765432",
                showPhone = false,
                birthday = "2001-08-17",
                email = memberEmail
            )
        }
        assertTrue("Member join failed: ${joinResult.exceptionOrNull()}", joinResult.isSuccess)

        // 3. Regular Member attempts to create a group -> MUST FAIL with PERMISSION_DENIED
        val memberCreateGroup = withTimeout(TIMEOUT_MS) {
            memberCoreRepo.createGroup(
                familyId = familyId,
                name = "Grup Member Ilegal",
                description = "Seharusnya ditolak",
                category = "Santai",
                memberUids = listOf(memberUid)
            )
        }
        assertTrue(memberCreateGroup.isFailure)
        val groupEx = memberCreateGroup.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, groupEx?.code)

        // 4. Regular Member attempts to promote self to admin -> MUST FAIL with PERMISSION_DENIED
        val selfPromote = withTimeout(TIMEOUT_MS) {
            memberCoreRepo.updateMemberRoles(
                familyId = familyId,
                targetMemberUid = memberUid,
                role = "admin",
                fundManager = true
            )
        }
        assertTrue(selfPromote.isFailure)
        val promoteEx = selfPromote.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, promoteEx?.code)

        // 5. Regular Member attempts to record a Family Fund transaction -> MUST FAIL with PERMISSION_DENIED
        val memberFundResult = withTimeout(TIMEOUT_MS) {
            memberLifeRepo.addFundTransaction(
                familyId = familyId,
                title = "Transaksi Ilegal",
                txType = "expense",
                amountRupiah = 100000L,
                category = "Lainnya",
                note = "Ditolak",
                recordedByName = "Budi",
                memberUids = listOf(adminUid, memberUid)
            )
        }
        assertTrue(memberFundResult.isFailure)
        val fundEx = memberFundResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fundEx?.code)

        // 6. Regular Member CAN send a message in a private chat or moment
        val momentResult = withTimeout(TIMEOUT_MS) {
            FamilyCommsRepository(firestore, auth).createMoment(
                familyId = familyId,
                authorName = "Budi",
                authorRoleLabel = "Anak",
                caption = "Selamat pagi keluarga!",
                momentType = "text",
                mediaLabel = "",
                memberUids = listOf(adminUid, memberUid)
            )
        }
        assertTrue(momentResult.isSuccess)

        // 7. Unauthenticated user querying groups -> MUST FAIL with PERMISSION_DENIED
        auth.signOut()
        var unauthFailed = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                FamilyCoreRepository(firestore, auth).observeGroups(familyId).first()
            }
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            if (fsEx != null) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
                unauthFailed = true
            } else if (e is IllegalStateException) {
                unauthFailed = true
            }
        }
        assertTrue("Expected unauthenticated access to fail", unauthFailed)

        // Cleanup admin group reference check
        assertTrue(adminGroupId.isNotBlank())
    }

    private companion object {
        const val TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
