package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.signOutUser
import com.example.data.model.InspectionFormData
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import com.example.ui.viewmodel.AuthorizedFilter
import com.example.ui.viewmodel.DashboardViewState
import com.example.ui.viewmodel.InspectionViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.ReportExportUtil
import com.example.util.WebVersionExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: InspectionViewModel,
    userEmail: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userAccountState by viewModel.userAccountState.collectAsStateWithLifecycle()
    val isPhoneLoggedIn by viewModel.isPhoneLoggedIn.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedEnterDataCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedViewDataCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var isFormOpen by rememberSaveable { mutableStateOf(false) }
    var editingReport by remember { mutableStateOf<InspectionReport?>(null) }
    var reportPendingDelete by remember { mutableStateOf<InspectionReport?>(null) }
    var showPdfReportGeneratorDialog by rememberSaveable { mutableStateOf(false) }
    var showWebVersionDialog by rememberSaveable { mutableStateOf(false) }
    var isInteractiveWebAppOpen by rememberSaveable { mutableStateOf(false) }

    val currentViewState = (uiState as? UiState.Success)?.data
    val currentAccount = (userAccountState as? UiState.Success)?.data ?: currentViewState?.userAccount

    // Wait while initial userAccountState is loading
    if (userAccountState is UiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Gate 1: Phone + Password Registration/Login & Admin Approval Gate
    if (!isPhoneLoggedIn || currentAccount == null || currentAccount.approvalStatus != "approved") {
        RegistrationAndLoginGateScreen(
            userAccount = currentAccount,
            userEmail = userEmail,
            isBootstrappedAdmin = viewModel.isBootstrappedAdmin,
            isSaving = isSaving,
            onRegisterSubmit = { phoneNumber, rawPassword ->
                viewModel.registerWithPhoneAndPassword(
                    phoneNumber = phoneNumber,
                    rawPassword = rawPassword,
                    existingAccount = currentAccount
                )
            },
            onPhonePasswordLogin = { phoneNumber, rawPassword, onError ->
                viewModel.loginWithPhoneAndPassword(
                    phoneNumber = phoneNumber,
                    rawPassword = rawPassword,
                    account = currentAccount,
                    onError = onError
                )
            }
        )
        return
    }

    // Gate 2: After logging in, user sets their name if not set yet ("লগইন করার পর ইউজার তার একটা নাম সেট করে নিবে")
    val resolvedUserName = currentAccount.inspectorName.ifBlank {
        currentViewState?.profile?.inspectorName.orEmpty()
    }
    if (resolvedUserName.isBlank()) {
        PostLoginSetNameScreen(
            phoneNumber = currentAccount.phoneNumber,
            initialDesignation = currentAccount.designation,
            initialOfficeName = currentAccount.officeName,
            isAdmin = currentAccount.role == "admin" || viewModel.isBootstrappedAdmin,
            isSaving = isSaving,
            onSaveName = { inspectorName, designation, officeName ->
                viewModel.saveUserNameAfterLogin(
                    inspectorName = inspectorName,
                    designation = designation,
                    officeName = officeName
                )
            }
        )
        return
    }

    if (!isFormOpen) {
        if (selectedTab == 0 && selectedEnterDataCategory != null) {
            BackHandler { selectedEnterDataCategory = null }
        } else if (selectedTab == 1 && selectedViewDataCategory != null) {
            BackHandler {
                selectedViewDataCategory = null
                viewModel.updateReportCategoryFilter(null)
            }
        } else if (selectedTab != 0) {
            BackHandler { selectedTab = 0 }
        }
    }

    val defaultInspectorName = resolvedUserName
        .ifBlank { currentViewState?.allMonthReports?.firstOrNull()?.inspectorName.orEmpty() }
    val isAdminUser = currentViewState?.isAdmin == true || currentAccount.role == "admin" || viewModel.isBootstrappedAdmin
    val pendingApprovalCount = currentViewState?.allUserAccounts?.count { it.approvalStatus == "pending" } ?: 0

    if (isInteractiveWebAppOpen) {
        WebAppInteractiveScreen(
            reports = currentViewState?.allTimeReports ?: emptyList(),
            users = currentViewState?.allUserAccounts ?: listOfNotNull(currentAccount),
            tasks = currentViewState?.allTasks ?: emptyList(),
            customOptions = currentViewState?.customOptions ?: emptyList(),
            onClose = { isInteractiveWebAppOpen = false }
        )
        return
    }

    if (isFormOpen) {
        InspectionFormScreen(
            initialReport = editingReport,
            initialCategory = editingReport?.reportCategory?.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" } ?: "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
            defaultMonth = currentViewState?.selectedMonth ?: viewModel.selectedMonth.collectAsStateWithLifecycle().value,
            defaultInspectorName = defaultInspectorName,
            isSaving = isSaving,
            onDismiss = {
                isFormOpen = false
                editingReport = null
            },
            onSave = { formData ->
                viewModel.saveInspectionReport(
                    existingReport = editingReport,
                    formData = formData,
                    onComplete = {
                        isFormOpen = false
                        editingReport = null
                    }
                )
            }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (isAdminUser) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = "ড্যাশবোর্ড") },
                        label = { Text("ড্যাশবোর্ড", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_admin_dashboard")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedViewDataCategory = null
                            viewModel.updateReportCategoryFilter(null)
                            selectedTab = 1
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "View Data") },
                        label = { Text("View Data", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_view_data")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            val pendingTasks = currentViewState?.allTasks?.count { it.status == "pending" } ?: 0
                            BadgedBox(
                                badge = {
                                    if (pendingTasks > 0) {
                                        Badge { Text(InspectionPresets.toBanglaDigits(pendingTasks)) }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "টাস্ক")
                            }
                        },
                        label = { Text("টাস্ক", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_task_manager")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingApprovalCount > 0) {
                                        Badge {
                                            Text(InspectionPresets.toBanglaDigits(pendingApprovalCount))
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.People, contentDescription = "ইউজার ও রোল")
                            }
                        },
                        label = { Text("ইউজার ও রোল", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_admin_approval")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "অপশন") },
                        label = { Text("অপশন", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_admin_options")
                    )
                } else {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            editingReport = null
                            selectedEnterDataCategory = null
                            selectedTab = 0
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Enter Data") },
                        label = { Text("Enter Data", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_enter_data")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedViewDataCategory = null
                            viewModel.updateReportCategoryFilter(null)
                            selectedTab = 1
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "View Data") },
                        label = { Text("View Data", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_view_data")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Edit, contentDescription = "Edit Data") },
                        label = { Text("Edit Data", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_edit_data")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            val myPending = currentViewState?.myTasks?.count { it.status == "pending" } ?: 0
                            BadgedBox(
                                badge = {
                                    if (myPending > 0) {
                                        Badge { Text(InspectionPresets.toBanglaDigits(myPending)) }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "আমার টাস্ক")
                            }
                        },
                        label = { Text("আমার টাস্ক", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_my_tasks")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeMonth = currentViewState?.selectedMonth
                ?: viewModel.selectedMonth.collectAsStateWithLifecycle().value

            TopMonthHeaderBar(
                selectedMonth = activeMonth,
                inspectorName = defaultInspectorName,
                onPreviousMonth = { viewModel.shiftMonth(-1) },
                onNextMonth = { viewModel.shiftMonth(1) },
                onQuickPdfClick = {
                    if (isAdminUser) {
                        showPdfReportGeneratorDialog = true
                    } else if (currentViewState != null) {
                        ReportExportUtil.printOrSavePdfReport(
                            context = context,
                            reportMonth = currentViewState.selectedMonth,
                            reports = currentViewState.allMonthReports,
                            profile = currentViewState.profile,
                            stats = currentViewState.stats
                        )
                    }
                },
                onWebVersionClick = {
                    showWebVersionDialog = true
                },
                onLogoutClick = {
                    viewModel.lockPhoneSession()
                }
            )

            if (isAdminUser) {
                // Admin Top Selector Bar (Dashboard, View Data, Task Manager, Users, Options)
                AdminOptionsSelectorBar(
                    selectedTab = selectedTab,
                    pendingTasksCount = currentViewState?.allTasks?.count { it.status == "pending" } ?: 0,
                    pendingApprovalsCount = pendingApprovalCount,
                    onSelectTab = { newTab ->
                        if (newTab == 1) {
                            selectedViewDataCategory = null
                            viewModel.updateReportCategoryFilter(null)
                        }
                        selectedTab = newTab
                    }
                )
            } else {
                // User 4-Option Selector Bar: Enter Data | View Data | Edit Data | My Tasks
                UserOptionsSelectorBar(
                    selectedTab = selectedTab,
                    myPendingTasksCount = currentViewState?.myTasks?.count { it.status == "pending" } ?: 0,
                    onSelectEnterData = {
                        editingReport = null
                        selectedEnterDataCategory = null
                        selectedTab = 0
                    },
                    onSelectViewData = {
                        selectedViewDataCategory = null
                        viewModel.updateReportCategoryFilter(null)
                        selectedTab = 1
                    },
                    onSelectEditData = {
                        selectedTab = 2
                    },
                    onSelectMyTasks = {
                        selectedTab = 3
                    }
                )
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    if (isAdminUser) {
                        when (selectedTab) {
                            0 -> AdminDashboardOverviewScreen(
                                data = data,
                                onNavigateToViewData = {
                                    selectedViewDataCategory = null
                                    viewModel.updateReportCategoryFilter(null)
                                    selectedTab = 1
                                },
                                onNavigateToTaskManager = { selectedTab = 2 },
                                onNavigateToUsersRoles = { selectedTab = 3 },
                                onNavigateToOptionManager = { selectedTab = 4 },
                                onDownloadPdf = {
                                    showPdfReportGeneratorDialog = true
                                }
                            )
                            1 -> {
                                val activeViewCategory = selectedViewDataCategory
                                if (activeViewCategory == null) {
                                    ViewDataFiveOptionsMenuScreen(
                                        selectedMonth = data.selectedMonth,
                                        monthReports = data.allMonthReports,
                                        onSelectCategory = { chosenCategory ->
                                            selectedViewDataCategory = chosenCategory
                                            viewModel.updateReportCategoryFilter(chosenCategory)
                                        }
                                    )
                                } else {
                                    ViewDataTabContent(
                                        data = data,
                                        activeViewCategory = activeViewCategory,
                                        onBackToCategories = {
                                            selectedViewDataCategory = null
                                            viewModel.updateReportCategoryFilter(null)
                                        },
                                        onSearchQueryChange = viewModel::updateSearchQuery,
                                        onReportCategoryFilterChange = { cat ->
                                            selectedViewDataCategory = cat ?: activeViewCategory
                                            viewModel.updateReportCategoryFilter(cat)
                                        },
                                        onAuthorizedFilterChange = viewModel::updateAuthorizedFilter,
                                        onBuildingTypeFilterChange = viewModel::updateBuildingTypeFilter,
                                        onEnterDataClick = {
                                            selectedTab = 0
                                        },
                                        onOpenAdminApprovalClick = {
                                            selectedTab = 3
                                        },
                                        onEditReport = { report ->
                                            editingReport = report
                                            isFormOpen = true
                                        },
                                        onDeleteReport = { report ->
                                            reportPendingDelete = report
                                        }
                                    )
                                }
                            }
                            2 -> TaskManagerScreen(
                                isAdmin = true,
                                allTasks = data.allTasks,
                                myTasks = data.myTasks,
                                allUserAccounts = data.allUserAccounts,
                                isSaving = isSaving,
                                onCreateTask = { title, desc, user, due, prio, addr ->
                                    viewModel.createAssignedTask(title, desc, user, due, prio, addr)
                                },
                                onUpdateTask = viewModel::updateAssignedTask,
                                onUpdateStatus = viewModel::updateTaskStatus,
                                onDeleteTask = viewModel::deleteAssignedTask
                            )
                            3 -> AdminApprovalPanelScreen(
                                allAccounts = data.allUserAccounts,
                                isSaving = isSaving,
                                onApprove = viewModel::approveUserAccount,
                                onReject = viewModel::rejectUserAccount,
                                onUpdateUserRole = viewModel::updateUserRole
                            )
                            4 -> OptionManagerScreen(
                                customOptions = data.customOptions,
                                isSaving = isSaving,
                                onAddOption = viewModel::addAppOption,
                                onUpdateOption = viewModel::updateAppOption,
                                onDeleteOption = viewModel::deleteAppOption
                            )
                        }
                    } else {
                        when (selectedTab) {
                            0 -> {
                                val activeCategory = selectedEnterDataCategory
                                if (activeCategory == null) {
                                    EnterDataFiveOptionsMenuScreen(
                                        selectedMonth = data.selectedMonth,
                                        monthReports = data.allMonthReports,
                                        onSelectCategory = { chosenCategory ->
                                            selectedEnterDataCategory = chosenCategory
                                        }
                                    )
                                } else {
                                    InspectionFormScreen(
                                        initialReport = null,
                                        initialCategory = activeCategory,
                                        defaultMonth = data.selectedMonth,
                                        defaultInspectorName = defaultInspectorName,
                                        isSaving = isSaving,
                                        showTopBar = false,
                                        onBackToCategories = {
                                            selectedEnterDataCategory = null
                                        },
                                        onDismiss = {
                                            selectedEnterDataCategory = null
                                        },
                                        onSave = { formData ->
                                            viewModel.saveInspectionReport(
                                                existingReport = null,
                                                formData = formData,
                                                onComplete = {
                                                    selectedEnterDataCategory = null
                                                    selectedViewDataCategory = formData.reportCategory
                                                    viewModel.updateReportCategoryFilter(formData.reportCategory)
                                                    selectedTab = 1
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                            1 -> {
                                val activeViewCategory = selectedViewDataCategory
                                if (activeViewCategory == null) {
                                    ViewDataFiveOptionsMenuScreen(
                                        selectedMonth = data.selectedMonth,
                                        monthReports = data.allMonthReports,
                                        onSelectCategory = { chosenCategory ->
                                            selectedViewDataCategory = chosenCategory
                                            viewModel.updateReportCategoryFilter(chosenCategory)
                                        }
                                    )
                                } else {
                                    ViewDataTabContent(
                                        data = data,
                                        activeViewCategory = activeViewCategory,
                                        onBackToCategories = {
                                            selectedViewDataCategory = null
                                            viewModel.updateReportCategoryFilter(null)
                                        },
                                        onSearchQueryChange = viewModel::updateSearchQuery,
                                        onReportCategoryFilterChange = { cat ->
                                            selectedViewDataCategory = cat ?: activeViewCategory
                                            viewModel.updateReportCategoryFilter(cat)
                                        },
                                        onAuthorizedFilterChange = viewModel::updateAuthorizedFilter,
                                        onBuildingTypeFilterChange = viewModel::updateBuildingTypeFilter,
                                        onEnterDataClick = {
                                            editingReport = null
                                            selectedEnterDataCategory = activeViewCategory
                                            selectedTab = 0
                                        },
                                        onOpenAdminApprovalClick = {}
                                    )
                                }
                            }
                            2 -> EditDataTabContent(
                                data = data,
                                userAccount = currentAccount,
                                isSaving = isSaving,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onReportCategoryFilterChange = viewModel::updateReportCategoryFilter,
                                onEnterDataClick = {
                                    editingReport = null
                                    selectedEnterDataCategory = null
                                    selectedTab = 0
                                },
                                onEditReport = { report ->
                                    editingReport = report
                                    isFormOpen = true
                                },
                                onDeleteReport = { report ->
                                    reportPendingDelete = report
                                },
                                onSaveProfile = { name, desig, office ->
                                    viewModel.saveUserNameAfterLogin(name, desig, office)
                                }
                            )
                            3 -> TaskManagerScreen(
                                isAdmin = false,
                                allTasks = data.allTasks,
                                myTasks = data.myTasks,
                                allUserAccounts = data.allUserAccounts,
                                isSaving = isSaving,
                                onCreateTask = { _, _, _, _, _, _ -> },
                                onUpdateTask = {},
                                onUpdateStatus = viewModel::updateTaskStatus,
                                onDeleteTask = {}
                            )
                        }
                    }
                }
            }
        }
    }

    if (reportPendingDelete != null) {
        val target = reportPendingDelete!!
        AlertDialog(
            onDismissRequest = { reportPendingDelete = null },
            title = { Text("পরিদর্শন তথ্য মুছে ফেলবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text("ভবন মালিক: ${target.ownerName}\nঠিকানা: ${target.buildingAddress}\nএই রেকর্ডটি স্থায়ীভাবে মুছে ফেলা হবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInspection(target.id, target.userId)
                        reportPendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportPendingDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showPdfReportGeneratorDialog && currentViewState != null) {
        AdminPdfReportDialog(
            isOpen = showPdfReportGeneratorDialog,
            reportMonth = currentViewState.selectedMonth,
            allReports = currentViewState.allMonthReports,
            profile = currentViewState.profile,
            stats = currentViewState.stats,
            onDismiss = { showPdfReportGeneratorDialog = false }
        )
    }

    if (showWebVersionDialog) {
        AlertDialog(
            onDismissRequest = { showWebVersionDialog = false },
            title = {
                Text(
                    text = "🌐 সম্পূর্ণ ওয়েব ভার্সন (সকল ফিচার সহ)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "আপনার সম্পূর্ণ প্রোগ্রামটির ওয়েব ভার্সন (/web/index.html) তৈরি করা হয়েছে—যাতে লগইন/রেজিস্ট্রেশন, ৫টি রিপোর্ট ক্যাটাগরি, টাস্ক ম্যানেজার, ইউজার অ্যাপ্রুভাল ও রোল ম্যানেজমেন্ট, অপশন কাস্টমাইজার এবং অফিসিয়াল A4 PDF রিপোর্ট জেনারেটর হুবহু একইভাবে যুক্ত আছে।"
                    )
                    Text(
                        text = "• বর্তমান সকল রিপোর্ট, ইউজার, টাস্ক ও কাস্টম অপশন সহ ওয়েব ভার্সনটি সরাসরি চালু করুন, ব্রাউজারে ওপেন করুন অথবা কম্পিউটারে/হোয়াটসঅ্যাপে শেয়ার করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            showWebVersionDialog = false
                            isInteractiveWebAppOpen = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_interactive_web_app_button")
                    ) {
                        Text("🖥️ ওয়েব ভার্সন এখানেই চালান (Live Web App)", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            WebVersionExportUtil.openWebVersionInBrowser(
                                context = context,
                                reports = currentViewState?.allTimeReports ?: emptyList(),
                                users = currentViewState?.allUserAccounts ?: listOfNotNull(currentAccount),
                                tasks = currentViewState?.allTasks ?: emptyList(),
                                customOptions = currentViewState?.customOptions ?: emptyList()
                            )
                            showWebVersionDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_web_in_browser_button")
                    ) {
                        Text("🌐 সরাসরি ব্রাউজারে ওপেন করুন")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                WebVersionExportUtil.downloadWebVersionToDevice(
                                    context = context,
                                    reports = currentViewState?.allTimeReports ?: emptyList(),
                                    users = currentViewState?.allUserAccounts ?: listOfNotNull(currentAccount),
                                    tasks = currentViewState?.allTasks ?: emptyList(),
                                    customOptions = currentViewState?.customOptions ?: emptyList()
                                )
                                showWebVersionDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_web_version_button")
                        ) {
                            Text("📥 ডাউনলোড (.html)")
                        }
                        OutlinedButton(
                            onClick = {
                                WebVersionExportUtil.shareWebVersionFile(
                                    context = context,
                                    reports = currentViewState?.allTimeReports ?: emptyList(),
                                    users = currentViewState?.allUserAccounts ?: listOfNotNull(currentAccount),
                                    tasks = currentViewState?.allTasks ?: emptyList(),
                                    customOptions = currentViewState?.customOptions ?: emptyList()
                                )
                                showWebVersionDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_web_version_button")
                        ) {
                            Text("📲 শেয়ার (PC/WA)")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWebVersionDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
private fun AdminOptionsSelectorBar(
    selectedTab: Int,
    pendingTasksCount: Int,
    pendingApprovalsCount: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminSelectorTabItem(
                    title = "ড্যাশবোর্ড",
                    subtitle = "সার্বিক তথ্য",
                    icon = Icons.Filled.Dashboard,
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    testTag = "admin_opt_dashboard"
                )
                AdminSelectorTabItem(
                    title = "View Data",
                    subtitle = "ডাটা দেখুন",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    testTag = "admin_opt_view_data"
                )
                AdminSelectorTabItem(
                    title = "টাস্ক ম্যানেজার",
                    subtitle = "টাস্ক মনিটর",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    badgeCount = pendingTasksCount,
                    selected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    testTag = "admin_opt_tasks"
                )
                AdminSelectorTabItem(
                    title = "ইউজার ও রোল",
                    subtitle = "অনুমোদন",
                    icon = Icons.Filled.People,
                    badgeCount = pendingApprovalsCount,
                    selected = selectedTab == 3,
                    onClick = { onSelectTab(3) },
                    testTag = "admin_opt_users"
                )
                AdminSelectorTabItem(
                    title = "অপশন",
                    subtitle = "কাস্টমাইজ",
                    icon = Icons.Filled.Settings,
                    selected = selectedTab == 4,
                    onClick = { onSelectTab(4) },
                    testTag = "admin_opt_options"
                )
            }
        }
    }
}

@Composable
private fun UserOptionsSelectorBar(
    selectedTab: Int,
    myPendingTasksCount: Int,
    onSelectEnterData: () -> Unit,
    onSelectViewData: () -> Unit,
    onSelectEditData: () -> Unit,
    onSelectMyTasks: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminSelectorTabItem(
                    title = "Enter Data",
                    subtitle = "ডাটা এন্ট্রি",
                    icon = Icons.Filled.Add,
                    selected = selectedTab == 0,
                    onClick = onSelectEnterData,
                    testTag = "option_enter_data"
                )
                AdminSelectorTabItem(
                    title = "View Data",
                    subtitle = "ডাটা দেখুন",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    selected = selectedTab == 1,
                    onClick = onSelectViewData,
                    testTag = "option_view_data"
                )
                AdminSelectorTabItem(
                    title = "Edit Data",
                    subtitle = "ডাটা এডিট",
                    icon = Icons.Filled.Edit,
                    selected = selectedTab == 2,
                    onClick = onSelectEditData,
                    testTag = "option_edit_data"
                )
                AdminSelectorTabItem(
                    title = "আমার টাস্ক",
                    subtitle = "অর্পিত কাজ",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    badgeCount = myPendingTasksCount,
                    selected = selectedTab == 3,
                    onClick = onSelectMyTasks,
                    testTag = "option_my_tasks"
                )
            }
        }
    }
}

@Composable
private fun AdminSelectorTabItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (badgeCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = InspectionPresets.toBanglaDigits(badgeCount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TopMonthHeaderBar(
    selectedMonth: String,
    inspectorName: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onQuickPdfClick: () -> Unit,
    onWebVersionClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF073440),
                        Color(0xFF0B4F60),
                        Color(0xFF126478)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ইমারত পরিদর্শনের মাসিক রিপোর্ট",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (inspectorName.isNotBlank()) "ব্যবহারকারী: $inspectorName"
                        else "মাসিক ভবন পরিদর্শন ও প্রতিবেদন ব্যবস্থাপনা",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFDE68A)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onWebVersionClick)
                            .testTag("header_web_version_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🌐 Web",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onQuickPdfClick)
                            .testTag("header_quick_pdf_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureAsPdf,
                                contentDescription = "PDF প্রিন্ট",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "PDF",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onLogoutClick)
                            .testTag("header_logout_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "লগআউট",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "লগআউট",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Month switcher pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "পূর্ববর্তী মাস",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = InspectionPresets.formatReportMonthBangla(selectedMonth),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "প্রতিবেদনের মাস ($selectedMonth)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "পরবর্তী মাস",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class EnterDataOptionInfo(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color
)

@Composable
private fun EnterDataFiveOptionsMenuScreen(
    selectedMonth: String,
    monthReports: List<InspectionReport>,
    onSelectCategory: (String) -> Unit
) {
    val options = listOf(
        EnterDataOptionInfo(
            title = "অনিষ্পন্ন রিপোর্ট",
            subtitle = "গত মাসের জের, আলোচ্য মাসের প্রাপ্তি, মোট প্রাপ্তি, মাসের শেষ কার্যদিবসে অনিষ্পন্ন (৩টি অপশন) ও মন্তব্য",
            icon = Icons.Filled.History,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        EnterDataOptionInfo(
            title = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
            subtitle = "ইমারত পরিদর্শকের মাসিক ভবন পরিদর্শন ও ব্যত্যয় সংক্রান্ত ৯টি তথ্য এন্ট্রি করুন",
            icon = Icons.Filled.Apartment,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            iconColor = MaterialTheme.colorScheme.primary
        ),
        EnterDataOptionInfo(
            title = "নকশা অনুমোদন",
            subtitle = "Submitted, Approved, Rejected, Completed সহ নকশা অনুমোদনের তথ্য এন্ট্রি করুন",
            icon = Icons.Filled.Assessment,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
            iconColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        EnterDataOptionInfo(
            title = "অকুপেন্সী সনদ",
            subtitle = "পূর্ববর্তী জের, বর্তমান প্রাপ্তি, পরিদর্শন, সনদ প্রদান/অপ্রদানের সংখ্যা ও কারণ এন্ট্রি করুন",
            icon = Icons.Filled.VerifiedUser,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.primary
        ),
        EnterDataOptionInfo(
            title = "মোবাইল কোর্টের তথ্য",
            subtitle = "ম্যাজিস্ট্রেট, ভবন মালিক, ঠিকানা, জরিমানা, মিটার কর্তন ও অঙ্গীকারনামার তথ্য এন্ট্রি করুন",
            icon = Icons.Filled.Gavel,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
            iconColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .testTag("enter_data_five_options_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Enter Data — ৫টি অপশন থেকে নির্বাচন করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${InspectionPresets.formatReportMonthBangla(selectedMonth)} মাসের জন্য নিচের যেকোনো একটি অপশনে ট্যাপ করে তথ্য ইনপুট করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(options) { index, option ->
                val countForCategory = monthReports.count {
                    it.reportCategory.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" } == option.title
                }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectCategory(option.title) }
                        .testTag("enter_data_option_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(option.containerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.title,
                                tint = option.iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = InspectionPresets.toBanglaDigits(index + 1),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (countForCategory > 0) {
                                Text(
                                    text = "এই মাসে এন্ট্রি সংখ্যা: ${InspectionPresets.toBanglaDigits(countForCategory)} টি",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "এন্ট্রি করুন",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ViewDataFiveOptionsMenuScreen(
    selectedMonth: String,
    monthReports: List<InspectionReport>,
    onSelectCategory: (String) -> Unit
) {
    val options = listOf(
        EnterDataOptionInfo(
            title = "অনিষ্পন্ন রিপোর্ট",
            subtitle = "গত মাসের জের, আলোচ্য মাসের প্রাপ্তি, মোট প্রাপ্তি, মাসের শেষ কার্যদিবসে অনিষ্পন্ন ও মন্তব্য দেখুন",
            icon = Icons.Filled.History,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        EnterDataOptionInfo(
            title = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
            subtitle = "ইমারত পরিদর্শকের মাসিক ভবন পরিদর্শন ও ব্যত্যয় সংক্রান্ত রিপোর্ট দেখুন",
            icon = Icons.Filled.Apartment,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            iconColor = MaterialTheme.colorScheme.primary
        ),
        EnterDataOptionInfo(
            title = "নকশা অনুমোদন",
            subtitle = "Submitted, Approved, Rejected, Completed সহ নকশা অনুমোদনের সংরক্ষিত তথ্য দেখুন",
            icon = Icons.Filled.Assessment,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
            iconColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        EnterDataOptionInfo(
            title = "অকুপেন্সী সনদ",
            subtitle = "পূর্ববর্তী জের, বর্তমান প্রাপ্তি, পরিদর্শন, সনদ প্রদান/অপ্রদানের সংখ্যা ও কারণ দেখুন",
            icon = Icons.Filled.VerifiedUser,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.primary
        ),
        EnterDataOptionInfo(
            title = "মোবাইল কোর্টের তথ্য",
            subtitle = "ম্যাজিস্ট্রেট, ভবন মালিক, ঠিকানা, জরিমানা, মিটার কর্তন ও অঙ্গীকারনামার তথ্য দেখুন",
            icon = Icons.Filled.Gavel,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
            iconColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .testTag("view_data_five_options_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Data — ৫টি অপশন থেকে নির্বাচন করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${InspectionPresets.formatReportMonthBangla(selectedMonth)} মাসের সংরক্ষিত তথ্য দেখতে নিচের যেকোনো একটি অপশনে ট্যাপ করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(options) { index, option ->
                val countForCategory = monthReports.count {
                    it.reportCategory.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" } == option.title
                }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectCategory(option.title) }
                        .testTag("view_data_option_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(option.containerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.title,
                                tint = option.iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = InspectionPresets.toBanglaDigits(index + 1),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "এই মাসে সংরক্ষিত তথ্য: ${InspectionPresets.toBanglaDigits(countForCategory)} টি",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (countForCategory > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "দেখুন",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ViewDataTabContent(
    data: DashboardViewState,
    activeViewCategory: String,
    onBackToCategories: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onReportCategoryFilterChange: (String?) -> Unit,
    onAuthorizedFilterChange: (AuthorizedFilter) -> Unit,
    onBuildingTypeFilterChange: (String?) -> Unit,
    onEnterDataClick: () -> Unit,
    onOpenAdminApprovalClick: () -> Unit,
    onEditReport: (InspectionReport) -> Unit = {},
    onDeleteReport: (InspectionReport) -> Unit = {}
) {
    var showFullTableReport by rememberSaveable { mutableStateOf(false) }

    if (showFullTableReport) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মাসিক পূর্ণাঙ্গ ছক ও রিপোর্ট প্রিভিউ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    TextButton(
                        onClick = { showFullTableReport = false },
                        modifier = Modifier.testTag("back_to_card_view_button")
                    ) {
                        Text("কার্ড ভিউতে ফিরুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
            MonthlyReportPreviewScreen(
                selectedMonth = data.selectedMonth,
                reports = data.allMonthReports,
                profile = data.profile,
                stats = data.stats,
                onAddClick = onEnterDataClick
            )
        }
        return
    }
    val pendingUsers = data.allUserAccounts.count { it.approvalStatus == "pending" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedButton(
                            onClick = onBackToCategories,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("back_to_view_data_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "৫টি অপশনে ফিরে যান",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("৫টি অপশন", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(
                                text = activeViewCategory,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "মাস: ${InspectionPresets.formatReportMonthBangla(data.selectedMonth)} (${InspectionPresets.toBanglaDigits(data.filteredReports.size)} টি তথ্য)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (data.isAdmin && pendingUsers > 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenAdminApprovalClick)
                            .testTag("admin_pending_users_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${InspectionPresets.toBanglaDigits(pendingUsers)} জন ব্যবহারকারীর রেজিস্ট্রেশন অ্যাডমিন অনুমোদনের অপেক্ষায় আছে — অনুমোদন দিতে ট্যাপ করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // KPI Summary Grid
            item {
                MonthlyKpiSection(
                    stats = data.stats,
                    onViewFullReportClick = { showFullTableReport = true }
                )
            }

            // Search & Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("মালিকের নাম, ঠিকানা, মোবাইল বা গৃহীত ব্যবস্থা খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (data.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_inspection_input")
                    )

                    // 5 Enter Data Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = data.selectedReportCategory == null,
                            onClick = { onReportCategoryFilterChange(null) },
                            label = { Text("সকল ক্যাটাগরি (${InspectionPresets.toBanglaDigits(data.allMonthReports.size)})") }
                        )
                        InspectionPresets.enterDataCategories.forEach { cat ->
                            FilterChip(
                                selected = data.selectedReportCategory == cat,
                                onClick = { onReportCategoryFilterChange(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuthorizedFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = data.authorizedFilter == filter,
                                onClick = { onAuthorizedFilterChange(filter) },
                                label = { Text(filter.labelBangla) }
                            )
                        }
                        InspectionPresets.buildingTypes.forEachIndexed { idx, bType ->
                            FilterChip(
                                selected = data.selectedBuildingType == bType,
                                onClick = { onBuildingTypeFilterChange(bType) },
                                label = { Text("ভবনের ধরন: $bType") },
                                modifier = Modifier.testTag("view_filter_building_type_$idx")
                            )
                        }
                    }
                }
            }

            if (data.filteredReports.isEmpty()) {
                item {
                    EmptyInspectionsCard(
                        monthBangla = InspectionPresets.formatReportMonthBangla(data.selectedMonth),
                        hasAnyInMonth = data.allMonthReports.isNotEmpty(),
                        onAddClick = onEnterDataClick
                    )
                }
            } else {
                itemsIndexed(
                    items = data.filteredReports,
                    key = { _, item -> item.id }
                ) { index, report ->
                    InspectionReportCard(
                        serialNumber = index + 1,
                        report = report,
                        showEditActions = data.isAdmin,
                        onEdit = { onEditReport(report) },
                        onDelete = { onDeleteReport(report) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EditDataTabContent(
    data: DashboardViewState,
    userAccount: com.example.data.model.UserAccount?,
    isSaving: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onReportCategoryFilterChange: (String?) -> Unit,
    onEnterDataClick: () -> Unit,
    onEditReport: (InspectionReport) -> Unit,
    onDeleteReport: (InspectionReport) -> Unit,
    onSaveProfile: (String, String, String) -> Unit
) {
    var showProfileEdit by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Edit Data (তথ্য সম্পাদনা)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "নিচের তালিকা থেকে যেকোনো রেকর্ডের 'Edit Data (সম্পাদনা)' বাটনে ট্যাপ করে তথ্য পরিবর্তন করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = { showProfileEdit = !showProfileEdit },
                            modifier = Modifier.testTag("toggle_profile_edit_button")
                        ) {
                            Icon(Icons.Filled.ManageAccounts, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showProfileEdit) "বন্ধ করুন" else "নাম পরিবর্তন")
                        }
                    }
                }
            }

            if (showProfileEdit) {
                item {
                    InlineProfileEditCard(
                        profile = data.profile,
                        userAccount = userAccount,
                        isSaving = isSaving,
                        onSaveProfile = onSaveProfile
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("এডিট করার জন্য মালিকের নাম, ঠিকানা বা মোবাইল খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (data.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_search_inspection_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = data.selectedReportCategory == null,
                            onClick = { onReportCategoryFilterChange(null) },
                            label = { Text("সকল ক্যাটাগরি") }
                        )
                        InspectionPresets.enterDataCategories.forEach { cat ->
                            FilterChip(
                                selected = data.selectedReportCategory == cat,
                                onClick = { onReportCategoryFilterChange(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }

            if (data.filteredReports.isEmpty()) {
                item {
                    EmptyInspectionsCard(
                        monthBangla = InspectionPresets.formatReportMonthBangla(data.selectedMonth),
                        hasAnyInMonth = data.allMonthReports.isNotEmpty(),
                        onAddClick = onEnterDataClick
                    )
                }
            } else {
                itemsIndexed(
                    items = data.filteredReports,
                    key = { _, item -> item.id }
                ) { index, report ->
                    InspectionReportCard(
                        serialNumber = index + 1,
                        report = report,
                        showEditActions = true,
                        onEdit = { onEditReport(report) },
                        onDelete = { onDeleteReport(report) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InlineProfileEditCard(
    profile: InspectorProfile?,
    userAccount: com.example.data.model.UserAccount?,
    isSaving: Boolean,
    onSaveProfile: (String, String, String) -> Unit
) {
    var inspectorName by rememberSaveable(userAccount?.inspectorName, profile?.inspectorName) {
        mutableStateOf(
            userAccount?.inspectorName?.ifBlank { profile?.inspectorName ?: "" }
                ?: profile?.inspectorName
                ?: ""
        )
    }
    var designation by rememberSaveable(userAccount?.designation, profile?.designation) {
        mutableStateOf(
            userAccount?.designation?.ifBlank { profile?.designation ?: "ইমারত পরিদর্শক" }
                ?: profile?.designation
                ?: "ইমারত পরিদর্শক"
        )
    }
    var officeName by rememberSaveable(userAccount?.officeName, profile?.officeName) {
        mutableStateOf(
            userAccount?.officeName?.ifBlank { profile?.officeName ?: "" }
                ?: profile?.officeName
                ?: ""
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "আপনার নাম ও পদবী সম্পাদনা করুন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = inspectorName,
                onValueChange = { inspectorName = it },
                label = { Text("আপনার নাম / ইমারত পরিদর্শকের নাম *") },
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_inspector_name_input")
            )
            OutlinedTextField(
                value = designation,
                onValueChange = { designation = it },
                label = { Text("পদবী / জোন") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_designation_input")
            )
            OutlinedTextField(
                value = officeName,
                onValueChange = { officeName = it },
                label = { Text("কার্যালয় / কর্তৃপক্ষের নাম") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_office_name_input")
            )
            Button(
                onClick = {
                    if (inspectorName.isNotBlank()) {
                        onSaveProfile(inspectorName, designation, officeName)
                    }
                },
                enabled = !isSaving && inspectorName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_profile_button")
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("নাম সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MonthlyKpiSection(
    stats: MonthlySummaryStats,
    onViewFullReportClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                title = "মোট পরিদর্শন",
                value = "${InspectionPresets.toBanglaDigits(stats.totalInspections)} টি",
                subtitle = "নির্মিত: ${InspectionPresets.toBanglaDigits(stats.constructedBuildingCount)} | নির্মাণাধীন: ${InspectionPresets.toBanglaDigits(stats.underConstructionBuildingCount)}",
                icon = Icons.Filled.Apartment,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            KpiStatCard(
                title = "অথরাইজড অফিসার",
                value = "${InspectionPresets.toBanglaDigits(stats.authorizedOfficerVisitedCount)} টি",
                subtitle = "পরিদর্শন করেছেন",
                icon = Icons.Filled.VerifiedUser,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                title = "ব্যত্যয়কৃত ভবন",
                value = "${InspectionPresets.toBanglaDigits(stats.deviatedBuildingCount)} টি",
                subtitle = "নকশা বহির্ভূত/অনুমোদনহীন",
                icon = Icons.Filled.WarningAmber,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            KpiStatCard(
                title = "পুনঃপরিদর্শন",
                value = "${InspectionPresets.toBanglaDigits(stats.repeatInspectionCount)} টি",
                subtitle = "একাধিকবার পরিদর্শনকৃত",
                icon = Icons.Filled.History,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onViewFullReportClick)
                .testTag("open_monthly_report_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "মাসিক পূর্ণাঙ্গ ছক দেখুন ও PDF/Excel ডাউনলোড করুন",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.85f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InspectionReportCard(
    serialNumber: Int,
    report: InspectionReport,
    showEditActions: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val resolvedCategory = report.reportCategory.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" }
    val resolvedBuildingType = report.buildingType.ifBlank { InspectionPresets.BUILDING_TYPE_CONSTRUCTED }
    val isUnderConstruction = resolvedBuildingType == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspection_card_${report.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Badge Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "ক্যাটাগরি: $resolvedCategory",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Card Top Bar: Serial Number, Owner Name / Report Title, Building Type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = InspectionPresets.toBanglaDigits(serialNumber),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = if (resolvedCategory == "অনিষ্পন্ন রিপোর্ট") {
                                "অনিষ্পন্ন রিপোর্ট (${InspectionPresets.formatReportMonthBangla(report.reportMonth)})"
                            } else if (InspectionPresets.isDesignApprovalCategory(resolvedCategory)) {
                                "নকশা অনুমোদন (${InspectionPresets.formatReportMonthBangla(report.reportMonth)})"
                            } else if (InspectionPresets.isOccupancyCertificateCategory(resolvedCategory)) {
                                "অকুপেন্সী সনদ (${InspectionPresets.formatReportMonthBangla(report.reportMonth)})"
                            } else if (InspectionPresets.isMobileCourtCategory(resolvedCategory)) {
                                "মোবাইল কোর্ট: ${report.ownerName}"
                            } else {
                                "মালিক: ${report.ownerName}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (InspectionPresets.isMobileCourtCategory(resolvedCategory)) {
                            val mcDate = report.mobileCourtDate.ifBlank { report.inspectionDate }
                            Text(
                                text = "মোবাইল কোর্টের তারিখ: ${InspectionPresets.toBanglaDigits(mcDate)} | ম্যাজিস্ট্রেট: ${report.magistrateName.ifBlank { "ম্যাজিস্ট্রেট" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (resolvedCategory != "অনিষ্পন্ন রিপোর্ট" && 
                            !InspectionPresets.isDesignApprovalCategory(resolvedCategory) &&
                            !InspectionPresets.isOccupancyCertificateCategory(resolvedCategory)) {
                            Text(
                                text = "পরিদর্শনের তারিখ: ${InspectionPresets.toBanglaDigits(report.inspectionDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "প্রতিবেদনের মাস: ${InspectionPresets.formatReportMonthBangla(report.reportMonth)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (resolvedCategory != "অনিষ্পন্ন রিপোর্ট" && 
                    !InspectionPresets.isDesignApprovalCategory(resolvedCategory) &&
                    !InspectionPresets.isOccupancyCertificateCategory(resolvedCategory) &&
                    !InspectionPresets.isMobileCourtCategory(resolvedCategory)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnderConstruction) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = "৫. ভবনের ধরন: $resolvedBuildingType",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isUnderConstruction) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            if (InspectionPresets.isUnresolvedCategory(resolvedCategory)) {
                val resolvedDuration = report.unresolvedDurationCategory.ifBlank {
                    InspectionPresets.unresolvedDurationOptions.first()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "১. গত মাসের জের",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = InspectionPresets.toBanglaDigits(report.previousMonthBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "২. আলোচ্য মাসের প্রাপ্তি",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = InspectionPresets.toBanglaDigits(report.currentMonthReceived),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৩. মোট প্রাপ্তি",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = InspectionPresets.toBanglaDigits(report.totalReceived),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = if (report.unresolvedAtMonthEnd > 0L) {
                                "$resolvedDuration (${InspectionPresets.toBanglaDigits(report.unresolvedAtMonthEnd)} টি)"
                            } else {
                                resolvedDuration
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "৫. মন্তব্য: ${report.remarks.ifBlank { "মন্তব্য নেই" }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (InspectionPresets.isDesignApprovalCategory(resolvedCategory)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "নকশা অনুমোদনের ৮টি অপশনের সংরক্ষিত বিবরণ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Row 1: Submitted & Approved
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "১. Submitted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planSubmitted)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "২. Approved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planApproved)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Row 2: Rejected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৩. Rejected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planRejected)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Row 3: Completed (Approved + Rejected + Sent for Applicants Feedback)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "৪. Completed (Approved + Rejected + Sent for Applicants Feedback):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "${InspectionPresets.toBanglaDigits(report.planCompleted)} টি (Approved ${InspectionPresets.toBanglaDigits(report.planApproved)} + Rejected ${InspectionPresets.toBanglaDigits(report.planRejected)} + Feedback ${InspectionPresets.toBanglaDigits(report.planSentForFeedback)})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Row 4: Work In Progress (WIP) & Sent For Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৫. Work In Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planWorkInProgress)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৬. Sent For Feedback",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planSentForFeedback)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 5: Completed <= 45 Days & Completed > 45 Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৭. Completed Within 45 Working Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planCompletedWithin45Days)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৮. Completed In More Than 45 Working Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.planCompletedMoreThan45Days)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (report.remarks.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "মন্তব্য: ${report.remarks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else if (InspectionPresets.isOccupancyCertificateCategory(resolvedCategory)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "অকুপেন্সী সনদের সংরক্ষিত তথ্য:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                val prevBal = if (report.occupancyPreviousMonthBalance > 0L) report.occupancyPreviousMonthBalance else report.previousMonthBalance
                val currRec = if (report.occupancyCurrentMonthReceived > 0L) report.occupancyCurrentMonthReceived else report.currentMonthReceived
                val totApp = if (report.occupancyTotalApplications > 0L) report.occupancyTotalApplications else report.totalReceived
                val inspBld = if (report.occupancyInspectedBuildings > 0L) report.occupancyInspectedBuildings else report.previousInspectionCount

                // Row 1: পূর্ববর্তী মাসের জের & বর্তমান মাসে প্রাপ্ত আবেদন
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "১. পূর্ববর্তী মাসের জের",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(prevBal)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "২. বর্তমান মাসে প্রাপ্ত আবেদন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(currRec)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 2: জেরসহ মোট আবেদন & পরিদর্শনকৃত ভবন
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৩. জেরসহ মোট আবেদন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(totApp)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৪. পরিদর্শনকৃত ভবনের সংখ্যা",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(inspBld)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 3: Occupancy Certificate প্রদান & অপ্রদান
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৫. সনদ প্রদান করা হয়েছে",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.occupancyIssuedCount)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৬. সনদ প্রদান করা হয় নি",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${InspectionPresets.toBanglaDigits(report.occupancyNotIssuedCount)} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Row 4: কারণ (if present)
                if (report.occupancyNotIssuedReason.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৭. Occupancy Certificate প্রদান না করার কারণ: ${report.occupancyNotIssuedReason}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 5: মন্তব্য (if present)
                if (report.remarks.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "৮. মন্তব্য: ${report.remarks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else if (InspectionPresets.isMobileCourtCategory(resolvedCategory)) {
                // Dedicated Mobile Court Structured Display
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
                        Text(
                            text = "মোবাইল কোর্টের সংরক্ষিত বিবরণ:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Row 1: ম্যাজিস্ট্রেট ও মোবাইল কোর্টের তারিখ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("১. মোবাইল কোর্টের তারিখ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = InspectionPresets.toBanglaDigits(report.mobileCourtDate.ifBlank { report.inspectionDate }),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("২. ম্যাজিস্ট্রেট এর নাম", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = report.magistrateName.ifBlank { "-" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 2: ভবন মালিকের নাম ও মোবাইল
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("৩. ভবন মালিকের নাম", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = report.ownerName.ifBlank { "-" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("৪. মোবাইল নাম্বার", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (report.ownerMobile.isNotBlank()) InspectionPresets.toBanglaDigits(report.ownerMobile) else "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 3: ভবনের ঠিকানা & ভবনের ধরণ (নির্মিত/নির্মাণাধীন)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("৫. ভবনের ঠিকানা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = report.buildingAddress.ifBlank { "-" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("৬. ভবনের ধরণ:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (report.buildingType == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = report.buildingType.ifBlank { InspectionPresets.BUILDING_TYPE_CONSTRUCTED },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (report.buildingType == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Row 4: ভবনে ব্যত্যয় & জরিমানার পরিমাণ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("৭. ব্যত্যয়ের ধরণ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(
                                text = report.deviationType.ifBlank { "-" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("৮. জরিমানার পরিমাণ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(
                                text = "৳ ${InspectionPresets.toBanglaDigits(report.mobileCourtFineAmount)} টাকা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Row 5: মিটার কর্তন ও অঙ্গীকারনামা দাখিল
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("৯ ও ১০. মিটার কর্তন?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (report.mobileCourtMeterCut) "হ্যাঁ (${InspectionPresets.toBanglaDigits(report.mobileCourtMeterCount)}টি)" else "না (কাটা হয়নি)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (report.mobileCourtMeterCut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("১১. অঙ্গীকারনামা দাখিল?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (report.mobileCourtUndertakingSubmitted) "হ্যাঁ (দাখিল করেছে)" else "না (দাখিল করেনি)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (report.mobileCourtUndertakingSubmitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Row 6: মন্তব্য
                if (report.remarks.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "১২. মন্তব্য: ${report.remarks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Building Inspection & Other Fields Structured View
                FieldRowItem(
                    icon = Icons.Filled.LocationOn,
                    label = "ভবনের ঠিকানা:",
                    value = report.buildingAddress
                )

                if (report.mouzaName.isNotBlank() || report.thanaName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FieldRowItem(
                                icon = Icons.Filled.LocationOn,
                                label = "মৌজার নাম:",
                                value = report.mouzaName.ifBlank { "-" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FieldRowItem(
                                icon = Icons.Filled.Apartment,
                                label = "থানার নাম:",
                                value = report.thanaName.ifBlank { "-" }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FieldRowItem(
                            icon = Icons.Filled.Call,
                            label = "মোবাইল নাম্বার:",
                            value = report.ownerMobile
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FieldRowItem(
                            icon = Icons.Filled.History,
                            label = "৬. আগে কতবার পরিদর্শন ($resolvedBuildingType):",
                            value = "${InspectionPresets.toBanglaDigits(report.previousInspectionCount)} বার"
                        )
                    }
                }

                // Authorized Officer & Deviation Badges (Fields 7 & 8)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val authYes = report.authorizedOfficerInspected
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (authYes) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = if (authYes) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "৭. অথরাইজড অফিসার পরিদর্শন? ($resolvedBuildingType)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (authYes) "হ্যাঁ (করেছেন)" else "না (করেননি)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (authYes) MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "৮. ব্যত্যয়কৃত ভবনের ধরণ ($resolvedBuildingType)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = report.deviationType,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Action Taken Banner (Field 9)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "৯. গৃহীত ব্যবস্থা ($resolvedBuildingType): ${report.actionTaken}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (report.remarks.isNotBlank()) {
                                Text(
                                    text = "মন্তব্য: ${report.remarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Action Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resolvedCategory != "অনিষ্পন্ন রিপোর্ট" && report.ownerMobile.isNotBlank() && report.ownerMobile != "-") {
                    TextButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${report.ownerMobile}"))
                            context.startActivity(dialIntent)
                        },
                        modifier = Modifier.testTag("call_owner_${report.id}")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "কল করুন", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল করুন")
                    }
                }

                if (showEditActions) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("edit_inspection_${report.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Data", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Data (সম্পাদনা)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_inspection_${report.id}")
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মুছুন", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldRowItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyInspectionsCard(
    monthBangla: String,
    hasAnyInMonth: Boolean,
    onAddClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Apartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = if (hasAnyInMonth) "আপনার অনুসন্ধানের সাথে কোনো ভবন মেলেনি"
                else "$monthBangla মাসের জন্য কোনো পরিদর্শন এন্ট্রি নেই",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "নতুন ভবন পরিদর্শনের তথ্য যুক্ত করতে 'Enter Data (ডাটা এন্ট্রি)' বাটনে ট্যাপ করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (!hasAnyInMonth) {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_add_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enter Data (নতুন তথ্য যুক্ত করুন)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

