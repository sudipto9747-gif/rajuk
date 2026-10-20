package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ADMIN_LOGIN_PHONE
import com.example.data.model.InspectionReport
import com.example.data.model.PasswordHasher
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleReport(uid: String) = InspectionReport(
        userId = uid,
        reportMonth = "2026-10",
        inspectionDate = "2026-10-19",
        inspectorName = "মোঃ রাশেদুল ইসলাম",
        ownerName = "আব্দুল করিম",
        buildingAddress = "বাড়ি ১২, রোড ৫, ধানমন্ডি, ঢাকা",
        ownerMobile = "01711223344",
        buildingType = "আবাসিক",
        previousInspectionCount = 2L,
        authorizedOfficerInspected = true,
        deviationType = "অনুমোদিত নকশা বহির্ভূত নির্মাণ",
        actionTaken = "কারণ দর্শানোর নোটিশ প্রদান",
        remarks = "৩য় তলার কাজ চলমান"
    )

    @Test
    fun createAndObserveInspection_phoneUser_succeeds() = runBlocking {
        val aliceUid = PasswordHasher.phoneToUserId("01711223344")
        val repository = InspectionRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createInspection(sampleReport(aliceUid), userId = aliceUid)
        }
        assertTrue(createResult.isSuccess)
        val reportId = createResult.getOrThrow()
        assertTrue(reportId.isNotBlank())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeInspections(aliceUid).first { list -> list.any { it.id == reportId } }
        }
        val fetched = emitted.first { it.id == reportId }
        assertEquals("আব্দুল করিম", fetched.ownerName)
        assertEquals("2026-10-19", fetched.inspectionDate)
        assertEquals(2L, fetched.previousInspectionCount)
        assertTrue(fetched.authorizedOfficerInspected)

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateInspection(
                fetched.copy(
                    actionTaken = "কাজ বন্ধের নির্দেশ ও নোটিশ জারি",
                    previousInspectionCount = 3L
                ),
                userId = aliceUid
            )
        }
        assertTrue(updateResult.isSuccess)
    }

    @Test
    fun fullRegistrationAdminApprovalAndPostLoginSetNameFlow_succeeds() = runBlocking {
        val bobPhone = "01812345678"
        val bobUid = PasswordHasher.phoneToUserId(bobPhone)
        val repository = InspectionRepository(firestore, auth)

        // 1. Bob registers with ONLY phone number and password (empty name, no Google login) -> creates 'pending' account
        val regResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.registerOrResubmitUserAccount(
                userId = bobUid,
                inspectorName = "",
                phoneNumber = bobPhone,
                rawPassword = "secretPassword123",
                isBootstrappedAdmin = false
            )
        }
        assertTrue(regResult.isSuccess)

        val observedAccount = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserAccount(bobUid).first { it != null }
        }
        assertNotNull(observedAccount)
        assertEquals("pending", observedAccount?.approvalStatus)
        assertEquals("", observedAccount?.inspectorName)
        assertEquals(bobPhone, observedAccount?.phoneNumber)
        assertEquals(PasswordHasher.sha256Hex("secretPassword123"), observedAccount?.passwordHash)

        // 2. Non-admin attempting to self-approve before Admin activation must fail with PERMISSION_DENIED
        val selfApproveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateAccountApprovalByAdmin(
                targetAccount = observedAccount!!,
                newApprovalStatus = "approved"
            )
        }
        assertTrue(selfApproveResult.isFailure)
        val ex = selfApproveResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)

        // 3. Admin logs in with phone 01767563095 and password 889747 -> activates admin account
        assertTrue(PasswordHasher.isAdminCredentials("01767563095", "889747"))
        val adminUid = PasswordHasher.phoneToUserId(ADMIN_LOGIN_PHONE)
        val activateAdminRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.activateAdminAccount(userId = adminUid, existingAccount = null)
        }
        assertTrue(activateAdminRes.isSuccess)

        val adminAcc = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserAccount(adminUid).first { it != null }
        }
        assertEquals("admin", adminAcc?.role)
        assertEquals("approved", adminAcc?.approvalStatus)
        assertEquals(ADMIN_LOGIN_PHONE, adminAcc?.phoneNumber)

        // Verify Admin can list all user accounts without PERMISSION_DENIED
        val allAccountsForAdmin = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAllUserAccountsForAdmin().first { list -> list.any { it.userId == bobUid } }
        }
        assertTrue(allAccountsForAdmin.any { it.userId == bobUid && it.approvalStatus == "pending" })

        // 4. Admin approves Bob's pending registration
        val adminApproveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateAccountApprovalByAdmin(
                targetAccount = observedAccount!!,
                newApprovalStatus = "approved"
            )
        }
        assertTrue(adminApproveResult.isSuccess)

        // 5. Bob logs in with phone & password and sets his name after login
        val approvedBobAccount = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserAccount(bobUid).first { it?.approvalStatus == "approved" }
        }
        assertNotNull(approvedBobAccount)

        val setNameResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserNameAfterLogin(
                userId = bobUid,
                inspectorName = "প্রকৌশলী মোঃ করিম উদ্দিন",
                designation = "ইমারত পরিদর্শক",
                officeName = "জোন-৩",
                existingAccount = approvedBobAccount,
                existingProfile = null
            )
        }
        assertTrue(setNameResult.isSuccess)

        val updatedBobAccount = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserAccount(bobUid).first { it?.inspectorName == "প্রকৌশলী মোঃ করিম উদ্দিন" }
        }
        assertEquals("প্রকৌশলী মোঃ করিম উদ্দিন", updatedBobAccount?.inspectorName)
    }

    @Test
    fun createInspection_withInvalidEmptyOwnerName_failsWithPermissionDenied() = runBlocking {
        val aliceUid = PasswordHasher.phoneToUserId("01711223344")
        val repository = InspectionRepository(firestore, auth)

        val invalidReport = sampleReport(aliceUid).copy(ownerName = "")
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createInspection(invalidReport, userId = aliceUid)
        }
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun createUnresolvedReport_withFiveFieldsAndDurationOption_succeeds() = runBlocking {
        val aliceUid = PasswordHasher.phoneToUserId("01711223344")
        val repository = InspectionRepository(firestore, auth)

        val unresolvedReport = sampleReport(aliceUid).copy(
            reportCategory = "অনিষ্পন্ন রিপোর্ট",
            ownerName = "অনিষ্পন্ন রিপোর্ট (অক্টোবর ২০২৬)",
            buildingAddress = "গত মাসের জের: ১২, আলোচ্য মাসের প্রাপ্তি: ৮, মোট প্রাপ্তি: ২০",
            ownerMobile = "-",
            buildingType = "অনিষ্পন্ন রিপোর্ট",
            deviationType = "২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়",
            actionTaken = "মাসের শেষ কার্যদিবসে অনিষ্পন্ন: ২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়",
            previousMonthBalance = 12L,
            currentMonthReceived = 8L,
            totalReceived = 20L,
            unresolvedAtMonthEnd = 5L,
            unresolvedDurationCategory = "২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়",
            remarks = "নিষ্পত্তি প্রক্রিয়াধীন"
        )
        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createInspection(unresolvedReport, userId = aliceUid)
        }
        assertTrue(createRes.isSuccess)

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeInspections(aliceUid).first { it.isNotEmpty() }
        }
        val fetched = list.first()
        assertEquals("অনিষ্পন্ন রিপোর্ট", fetched.reportCategory)
        assertEquals(12L, fetched.previousMonthBalance)
        assertEquals(8L, fetched.currentMonthReceived)
        assertEquals(20L, fetched.totalReceived)
        assertEquals("২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়", fetched.unresolvedDurationCategory)
        assertEquals("নিষ্পত্তি প্রক্রিয়াধীন", fetched.remarks)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
