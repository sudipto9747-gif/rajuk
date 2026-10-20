package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppOption
import com.example.data.model.AssignedTask
import com.example.data.model.BOOTSTRAPPED_ADMIN_EMAIL
import com.example.data.model.InspectionFormData
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import com.example.data.model.PasswordHasher
import com.example.data.model.UserAccount
import com.example.data.repository.InspectionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

enum class AuthorizedFilter(val labelBangla: String) {
    ALL("সকল পরিদর্শন"),
    AUTHORIZED_YES("অথরাইজড অফিসার পরিদর্শনকৃত"),
    AUTHORIZED_NO("অথরাইজড অফিসার পরিদর্শন বাকি")
}

data class DashboardViewState(
    val selectedMonth: String,
    val allMonthReports: List<InspectionReport>,
    val filteredReports: List<InspectionReport>,
    val allTimeReports: List<InspectionReport> = emptyList(),
    val stats: MonthlySummaryStats,
    val profile: InspectorProfile?,
    val userAccount: UserAccount?,
    val isAdmin: Boolean,
    val allUserAccounts: List<UserAccount>,
    val searchQuery: String,
    val selectedReportCategory: String? = null,
    val selectedBuildingType: String?,
    val authorizedFilter: AuthorizedFilter,
    val customOptions: List<AppOption> = emptyList(),
    val allTasks: List<AssignedTask> = emptyList(),
    val myTasks: List<AssignedTask> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionViewModel(
    private val repository: InspectionRepository,
    initialUserId: String = "",
    private val currentUserEmail: String = ""
) : ViewModel() {

    private val currentMonthDefault: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private val _activeUserId = MutableStateFlow(initialUserId)
    val activeUserId: StateFlow<String> = _activeUserId.asStateFlow()

    private val _pendingPhoneUserId = MutableStateFlow("")

    private val _selectedMonth = MutableStateFlow(currentMonthDefault)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedReportCategory = MutableStateFlow<String?>(null)
    val selectedReportCategory: StateFlow<String?> = _selectedReportCategory.asStateFlow()

    private val _selectedBuildingType = MutableStateFlow<String?>(null)
    val selectedBuildingType: StateFlow<String?> = _selectedBuildingType.asStateFlow()

    private val _authorizedFilter = MutableStateFlow(AuthorizedFilter.ALL)
    val authorizedFilter: StateFlow<AuthorizedFilter> = _authorizedFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isPhoneLoggedIn = MutableStateFlow(false)
    val isPhoneLoggedIn: StateFlow<Boolean> = _isPhoneLoggedIn.asStateFlow()

    val isBootstrappedAdmin: Boolean =
        currentUserEmail.trim().equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)

    val userAccountState: StateFlow<UiState<UserAccount?>> =
        combine(_activeUserId, _pendingPhoneUserId) { activeId, pendingId ->
            activeId.ifBlank { pendingId }
        }.flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf<UiState<UserAccount?>>(UiState.Success(null))
            } else {
                repository.observeUserAccount(uid)
                    .map<UserAccount?, UiState<UserAccount?>> { UiState.Success(it) }
                    .catch { error ->
                        Log.w(TAG, "Error observing user account", error)
                        emit(UiState.Error(error.localizedMessage ?: "অ্যাকাউন্ট তথ্য লোড করতে সমস্যা হয়েছে"))
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = if (initialUserId.isBlank()) UiState.Success(null) else UiState.Loading
        )

    private val allUserAccountsStream: StateFlow<List<UserAccount>> =
        _isPhoneLoggedIn.flatMapLatest { loggedIn ->
            if (loggedIn) {
                repository.observeAllUserAccountsForAdmin()
                    .catch { error ->
                        Log.w(TAG, "Error observing all user accounts", error)
                        emit(emptyList())
                    }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val inspectionsStream: StateFlow<UiState<List<InspectionReport>>> =
        _activeUserId.flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeInspections(uid)
                    .map<List<InspectionReport>, UiState<List<InspectionReport>>> { UiState.Success(it) }
                    .catch { error ->
                        Log.w(TAG, "Error observing inspections", error)
                        emit(UiState.Error(error.localizedMessage ?: "তথ্য লোড করতে সমস্যা হয়েছে"))
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = if (initialUserId.isBlank()) UiState.Success(emptyList()) else UiState.Loading
        )

    private val profileStream: StateFlow<InspectorProfile?> =
        _activeUserId.flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(null)
            } else {
                repository.observeInspectorProfile(uid)
                    .catch { error ->
                        Log.w(TAG, "Error observing inspector profile", error)
                        emit(null)
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val appOptionsStream: StateFlow<List<AppOption>> = repository.observeAppOptions()
        .catch { error ->
            Log.w(TAG, "Error observing app options", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val allTasksStream: StateFlow<List<AssignedTask>> = repository.observeAllTasks()
        .catch { error ->
            Log.w(TAG, "Error observing all tasks", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private data class FilterBundle(
        val query: String,
        val reportCategory: String?,
        val buildingType: String?,
        val authorizedFilter: AuthorizedFilter
    )

    private val filterBundleStream = combine(
        _searchQuery,
        _selectedReportCategory,
        _selectedBuildingType,
        _authorizedFilter
    ) { q, cat, bType, aFilter ->
        FilterBundle(q, cat, bType, aFilter)
    }

    private val userAndAccountsStream = combine(
        userAccountState,
        allUserAccountsStream,
        _selectedMonth
    ) { accState, allAccs, month ->
        Triple(accState, allAccs, month)
    }

    private val optionsAndTasksStream = combine(
        appOptionsStream,
        allTasksStream
    ) { options, tasks ->
        options to tasks
    }

    val uiState: StateFlow<UiState<DashboardViewState>> = combine(
        inspectionsStream,
        profileStream,
        userAndAccountsStream,
        filterBundleStream,
        optionsAndTasksStream
    ) { inspectionsResult, profile, (accState, allAccounts, month), filterBundle, (options, tasks) ->
        val query = filterBundle.query
        val rCategory = filterBundle.reportCategory
        val bType = filterBundle.buildingType
        val aFilter = filterBundle.authorizedFilter
        when (inspectionsResult) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> UiState.Error(inspectionsResult.message)
            is UiState.Success -> {
                val userAccount = (accState as? UiState.Success)?.data
                val isAdmin = isBootstrappedAdmin || userAccount?.role == "admin"
                val monthReports = inspectionsResult.data.filter { it.reportMonth == month }
                val stats = InspectionPresets.computeStats(monthReports)
                val q = query.trim().lowercase(Locale.getDefault())
                val filtered = monthReports.filter { item ->
                    val matchesQuery = q.isEmpty() ||
                        item.reportCategory.lowercase().contains(q) ||
                        item.ownerName.lowercase().contains(q) ||
                        item.buildingAddress.lowercase().contains(q) ||
                        item.mouzaName.lowercase().contains(q) ||
                        item.thanaName.lowercase().contains(q) ||
                        item.ownerMobile.lowercase().contains(q) ||
                        item.inspectorName.lowercase().contains(q) ||
                        item.deviationType.lowercase().contains(q) ||
                        item.actionTaken.lowercase().contains(q) ||
                        item.unresolvedDurationCategory.lowercase().contains(q) ||
                        item.remarks.lowercase().contains(q)

                    val matchesCategory = rCategory == null || item.reportCategory == rCategory
                    val matchesType = bType == null || item.buildingType == bType
                    val matchesAuth = when (aFilter) {
                        AuthorizedFilter.ALL -> true
                        AuthorizedFilter.AUTHORIZED_YES -> item.authorizedOfficerInspected
                        AuthorizedFilter.AUTHORIZED_NO -> !item.authorizedOfficerInspected
                    }
                    matchesQuery && matchesCategory && matchesType && matchesAuth
                }

                val currentUid = userAccount?.userId.orEmpty()
                val currentPhone = userAccount?.phoneNumber.orEmpty()
                val myTasks = tasks.filter {
                    (currentUid.isNotBlank() && it.assignedToUserId == currentUid) ||
                    (currentPhone.isNotBlank() && it.assignedToPhone == currentPhone)
                }

                UiState.Success(
                    DashboardViewState(
                        selectedMonth = month,
                        allMonthReports = monthReports,
                        filteredReports = filtered,
                        allTimeReports = inspectionsResult.data,
                        stats = stats,
                        profile = profile,
                        userAccount = userAccount,
                        isAdmin = isAdmin,
                        allUserAccounts = allAccounts,
                        searchQuery = query,
                        selectedReportCategory = rCategory,
                        selectedBuildingType = bType,
                        authorizedFilter = aFilter,
                        customOptions = options,
                        allTasks = tasks,
                        myTasks = myTasks
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    fun registerWithPhoneAndPassword(
        phoneNumber: String,
        rawPassword: String,
        inspectorName: String = "",
        designation: String = "ইমারত পরিদর্শক",
        officeName: String = "",
        existingAccount: UserAccount? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val cleanPhone = phoneNumber.trim()
            val targetUid = PasswordHasher.phoneToUserId(cleanPhone)
            val isAdminCreds = PasswordHasher.isAdminCredentials(cleanPhone, rawPassword)
            val res = repository.registerOrResubmitUserAccount(
                userId = targetUid,
                email = currentUserEmail,
                inspectorName = inspectorName,
                designation = designation,
                officeName = officeName,
                phoneNumber = cleanPhone,
                rawPassword = rawPassword,
                isBootstrappedAdmin = isBootstrappedAdmin || isAdminCreds,
                existingAccount = existingAccount?.takeIf { it.userId == targetUid }
            )
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    if (isBootstrappedAdmin || isAdminCreds) {
                        _activeUserId.value = targetUid
                        _pendingPhoneUserId.value = targetUid
                        _isPhoneLoggedIn.value = true
                        _statusMessage.value = "অ্যাডমিন রেজিস্ট্রেশন ও লগইন সম্পন্ন হয়েছে"
                    } else {
                        _pendingPhoneUserId.value = targetUid
                        _statusMessage.value = "রেজিস্ট্রেশন আবেদন জমা হয়েছে। অ্যাডমিন অনুমোদনের অপেক্ষায়।"
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    _statusMessage.value = "রেজিস্ট্রেশন ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun loginWithPhoneAndPassword(
        phoneNumber: String,
        rawPassword: String,
        account: UserAccount?,
        onError: (String) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim()
        val targetUid = PasswordHasher.phoneToUserId(cleanPhone)
        val inputHash = PasswordHasher.sha256Hex(rawPassword)

        // Check designated Admin credentials first: phone 01767563095, password 889747
        if (PasswordHasher.isAdminCredentials(cleanPhone, rawPassword)) {
            viewModelScope.launch {
                _isSaving.value = true
                val res = repository.activateAdminAccount(
                    userId = targetUid,
                    email = currentUserEmail,
                    existingAccount = account?.takeIf { it.userId == targetUid }
                )
                _isSaving.value = false
                res.fold(
                    onSuccess = {
                        _activeUserId.value = targetUid
                        _pendingPhoneUserId.value = targetUid
                        _isPhoneLoggedIn.value = true
                        _statusMessage.value = "অ্যাডমিন হিসেবে সফলভাবে লগইন সম্পন্ন হয়েছে"
                    },
                    onFailure = { err ->
                        onError("অ্যাডমিন লগইন ত্রুটি: ${err.localizedMessage ?: "আবার চেষ্টা করুন"}")
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val resolvedAccount = if (account != null && account.phoneNumber.trim() == cleanPhone) {
                account
            } else {
                repository.getUserAccount(targetUid).getOrNull()
            }
            _isSaving.value = false

            when {
                resolvedAccount == null -> {
                    onError("এই ফোন নাম্বারে কোনো নিবন্ধিত অ্যাকাউন্ট পাওয়া যায়নি। প্রথমে 'রেজিস্ট্রেশন' ট্যাব থেকে রেজিস্ট্রেশন করুন।")
                }
                cleanPhone != resolvedAccount.phoneNumber.trim() -> {
                    onError("ফোন নাম্বারটি আপনার নিবন্ধিত ফোন নাম্বারের (${resolvedAccount.phoneNumber}) সাথে মেলেনি")
                }
                inputHash != resolvedAccount.passwordHash -> {
                    onError("পাসওয়ার্ড সঠিক নয়, অনুগ্রহ করে আবার চেষ্টা করুন")
                }
                resolvedAccount.approvalStatus == "pending" -> {
                    _pendingPhoneUserId.value = targetUid
                    onError("আপনার রেজিস্ট্রেশন এখনো অ্যাডমিন অনুমোদনের অপেক্ষায় রয়েছে। অ্যাডমিন অনুমোদন দিলে লগইন করতে পারবেন।")
                }
                resolvedAccount.approvalStatus == "rejected" -> {
                    _pendingPhoneUserId.value = targetUid
                    onError("অ্যাডমিন আপনার রেজিস্ট্রেশন আবেদনটি বাতিল করেছেন। পুনরায় রেজিস্ট্রেশন করুন।")
                }
                else -> {
                    _activeUserId.value = targetUid
                    _pendingPhoneUserId.value = targetUid
                    _isPhoneLoggedIn.value = true
                    _statusMessage.value = "সফলভাবে লগইন সম্পন্ন হয়েছে"
                }
            }
        }
    }

    fun saveUserNameAfterLogin(
        inspectorName: String,
        designation: String,
        officeName: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val currentAcc = (userAccountState.value as? UiState.Success)?.data
            val currentProf = profileStream.value
            val uid = _activeUserId.value.ifBlank { currentAcc?.userId.orEmpty() }
            val res = repository.saveUserNameAfterLogin(
                userId = uid,
                inspectorName = inspectorName,
                designation = designation,
                officeName = officeName,
                existingAccount = currentAcc,
                existingProfile = currentProf
            )
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "আপনার নাম সফলভাবে সেট করা হয়েছে"
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "নাম সংরক্ষণ করতে ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun lockPhoneSession() {
        _isPhoneLoggedIn.value = false
        _activeUserId.value = ""
    }

    fun approveUserAccount(targetAccount: UserAccount) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.updateAccountApprovalByAdmin(
                targetAccount = targetAccount,
                newApprovalStatus = "approved"
            )
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    val label = targetAccount.inspectorName.ifBlank { targetAccount.phoneNumber }
                    _statusMessage.value = "$label-এর রেজিস্ট্রেশন অনুমোদন করা হয়েছে"
                },
                onFailure = { err ->
                    _statusMessage.value = "অনুমোদন করতে ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun rejectUserAccount(targetAccount: UserAccount) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.updateAccountApprovalByAdmin(
                targetAccount = targetAccount,
                newApprovalStatus = "rejected"
            )
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    val label = targetAccount.inspectorName.ifBlank { targetAccount.phoneNumber }
                    _statusMessage.value = "$label-এর রেজিস্ট্রেশন বাতিল করা হয়েছে"
                },
                onFailure = { err ->
                    _statusMessage.value = "ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    // Role Management (Admin can grant admin role to any user)
    fun updateUserRole(targetAccount: UserAccount, newRole: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.updateUserRole(targetAccount, newRole)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    val label = if (newRole == "admin") "অ্যাডমিন" else "পরিদর্শক"
                    val name = targetAccount.inspectorName.ifBlank { targetAccount.phoneNumber }
                    _statusMessage.value = "$name কে সফলভাবে $label রোল দেওয়া হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to update user role", err)
                    _statusMessage.value = "রোল পরিবর্তন ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    // App Options (Admin can add, edit, delete any option)
    fun addAppOption(group: String, name: String, subGroup: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            _isSaving.value = true
            val opt = AppOption(
                group = group,
                name = name.trim(),
                subGroup = subGroup.trim()
            )
            val res = repository.addAppOption(opt)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "নতুন অপশন যোগ করা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to add option", err)
                    _statusMessage.value = "অপশন যোগ করতে ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateAppOption(option: AppOption, newName: String, newSubGroup: String = option.subGroup) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            _isSaving.value = true
            val updated = option.copy(name = newName.trim(), subGroup = newSubGroup.trim())
            val res = repository.updateAppOption(updated)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "অপশনটি সফলভাবে সম্পাদনা করা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to update option", err)
                    _statusMessage.value = "অপশন আপডেট ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun deleteAppOption(optionId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.deleteAppOption(optionId)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "অপশনটি মুছে ফেলা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to delete option", err)
                    _statusMessage.value = "অপশন মুছে ফেলতে ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    // Task Manager (Admin assigns tasks, monitors tasks)
    fun createAssignedTask(
        title: String,
        description: String,
        assignedTo: UserAccount,
        dueDate: String,
        priority: String,
        buildingAddress: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _isSaving.value = true
            val currentAdmin = (userAccountState.value as? UiState.Success)?.data
            val task = AssignedTask(
                title = title.trim(),
                description = description.trim(),
                assignedToUserId = assignedTo.userId,
                assignedToName = assignedTo.inspectorName.ifBlank { assignedTo.phoneNumber },
                assignedToDesignation = assignedTo.designation.ifBlank { "ইমারত পরিদর্শক" },
                assignedToPhone = assignedTo.phoneNumber,
                createdByUserId = currentAdmin?.userId.orEmpty(),
                createdByName = currentAdmin?.inspectorName?.ifBlank { "অ্যাডমিন" } ?: "অ্যাডমিন",
                dueDate = dueDate.trim(),
                priority = priority.ifBlank { "Medium" },
                status = "pending",
                buildingAddress = buildingAddress.trim()
            )
            val res = repository.createAssignedTask(task)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "টাস্ক সফলভাবে এসাইন করা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to create task", err)
                    _statusMessage.value = "টাস্ক প্রদান ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateAssignedTask(task: AssignedTask) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.updateAssignedTask(task)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "টাস্ক সফলভাবে আপডেট করা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to update task", err)
                    _statusMessage.value = "টাস্ক আপডেট ত্রুটি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String, completionNote: String = "") {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.updateTaskStatus(taskId, newStatus, completionNote)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    val statusLabel = when (newStatus) {
                        "in_progress" -> "চলমান"
                        "completed" -> "সম্পন্ন"
                        else -> "অপেক্ষমাণ"
                    }
                    _statusMessage.value = "টাস্ক স্ট্যাটাস $statusLabel করা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to update task status", err)
                    _statusMessage.value = "স্ট্যাটাস পরিবর্তন ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun deleteAssignedTask(taskId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.deleteAssignedTask(taskId)
            _isSaving.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "টাস্ক মুছে ফেলা হয়েছে"
                },
                onFailure = { err ->
                    Log.e(TAG, "Failed to delete task", err)
                    _statusMessage.value = "টাস্ক ডিলিট ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun selectMonth(reportMonth: String) {
        _selectedMonth.value = reportMonth
    }

    fun shiftMonth(deltaMonths: Int) {
        val current = _selectedMonth.value
        val parts = current.split("-")
        if (parts.size != 2) return
        val year = parts[0].toIntOrNull() ?: return
        val month = parts[1].toIntOrNull() ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        val formatted = String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
        _selectedMonth.value = formatted
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateReportCategoryFilter(category: String?) {
        _selectedReportCategory.value = if (_selectedReportCategory.value == category) null else category
    }

    fun updateBuildingTypeFilter(buildingType: String?) {
        _selectedBuildingType.value = if (_selectedBuildingType.value == buildingType) null else buildingType
    }

    fun updateAuthorizedFilter(filter: AuthorizedFilter) {
        _authorizedFilter.value = filter
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun saveInspectionReport(
        existingReport: InspectionReport?,
        formData: InspectionFormData,
        onComplete: () -> Unit
    ) {
        saveInspectionReport(
            existingReport = existingReport,
            reportCategory = formData.reportCategory,
            reportMonth = formData.reportMonth,
            inspectionDate = formData.inspectionDate,
            inspectorName = formData.inspectorName,
            ownerName = formData.ownerName,
            buildingAddress = formData.buildingAddress,
            mouzaName = formData.mouzaName,
            thanaName = formData.thanaName,
            ownerMobile = formData.ownerMobile,
            buildingType = formData.buildingType,
            previousInspectionCount = formData.previousInspectionCount,
            authorizedOfficerInspected = formData.authorizedOfficerInspected,
            deviationType = formData.deviationType,
            actionTaken = formData.actionTaken,
            previousMonthBalance = formData.previousMonthBalance,
            currentMonthReceived = formData.currentMonthReceived,
            totalReceived = formData.totalReceived,
            unresolvedAtMonthEnd = formData.unresolvedAtMonthEnd,
            unresolvedDurationCategory = formData.unresolvedDurationCategory,
            planSubmitted = formData.planSubmitted,
            planApproved = formData.planApproved,
            planRejected = formData.planRejected,
            planCompleted = formData.planCompleted,
            planWorkInProgress = formData.planWorkInProgress,
            planSentForFeedback = formData.planSentForFeedback,
            planCompletedWithin45Days = formData.planCompletedWithin45Days,
            planCompletedMoreThan45Days = formData.planCompletedMoreThan45Days,
            occupancyPreviousMonthBalance = formData.occupancyPreviousMonthBalance,
            occupancyCurrentMonthReceived = formData.occupancyCurrentMonthReceived,
            occupancyTotalApplications = formData.occupancyTotalApplications,
            occupancyInspectedBuildings = formData.occupancyInspectedBuildings,
            occupancyIssuedCount = formData.occupancyIssuedCount,
            occupancyNotIssuedCount = formData.occupancyNotIssuedCount,
            occupancyNotIssuedReason = formData.occupancyNotIssuedReason,
            mobileCourtDate = formData.mobileCourtDate,
            magistrateName = formData.magistrateName,
            mobileCourtFineAmount = formData.mobileCourtFineAmount,
            mobileCourtMeterCut = formData.mobileCourtMeterCut,
            mobileCourtMeterCount = formData.mobileCourtMeterCount,
            mobileCourtUndertakingSubmitted = formData.mobileCourtUndertakingSubmitted,
            remarks = formData.remarks,
            onComplete = onComplete
        )
    }

    fun saveInspectionReport(
        existingReport: InspectionReport?,
        reportCategory: String = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
        reportMonth: String,
        inspectionDate: String,
        inspectorName: String,
        ownerName: String,
        buildingAddress: String,
        mouzaName: String = "",
        thanaName: String = "",
        ownerMobile: String,
        buildingType: String,
        previousInspectionCount: Long,
        authorizedOfficerInspected: Boolean,
        deviationType: String,
        actionTaken: String,
        previousMonthBalance: Long = 0L,
        currentMonthReceived: Long = 0L,
        totalReceived: Long = 0L,
        unresolvedAtMonthEnd: Long = 0L,
        unresolvedDurationCategory: String = "",
        planSubmitted: Long = 0L,
        planApproved: Long = 0L,
        planRejected: Long = 0L,
        planCompleted: Long = 0L,
        planWorkInProgress: Long = 0L,
        planSentForFeedback: Long = 0L,
        planCompletedWithin45Days: Long = 0L,
        planCompletedMoreThan45Days: Long = 0L,
        occupancyPreviousMonthBalance: Long = 0L,
        occupancyCurrentMonthReceived: Long = 0L,
        occupancyTotalApplications: Long = 0L,
        occupancyInspectedBuildings: Long = 0L,
        occupancyIssuedCount: Long = 0L,
        occupancyNotIssuedCount: Long = 0L,
        occupancyNotIssuedReason: String = "",
        mobileCourtDate: String = "",
        magistrateName: String = "",
        mobileCourtFineAmount: Long = 0L,
        mobileCourtMeterCut: Boolean = false,
        mobileCourtMeterCount: Long = 0L,
        mobileCourtUndertakingSubmitted: Boolean = false,
        remarks: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val uid = _activeUserId.value
            val resolvedCategory = reportCategory.trim().ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" }
            val result = if (existingReport != null && existingReport.id.isNotBlank()) {
                repository.updateInspection(
                    existingReport.copy(
                        userId = uid.ifBlank { existingReport.userId },
                        reportCategory = resolvedCategory,
                        reportMonth = reportMonth.trim(),
                        inspectionDate = inspectionDate.trim(),
                        inspectorName = inspectorName.trim(),
                        ownerName = ownerName.trim(),
                        buildingAddress = buildingAddress.trim(),
                        mouzaName = mouzaName.trim().take(150),
                        thanaName = thanaName.trim().take(150),
                        ownerMobile = ownerMobile.trim(),
                        buildingType = buildingType.trim(),
                        previousInspectionCount = previousInspectionCount.coerceIn(0L, 1000L),
                        authorizedOfficerInspected = authorizedOfficerInspected,
                        deviationType = deviationType.trim(),
                        actionTaken = actionTaken.trim(),
                        previousMonthBalance = previousMonthBalance.coerceIn(0L, 1_000_000L),
                        currentMonthReceived = currentMonthReceived.coerceIn(0L, 1_000_000L),
                        totalReceived = totalReceived.coerceIn(0L, 1_000_000L),
                        unresolvedAtMonthEnd = unresolvedAtMonthEnd.coerceIn(0L, 1_000_000L),
                        unresolvedDurationCategory = unresolvedDurationCategory.trim(),
                        planSubmitted = planSubmitted.coerceIn(0L, 1_000_000L),
                        planApproved = planApproved.coerceIn(0L, 1_000_000L),
                        planRejected = planRejected.coerceIn(0L, 1_000_000L),
                        planCompleted = planCompleted.coerceIn(0L, 1_000_000L),
                        planWorkInProgress = planWorkInProgress.coerceIn(0L, 1_000_000L),
                        planSentForFeedback = planSentForFeedback.coerceIn(0L, 1_000_000L),
                        planCompletedWithin45Days = planCompletedWithin45Days.coerceIn(0L, 1_000_000L),
                        planCompletedMoreThan45Days = planCompletedMoreThan45Days.coerceIn(0L, 1_000_000L),
                        occupancyPreviousMonthBalance = occupancyPreviousMonthBalance.coerceIn(0L, 1_000_000L),
                        occupancyCurrentMonthReceived = occupancyCurrentMonthReceived.coerceIn(0L, 1_000_000L),
                        occupancyTotalApplications = occupancyTotalApplications.coerceIn(0L, 1_000_000L),
                        occupancyInspectedBuildings = occupancyInspectedBuildings.coerceIn(0L, 1_000_000L),
                        occupancyIssuedCount = occupancyIssuedCount.coerceIn(0L, 1_000_000L),
                        occupancyNotIssuedCount = occupancyNotIssuedCount.coerceIn(0L, 1_000_000L),
                        occupancyNotIssuedReason = occupancyNotIssuedReason.trim().take(300),
                        mobileCourtDate = mobileCourtDate.trim(),
                        magistrateName = magistrateName.trim().take(150),
                        mobileCourtFineAmount = mobileCourtFineAmount.coerceIn(0L, 100_000_000L),
                        mobileCourtMeterCut = mobileCourtMeterCut,
                        mobileCourtMeterCount = if (mobileCourtMeterCut) mobileCourtMeterCount.coerceIn(0L, 100_000L) else 0L,
                        mobileCourtUndertakingSubmitted = mobileCourtUndertakingSubmitted,
                        remarks = remarks.trim()
                    ),
                    userId = uid
                ).map { existingReport.id }
            } else {
                repository.createInspection(
                    InspectionReport(
                        userId = uid,
                        reportCategory = resolvedCategory,
                        reportMonth = reportMonth.trim(),
                        inspectionDate = inspectionDate.trim(),
                        inspectorName = inspectorName.trim(),
                        ownerName = ownerName.trim(),
                        buildingAddress = buildingAddress.trim(),
                        mouzaName = mouzaName.trim().take(150),
                        thanaName = thanaName.trim().take(150),
                        ownerMobile = ownerMobile.trim(),
                        buildingType = buildingType.trim(),
                        previousInspectionCount = previousInspectionCount.coerceIn(0L, 1000L),
                        authorizedOfficerInspected = authorizedOfficerInspected,
                        deviationType = deviationType.trim(),
                        actionTaken = actionTaken.trim(),
                        previousMonthBalance = previousMonthBalance.coerceIn(0L, 1_000_000L),
                        currentMonthReceived = currentMonthReceived.coerceIn(0L, 1_000_000L),
                        totalReceived = totalReceived.coerceIn(0L, 1_000_000L),
                        unresolvedAtMonthEnd = unresolvedAtMonthEnd.coerceIn(0L, 1_000_000L),
                        unresolvedDurationCategory = unresolvedDurationCategory.trim(),
                        planSubmitted = planSubmitted.coerceIn(0L, 1_000_000L),
                        planApproved = planApproved.coerceIn(0L, 1_000_000L),
                        planRejected = planRejected.coerceIn(0L, 1_000_000L),
                        planCompleted = planCompleted.coerceIn(0L, 1_000_000L),
                        planWorkInProgress = planWorkInProgress.coerceIn(0L, 1_000_000L),
                        planSentForFeedback = planSentForFeedback.coerceIn(0L, 1_000_000L),
                        planCompletedWithin45Days = planCompletedWithin45Days.coerceIn(0L, 1_000_000L),
                        planCompletedMoreThan45Days = planCompletedMoreThan45Days.coerceIn(0L, 1_000_000L),
                        occupancyPreviousMonthBalance = occupancyPreviousMonthBalance.coerceIn(0L, 1_000_000L),
                        occupancyCurrentMonthReceived = occupancyCurrentMonthReceived.coerceIn(0L, 1_000_000L),
                        occupancyTotalApplications = occupancyTotalApplications.coerceIn(0L, 1_000_000L),
                        occupancyInspectedBuildings = occupancyInspectedBuildings.coerceIn(0L, 1_000_000L),
                        occupancyIssuedCount = occupancyIssuedCount.coerceIn(0L, 1_000_000L),
                        occupancyNotIssuedCount = occupancyNotIssuedCount.coerceIn(0L, 1_000_000L),
                        occupancyNotIssuedReason = occupancyNotIssuedReason.trim().take(300),
                        mobileCourtDate = mobileCourtDate.trim(),
                        magistrateName = magistrateName.trim().take(150),
                        mobileCourtFineAmount = mobileCourtFineAmount.coerceIn(0L, 100_000_000L),
                        mobileCourtMeterCut = mobileCourtMeterCut,
                        mobileCourtMeterCount = if (mobileCourtMeterCut) mobileCourtMeterCount.coerceIn(0L, 100_000L) else 0L,
                        mobileCourtUndertakingSubmitted = mobileCourtUndertakingSubmitted,
                        remarks = remarks.trim()
                    ),
                    userId = uid
                )
            }

            val currentProfile = profileStream.value
            if (currentProfile == null && inspectorName.isNotBlank() && uid.isNotBlank()) {
                repository.saveInspectorProfile(
                    userId = uid,
                    inspectorName = inspectorName.trim(),
                    designation = "ইমারত পরিদর্শক",
                    officeName = "",
                    existingProfile = null
                )
            }

            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _selectedMonth.value = reportMonth.trim()
                    _statusMessage.value = if (existingReport != null) {
                        "$resolvedCategory — তথ্য সফলভাবে হালনাগাদ হয়েছে"
                    } else {
                        "$resolvedCategory — নতুন তথ্য সংরক্ষিত হয়েছে"
                    }
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "ত্রুটি: ${err.localizedMessage ?: "সংরক্ষণ করা যায়নি"}"
                }
            )
        }
    }

    fun deleteInspection(inspectionId: String) {
        viewModelScope.launch {
            val uid = _activeUserId.value
            val res = repository.deleteInspection(inspectionId, userId = uid)
            res.fold(
                onSuccess = { _statusMessage.value = "পরিদর্শন রেকর্ডটি মুছে ফেলা হয়েছে" },
                onFailure = { err -> _statusMessage.value = "মুছে ফেলতে ত্রুটি: ${err.localizedMessage}" }
            )
        }
    }

    fun saveProfile(inspectorName: String, designation: String, officeName: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val uid = _activeUserId.value
            val res = repository.saveInspectorProfile(
                userId = uid,
                inspectorName = inspectorName,
                designation = designation,
                officeName = officeName,
                existingProfile = profileStream.value
            )
            _isSaving.value = false
            res.fold(
                onSuccess = { _statusMessage.value = "পরিদর্শকের প্রোফাইল সংরক্ষিত হয়েছে" },
                onFailure = { err -> _statusMessage.value = "ত্রুটি: ${err.localizedMessage}" }
            )
        }
    }

    private companion object {
        const val TAG = "InspectionVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
