package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ADMIN_LOGIN_PASSWORD_HASH
import com.example.data.model.ADMIN_LOGIN_PHONE
import com.example.data.model.AppOption
import com.example.data.model.AssignedTask
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.PasswordHasher
import com.example.data.model.UserAccount
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class InspectionRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor resolving R.string.firestore_database_id from applicationContext
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun resolveUserId(explicitUserId: String = "", fallbackPhone: String = ""): String {
        if (explicitUserId.isNotBlank()) return explicitUserId.trim()
        val authUid = auth.currentUser?.uid
        if (!authUid.isNullOrBlank()) return authUid
        if (fallbackPhone.isNotBlank()) return PasswordHasher.phoneToUserId(fallbackPhone)
        throw IllegalStateException("Active userId is required before accessing Firestore.")
    }

    fun observeUserAccount(userId: String): Flow<UserAccount?> = callbackFlow {
        val path = "user_accounts/$userId"
        val registration = db.collection("user_accounts")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(
                        snapshot.toObject(
                            UserAccount::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    )
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun getUserAccount(userId: String): Result<UserAccount?> {
        val docRef = db.collection("user_accounts").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                Result.success(
                    snapshot.toObject(
                        UserAccount::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                )
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAllUserAccountsForAdmin(): Flow<List<UserAccount>> = callbackFlow {
        val path = "user_accounts"
        val registration = db.collection("user_accounts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val accounts = snapshot.toObjects(
                        UserAccount::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareBy<UserAccount> {
                            when (it.approvalStatus) {
                                "pending" -> 0
                                "approved" -> 1
                                else -> 2
                            }
                        }.thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                    )
                    trySend(accounts)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun registerOrResubmitUserAccount(
        userId: String = "",
        email: String = "",
        inspectorName: String = "",
        designation: String = "ইমারত পরিদর্শক",
        officeName: String = "",
        phoneNumber: String,
        rawPassword: String,
        isBootstrappedAdmin: Boolean = false,
        existingAccount: UserAccount? = null
    ): Result<Unit> {
        val cleanedPhone = phoneNumber.trim()
        val uid = resolveUserId(explicitUserId = userId, fallbackPhone = cleanedPhone)
        val docRef = db.collection("user_accounts").document(uid)
        val hash = PasswordHasher.sha256Hex(rawPassword)
        val isAdminCreds = PasswordHasher.isAdminCredentials(cleanedPhone, rawPassword)
        val grantAdmin = isBootstrappedAdmin || isAdminCreds

        return try {
            val resolvedExisting = existingAccount?.takeIf { it.userId == uid }
                ?: docRef.get().await().takeIf { it.exists() }?.toObject(
                    UserAccount::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            if (resolvedExisting != null && resolvedExisting.createdAt != null) {
                val nextStatus = when {
                    grantAdmin -> "approved"
                    inspectorName.isBlank() -> "pending"
                    resolvedExisting.approvalStatus == "rejected" -> "pending"
                    else -> resolvedExisting.approvalStatus
                }
                val nextRole = if (grantAdmin) "admin" else resolvedExisting.role
                val resolvedName = inspectorName.trim()
                val updated = resolvedExisting.copy(
                    userId = uid,
                    email = email.trim(),
                    inspectorName = resolvedName,
                    designation = designation.trim().ifBlank { resolvedExisting.designation },
                    officeName = officeName.trim().ifBlank { resolvedExisting.officeName },
                    phoneNumber = cleanedPhone,
                    passwordHash = hash,
                    approvalStatus = nextStatus,
                    role = nextRole,
                    updatedAt = null
                )
                docRef.set(updated).await()
            } else {
                val initialStatus = if (grantAdmin) "approved" else "pending"
                val initialRole = if (grantAdmin) "admin" else "inspector"
                val created = UserAccount(
                    id = uid,
                    userId = uid,
                    email = email.trim(),
                    inspectorName = inspectorName.trim(),
                    designation = designation.trim(),
                    officeName = officeName.trim(),
                    phoneNumber = cleanedPhone,
                    passwordHash = hash,
                    approvalStatus = initialStatus,
                    role = initialRole,
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(created).await()
            }

            if (inspectorName.isNotBlank()) {
                saveInspectorProfile(
                    userId = uid,
                    inspectorName = inspectorName,
                    designation = designation,
                    officeName = officeName,
                    existingProfile = null
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun activateAdminAccount(
        userId: String = "",
        email: String = "",
        existingAccount: UserAccount? = null
    ): Result<Unit> {
        val uid = resolveUserId(explicitUserId = userId, fallbackPhone = ADMIN_LOGIN_PHONE)
        val docRef = db.collection("user_accounts").document(uid)

        return try {
            val resolvedExisting = existingAccount?.takeIf { it.userId == uid }
                ?: docRef.get().await().takeIf { it.exists() }?.toObject(
                    UserAccount::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            if (resolvedExisting != null && resolvedExisting.createdAt != null) {
                val updated = resolvedExisting.copy(
                    userId = uid,
                    email = email.trim().ifBlank { resolvedExisting.email },
                    phoneNumber = ADMIN_LOGIN_PHONE,
                    passwordHash = ADMIN_LOGIN_PASSWORD_HASH,
                    approvalStatus = "approved",
                    role = "admin",
                    updatedAt = null
                )
                docRef.set(updated).await()
            } else {
                val created = UserAccount(
                    id = uid,
                    userId = uid,
                    email = email.trim(),
                    inspectorName = "",
                    designation = "অ্যাডমিন / অথরাইজড অফিসার",
                    officeName = "",
                    phoneNumber = ADMIN_LOGIN_PHONE,
                    passwordHash = ADMIN_LOGIN_PASSWORD_HASH,
                    approvalStatus = "approved",
                    role = "admin",
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(created).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveUserNameAfterLogin(
        userId: String = "",
        inspectorName: String,
        designation: String,
        officeName: String,
        existingAccount: UserAccount?,
        existingProfile: InspectorProfile?
    ): Result<Unit> {
        val uid = resolveUserId(
            explicitUserId = userId.ifBlank { existingAccount?.userId.orEmpty() },
            fallbackPhone = existingAccount?.phoneNumber.orEmpty()
        )
        val cleanName = inspectorName.trim()
        val cleanDesignation = designation.trim().ifBlank { "ইমারত পরিদর্শক" }
        val cleanOffice = officeName.trim()

        return try {
            val accountRef = db.collection("user_accounts").document(uid)
            val resolvedExistingAcc = existingAccount?.takeIf { it.userId == uid }
                ?: accountRef.get().await().takeIf { it.exists() }?.toObject(
                    UserAccount::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            if (resolvedExistingAcc != null) {
                val updatedAccount = resolvedExistingAcc.copy(
                    id = uid,
                    userId = uid,
                    inspectorName = cleanName,
                    designation = cleanDesignation,
                    officeName = cleanOffice,
                    updatedAt = null
                )
                accountRef.set(updatedAccount).await()
            }
            saveInspectorProfile(
                userId = uid,
                inspectorName = cleanName,
                designation = cleanDesignation,
                officeName = cleanOffice,
                existingProfile = existingProfile
            ).getOrThrow()

            // Also propagate updated inspectorName to this user's existing reports so Admin sees the updated name
            if (cleanName.isNotBlank()) {
                runCatching {
                    val userInspections = db.collection("users")
                        .document(uid)
                        .collection("inspections")
                        .get()
                        .await()
                    for (doc in userInspections.documents) {
                        doc.reference.update(
                            mapOf(
                                "inspectorName" to cleanName,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                        runCatching {
                            db.collection("all_inspections")
                                .document(doc.id)
                                .update(
                                    mapOf(
                                        "inspectorName" to cleanName,
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )
                                ).await()
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "user_accounts/$uid")
            Result.failure(e)
        }
    }

    suspend fun updateAccountApprovalByAdmin(
        targetAccount: UserAccount,
        newApprovalStatus: String,
        newRole: String = targetAccount.role
    ): Result<Unit> {
        require(targetAccount.userId.isNotBlank()) { "Target userId must not be blank" }
        val docRef = db.collection("user_accounts").document(targetAccount.userId)
        return try {
            val updates = mapOf(
                "approvalStatus" to newApprovalStatus,
                "role" to newRole,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeInspections(userId: String): Flow<List<InspectionReport>> = callbackFlow {
        val path = "users/$userId/inspections"
        val registration = db.collection("users")
            .document(userId)
            .collection("inspections")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(
                        InspectionReport::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).map { report ->
                        if (report.userId.isBlank()) report.copy(userId = userId) else report
                    }.sortedWith(
                        compareByDescending<InspectionReport> { it.inspectionDate }
                            .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                    )
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    private fun observeCentralInspections(): Flow<List<InspectionReport>> = callbackFlow {
        val registration = db.collection("all_inspections")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(
                        InspectionReport::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    private fun observeCollectionGroupInspections(): Flow<List<InspectionReport>> = callbackFlow {
        val registration = db.collectionGroup("inspections")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val parsed = doc.toObject(
                            InspectionReport::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                        val parentUid = doc.reference.parent.parent?.id.orEmpty()
                        if (parsed != null && parsed.userId.isBlank() && parentUid.isNotBlank()) {
                            parsed.copy(userId = parentUid)
                        } else {
                            parsed
                        }
                    }
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    fun observeAllInspectionsForAdmin(knownUserIds: List<String>): Flow<List<InspectionReport>> {
        val cleanUids = (knownUserIds + PasswordHasher.phoneToUserId(ADMIN_LOGIN_PHONE))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val perUserFlows: List<Flow<List<InspectionReport>>> = cleanUids.map { uid ->
            observeInspections(uid)
        }
        val allFlows: List<Flow<List<InspectionReport>>> =
            listOf(observeCentralInspections(), observeCollectionGroupInspections()) + perUserFlows

        return combine(allFlows) { arraysOfLists ->
            val mergedById = linkedMapOf<String, InspectionReport>()
            for (list in arraysOfLists) {
                for (report in list) {
                    val key = report.id.ifBlank { "${report.userId}_${report.inspectionDate}_${report.ownerName}" }
                    val existing = mergedById[key]
                    if (existing == null) {
                        mergedById[key] = report
                    } else {
                        val existingTime = (existing.updatedAt ?: existing.createdAt)?.toDate()?.time ?: 0L
                        val incomingTime = (report.updatedAt ?: report.createdAt)?.toDate()?.time ?: 0L
                        val adminUid = PasswordHasher.phoneToUserId(ADMIN_LOGIN_PHONE)
                        // Prefer the newer update, or if timestamps are equal, prefer the original non-admin userId owner
                        if (incomingTime > existingTime ||
                            (incomingTime == existingTime && existing.userId == adminUid && report.userId.isNotBlank() && report.userId != adminUid)
                        ) {
                            mergedById[key] = report
                        }
                    }
                }
            }
            mergedById.values.sortedWith(
                compareByDescending<InspectionReport> { it.inspectionDate }
                    .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            )
        }
    }

    fun observeInspectorProfile(userId: String): Flow<InspectorProfile?> = callbackFlow {
        val path = "users/$userId"
        val registration = db.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(
                        snapshot.toObject(
                            InspectorProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    )
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun createInspection(report: InspectionReport, userId: String = ""): Result<String> {
        val uid = resolveUserId(explicitUserId = userId.ifBlank { report.userId })
        val docId = report.id.ifBlank { "insp_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("users")
            .document(uid)
            .collection("inspections")
            .document(docId)

        val payload = report.copy(
            id = docId,
            userId = uid,
            previousInspectionCount = report.previousInspectionCount.coerceIn(0L, 1000L),
            previousMonthBalance = report.previousMonthBalance.coerceIn(0L, 1_000_000L),
            currentMonthReceived = report.currentMonthReceived.coerceIn(0L, 1_000_000L),
            totalReceived = report.totalReceived.coerceIn(0L, 1_000_000L),
            unresolvedAtMonthEnd = report.unresolvedAtMonthEnd.coerceIn(0L, 1_000_000L),
            createdAt = null,
            updatedAt = null
        )

        return try {
            docRef.set(payload).await()
            runCatching {
                db.collection("all_inspections").document(docId).set(payload).await()
            }
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateInspection(report: InspectionReport, userId: String = ""): Result<Unit> {
        val ownerUid = resolveUserId(explicitUserId = report.userId.ifBlank { userId })
        if (report.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Inspection ID must not be blank for update"))
        }

        val docRef = db.collection("users")
            .document(ownerUid)
            .collection("inspections")
            .document(report.id)

        val updatedPayload = report.copy(
            userId = ownerUid,
            previousInspectionCount = report.previousInspectionCount.coerceIn(0L, 1000L),
            previousMonthBalance = report.previousMonthBalance.coerceIn(0L, 1_000_000L),
            currentMonthReceived = report.currentMonthReceived.coerceIn(0L, 1_000_000L),
            totalReceived = report.totalReceived.coerceIn(0L, 1_000_000L),
            unresolvedAtMonthEnd = report.unresolvedAtMonthEnd.coerceIn(0L, 1_000_000L),
            updatedAt = null
        )

        return try {
            docRef.set(updatedPayload).await()
            runCatching {
                db.collection("all_inspections").document(report.id).set(updatedPayload).await()
            }
            // If an admin or another session previously saved a duplicate under a different userId, clean it up
            if (userId.isNotBlank() && userId != ownerUid) {
                runCatching {
                    db.collection("users")
                        .document(userId)
                        .collection("inspections")
                        .document(report.id)
                        .delete()
                        .await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteInspection(inspectionId: String, userId: String = ""): Result<Unit> {
        val uid = resolveUserId(explicitUserId = userId)
        if (inspectionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Inspection ID must not be blank"))
        }
        val docRef = db.collection("users")
            .document(uid)
            .collection("inspections")
            .document(inspectionId)

        return try {
            docRef.delete().await()
            runCatching {
                db.collection("all_inspections").document(inspectionId).delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getInspectionById(targetUserId: String, inspectionId: String): Result<InspectionReport?> {
        val docRef = db.collection("users")
            .document(targetUserId)
            .collection("inspections")
            .document(inspectionId)
        return try {
            val snapshot = docRef.get().await()
            val item = snapshot.toObject(
                InspectionReport::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveInspectorProfile(
        userId: String = "",
        inspectorName: String,
        designation: String,
        officeName: String,
        existingProfile: InspectorProfile?
    ): Result<Unit> {
        val uid = resolveUserId(explicitUserId = userId.ifBlank { existingProfile?.userId.orEmpty() })
        val docRef = db.collection("users").document(uid)
        return try {
            val resolvedExisting = existingProfile?.takeIf { it.userId == uid && it.createdAt != null }
                ?: docRef.get().await().takeIf { it.exists() }?.toObject(
                    InspectorProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            if (resolvedExisting != null && resolvedExisting.createdAt != null) {
                val updated = resolvedExisting.copy(
                    userId = uid,
                    inspectorName = inspectorName.trim(),
                    designation = designation.trim(),
                    officeName = officeName.trim(),
                    updatedAt = null
                )
                docRef.set(updated).await()
            } else {
                val created = InspectorProfile(
                    id = uid,
                    userId = uid,
                    inspectorName = inspectorName.trim(),
                    designation = designation.trim(),
                    officeName = officeName.trim(),
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(created).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(targetAccount: UserAccount, newRole: String): Result<Unit> {
        return updateAccountApprovalByAdmin(
            targetAccount = targetAccount,
            newApprovalStatus = targetAccount.approvalStatus,
            newRole = newRole
        )
    }

    // App Options (Admin can add, edit, delete any option)
    fun observeAppOptions(): Flow<List<AppOption>> = callbackFlow {
        val path = "system_options"
        val registration = db.collection("system_options")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(
                        AppOption::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareBy<AppOption> { it.group }
                            .thenBy { it.sortOrder }
                            .thenBy { it.name }
                    )
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun addAppOption(option: AppOption): Result<String> {
        val docId = option.id.ifBlank { "opt_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("system_options").document(docId)
        return try {
            val payload = option.copy(
                id = docId,
                name = option.name.trim(),
                group = option.group.trim(),
                subGroup = option.subGroup.trim(),
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateAppOption(option: AppOption): Result<Unit> {
        require(option.id.isNotBlank()) { "Option ID must not be blank" }
        val docRef = db.collection("system_options").document(option.id)
        return try {
            val payload = option.copy(
                name = option.name.trim(),
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAppOption(optionId: String): Result<Unit> {
        require(optionId.isNotBlank()) { "Option ID must not be blank" }
        val docRef = db.collection("system_options").document(optionId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Task Manager (Admin assigns tasks, monitors tasks)
    fun observeAllTasks(): Flow<List<AssignedTask>> = callbackFlow {
        val path = "tasks"
        val registration = db.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(
                        AssignedTask::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<AssignedTask> { it.createdAt ?: Timestamp(0, 0) }
                    )
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    fun observeTasksForUser(userId: String): Flow<List<AssignedTask>> = callbackFlow {
        val path = "tasks"
        val registration = db.collection("tasks")
            .whereEqualTo("assignedToUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(
                        AssignedTask::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<AssignedTask> { it.createdAt ?: Timestamp(0, 0) }
                    )
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun createAssignedTask(task: AssignedTask): Result<String> {
        val docId = task.id.ifBlank { "task_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("tasks").document(docId)
        return try {
            val payload = task.copy(
                id = docId,
                title = task.title.trim(),
                description = task.description.trim(),
                buildingAddress = task.buildingAddress.trim(),
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateAssignedTask(task: AssignedTask): Result<Unit> {
        require(task.id.isNotBlank()) { "Task ID must not be blank" }
        val docRef = db.collection("tasks").document(task.id)
        return try {
            val payload = task.copy(
                title = task.title.trim(),
                description = task.description.trim(),
                buildingAddress = task.buildingAddress.trim(),
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(taskId: String, newStatus: String, completionNote: String = ""): Result<Unit> {
        require(taskId.isNotBlank()) { "Task ID must not be blank" }
        val docRef = db.collection("tasks").document(taskId)
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (completionNote.isNotBlank()) {
                updates["completionNote"] = completionNote.trim()
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAssignedTask(taskId: String): Result<Unit> {
        require(taskId.isNotBlank()) { "Task ID must not be blank" }
        val docRef = db.collection("tasks").document(taskId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
