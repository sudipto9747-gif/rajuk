package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.InspectionFormData
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectionFormScreen(
    initialReport: InspectionReport?,
    initialCategory: String = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
    defaultMonth: String,
    defaultInspectorName: String,
    isSaving: Boolean,
    showTopBar: Boolean = true,
    onBackToCategories: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (InspectionFormData) -> Unit
) {
    if (showTopBar) {
        BackHandler { onDismiss() }
    } else if (onBackToCategories != null) {
        BackHandler { onBackToCategories() }
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    val yesterdayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 86_400_000L))
    }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val hasInitialReport = initialReport != null
    val initReport = initialReport ?: InspectionReport()
    val reportIdKey = initReport.id

    var reportCategory by rememberSaveable(reportIdKey, initialCategory) {
        mutableStateOf(
            if (hasInitialReport && initReport.reportCategory.isNotBlank()) {
                initReport.reportCategory
            } else {
                initialCategory
            }
        )
    }
    var reportMonth by rememberSaveable(reportIdKey, defaultMonth) {
        mutableStateOf(if (hasInitialReport && initReport.reportMonth.isNotBlank()) initReport.reportMonth else defaultMonth)
    }
    var inspectionDate by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && initReport.inspectionDate.isNotBlank()) initReport.inspectionDate else todayStr)
    }
    var inspectorName by rememberSaveable(reportIdKey, defaultInspectorName) {
        mutableStateOf(if (hasInitialReport && initReport.inspectorName.isNotBlank()) initReport.inspectorName else defaultInspectorName)
    }
    var ownerName by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.ownerName else "")
    }
    var buildingAddress by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.buildingAddress else "")
    }
    var mouzaName by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mouzaName else "")
    }
    var thanaName by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.thanaName else "")
    }
    var ownerMobile by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.ownerMobile else "")
    }
    val initialResolvedBuildingType = if (
        hasInitialReport && initReport.buildingType.trim() == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
    ) {
        InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
    } else {
        InspectionPresets.BUILDING_TYPE_CONSTRUCTED
    }
    var buildingType by rememberSaveable(reportIdKey) {
        mutableStateOf(initialResolvedBuildingType)
    }

    val isInitConstructed = initialResolvedBuildingType == InspectionPresets.BUILDING_TYPE_CONSTRUCTED
    val isInitUnderConstruction = initialResolvedBuildingType == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION

    // Separate 6–9 state for "নির্মিত" (Constructed)
    var constructedPrevCountText by rememberSaveable(reportIdKey) {
        val initCount = if (hasInitialReport && isInitConstructed) initReport.previousInspectionCount else 0L
        mutableStateOf(initCount.toString())
    }
    var constructedPrevCount by rememberSaveable(reportIdKey) {
        val initCount = if (hasInitialReport && isInitConstructed) initReport.previousInspectionCount else 0L
        mutableLongStateOf(initCount)
    }
    var constructedAuthOfficer by rememberSaveable(reportIdKey) {
        val initAuth = hasInitialReport && isInitConstructed && initReport.authorizedOfficerInspected
        mutableStateOf(initAuth)
    }
    var constructedDeviationType by rememberSaveable(reportIdKey) {
        val initDev = if (hasInitialReport && isInitConstructed && initReport.deviationType.isNotBlank()) {
            initReport.deviationType
        } else {
            InspectionPresets.deviationTypesConstructed.first()
        }
        mutableStateOf(initDev)
    }
    var constructedActionTaken by rememberSaveable(reportIdKey) {
        val initAct = if (hasInitialReport && isInitConstructed && initReport.actionTaken.isNotBlank()) {
            initReport.actionTaken
        } else {
            InspectionPresets.actionTakenTypesConstructed.first()
        }
        mutableStateOf(initAct)
    }
    var constructedRemarks by rememberSaveable(reportIdKey) {
        val initRem = if (hasInitialReport && isInitConstructed) initReport.remarks else ""
        mutableStateOf(initRem)
    }

    // Separate 6–9 state for "নির্মাণাধীন" (Under Construction)
    var underConstructionPrevCountText by rememberSaveable(reportIdKey) {
        val initCount = if (hasInitialReport && isInitUnderConstruction) initReport.previousInspectionCount else 0L
        mutableStateOf(initCount.toString())
    }
    var underConstructionPrevCount by rememberSaveable(reportIdKey) {
        val initCount = if (hasInitialReport && isInitUnderConstruction) initReport.previousInspectionCount else 0L
        mutableLongStateOf(initCount)
    }
    var underConstructionAuthOfficer by rememberSaveable(reportIdKey) {
        val initAuth = hasInitialReport && isInitUnderConstruction && initReport.authorizedOfficerInspected
        mutableStateOf(initAuth)
    }
    var underConstructionDeviationType by rememberSaveable(reportIdKey) {
        val initDev = if (hasInitialReport && isInitUnderConstruction && initReport.deviationType.isNotBlank()) {
            initReport.deviationType
        } else {
            InspectionPresets.deviationTypesUnderConstruction.first()
        }
        mutableStateOf(initDev)
    }
    var underConstructionActionTaken by rememberSaveable(reportIdKey) {
        val initAct = if (hasInitialReport && isInitUnderConstruction && initReport.actionTaken.isNotBlank()) {
            initReport.actionTaken
        } else {
            InspectionPresets.actionTakenTypesUnderConstruction.first()
        }
        mutableStateOf(initAct)
    }
    var underConstructionRemarks by rememberSaveable(reportIdKey) {
        val initRem = if (hasInitialReport && isInitUnderConstruction) initReport.remarks else ""
        mutableStateOf(initRem)
    }

    val isUnderConstructionSelected = buildingType == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
    val activePrevCountText = if (isUnderConstructionSelected) underConstructionPrevCountText else constructedPrevCountText
    val activePrevCount = if (isUnderConstructionSelected) underConstructionPrevCount else constructedPrevCount
    val activeAuthOfficer = if (isUnderConstructionSelected) underConstructionAuthOfficer else constructedAuthOfficer
    val activeDeviationType = if (isUnderConstructionSelected) underConstructionDeviationType else constructedDeviationType
    val activeActionTaken = if (isUnderConstructionSelected) underConstructionActionTaken else constructedActionTaken
    val activeBuildingRemarks = if (isUnderConstructionSelected) underConstructionRemarks else constructedRemarks

    val updateActivePrevCount: (Long, String) -> Unit = { count, text ->
        if (isUnderConstructionSelected) {
            underConstructionPrevCount = count
            underConstructionPrevCountText = text
        } else {
            constructedPrevCount = count
            constructedPrevCountText = text
        }
    }
    val updateActiveAuthOfficer: (Boolean) -> Unit = { checked ->
        if (isUnderConstructionSelected) {
            underConstructionAuthOfficer = checked
        } else {
            constructedAuthOfficer = checked
        }
    }
    val updateActiveDeviationType: (String) -> Unit = { value ->
        if (isUnderConstructionSelected) {
            underConstructionDeviationType = value
        } else {
            constructedDeviationType = value
        }
    }
    val updateActiveActionTaken: (String) -> Unit = { value ->
        if (isUnderConstructionSelected) {
            underConstructionActionTaken = value
        } else {
            constructedActionTaken = value
        }
    }
    val updateActiveBuildingRemarks: (String) -> Unit = { value ->
        if (isUnderConstructionSelected) {
            underConstructionRemarks = value
        } else {
            constructedRemarks = value
        }
    }

    // Unresolved Report ("অনিষ্পন্ন রিপোর্ট") State:
    var previousMonthBalanceText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.previousMonthBalance else 0L).toString())
    }
    var previousMonthBalance by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.previousMonthBalance else 0L)
    }
    var currentMonthReceivedText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.currentMonthReceived else 0L).toString())
    }
    var currentMonthReceived by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.currentMonthReceived else 0L)
    }
    var totalReceivedText by rememberSaveable(reportIdKey) {
        val initTotal = if (hasInitialReport) {
            initReport.totalReceived
        } else {
            0L
        }
        mutableStateOf(initTotal.toString())
    }
    var totalReceived by rememberSaveable(reportIdKey) {
        val initTotal = if (hasInitialReport) {
            initReport.totalReceived
        } else {
            0L
        }
        mutableLongStateOf(initTotal)
    }
    var unresolvedAtMonthEndText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.unresolvedAtMonthEnd else 0L).toString())
    }
    var unresolvedAtMonthEnd by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.unresolvedAtMonthEnd else 0L)
    }
    var unresolvedDurationCategory by rememberSaveable(reportIdKey) {
        mutableStateOf(
            if (hasInitialReport && initReport.unresolvedDurationCategory.isNotBlank()) {
                initReport.unresolvedDurationCategory
            } else {
                InspectionPresets.unresolvedDurationOptions.first()
            }
        )
    }

    var unresolvedRemarks by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.remarks else "")
    }

    // Design Approval ("নকশা অনুমোদনের তথ্য") 8 Fields State:
    var planSubmittedText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planSubmitted else 0L).toString())
    }
    var planSubmitted by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planSubmitted else 0L)
    }

    var planApprovedText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planApproved else 0L).toString())
    }
    var planApproved by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planApproved else 0L)
    }

    var planRejectedText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planRejected else 0L).toString())
    }
    var planRejected by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planRejected else 0L)
    }

    var planSentForFeedbackText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planSentForFeedback else 0L).toString())
    }
    var planSentForFeedback by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planSentForFeedback else 0L)
    }

    var planCompletedText by rememberSaveable(reportIdKey) {
        val initialComp = if (hasInitialReport) {
            initReport.planCompleted
        } else {
            0L
        }
        mutableStateOf(initialComp.toString())
    }
    var planCompleted by rememberSaveable(reportIdKey) {
        val initialComp = if (hasInitialReport) {
            initReport.planCompleted
        } else {
            0L
        }
        mutableLongStateOf(initialComp)
    }

    var planWorkInProgressText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planWorkInProgress else 0L).toString())
    }
    var planWorkInProgress by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planWorkInProgress else 0L)
    }

    var planCompletedWithin45DaysText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planCompletedWithin45Days else 0L).toString())
    }
    var planCompletedWithin45Days by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planCompletedWithin45Days else 0L)
    }

    var planCompletedMoreThan45DaysText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.planCompletedMoreThan45Days else 0L).toString())
    }
    var planCompletedMoreThan45Days by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.planCompletedMoreThan45Days else 0L)
    }

    var planRemarks by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isDesignApprovalCategory(initReport.reportCategory)) initReport.remarks else "")
    }

    // Occupancy Certificate ("অকুপেন্সী সনদ") 8 Fields State:
    var occupancyPreviousMonthBalanceText by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyPreviousMonthBalance > 0L) initReport.occupancyPreviousMonthBalance else initReport.previousMonthBalance
        } else 0L
        mutableStateOf(initVal.toString())
    }
    var occupancyPreviousMonthBalance by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyPreviousMonthBalance > 0L) initReport.occupancyPreviousMonthBalance else initReport.previousMonthBalance
        } else 0L
        mutableLongStateOf(initVal)
    }

    var occupancyCurrentMonthReceivedText by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyCurrentMonthReceived > 0L) initReport.occupancyCurrentMonthReceived else initReport.currentMonthReceived
        } else 0L
        mutableStateOf(initVal.toString())
    }
    var occupancyCurrentMonthReceived by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyCurrentMonthReceived > 0L) initReport.occupancyCurrentMonthReceived else initReport.currentMonthReceived
        } else 0L
        mutableLongStateOf(initVal)
    }

    var occupancyTotalApplicationsText by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyTotalApplications > 0L) initReport.occupancyTotalApplications else initReport.totalReceived
        } else 0L
        mutableStateOf(initVal.toString())
    }
    var occupancyTotalApplications by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyTotalApplications > 0L) initReport.occupancyTotalApplications else initReport.totalReceived
        } else 0L
        mutableLongStateOf(initVal)
    }

    var occupancyInspectedBuildingsText by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyInspectedBuildings > 0L) initReport.occupancyInspectedBuildings else initReport.previousInspectionCount
        } else 0L
        mutableStateOf(initVal.toString())
    }
    var occupancyInspectedBuildings by rememberSaveable(reportIdKey) {
        val initVal = if (hasInitialReport) {
            if (initReport.occupancyInspectedBuildings > 0L) initReport.occupancyInspectedBuildings else initReport.previousInspectionCount
        } else 0L
        mutableLongStateOf(initVal)
    }

    var occupancyIssuedCountText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.occupancyIssuedCount else 0L).toString())
    }
    var occupancyIssuedCount by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.occupancyIssuedCount else 0L)
    }

    var occupancyNotIssuedCountText by rememberSaveable(reportIdKey) {
        mutableStateOf((if (hasInitialReport) initReport.occupancyNotIssuedCount else 0L).toString())
    }
    var occupancyNotIssuedCount by rememberSaveable(reportIdKey) {
        mutableLongStateOf(if (hasInitialReport) initReport.occupancyNotIssuedCount else 0L)
    }

    var occupancyNotIssuedReason by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.occupancyNotIssuedReason else "")
    }

    var occupancyRemarks by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isOccupancyCertificateCategory(initReport.reportCategory)) initReport.remarks else "")
    }

    // Mobile Court ("মোবাইল কোর্টের তথ্য") States
    var mobileCourtDate by rememberSaveable(reportIdKey) {
        mutableStateOf(
            if (hasInitialReport && initReport.mobileCourtDate.isNotBlank()) initReport.mobileCourtDate
            else if (hasInitialReport && initReport.inspectionDate.isNotBlank()) initReport.inspectionDate
            else todayStr
        )
    }
    var magistrateName by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.magistrateName else "")
    }
    var mobileCourtOwnerName by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isMobileCourtCategory(initReport.reportCategory)) initReport.ownerName else "")
    }
    var mobileCourtOwnerMobile by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isMobileCourtCategory(initReport.reportCategory)) initReport.ownerMobile else "")
    }
    var mobileCourtBuildingAddress by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isMobileCourtCategory(initReport.reportCategory)) initReport.buildingAddress else "")
    }
    var mobileCourtBuildingType by rememberSaveable(reportIdKey) {
        mutableStateOf(
            if (hasInitialReport && initReport.buildingType.isNotBlank()) initReport.buildingType 
            else InspectionPresets.BUILDING_TYPE_CONSTRUCTED
        )
    }
    var mobileCourtDeviationType by rememberSaveable(reportIdKey) {
        mutableStateOf(
            if (hasInitialReport && initReport.deviationType.isNotBlank()) initReport.deviationType
            else InspectionPresets.deviationTypesConstructed.first()
        )
    }
    var mobileCourtFineAmount by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtFineAmount else 0L)
    }
    var mobileCourtFineAmountText by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtFineAmount.toString() else "0")
    }
    var mobileCourtMeterCut by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtMeterCut else false)
    }
    var mobileCourtMeterCount by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtMeterCount else 0L)
    }
    var mobileCourtMeterCountText by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtMeterCount.toString() else "0")
    }
    var mobileCourtUndertakingSubmitted by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport) initReport.mobileCourtUndertakingSubmitted else false)
    }
    var mobileCourtRemarks by rememberSaveable(reportIdKey) {
        mutableStateOf(if (hasInitialReport && InspectionPresets.isMobileCourtCategory(initReport.reportCategory)) initReport.remarks else "")
    }
    var showMobileCourtDatePickerDialog by remember { mutableStateOf(false) }

    var deviationTypeExpanded by remember { mutableStateOf(false) }
    var actionTakenExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val resetForm = {
        ownerName = ""
        buildingAddress = ""
        mouzaName = ""
        thanaName = ""
        ownerMobile = ""
        buildingType = InspectionPresets.BUILDING_TYPE_CONSTRUCTED
        constructedPrevCount = 0L
        constructedPrevCountText = "0"
        constructedAuthOfficer = false
        constructedDeviationType = InspectionPresets.deviationTypesConstructed.first()
        constructedActionTaken = InspectionPresets.actionTakenTypesConstructed.first()
        constructedRemarks = ""
        underConstructionPrevCount = 0L
        underConstructionPrevCountText = "0"
        underConstructionAuthOfficer = false
        underConstructionDeviationType = InspectionPresets.deviationTypesUnderConstruction.first()
        underConstructionActionTaken = InspectionPresets.actionTakenTypesUnderConstruction.first()
        underConstructionRemarks = ""
        previousMonthBalance = 0L
        previousMonthBalanceText = "0"
        currentMonthReceived = 0L
        currentMonthReceivedText = "0"
        totalReceived = 0L
        totalReceivedText = "0"
        unresolvedAtMonthEnd = 0L
        unresolvedAtMonthEndText = "0"
        unresolvedDurationCategory = InspectionPresets.unresolvedDurationOptions.first()
        unresolvedRemarks = ""
        planSubmitted = 0L
        planSubmittedText = "0"
        planApproved = 0L
        planApprovedText = "0"
        planRejected = 0L
        planRejectedText = "0"
        planSentForFeedback = 0L
        planSentForFeedbackText = "0"
        planCompleted = 0L
        planCompletedText = "0"
        planWorkInProgress = 0L
        planWorkInProgressText = "0"
        planCompletedWithin45Days = 0L
        planCompletedWithin45DaysText = "0"
        planCompletedMoreThan45Days = 0L
        planCompletedMoreThan45DaysText = "0"
        planRemarks = ""
        occupancyPreviousMonthBalance = 0L
        occupancyPreviousMonthBalanceText = "0"
        occupancyCurrentMonthReceived = 0L
        occupancyCurrentMonthReceivedText = "0"
        occupancyTotalApplications = 0L
        occupancyTotalApplicationsText = "0"
        occupancyInspectedBuildings = 0L
        occupancyInspectedBuildingsText = "0"
        occupancyIssuedCount = 0L
        occupancyIssuedCountText = "0"
        occupancyNotIssuedCount = 0L
        occupancyNotIssuedCountText = "0"
        occupancyNotIssuedReason = ""
        occupancyRemarks = ""
        mobileCourtDate = todayStr
        magistrateName = ""
        mobileCourtOwnerName = ""
        mobileCourtOwnerMobile = ""
        mobileCourtBuildingAddress = ""
        mobileCourtBuildingType = InspectionPresets.BUILDING_TYPE_CONSTRUCTED
        mobileCourtDeviationType = InspectionPresets.deviationTypesConstructed.first()
        mobileCourtFineAmount = 0L
        mobileCourtFineAmountText = "0"
        mobileCourtMeterCut = false
        mobileCourtMeterCount = 0L
        mobileCourtMeterCountText = "0"
        mobileCourtUndertakingSubmitted = false
        mobileCourtRemarks = ""
        validationError = null
    }

    Scaffold(
        contentWindowInsets = if (showTopBar) WindowInsets.safeDrawing else WindowInsets(0, 0, 0, 0),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (initialReport == null) reportCategory else "তথ্য সম্পাদনা: $reportCategory",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "মাস: ${InspectionPresets.formatReportMonthBangla(reportMonth)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_form_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান"
                            )
                        }
                    },
                    actions = {
                        if (initialReport == null) {
                            TextButton(
                                onClick = resetForm,
                                modifier = Modifier.testTag("reset_form_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "রিসেট",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("রিসেট")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("inspection_input_form_scroll"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!showTopBar) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onBackToCategories != null) {
                                OutlinedButton(
                                    onClick = onBackToCategories,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("back_to_enter_data_options_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "৫টি অপশনে ফিরে যান",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("৫টি অপশন", fontWeight = FontWeight.Bold)
                                }
                            }
                            Column {
                                Text(
                                    text = reportCategory,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "তথ্য পূরণ করে রিপোর্টে সংরক্ষণ করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = resetForm,
                            modifier = Modifier.testTag("reset_form_inline_button")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রিসেট")
                        }
                    }
                }

                if (reportCategory != "অনিষ্পন্ন রিপোর্ট" && 
                    reportCategory != "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" && 
                    !InspectionPresets.isDesignApprovalCategory(reportCategory) &&
                    !InspectionPresets.isOccupancyCertificateCategory(reportCategory) &&
                    !InspectionPresets.isMobileCourtCategory(reportCategory)) {
                    // Enter Data 5 Options Selector inside Form
                    FormSectionCard(title = "ডাটা এন্ট্রির ধরণ / অপশন (Enter Data Category) *") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InspectionPresets.enterDataCategories.forEachIndexed { idx, categoryOption ->
                                val selected = reportCategory == categoryOption
                                FilterChip(
                                    selected = selected,
                                    onClick = { reportCategory = categoryOption },
                                    label = { Text("${InspectionPresets.toBanglaDigits(idx + 1)}. $categoryOption") },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.testTag("form_category_chip_$idx")
                                )
                            }
                        }
                    }
                }

                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_validation_error_card")
                    ) {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                if (InspectionPresets.isMobileCourtCategory(reportCategory)) {
                    // মোবাইল কোর্টের তারিখ এর জন্য ডেডিকেটেড কার্ডে ক্যালেন্ডার ও তারিখ ইনপুট রয়েছে
                } else if (InspectionPresets.isDesignApprovalCategory(reportCategory) || 
                    InspectionPresets.isUnresolvedCategory(reportCategory) ||
                    InspectionPresets.isOccupancyCertificateCategory(reportCategory)) {
                    // প্রতিবেদনের মাস (Report Month) Section for নকশা অনুমোদন, অনিষ্পন্ন রিপোর্ট ও অকুপেন্সী সনদ (পরিদর্শনের তারিখ বাদ)
                    FormSectionCard(title = "প্রতিবেদনের মাস (Report Month) *") {
                        OutlinedTextField(
                            value = reportMonth,
                            onValueChange = { reportMonth = it },
                            label = { Text("প্রতিবেদনের মাস (YYYY-MM) *") },
                            placeholder = { Text("যেমন: $defaultMonth") },
                            leadingIcon = {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_month_input")
                        )
                    }
                } else {
                    // Building Inspection Date & Month Section (for regular inspection reports)
                    FormSectionCard(title = "ভবন পরিদর্শনের তারিখ ও মাস (Building Inspection Date & Month) *") {
                        OutlinedTextField(
                            value = inspectionDate,
                            onValueChange = {
                                inspectionDate = it
                                if (it.length >= 7 && it.matches(Regex("^[0-9]{4}-[0-9]{2}.*"))) {
                                    reportMonth = it.substring(0, 7)
                                }
                            },
                            label = { Text("ভবন পরিদর্শনের তারিখ (YYYY-MM-DD) *") },
                            placeholder = { Text("যেমন: $todayStr") },
                            leadingIcon = {
                                Icon(Icons.Filled.Event, contentDescription = null)
                            },
                            trailingIcon = {
                                TextButton(
                                    onClick = { showDatePickerDialog = true },
                                    modifier = Modifier.testTag("open_date_picker_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = "ক্যালেন্ডার থেকে তারিখ নির্বাচন",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ক্যালেন্ডার", fontWeight = FontWeight.Bold)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inspection_date_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = inspectionDate == todayStr,
                                onClick = {
                                    inspectionDate = todayStr
                                    reportMonth = todayStr.substring(0, 7)
                                },
                                label = { Text("আজ (${InspectionPresets.toBanglaDigits(todayStr)})") },
                                leadingIcon = if (inspectionDate == todayStr) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.testTag("date_today_chip")
                            )
                            FilterChip(
                                selected = inspectionDate == yesterdayStr,
                                onClick = {
                                    inspectionDate = yesterdayStr
                                    reportMonth = yesterdayStr.substring(0, 7)
                                },
                                label = { Text("গতকাল (${InspectionPresets.toBanglaDigits(yesterdayStr)})") },
                                leadingIcon = if (inspectionDate == yesterdayStr) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.testTag("date_yesterday_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = reportMonth,
                            onValueChange = { reportMonth = it },
                            label = { Text("প্রতিবেদনের মাস (YYYY-MM)") },
                            leadingIcon = {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_month_input")
                        )
                    }
                }

                if (reportCategory == "অনিষ্পন্ন রিপোর্ট") {
                    // Unresolved Report 5 Fields Card
                    FormSectionCard(title = "অনিষ্পন্ন রিপোর্টের তথ্য (Unresolved Report Details) *") {
                        // 1. গত মাসের জের
                        Text(
                            text = "১. গত মাসের জের *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val next = (previousMonthBalance - 1L).coerceAtLeast(0L)
                                    previousMonthBalance = next
                                    previousMonthBalanceText = next.toString()
                                    val sum = (next + currentMonthReceived).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("decrement_prev_month_balance_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "কমান")
                            }

                            OutlinedTextField(
                                value = previousMonthBalanceText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(6)
                                    previousMonthBalanceText = filtered
                                    val parsed = filtered.toLongOrNull() ?: 0L
                                    previousMonthBalance = parsed
                                    val sum = (parsed + currentMonthReceived).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                label = { Text("গত মাসের জের") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("previous_month_balance_input")
                            )

                            IconButton(
                                onClick = {
                                    val next = (previousMonthBalance + 1L).coerceAtMost(1_000_000L)
                                    previousMonthBalance = next
                                    previousMonthBalanceText = next.toString()
                                    val sum = (next + currentMonthReceived).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("increment_prev_month_balance_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "বাড়ান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. আলোচ্য মাসের প্রাপ্তি
                        Text(
                            text = "২. আলোচ্য মাসের প্রাপ্তি *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val next = (currentMonthReceived - 1L).coerceAtLeast(0L)
                                    currentMonthReceived = next
                                    currentMonthReceivedText = next.toString()
                                    val sum = (previousMonthBalance + next).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("decrement_curr_month_received_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "কমান")
                            }

                            OutlinedTextField(
                                value = currentMonthReceivedText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(6)
                                    currentMonthReceivedText = filtered
                                    val parsed = filtered.toLongOrNull() ?: 0L
                                    currentMonthReceived = parsed
                                    val sum = (previousMonthBalance + parsed).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                label = { Text("আলোচ্য মাসের প্রাপ্তি") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("current_month_received_input")
                            )

                            IconButton(
                                onClick = {
                                    val next = (currentMonthReceived + 1L).coerceAtMost(1_000_000L)
                                    currentMonthReceived = next
                                    currentMonthReceivedText = next.toString()
                                    val sum = (previousMonthBalance + next).coerceAtMost(1_000_000L)
                                    totalReceived = sum
                                    totalReceivedText = sum.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("increment_curr_month_received_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "বাড়ান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. মোট প্রাপ্তি
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "৩. মোট প্রাপ্তি *",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "গত মাসের জের (${InspectionPresets.toBanglaDigits(previousMonthBalance)}) + আলোচ্য মাসের প্রাপ্তি (${InspectionPresets.toBanglaDigits(currentMonthReceived)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = totalReceivedText,
                            onValueChange = { text ->
                                val filtered = text.filter { it.isDigit() }.take(6)
                                totalReceivedText = filtered
                                totalReceived = filtered.toLongOrNull() ?: 0L
                            },
                            label = { Text("মোট প্রাপ্তি (গত মাসের জের + আলোচ্য মাসের প্রাপ্তি)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("total_received_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. মাসের শেষ কার্যদিবসে অনিষ্পন্ন (৩টি অপশনের যেকোনো ১টি সিলেক্ট করতে হবে)
                        Text(
                            text = "৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন (যে কোনো ১টি অপশন সিলেক্ট করুন) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InspectionPresets.unresolvedDurationOptions.forEachIndexed { idx, optionText ->
                                val isSelected = unresolvedDurationCategory == optionText
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { unresolvedDurationCategory = optionText }
                                        .testTag("unresolved_duration_option_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { unresolvedDurationCategory = optionText }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${InspectionPresets.toBanglaDigits(idx + 1)}. $optionText",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "নির্বাচিত",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // সংখ্যা ইনপুট (মাসের শেষ কার্যদিবসে অনিষ্পন্ন সংখ্যা)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val next = (unresolvedAtMonthEnd - 1L).coerceAtLeast(0L)
                                    unresolvedAtMonthEnd = next
                                    unresolvedAtMonthEndText = next.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("decrement_unresolved_count_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "কমান")
                            }

                            OutlinedTextField(
                                value = unresolvedAtMonthEndText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(6)
                                    unresolvedAtMonthEndText = filtered
                                    unresolvedAtMonthEnd = filtered.toLongOrNull() ?: 0L
                                },
                                label = { Text("অনিষ্পন্ন সংখ্যা (ঐচ্ছিক)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("unresolved_month_end_input")
                            )

                            IconButton(
                                onClick = {
                                    val next = (unresolvedAtMonthEnd + 1L).coerceAtMost(1_000_000L)
                                    unresolvedAtMonthEnd = next
                                    unresolvedAtMonthEndText = next.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("increment_unresolved_count_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "বাড়ান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. মন্তব্য
                        Text(
                            text = "৫. মন্তব্য",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = unresolvedRemarks,
                            onValueChange = { unresolvedRemarks = it },
                            label = { Text("৫. মন্তব্য (Remarks)") },
                            placeholder = { Text("অনিষ্পন্ন রিপোর্ট সম্পর্কিত মন্তব্য লিখুন...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("remarks_input")
                        )
                    }
                } else if (InspectionPresets.isDesignApprovalCategory(reportCategory)) {
                    // Design Approval 8 Options Card
                    // নকশা অনুমোদন অপশনে আগের সব বাদ দিয়ে Submitted, Approved, Rejected, Completed (Approved + Rejected + Sent for Applicants Feedback), Work In Progress, Sent For Applicants Feedback, Completed Within 45 Working Days, Completed In More Than 45 Working Days
                    FormSectionCard(title = "নকশা অনুমোদন অপশনসমূহ (Design Approval Data Entry) *") {
                        Text(
                            text = "নকশা অনুমোদনের নির্ধারিত ৮টি অপশনের ডাটা এন্ট্রি করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Submitted
                        DesignApprovalCounterRow(
                            label = "১. Submitted (দাখিলকৃত নকশার সংখ্যা) *",
                            valueText = planSubmittedText,
                            currentValue = planSubmitted,
                            testTagPrefix = "plan_submitted",
                            onValueChange = { count, text ->
                                planSubmitted = count
                                planSubmittedText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Approved
                        DesignApprovalCounterRow(
                            label = "২. Approved (অনুমোদিত নকশা) *",
                            valueText = planApprovedText,
                            currentValue = planApproved,
                            testTagPrefix = "plan_approved",
                            onValueChange = { count, text ->
                                planApproved = count
                                planApprovedText = text
                                val comp = (count + planRejected + planSentForFeedback).coerceIn(0L, 1_000_000L)
                                planCompleted = comp
                                planCompletedText = comp.toString()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Rejected
                        DesignApprovalCounterRow(
                            label = "৩. Rejected (বাতিলকৃত নকশা) *",
                            valueText = planRejectedText,
                            currentValue = planRejected,
                            testTagPrefix = "plan_rejected",
                            onValueChange = { count, text ->
                                planRejected = count
                                planRejectedText = text
                                val comp = (planApproved + count + planSentForFeedback).coerceIn(0L, 1_000_000L)
                                planCompleted = comp
                                planCompletedText = comp.toString()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Completed (Approved + Rejected + Sent for Applicants Feedback)
                        Text(
                            text = "৪. Completed (Approved + Rejected + Sent for Applicants Feedback) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "হিসাব: Approved (${InspectionPresets.toBanglaDigits(planApproved)}) + Rejected (${InspectionPresets.toBanglaDigits(planRejected)}) + Sent for Feedback (${InspectionPresets.toBanglaDigits(planSentForFeedback)}) = ${InspectionPresets.toBanglaDigits(planCompleted)} টি",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val next = (planCompleted - 1L).coerceAtLeast(0L)
                                    planCompleted = next
                                    planCompletedText = next.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("decrement_plan_completed_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "কমান")
                            }

                            OutlinedTextField(
                                value = planCompletedText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(6)
                                    planCompletedText = filtered
                                    planCompleted = filtered.toLongOrNull() ?: 0L
                                },
                                label = { Text("Completed (Approved + Rejected + Feedback)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("plan_completed_input")
                            )

                            IconButton(
                                onClick = {
                                    val next = (planCompleted + 1L).coerceAtMost(1_000_000L)
                                    planCompleted = next
                                    planCompletedText = next.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("increment_plan_completed_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "বাড়ান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. Work In Progress
                        DesignApprovalCounterRow(
                            label = "৫. Work In Progress (চলমান / প্রক্রিয়াধীন নকশা) *",
                            valueText = planWorkInProgressText,
                            currentValue = planWorkInProgress,
                            testTagPrefix = "plan_work_in_progress",
                            onValueChange = { count, text ->
                                planWorkInProgress = count
                                planWorkInProgressText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 6. Sent For Applicants Feedback
                        DesignApprovalCounterRow(
                            label = "৬. Sent For Applicants Feedback (আবেদনকারীর মতামতের জন্য প্রেরিত) *",
                            valueText = planSentForFeedbackText,
                            currentValue = planSentForFeedback,
                            testTagPrefix = "plan_sent_feedback",
                            onValueChange = { count, text ->
                                planSentForFeedback = count
                                planSentForFeedbackText = text
                                val comp = (planApproved + planRejected + count).coerceIn(0L, 1_000_000L)
                                planCompleted = comp
                                planCompletedText = comp.toString()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 7. Completed Within 45 Working Days
                        DesignApprovalCounterRow(
                            label = "৭. Completed Within 45 Working Days (৪৫ কার্যদিবসের মধ্যে নিষ্পন্ন) *",
                            valueText = planCompletedWithin45DaysText,
                            currentValue = planCompletedWithin45Days,
                            testTagPrefix = "plan_completed_within_45",
                            onValueChange = { count, text ->
                                planCompletedWithin45Days = count
                                planCompletedWithin45DaysText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 8. Completed In More Than 45 Working Days
                        DesignApprovalCounterRow(
                            label = "৮. Completed In More Than 45 Working Days (৪৫ কার্যদিবসের অধিক সময়ে নিষ্পন্ন) *",
                            valueText = planCompletedMoreThan45DaysText,
                            currentValue = planCompletedMoreThan45Days,
                            testTagPrefix = "plan_completed_more_than_45",
                            onValueChange = { count, text ->
                                planCompletedMoreThan45Days = count
                                planCompletedMoreThan45DaysText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Remarks for Design Approval
                        Text(
                            text = "মন্তব্য (Remarks - ঐচ্ছিক)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = planRemarks,
                            onValueChange = { planRemarks = it },
                            label = { Text("নকশা অনুমোদন সংক্রান্ত মন্তব্য (ঐচ্ছিক)") },
                            placeholder = { Text("নকশা অনুমোদন সংক্রান্ত তথ্য বা স্মারক নং...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plan_remarks_input")
                        )
                    }
                } else if (InspectionPresets.isOccupancyCertificateCategory(reportCategory)) {
                    // Occupancy Certificate (অকুপেন্সী সনদ) 8 Options Card
                    // পূর্ববর্তী মাসের জের, বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা, জেরসহ মোট আবেদন সংখ্যা, পরিদর্শনকৃত ভবনের সংখ্যা, কতটি Occupancy Certificate প্রদান করা হয়েছে, কতটি Occupancy Certificate প্রদান করা হয় নি, Occupancy Certificate প্রদান না করার কারণ, মন্তব্য
                    FormSectionCard(title = "অকুপেন্সী সনদ তথ্য এন্ট্রি (Occupancy Certificate Data Entry) *") {
                        Text(
                            text = "অকুপেন্সী সনদের নির্ধারিত ৮টি অপশনের তথ্য পূরণ করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // ১. পূর্ববর্তী মাসের জের
                        DesignApprovalCounterRow(
                            label = "১. পূর্ববর্তী মাসের জের *",
                            valueText = occupancyPreviousMonthBalanceText,
                            currentValue = occupancyPreviousMonthBalance,
                            testTagPrefix = "occupancy_prev_balance",
                            onValueChange = { count, text ->
                                occupancyPreviousMonthBalance = count
                                occupancyPreviousMonthBalanceText = text
                                val sum = (count + occupancyCurrentMonthReceived).coerceAtMost(1_000_000L)
                                occupancyTotalApplications = sum
                                occupancyTotalApplicationsText = sum.toString()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ২. বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা
                        DesignApprovalCounterRow(
                            label = "২. বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা *",
                            valueText = occupancyCurrentMonthReceivedText,
                            currentValue = occupancyCurrentMonthReceived,
                            testTagPrefix = "occupancy_curr_received",
                            onValueChange = { count, text ->
                                occupancyCurrentMonthReceived = count
                                occupancyCurrentMonthReceivedText = text
                                val sum = (occupancyPreviousMonthBalance + count).coerceAtMost(1_000_000L)
                                occupancyTotalApplications = sum
                                occupancyTotalApplicationsText = sum.toString()
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৩. জেরসহ মোট আবেদন সংখ্যা
                        DesignApprovalCounterRow(
                            label = "৩. জেরসহ মোট আবেদন সংখ্যা (১ম + ২য় ঘর) *",
                            valueText = occupancyTotalApplicationsText,
                            currentValue = occupancyTotalApplications,
                            testTagPrefix = "occupancy_total_applications",
                            onValueChange = { count, text ->
                                occupancyTotalApplications = count
                                occupancyTotalApplicationsText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৪. পরিদর্শনকৃত ভবনের সংখ্যা
                        DesignApprovalCounterRow(
                            label = "৪. পরিদর্শনকৃত ভবনের সংখ্যা *",
                            valueText = occupancyInspectedBuildingsText,
                            currentValue = occupancyInspectedBuildings,
                            testTagPrefix = "occupancy_inspected_buildings",
                            onValueChange = { count, text ->
                                occupancyInspectedBuildings = count
                                occupancyInspectedBuildingsText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৫. কতটি Occupancy Certificate প্রদান করা হয়েছে
                        DesignApprovalCounterRow(
                            label = "৫. কতটি Occupancy Certificate প্রদান করা হয়েছে *",
                            valueText = occupancyIssuedCountText,
                            currentValue = occupancyIssuedCount,
                            testTagPrefix = "occupancy_issued_count",
                            onValueChange = { count, text ->
                                occupancyIssuedCount = count
                                occupancyIssuedCountText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৬. কতটি Occupancy Certificate প্রদান করা হয় নি
                        DesignApprovalCounterRow(
                            label = "৬. কতটি Occupancy Certificate প্রদান করা হয় নি *",
                            valueText = occupancyNotIssuedCountText,
                            currentValue = occupancyNotIssuedCount,
                            testTagPrefix = "occupancy_not_issued_count",
                            onValueChange = { count, text ->
                                occupancyNotIssuedCount = count
                                occupancyNotIssuedCountText = text
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৭. Occupancy Certificate প্রদান না করার কারণ
                        Text(
                            text = "৭. Occupancy Certificate প্রদান না করার কারণ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = occupancyNotIssuedReason,
                            onValueChange = { occupancyNotIssuedReason = it },
                            label = { Text("প্রদান না করার কারণ (যদি থাকে)") },
                            placeholder = { Text("ব্যত্যয়, কাগজপত্র অসম্পূর্ণতা বা অন্যান্য কারণ লিখুন...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("occupancy_not_issued_reason_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৮. মন্তব্য
                        Text(
                            text = "৮. মন্তব্য",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = occupancyRemarks,
                            onValueChange = { occupancyRemarks = it },
                            label = { Text("অকুপেন্সী সনদ সংক্রান্ত মন্তব্য (ঐচ্ছিক)") },
                            placeholder = { Text("অকুপেন্সী সনদ সংক্রান্ত তথ্য বা স্মারক নং...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("occupancy_remarks_input")
                        )
                    }
                } else if (InspectionPresets.isMobileCourtCategory(reportCategory)) {
                    // Mobile Court (মোবাইল কোর্টের তথ্য) Dedicated Form Section
                    // মোবাইল কোর্টের তারিখ, ম্যাজিস্ট্রেট এর নাম, ভবন মালিকের নাম, ভবন মালিকের মোবাইল নাম্বার, ভবনের ঠিকানা, ভবন কি নির্মিত নাকি নির্মাণাধীন, ভবনে কোন ধরনের ব্যত্যয় আছে? ভবন মালিক কে কত টাকা জরিমানা করা হয়েছে? মিটার কাটা হয়েছে কিনা? হলে কতটি মিটার কাটা হয়েছে, ভবন মালিক অঙ্গীকার নামা দাখিল করেছে কিনা
                    FormSectionCard(title = "মোবাইল কোর্টের তথ্য এন্ট্রি (Mobile Court Data Entry) *") {
                        Text(
                            text = "ইমারত নির্মাণ আইন ও বিধিমালা অনুযায়ী মোবাইল কোর্ট পরিচালনার তথ্য পূরণ করুন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // ১. মোবাইল কোর্টের তারিখ
                        Text(
                            text = "১. মোবাইল কোর্টের তারিখ (Date of Mobile Court) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mobileCourtDate,
                            onValueChange = { mobileCourtDate = it },
                            label = { Text("মোবাইল কোর্টের তারিখ (YYYY-MM-DD) *") },
                            placeholder = { Text("যেমন: $todayStr") },
                            leadingIcon = {
                                Icon(Icons.Filled.Event, contentDescription = null)
                            },
                            trailingIcon = {
                                TextButton(
                                    onClick = { showMobileCourtDatePickerDialog = true },
                                    modifier = Modifier.testTag("open_mc_date_picker_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = "ক্যালেন্ডার",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ক্যালেন্ডার", fontWeight = FontWeight.Bold)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mobile_court_date_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = mobileCourtDate == todayStr,
                                onClick = { mobileCourtDate = todayStr },
                                label = { Text("আজ (${InspectionPresets.toBanglaDigits(todayStr)})") },
                                leadingIcon = if (mobileCourtDate == todayStr) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.testTag("mc_date_today_chip")
                            )
                            FilterChip(
                                selected = mobileCourtDate == yesterdayStr,
                                onClick = { mobileCourtDate = yesterdayStr },
                                label = { Text("গতকাল (${InspectionPresets.toBanglaDigits(yesterdayStr)})") },
                                leadingIcon = if (mobileCourtDate == yesterdayStr) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.testTag("mc_date_yesterday_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ২. ম্যাজিস্ট্রেট এর নাম
                        Text(
                            text = "২. ম্যাজিস্ট্রেট এর নাম (Magistrate's Name) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = magistrateName,
                            onValueChange = { magistrateName = it },
                            label = { Text("এক্সিকিউটিভ ম্যাজিস্ট্রেট / বিচারকের নাম *") },
                            placeholder = { Text("যেমন: জনাব মো: রফিকুল ইসলাম, এক্সিকিউটিভ ম্যাজিস্ট্রেট") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("magistrate_name_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৩. ভবন মালিকের নাম
                        Text(
                            text = "৩. ভবন মালিকের নাম (Building Owner Name) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mobileCourtOwnerName,
                            onValueChange = { mobileCourtOwnerName = it },
                            label = { Text("ভবন মালিকের পূর্ণ নাম *") },
                            placeholder = { Text("যেমন: মোঃ আব্দুল করিম") },
                            leadingIcon = {
                                Icon(Icons.Filled.Badge, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mc_owner_name_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৪. ভবন মালিকের মোবাইল নাম্বার
                        Text(
                            text = "৪. ভবন মালিকের মোবাইল নাম্বার (Owner Mobile Number) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mobileCourtOwnerMobile,
                            onValueChange = { mobileCourtOwnerMobile = it },
                            label = { Text("১১ ডিজিটের মোবাইল নম্বর") },
                            placeholder = { Text("যেমন: 017xxxxxxxx") },
                            leadingIcon = {
                                Icon(Icons.Filled.Phone, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mc_owner_mobile_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৫. ভবনের ঠিকানা
                        Text(
                            text = "৫. ভবনের ঠিকানা (Building Address) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mobileCourtBuildingAddress,
                            onValueChange = { mobileCourtBuildingAddress = it },
                            label = { Text("প্লট, রোড, ব্লক ও এলাকা *") },
                            placeholder = { Text("যেমন: বাড়ি-১২, রোড-০৫, সেক্টর-০৩, উত্তরা, ঢাকা") },
                            leadingIcon = {
                                Icon(Icons.Filled.LocationOn, contentDescription = null)
                            },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mc_building_address_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৬. ভবন কি নির্মিত নাকি নির্মাণাধীন
                        Text(
                            text = "৬. ভবন কি নির্মিত নাকি নির্মাণাধীন? (Building Status) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                InspectionPresets.BUILDING_TYPE_CONSTRUCTED,
                                InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
                            ).forEach { bType ->
                                val isSelected = mobileCourtBuildingType == bType
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        mobileCourtBuildingType = bType
                                        val presets = if (bType == InspectionPresets.BUILDING_TYPE_CONSTRUCTED) {
                                            InspectionPresets.deviationTypesConstructed
                                        } else {
                                            InspectionPresets.deviationTypesUnderConstruction
                                        }
                                        if (mobileCourtDeviationType !in presets) {
                                            mobileCourtDeviationType = presets.first()
                                        }
                                    },
                                    label = { Text(bType, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (bType == InspectionPresets.BUILDING_TYPE_CONSTRUCTED) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mc_building_type_$bType")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৭. ভবনে কোন ধরনের ব্যত্যয় আছে?
                        Text(
                            text = "৭. ভবনে কোন ধরনের ব্যত্যয় আছে? (Type of Deviation) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val relevantDeviations = if (mobileCourtBuildingType == InspectionPresets.BUILDING_TYPE_CONSTRUCTED) {
                            InspectionPresets.deviationTypesConstructed
                        } else {
                            InspectionPresets.deviationTypesUnderConstruction
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            relevantDeviations.forEach { devOption ->
                                val isSelected = mobileCourtDeviationType == devOption
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { mobileCourtDeviationType = devOption },
                                    label = { Text(devOption, style = MaterialTheme.typography.bodySmall) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = mobileCourtDeviationType,
                            onValueChange = { mobileCourtDeviationType = it },
                            label = { Text("ব্যত্যয়ের নির্দিষ্ট বিবরণ *") },
                            placeholder = { Text("ব্যত্যয়ের ধরণ নির্বাচন করুন অথবা বিস্তারিত লিখুন...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mc_deviation_type_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৮. ভবন মালিক কে কত টাকা জরিমানা করা হয়েছে?
                        Text(
                            text = "৮. ভবন মালিক কে কত টাকা জরিমানা করা হয়েছে? (Fine Amount) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = mobileCourtFineAmountText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(9)
                                    mobileCourtFineAmountText = filtered
                                    mobileCourtFineAmount = filtered.toLongOrNull() ?: 0L
                                },
                                label = { Text("জরিমানার পরিমাণ (টাকা / ৳) *") },
                                placeholder = { Text("0") },
                                leadingIcon = {
                                    Text(" ৳ ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mc_fine_amount_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10_000L, 50_000L, 100_000L, 200_000L, 500_000L).forEach { amount ->
                                val isSelected = mobileCourtFineAmount == amount
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        mobileCourtFineAmount = amount
                                        mobileCourtFineAmountText = amount.toString()
                                    },
                                    label = { Text("৳${InspectionPresets.toBanglaDigits(amount)}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ৯. মিটার কাটা হয়েছে কিনা?
                        Text(
                            text = "৯. মিটার কাটা হয়েছে কিনা? (Meter Cut / Disconnected?) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = mobileCourtMeterCut,
                                onClick = { 
                                    mobileCourtMeterCut = true 
                                    if (mobileCourtMeterCount == 0L) {
                                        mobileCourtMeterCount = 1L
                                        mobileCourtMeterCountText = "1"
                                    }
                                },
                                label = { Text("হ্যাঁ (মিটার কাটা হয়েছে)", fontWeight = if (mobileCourtMeterCut) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (mobileCourtMeterCut) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mc_meter_cut_yes")
                            )
                            FilterChip(
                                selected = !mobileCourtMeterCut,
                                onClick = { 
                                    mobileCourtMeterCut = false 
                                    mobileCourtMeterCount = 0L
                                    mobileCourtMeterCountText = "0"
                                },
                                label = { Text("না (মিটার কাটা হয়নি)", fontWeight = if (!mobileCourtMeterCut) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (!mobileCourtMeterCut) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mc_meter_cut_no")
                            )
                        }

                        // ১০. হলে কতটি মিটার কাটা হয়েছে
                        if (mobileCourtMeterCut) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DesignApprovalCounterRow(
                                label = "১০. হলে কতটি মিটার কাটা হয়েছে? (Number of Meters Cut) *",
                                valueText = mobileCourtMeterCountText,
                                currentValue = mobileCourtMeterCount,
                                testTagPrefix = "mc_meter_cut_count",
                                onValueChange = { count, text ->
                                    mobileCourtMeterCount = count
                                    mobileCourtMeterCountText = text
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ১১. ভবন মালিক অঙ্গীকার নামা দাখিল করেছে কিনা
                        Text(
                            text = "১১. ভবন মালিক অঙ্গীকার নামা দাখিল করেছে কিনা? (Undertaking Submitted?) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = mobileCourtUndertakingSubmitted,
                                onClick = { mobileCourtUndertakingSubmitted = true },
                                label = { Text("হ্যাঁ (অঙ্গীকার নামা দাখিল করেছে)", fontWeight = if (mobileCourtUndertakingSubmitted) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (mobileCourtUndertakingSubmitted) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mc_undertaking_yes")
                            )
                            FilterChip(
                                selected = !mobileCourtUndertakingSubmitted,
                                onClick = { mobileCourtUndertakingSubmitted = false },
                                label = { Text("না (দাখিল করেনি)", fontWeight = if (!mobileCourtUndertakingSubmitted) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (!mobileCourtUndertakingSubmitted) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mc_undertaking_no")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ১২. মন্তব্য
                        Text(
                            text = "১২. মন্তব্য (Remarks)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mobileCourtRemarks,
                            onValueChange = { mobileCourtRemarks = it },
                            label = { Text("মোবাইল কোর্ট সংক্রান্ত অতিরিক্ত মন্তব্য (ঐচ্ছিক)") },
                            placeholder = { Text("মামলা নং, জব্দকৃত মালামাল বা অন্যান্য নির্দেশনা লিখুন...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mc_remarks_input")
                        )
                    }
                } else {
                    // Owner, Address, Mouza, Thana & Mobile Details
                    FormSectionCard(title = "১–৪. ভবন মালিক, ঠিকানা, মৌজা ও থানার তথ্য (Owner, Address, Mouza & Thana)") {
                        // Field: Building Owner Name
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("১. ভবন মালিকের নাম (Building Owner Name) *") },
                            placeholder = { Text("যেমন: জনাব আব্দুল করিম") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Field: Building Address
                        OutlinedTextField(
                            value = buildingAddress,
                            onValueChange = { buildingAddress = it },
                            label = { Text("২. ভবনের ঠিকানা (Building Address) *") },
                            placeholder = { Text("হোল্ডিং নং, রোড, ব্লক/সেক্টর, এলাকা") },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("building_address_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Field: Mouza Name & Thana Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = mouzaName,
                                onValueChange = { mouzaName = it },
                                label = { Text("মৌজার নাম (Mouza) *") },
                                placeholder = { Text("মৌজার নাম লিখুন") },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mouza_name_input")
                            )

                            OutlinedTextField(
                                value = thanaName,
                                onValueChange = { thanaName = it },
                                label = { Text("থানার নাম (Thana) *") },
                                placeholder = { Text("থানার নাম লিখুন") },
                                leadingIcon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("thana_name_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Field: Owner Mobile Number
                        OutlinedTextField(
                            value = ownerMobile,
                            onValueChange = { ownerMobile = it },
                            label = { Text("মালিকের মোবাইল নাম্বার (Owner Phone Number) *") },
                            placeholder = { Text("017XXXXXXXX") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_mobile_input")
                        )
                    }

                    // Field 5: Building Type — ২ প্রকার: নির্মিত ও নির্মাণাধীন
                    FormSectionCard(title = "৫. ভবনের ধরণ (২ প্রকার: নির্মিত ও নির্মাণাধীন) *") {
                        Text(
                            text = "নিচের ২টি অপশন (নির্মিত / নির্মাণাধীন) থেকে যেকোনো ১টি সিলেক্ট করুন। যে অপশন সিলেক্ট করবেন বাকি ৬–৯ এর ডাটা ওই অপশন অনুযায়ী সেভ হবে:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InspectionPresets.buildingTypes.forEachIndexed { idx, optionType ->
                                val isSelected = buildingType == optionType
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { buildingType = optionType }
                                        .testTag("building_type_option_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { buildingType = optionType }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${InspectionPresets.toBanglaDigits(idx + 1)}. $optionType",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Text(
                                                text = if (optionType == InspectionPresets.BUILDING_TYPE_CONSTRUCTED) {
                                                    "নির্মিত ভবন (৬–৯ তথ্য)"
                                                } else {
                                                    "নির্মাণাধীন ভবন (৬–৯ তথ্য)"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "নির্বাচিত",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_building_type_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "নির্বাচিত ভবনের ধরণ: $buildingType — বাকি ৬ থেকে ৯ নং ঘরের ডাটা '$buildingType' অপশন অনুযায়ী সেভ হবে",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Field 6 & Field 7: Previous Inspection Count & Authorized Officer Presence (bound to selected buildingType)
                    FormSectionCard(title = "৬–৭. পূর্ববর্তী পরিদর্শন ও অথরাইজড অফিসার ($buildingType ভবন)") {
                        Text(
                            text = "৬. আগে কতবার পরিদর্শন করা হয়েছে ($buildingType) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val next = (activePrevCount - 1L).coerceAtLeast(0L)
                                    updateActivePrevCount(next, next.toString())
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("decrement_prev_inspection_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "কমান")
                            }

                            OutlinedTextField(
                                value = activePrevCountText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(4)
                                    val parsed = filtered.toLongOrNull() ?: 0L
                                    updateActivePrevCount(parsed, filtered)
                                },
                                label = { Text("পরিদর্শন সংখ্যা (বার) — $buildingType") },
                                leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prev_inspection_count_input")
                            )

                            IconButton(
                                onClick = {
                                    val next = (activePrevCount + 1L).coerceAtMost(1000L)
                                    updateActivePrevCount(next, next.toString())
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("increment_prev_inspection_button")
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "বাড়ান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0L, 1L, 2L, 3L, 4L, 5L).forEach { count ->
                                FilterChip(
                                    selected = activePrevCount == count,
                                    onClick = {
                                        updateActivePrevCount(count, count.toString())
                                    },
                                    label = { Text("${InspectionPresets.toBanglaDigits(count)} বার") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Field 7: Authorized Officer Presence with Material 3 Switch
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (activeAuthOfficer) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (activeAuthOfficer) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { updateActiveAuthOfficer(!activeAuthOfficer) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeAuthOfficer) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.VerifiedUser,
                                            contentDescription = null,
                                            tint = if (activeAuthOfficer) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "৭. অথরাইজড অফিসার পরিদর্শন করেছে ? ($buildingType)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (activeAuthOfficer) {
                                                "হ্যাঁ — অথরাইজড অফিসার পরিদর্শন করেছেন (Yes)"
                                            } else {
                                                "না — এখনো পরিদর্শন করেননি (No)"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (activeAuthOfficer) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                Switch(
                                    checked = activeAuthOfficer,
                                    onCheckedChange = { updateActiveAuthOfficer(it) },
                                    thumbContent = if (activeAuthOfficer) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier
                                        .semantics {
                                            contentDescription = "অথরাইজড অফিসার পরিদর্শন করেছে সুইচ"
                                        }
                                        .testTag("authorized_officer_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Yes/No buttons in addition to the Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = activeAuthOfficer,
                                onClick = { updateActiveAuthOfficer(true) },
                                label = { Text("হ্যাঁ (Yes)") },
                                leadingIcon = if (activeAuthOfficer) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("authorized_officer_yes_button")
                            )
                            FilterChip(
                                selected = !activeAuthOfficer,
                                onClick = { updateActiveAuthOfficer(false) },
                                label = { Text("না (No)") },
                                leadingIcon = if (!activeAuthOfficer) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("authorized_officer_no_button")
                            )
                        }
                    }

                    // Field 8: Violation / Deviation Type (bound to selected buildingType)
                    val currentDeviationPresets = InspectionPresets.deviationTypesFor(buildingType)
                    FormSectionCard(title = "৮. ব্যত্যয়কৃত ভবনের ধরণ ($buildingType ভবন) *") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currentDeviationPresets.forEach { preset ->
                                val selected = activeDeviationType == preset
                                FilterChip(
                                    selected = selected,
                                    onClick = { updateActiveDeviationType(preset) },
                                    label = { Text(preset) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = deviationTypeExpanded,
                            onExpandedChange = { deviationTypeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = activeDeviationType,
                                onValueChange = { updateActiveDeviationType(it) },
                                label = { Text("৮. ব্যত্যয়কৃত ভবনের ধরণ ($buildingType) *") },
                                leadingIcon = { Icon(Icons.Filled.WarningAmber, contentDescription = null) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviationTypeExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .fillMaxWidth()
                                    .testTag("deviation_type_input")
                            )
                            ExposedDropdownMenu(
                                expanded = deviationTypeExpanded,
                                onDismissRequest = { deviationTypeExpanded = false }
                            ) {
                                currentDeviationPresets.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            updateActiveDeviationType(option)
                                            deviationTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Field 9: Action Taken & Remarks (bound to selected buildingType)
                    val currentActionPresets = InspectionPresets.actionTakenTypesFor(buildingType)
                    FormSectionCard(title = "৯. গৃহীত ব্যবস্থা ও মন্তব্য ($buildingType ভবন) *") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currentActionPresets.forEach { preset ->
                                val selected = activeActionTaken == preset
                                FilterChip(
                                    selected = selected,
                                    onClick = { updateActiveActionTaken(preset) },
                                    label = { Text(preset) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = actionTakenExpanded,
                            onExpandedChange = { actionTakenExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = activeActionTaken,
                                onValueChange = { updateActiveActionTaken(it) },
                                label = { Text("৯. গৃহীত ব্যবস্থা ($buildingType) *") },
                                leadingIcon = { Icon(Icons.Filled.Gavel, contentDescription = null) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionTakenExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .fillMaxWidth()
                                    .testTag("action_taken_input")
                            )
                            ExposedDropdownMenu(
                                expanded = actionTakenExpanded,
                                onDismissRequest = { actionTakenExpanded = false }
                            ) {
                                currentActionPresets.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            updateActiveActionTaken(option)
                                            actionTakenExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = activeBuildingRemarks,
                            onValueChange = { updateActiveBuildingRemarks(it) },
                            label = { Text("মন্তব্য / নথি বা স্মারক নং ($buildingType - ঐচ্ছিক)") },
                            placeholder = { Text("যেমন: নোটিশ স্মারক নং-২৫.৩৯... বা তলার বিবরণ") },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("remarks_input")
                        )
                    }
                }

                // Bottom Save / Cancel Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showTopBar) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("cancel_inspection_button")
                        ) {
                            Text("বাতিল করুন")
                        }
                    }

                    Button(
                        onClick = {
                            val cleanMonth = reportMonth.trim()
                            val resolvedCategory = reportCategory.trim().ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" }
                            if (InspectionPresets.isUnresolvedCategory(resolvedCategory)) {
                                when {
                                    unresolvedDurationCategory.isBlank() -> {
                                        validationError = "৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন-এর ৩টি অপশনের যেকোনো ১টি সিলেক্ট করুন"
                                    }
                                    else -> {
                                        validationError = null
                                        val finalMonth = if (cleanMonth.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
                                            cleanMonth
                                        } else {
                                            defaultMonth.trim().takeIf { it.matches(Regex("^[0-9]{4}-[0-9]{2}$")) }
                                                ?: todayStr.substring(0, 7)
                                        }
                                        val finalDate = inspectionDate.trim().ifBlank { todayStr }
                                        val finalInspector = inspectorName.trim()
                                            .ifBlank { defaultInspectorName.trim() }
                                            .ifBlank { "ইমারত পরিদর্শক" }
                                        val finalDuration = unresolvedDurationCategory.trim()
                                        val monthBangla = InspectionPresets.formatReportMonthBangla(finalMonth)
                                        onSave(
                                            InspectionFormData(
                                                reportCategory = resolvedCategory,
                                                reportMonth = finalMonth,
                                                inspectionDate = finalDate,
                                                inspectorName = finalInspector,
                                                ownerName = "অনিষ্পন্ন রিপোর্ট ($monthBangla)",
                                                buildingAddress = "গত মাসের জের: ${InspectionPresets.toBanglaDigits(previousMonthBalance)}, আলোচ্য মাসের প্রাপ্তি: ${InspectionPresets.toBanglaDigits(currentMonthReceived)}, মোট প্রাপ্তি: ${InspectionPresets.toBanglaDigits(totalReceived)}",
                                                mouzaName = "",
                                                thanaName = "",
                                                ownerMobile = "-",
                                                buildingType = "অনিষ্পন্ন রিপোর্ট",
                                                previousInspectionCount = 0L,
                                                authorizedOfficerInspected = false,
                                                deviationType = finalDuration,
                                                actionTaken = "মাসের শেষ কার্যদিবসে অনিষ্পন্ন: $finalDuration",
                                                previousMonthBalance = previousMonthBalance,
                                                currentMonthReceived = currentMonthReceived,
                                                totalReceived = totalReceived,
                                                unresolvedAtMonthEnd = unresolvedAtMonthEnd,
                                                unresolvedDurationCategory = finalDuration,
                                                remarks = unresolvedRemarks
                                            )
                                        )
                                    }
                                }
                            } else if (InspectionPresets.isDesignApprovalCategory(resolvedCategory)) {
                                val finalMonth = if (cleanMonth.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
                                    cleanMonth
                                } else {
                                    defaultMonth.trim().takeIf { it.matches(Regex("^[0-9]{4}-[0-9]{2}$")) }
                                        ?: todayStr.substring(0, 7)
                                }
                                val finalDate = inspectionDate.trim().ifBlank { todayStr }
                                val finalInspector = inspectorName.trim()
                                    .ifBlank { defaultInspectorName.trim() }
                                    .ifBlank { "ইমারত পরিদর্শক" }
                                val monthBangla = InspectionPresets.formatReportMonthBangla(finalMonth)
                                validationError = null
                                onSave(
                                    InspectionFormData(
                                        reportCategory = resolvedCategory,
                                        reportMonth = finalMonth,
                                        inspectionDate = finalDate,
                                        inspectorName = finalInspector,
                                        ownerName = "নকশা অনুমোদন ($monthBangla)",
                                        buildingAddress = "Submitted: ${InspectionPresets.toBanglaDigits(planSubmitted)}, Approved: ${InspectionPresets.toBanglaDigits(planApproved)}, Rejected: ${InspectionPresets.toBanglaDigits(planRejected)}, Completed: ${InspectionPresets.toBanglaDigits(planCompleted)}",
                                        mouzaName = "",
                                        thanaName = "",
                                        ownerMobile = "-",
                                        buildingType = "নকশা অনুমোদন",
                                        previousInspectionCount = 0L,
                                        authorizedOfficerInspected = false,
                                        deviationType = "Completed: ${InspectionPresets.toBanglaDigits(planCompleted)}",
                                        actionTaken = "WIP: ${InspectionPresets.toBanglaDigits(planWorkInProgress)}",
                                        planSubmitted = planSubmitted,
                                        planApproved = planApproved,
                                        planRejected = planRejected,
                                        planCompleted = planCompleted,
                                        planWorkInProgress = planWorkInProgress,
                                        planSentForFeedback = planSentForFeedback,
                                        planCompletedWithin45Days = planCompletedWithin45Days,
                                        planCompletedMoreThan45Days = planCompletedMoreThan45Days,
                                    )
                                )
                            } else if (InspectionPresets.isOccupancyCertificateCategory(resolvedCategory)) {
                                val finalMonth = if (cleanMonth.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
                                    cleanMonth
                                } else {
                                    defaultMonth.trim().takeIf { it.matches(Regex("^[0-9]{4}-[0-9]{2}$")) }
                                        ?: todayStr.substring(0, 7)
                                }
                                val finalDate = inspectionDate.trim().ifBlank { todayStr }
                                val finalInspector = inspectorName.trim()
                                    .ifBlank { defaultInspectorName.trim() }
                                    .ifBlank { "ইমারত পরিদর্শক" }
                                val monthBangla = InspectionPresets.formatReportMonthBangla(finalMonth)
                                validationError = null
                                onSave(
                                    InspectionFormData(
                                        reportCategory = "অকুপেন্সী সনদ",
                                        reportMonth = finalMonth,
                                        inspectionDate = finalDate,
                                        inspectorName = finalInspector,
                                        ownerName = "অকুপেন্সী সনদ ($monthBangla)",
                                        buildingAddress = "জের: ${InspectionPresets.toBanglaDigits(occupancyPreviousMonthBalance)}, প্রাপ্তি: ${InspectionPresets.toBanglaDigits(occupancyCurrentMonthReceived)}, মোট: ${InspectionPresets.toBanglaDigits(occupancyTotalApplications)}",
                                        mouzaName = "",
                                        thanaName = "",
                                        ownerMobile = "-",
                                        buildingType = "অকুপেন্সী সনদ",
                                        previousInspectionCount = occupancyInspectedBuildings,
                                        authorizedOfficerInspected = false,
                                        deviationType = "সনদ প্রদান: ${InspectionPresets.toBanglaDigits(occupancyIssuedCount)}",
                                        actionTaken = "সনদ অপ্রদান: ${InspectionPresets.toBanglaDigits(occupancyNotIssuedCount)}",
                                        previousMonthBalance = occupancyPreviousMonthBalance,
                                        currentMonthReceived = occupancyCurrentMonthReceived,
                                        totalReceived = occupancyTotalApplications,
                                        occupancyPreviousMonthBalance = occupancyPreviousMonthBalance,
                                        occupancyCurrentMonthReceived = occupancyCurrentMonthReceived,
                                        occupancyTotalApplications = occupancyTotalApplications,
                                        occupancyInspectedBuildings = occupancyInspectedBuildings,
                                        occupancyIssuedCount = occupancyIssuedCount,
                                        occupancyNotIssuedCount = occupancyNotIssuedCount,
                                        occupancyNotIssuedReason = occupancyNotIssuedReason,
                                        remarks = occupancyRemarks
                                    )
                                )
                            } else if (InspectionPresets.isMobileCourtCategory(resolvedCategory)) {
                                val cleanDate = mobileCourtDate.trim().ifBlank { todayStr }
                                val finalMonth = if (cleanDate.length >= 7 && cleanDate.matches(Regex("^[0-9]{4}-[0-9]{2}.*"))) {
                                    cleanDate.substring(0, 7)
                                } else if (cleanMonth.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
                                    cleanMonth
                                } else {
                                    defaultMonth.trim().takeIf { it.matches(Regex("^[0-9]{4}-[0-9]{2}$")) }
                                        ?: todayStr.substring(0, 7)
                                }
                                val finalInspector = inspectorName.trim()
                                    .ifBlank { defaultInspectorName.trim() }
                                    .ifBlank { "ইমারত পরিদর্শক" }
                                val finalMagistrate = magistrateName.trim()
                                val finalOwner = mobileCourtOwnerName.trim()
                                val finalAddress = mobileCourtBuildingAddress.trim()
                                when {
                                    finalMagistrate.isBlank() -> {
                                        validationError = "ম্যাজিস্ট্রেট এর নাম লিখুন"
                                    }
                                    finalOwner.isBlank() -> {
                                        validationError = "ভবন মালিকের নাম লিখুন"
                                    }
                                    finalAddress.isBlank() -> {
                                        validationError = "ভবনের ঠিকানা লিখুন"
                                    }
                                    else -> {
                                        validationError = null
                                        onSave(
                                            InspectionFormData(
                                                reportCategory = "মোবাইল কোর্টের তথ্য",
                                                reportMonth = finalMonth,
                                                inspectionDate = cleanDate,
                                                inspectorName = finalInspector,
                                                ownerName = finalOwner,
                                                buildingAddress = finalAddress,
                                                mouzaName = "",
                                                thanaName = "",
                                                ownerMobile = mobileCourtOwnerMobile.trim(),
                                                buildingType = mobileCourtBuildingType,
                                                previousInspectionCount = 0L,
                                                authorizedOfficerInspected = false,
                                                deviationType = mobileCourtDeviationType.trim().ifBlank { "ব্যত্যয় চিহ্নিত" },
                                                actionTaken = "জরিমানা: ৳${InspectionPresets.toBanglaDigits(mobileCourtFineAmount)}${if (mobileCourtMeterCut) ", মিটার কর্তন: ${InspectionPresets.toBanglaDigits(mobileCourtMeterCount)}টি" else ""}${if (mobileCourtUndertakingSubmitted) ", অঙ্গীকারনামা দাখিল" else ""}",
                                                mobileCourtDate = cleanDate,
                                                magistrateName = finalMagistrate,
                                                mobileCourtFineAmount = mobileCourtFineAmount,
                                                mobileCourtMeterCut = mobileCourtMeterCut,
                                                mobileCourtMeterCount = if (mobileCourtMeterCut) mobileCourtMeterCount else 0L,
                                                mobileCourtUndertakingSubmitted = mobileCourtUndertakingSubmitted,
                                                remarks = mobileCourtRemarks.trim()
                                            )
                                        )
                                    }
                                }
                            } else {
                                val finalInspector = inspectorName.trim()
                                    .ifBlank { defaultInspectorName.trim() }
                                    .ifBlank { "ইমারত পরিদর্শক" }
                                val selectedOptionType = if (buildingType.trim() == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION) {
                                    InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION
                                } else {
                                    InspectionPresets.BUILDING_TYPE_CONSTRUCTED
                                }
                                when {
                                    inspectionDate.isBlank() -> {
                                        validationError = "ভবন পরিদর্শনের তারিখ নির্বাচন করুন বা লিখুন"
                                    }
                                    !cleanMonth.matches(Regex("^[0-9]{4}-[0-9]{2}$")) -> {
                                        validationError = "অনুগ্রহ করে সঠিক মাস ফরম্যাট দিন (যেমন: 2026-10)"
                                    }
                                    ownerName.isBlank() -> {
                                        validationError = "ভবন মালিকের নাম লিখুন"
                                    }
                                    buildingAddress.isBlank() -> {
                                        validationError = "ভবনের ঠিকানা লিখুন"
                                    }
                                    mouzaName.isBlank() -> {
                                        validationError = "মৌজার নাম লিখুন"
                                    }
                                    thanaName.isBlank() -> {
                                        validationError = "থানার নাম লিখুন"
                                    }
                                    ownerMobile.isBlank() -> {
                                        validationError = "মালিকের মোবাইল নাম্বার লিখুন"
                                    }
                                    selectedOptionType.isBlank() -> {
                                        validationError = "৫. ভবনের ধরণ (নির্মিত বা নির্মাণাধীন) নির্বাচন করুন"
                                    }
                                    activeDeviationType.isBlank() -> {
                                        validationError = "৮. ব্যত্যয়কৃত ভবনের ধরণ ($selectedOptionType) উল্লেখ করুন"
                                    }
                                    activeActionTaken.isBlank() -> {
                                        validationError = "৯. গৃহীত ব্যবস্থা ($selectedOptionType) উল্লেখ করুন"
                                    }
                                    else -> {
                                        validationError = null
                                        onSave(
                                            InspectionFormData(
                                                reportCategory = resolvedCategory,
                                                reportMonth = cleanMonth,
                                                inspectionDate = inspectionDate.trim().ifBlank { "$cleanMonth-01" },
                                                inspectorName = finalInspector,
                                                ownerName = ownerName,
                                                buildingAddress = buildingAddress,
                                                mouzaName = mouzaName,
                                                thanaName = thanaName,
                                                ownerMobile = ownerMobile,
                                                buildingType = selectedOptionType,
                                                previousInspectionCount = activePrevCount,
                                                authorizedOfficerInspected = activeAuthOfficer,
                                                deviationType = activeDeviationType,
                                                actionTaken = activeActionTaken,
                                                remarks = activeBuildingRemarks
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp)
                            .testTag("save_inspection_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "রিপোর্টে সংরক্ষণ করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                            inspectionDate = formattedDate
                            reportMonth = formattedDate.substring(0, 7)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("নির্বাচন করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
        }
    }

    if (showMobileCourtDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showMobileCourtDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                            mobileCourtDate = formattedDate
                            if (reportMonth.isBlank()) {
                                reportMonth = formattedDate.substring(0, 7)
                            }
                        }
                        showMobileCourtDatePickerDialog = false
                    }
                ) {
                    Text("নির্বাচন করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMobileCourtDatePickerDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DesignApprovalCounterRow(
    label: String,
    valueText: String,
    currentValue: Long,
    testTagPrefix: String,
    onValueChange: (Long, String) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                val next = (currentValue - 1L).coerceAtLeast(0L)
                onValueChange(next, next.toString())
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("decrement_${testTagPrefix}_button")
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "কমান")
        }

        OutlinedTextField(
            value = valueText,
            onValueChange = { text ->
                val filtered = text.filter { it.isDigit() }.take(6)
                val parsed = filtered.toLongOrNull() ?: 0L
                onValueChange(parsed, filtered)
            },
            label = { Text("সংখ্যা (টি)") },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("${testTagPrefix}_input")
        )

        IconButton(
            onClick = {
                val next = (currentValue + 1L).coerceAtMost(1_000_000L)
                onValueChange(next, next.toString())
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .testTag("increment_${testTagPrefix}_button")
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "বাড়ান",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
