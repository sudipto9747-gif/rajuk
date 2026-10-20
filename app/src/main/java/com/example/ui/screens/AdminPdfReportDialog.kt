package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import com.example.util.ReportColumn
import com.example.util.ReportExportUtil

/**
 * এডমিন আইডিতে ২ ধরনের পিডিএফ রিপোর্ট জেনারেটর:
 * ১. হেড অফিসের রিপোর্ট (Head Office Report) - সম্পূর্ণ অফিশিয়াল ফরম্যাট
 * ২. কাস্টমাইজড রিপোর্ট (Customized Report) - এডমিন নিজের পছন্দমত ক্যাটাগরি ও কলাম বেছে নিয়ে রিপোর্ট জেনারেট করবে
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPdfReportDialog(
    isOpen: Boolean,
    reportMonth: String,
    allReports: List<InspectionReport>,
    profile: InspectorProfile?,
    stats: MonthlySummaryStats,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)

    var selectedReportType by rememberSaveable { mutableIntStateOf(0) } // 0: Head Office, 1: Customized
    var headOfficeZoneName by rememberSaveable { mutableStateOf("") }
    var headOfficeSubZoneName by rememberSaveable { mutableStateOf("") }
    var headOfficeValidationError by remember { mutableStateOf<String?>(null) }

    var customTitle by rememberSaveable { mutableStateOf("ইমারত পরিদর্শনের কাস্টমাইজড প্রতিবেদন") }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf("সকল ক্যাটাগরি") }

    var selectedColumns by remember {
        mutableStateOf(
            ReportColumn.entries.filter { it.defaultSelected }.toSet()
        )
    }

    val zonePresetOptions = remember {
        listOf("জোন-১", "জোন-২", "জোন-৩", "জোন-৪", "জোন-৫", "জোন-৬", "জোন-৭", "জোন-৮")
    }

    val subZonePresetOptions = remember {
        listOf("সাবজোন-১", "সাবজোন-২", "সাবজোন-৩/১", "সাবজোন-৩/২", "সাবজোন-৪/১", "সাবজোন-৪/২", "সাবজোন-৫/১")
    }

    val categoryOptions = remember {
        listOf(
            "সকল ক্যাটাগরি",
            "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
            "অনিষ্পন্ন রিপোর্ট",
            "নকশা অনুমোদন",
            "অকুপেন্সী সনদ",
            "মোবাইল কোর্টের তথ্য"
        )
    }

    val filteredReports = remember(allReports, selectedCategoryFilter) {
        if (selectedCategoryFilter == "সকল ক্যাটাগরি") {
            allReports
        } else {
            allReports.filter { it.reportCategory.trim() == selectedCategoryFilter.trim() }
        }
    }

    // Category metrics
    val mobileCourtReports = allReports.filter { InspectionPresets.isMobileCourtCategory(it.reportCategory) }
    val totalFine = mobileCourtReports.sumOf { it.mobileCourtFineAmount }
    val occupancyReports = allReports.filter { InspectionPresets.isOccupancyCertificateCategory(it.reportCategory) }
    val occupancyIssued = occupancyReports.sumOf { it.occupancyIssuedCount }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("admin_pdf_generator_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Dialog Header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "পিডিএফ রিপোর্ট জেনারেটর",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "প্রতিবেদনের মাস: $monthBangla | মোট রেকর্ড: ${InspectionPresets.toBanglaDigits(allReports.size)} টি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("pdf_dialog_close_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "বন্ধ করুন")
                        }
                    }
                }

                // 2. Tabs: Head Office vs Customized Report
                TabRow(
                    selectedTabIndex = selectedReportType,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedReportType == 0,
                        onClick = { selectedReportType = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("১. হেড অফিস রিপোর্ট", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_head_office_report")
                    )
                    Tab(
                        selected = selectedReportType == 1,
                        onClick = { selectedReportType = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("২. কাস্টমাইজড রিপোর্ট", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_customized_report")
                    )
                }

                // 3. Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedReportType == 0) {
                        // TAB 0: HEAD OFFICE REPORT
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "অফিসিয়াল রাজউক স্ট্যান্ডার্ড",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = "প্রধান কার্যালয়ের জন্য পূর্ণাঙ্গ প্রতিবেদন",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "হেড অফিস রিপোর্টের জন্য নিচে জোনের নাম ও সাবজোনের নাম ইনপুট দিন। রাজউকের মাসিক সমন্বয় সভার নির্ধারিত ফরম্যাটে ৫টি ছকের (নিষ্পত্তি সংক্রান্ত, নির্মাণাধীন ভবন পরিদর্শন, নকশা অনুমোদন, ব্যবহার সনদ ও মোবাইল কোর্ট) পিডিএফ তৈরি হবে এবং সরাসরি WhatsApp-এ শেয়ার করা যাবে।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Zone Name & Sub-zone Name Required Input Section
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "জোন ও সাবজোনের তথ্য ইনপুট দিন (আবশ্যক):",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // Zone Input
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = headOfficeZoneName,
                                                onValueChange = {
                                                    headOfficeZoneName = it
                                                    headOfficeValidationError = null
                                                },
                                                label = { Text("জোনের নাম (Zone Name) *") },
                                                placeholder = { Text("যেমন: জোন-৩ অথবা জোন-৫") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("head_office_zone_input")
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                zonePresetOptions.forEach { z ->
                                                    FilterChip(
                                                        selected = headOfficeZoneName == z,
                                                        onClick = {
                                                            headOfficeZoneName = z
                                                            headOfficeValidationError = null
                                                        },
                                                        label = { Text(z, style = MaterialTheme.typography.labelSmall) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Sub-zone Input
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = headOfficeSubZoneName,
                                                onValueChange = {
                                                    headOfficeSubZoneName = it
                                                    headOfficeValidationError = null
                                                },
                                                label = { Text("সাবজোনের নাম (Sub-zone Name) *") },
                                                placeholder = { Text("যেমন: সাবজোন-৩/১ অথবা সাবজোন-৪/২") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("head_office_subzone_input")
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                subZonePresetOptions.forEach { sz ->
                                                    FilterChip(
                                                        selected = headOfficeSubZoneName == sz,
                                                        onClick = {
                                                            headOfficeSubZoneName = sz
                                                            headOfficeValidationError = null
                                                        },
                                                        label = { Text(sz, style = MaterialTheme.typography.labelSmall) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (headOfficeValidationError != null) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    text = headOfficeValidationError!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "এই রিপোর্টে অন্তর্ভুক্ত ৫টি ক্যাটাগরির সংক্ষিপ্ত ডাটা:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ReportSummaryRow(
                                        title = "১. ভবন পরিদর্শন সম্পর্কিত তথ্য",
                                        detail = "মোট রেকর্ড: ${InspectionPresets.toBanglaDigits(stats.totalInspections)} টি | নির্মিত: ${InspectionPresets.toBanglaDigits(stats.constructedBuildingCount)} | নির্মাণাধীন: ${InspectionPresets.toBanglaDigits(stats.underConstructionBuildingCount)} | ব্যত্যয়: ${InspectionPresets.toBanglaDigits(stats.deviatedBuildingCount)} টি",
                                        badgeColor = MaterialTheme.colorScheme.primary
                                    )
                                    ReportSummaryRow(
                                        title = "২. অনিষ্পন্ন রিপোর্ট",
                                        detail = "পূর্ববর্তী জের, বর্তমান প্রাপ্তি এবং মাসান্তে অনিষ্পন্ন সংখ্যা ও সময়সীমা",
                                        badgeColor = MaterialTheme.colorScheme.error
                                    )
                                    ReportSummaryRow(
                                        title = "৩. নকশা অনুমোদন",
                                        detail = "দাখিলকৃত, অনুমোদিত, প্রত্যাখ্যাত এবং ৪৫ দিন ও তদূর্ধ্ব সময়ে নিষ্পন্ন",
                                        badgeColor = MaterialTheme.colorScheme.tertiary
                                    )
                                    ReportSummaryRow(
                                        title = "৪. অকুপেন্সী সনদ",
                                        detail = "জেরসহ মোট আবেদন, সনদ প্রদান (${InspectionPresets.toBanglaDigits(occupancyIssued)} টি) ও অপ্রদানের কারণ",
                                        badgeColor = Color(0xFF00796B)
                                    )
                                    ReportSummaryRow(
                                        title = "৫. মোবাইল কোর্টের তথ্য",
                                        detail = "মোট জরিমানা: ৳ ${InspectionPresets.toBanglaDigits(totalFine)} | মিটার কর্তন ও অঙ্গীকার নামা",
                                        badgeColor = Color(0xFFC2185B)
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            if (headOfficeZoneName.isBlank() || headOfficeSubZoneName.isBlank()) {
                                                headOfficeValidationError = "অনুগ্রহ করে জোনের নাম এবং সাবজোনের নাম ইনপুট দিন।"
                                            } else {
                                                headOfficeValidationError = null
                                                ReportExportUtil.printOrSaveHeadOfficePdfReport(
                                                    context = context,
                                                    reportMonth = reportMonth,
                                                    reports = allReports,
                                                    profile = profile,
                                                    stats = stats,
                                                    zoneName = headOfficeZoneName.trim(),
                                                    subZoneName = headOfficeSubZoneName.trim()
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_generate_head_office_pdf"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "হেড অফিস রিপোর্ট প্রিন্ট / পিডিএফ সংরক্ষণ করুন",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (headOfficeZoneName.isBlank() || headOfficeSubZoneName.isBlank()) {
                                                headOfficeValidationError = "অনুগ্রহ করে জোনের নাম এবং সাবজোনের নাম ইনপুট দিন।"
                                            } else {
                                                headOfficeValidationError = null
                                                ReportExportUtil.shareHeadOfficePdfOnWhatsApp(
                                                    context = context,
                                                    reportMonth = reportMonth,
                                                    reports = allReports,
                                                    profile = profile,
                                                    stats = stats,
                                                    zoneName = headOfficeZoneName.trim(),
                                                    subZoneName = headOfficeSubZoneName.trim()
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_whatsapp_share_head_office_pdf"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF128C7E),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "সরাসরি WhatsApp-এ পিডিএফ শেয়ার করুন",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 1: CUSTOMIZED REPORT
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = customTitle,
                                    onValueChange = { customTitle = it },
                                    label = { Text("রিপোর্টের শিরোনাম (ঐচ্ছিক)") },
                                    placeholder = { Text("যেমন: ব্যত্যয়কৃত ভবনের তালিকা / মোবাইল কোর্ট বিবরণী") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_report_title_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Category Filter
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "ক্যাটাগরি ফিল্টার (কোন ক্যাটাগরির ডাটা চান?):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        categoryOptions.forEach { cat ->
                                            val isSelected = selectedCategoryFilter == cat
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedCategoryFilter = cat },
                                                label = {
                                                    val count = if (cat == "সকল ক্যাটাগরি") {
                                                        allReports.size
                                                    } else {
                                                        allReports.count { it.reportCategory.trim() == cat.trim() }
                                                    }
                                                    Text("$cat (${InspectionPresets.toBanglaDigits(count)})")
                                                },
                                                leadingIcon = if (isSelected) {
                                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                } else null,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Presets for quick column selection
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "কলাম সিলেকশন প্রিসেট (Quick Presets):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedColumns = ReportColumn.entries.toSet() },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("সব কলাম (${ReportColumn.entries.size})", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                selectedColumns = setOf(
                                                    ReportColumn.SL,
                                                    ReportColumn.INSPECTION_DATE,
                                                    ReportColumn.OWNER_NAME,
                                                    ReportColumn.BUILDING_ADDRESS,
                                                    ReportColumn.OWNER_MOBILE,
                                                    ReportColumn.BUILDING_TYPE
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("মৌলিক তথ্য", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                selectedColumns = setOf(
                                                    ReportColumn.SL,
                                                    ReportColumn.INSPECTION_DATE,
                                                    ReportColumn.OWNER_NAME,
                                                    ReportColumn.BUILDING_ADDRESS,
                                                    ReportColumn.DEVIATION_TYPE,
                                                    ReportColumn.ACTION_TAKEN,
                                                    ReportColumn.REMARKS
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("ব্যত্যয় ও ব্যবস্থা", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                selectedCategoryFilter = "মোবাইল কোর্টের তথ্য"
                                                selectedColumns = setOf(
                                                    ReportColumn.SL,
                                                    ReportColumn.INSPECTION_DATE,
                                                    ReportColumn.OWNER_NAME,
                                                    ReportColumn.BUILDING_ADDRESS,
                                                    ReportColumn.OWNER_MOBILE,
                                                    ReportColumn.MOBILE_COURT,
                                                    ReportColumn.REMARKS
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("মোবাইল কোর্ট", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                selectedCategoryFilter = "অকুপেন্সী সনদ"
                                                selectedColumns = setOf(
                                                    ReportColumn.SL,
                                                    ReportColumn.OWNER_NAME,
                                                    ReportColumn.BUILDING_ADDRESS,
                                                    ReportColumn.OCCUPANCY_CERT,
                                                    ReportColumn.REMARKS
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("অকুপেন্সী সনদ", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            // Columns Checkboxes List
                            item {
                                Text(
                                    text = "প্রতিবেদনে যে যে কলাম অন্তর্ভুক্ত করতে চান তা টিক দিন (${InspectionPresets.toBanglaDigits(selectedColumns.size)}টি নির্বাচিত):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (col in ReportColumn.entries) {
                                        val isChecked = col in selectedColumns
                                        ColumnSelectionChip(
                                            col = col,
                                            isChecked = isChecked,
                                            onToggle = {
                                                selectedColumns = if (isChecked) {
                                                    if (selectedColumns.size > 1) selectedColumns - col else selectedColumns
                                                } else {
                                                    selectedColumns + col
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Live Summary Badge
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "নির্বাচিত কলাম: ${InspectionPresets.toBanglaDigits(selectedColumns.size)} টি",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "অন্তর্ভুক্ত রেকর্ড: ${InspectionPresets.toBanglaDigits(filteredReports.size)} টি",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            ReportExportUtil.printOrSaveCustomizedPdfReport(
                                                context = context,
                                                reportMonth = reportMonth,
                                                reports = allReports,
                                                profile = profile,
                                                customTitle = customTitle,
                                                selectedCategory = selectedCategoryFilter,
                                                selectedColumns = selectedColumns
                                            )
                                        },
                                        enabled = selectedColumns.isNotEmpty(),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_generate_custom_pdf"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "কাস্টমাইজড রিপোর্ট প্রিন্ট / পিডিএফ সংরক্ষণ করুন",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            ReportExportUtil.shareCustomizedPdfOnWhatsApp(
                                                context = context,
                                                reportMonth = reportMonth,
                                                reports = allReports,
                                                profile = profile,
                                                customTitle = customTitle,
                                                selectedCategory = selectedCategoryFilter,
                                                selectedColumns = selectedColumns
                                            )
                                        },
                                        enabled = selectedColumns.isNotEmpty(),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_whatsapp_share_custom_pdf"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF128C7E),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "সরাসরি WhatsApp-এ কাস্টম পিডিএফ শেয়ার করুন",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(
    title: String,
    detail: String,
    badgeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColumnSelectionChip(
    col: ReportColumn,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isChecked) CardDefaults.outlinedCardBorder() else null,
        modifier = Modifier.clickable(onClick = onToggle)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = col.titleBangla,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
