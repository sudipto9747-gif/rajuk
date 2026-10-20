package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import com.example.util.ReportExportUtil

@Composable
fun MonthlyReportPreviewScreen(
    selectedMonth: String,
    reports: List<InspectionReport>,
    profile: InspectorProfile?,
    stats: MonthlySummaryStats,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val monthBangla = InspectionPresets.formatReportMonthBangla(selectedMonth)
    val inspectorName = profile?.inspectorName?.ifBlank { null }
        ?: reports.firstOrNull()?.inspectorName
        ?: "নির্ধারিত নয়"
    val officeName = profile?.officeName?.ifBlank { null } ?: "ইমারত পরিদর্শন শাখা"
    val designation = profile?.designation?.ifBlank { null } ?: "ইমারত পরিদর্শক"

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 920.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Actions Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "মাসিক প্রতিবেদন জেনারেট ও এক্সপোর্ট ($monthBangla)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "আপনার এন্ট্রিকৃত ${InspectionPresets.toBanglaDigits(reports.size)} টি ভবন পরিদর্শনের তথ্যের ভিত্তিতে অফিসিয়াল A4 ছক প্রিন্ট/PDF সেভ করুন অথবা Excel (CSV) ও টেক্সট আকারে শেয়ার করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ReportExportUtil.printOrSavePdfReport(
                                    context = context,
                                    reportMonth = selectedMonth,
                                    reports = reports,
                                    profile = profile,
                                    stats = stats
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("print_pdf_button")
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF প্রিন্ট / সেভ", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                ReportExportUtil.shareCsvDataAsText(
                                    context = context,
                                    reportMonth = selectedMonth,
                                    reports = reports
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_csv_button")
                        ) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Excel (CSV)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val text = ReportExportUtil.buildFormattedTextReport(
                                    reportMonth = selectedMonth,
                                    reports = reports,
                                    profile = profile,
                                    stats = stats
                                )
                                ReportExportUtil.copyReportToClipboard(context, text)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_report_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("রিপোর্ট কপি করুন")
                        }

                        OutlinedButton(
                            onClick = {
                                val text = ReportExportUtil.buildFormattedTextReport(
                                    reportMonth = selectedMonth,
                                    reports = reports,
                                    profile = profile,
                                    stats = stats
                                )
                                ReportExportUtil.shareTextReport(context, selectedMonth, text)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_text_button")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("টেক্সট শেয়ার")
                        }
                    }
                }
            }

            // Official Report Sheet Preview
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Official Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ইমারত পরিদর্শনের মাসিক প্রতিবেদন",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = officeName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "মাস: $monthBangla",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Inspector Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ইমারত পরিদর্শকের নাম: $inspectorName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "পদবী: $designation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "মোট পরিদর্শন: ${InspectionPresets.toBanglaDigits(stats.totalInspections)} টি (নির্মিত: ${InspectionPresets.toBanglaDigits(stats.constructedBuildingCount)}, নির্মাণাধীন: ${InspectionPresets.toBanglaDigits(stats.underConstructionBuildingCount)})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "অথরাইজড অফিসার পরিদর্শন: ${InspectionPresets.toBanglaDigits(stats.authorizedOfficerVisitedCount)} টি",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (reports.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "$monthBangla মাসে এখনো কোনো ভবন পরিদর্শন তথ্য যুক্ত করা হয়নি।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onAddClick,
                                modifier = Modifier.testTag("empty_report_add_button")
                            ) {
                                Text("প্রথম পরিদর্শন তথ্য যুক্ত করুন")
                            }
                        }
                    } else {
                        val unresolvedReports = reports.filter { it.reportCategory == "অনিষ্পন্ন রিপোর্ট" }
                        if (unresolvedReports.isNotEmpty()) {
                            Text(
                                text = "অনিষ্পন্ন রিপোর্ট ছক (ডানে-বামে স্ক্রল করুন):",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondary)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        TableHeaderCell("ক্রমিক", 56.dp)
                                        TableHeaderCell("১. গত মাসের জের", 125.dp)
                                        TableHeaderCell("২. আলোচ্য মাসের প্রাপ্তি", 145.dp)
                                        TableHeaderCell("৩. মোট প্রাপ্তি", 120.dp)
                                        TableHeaderCell("৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন", 220.dp)
                                        TableHeaderCell("৫. মন্তব্য", 190.dp)
                                    }
                                    unresolvedReports.forEachIndexed { uIdx, uReport ->
                                        val rowBg = if (uIdx % 2 == 0) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                        val resolvedDuration = uReport.unresolvedDurationCategory.ifBlank {
                                            InspectionPresets.unresolvedDurationOptions.first()
                                        }
                                        val unresolvedCellText = if (uReport.unresolvedAtMonthEnd > 0L) {
                                            "$resolvedDuration (${InspectionPresets.toBanglaDigits(uReport.unresolvedAtMonthEnd)} টি)"
                                        } else {
                                            resolvedDuration
                                        }
                                        Row(
                                            modifier = Modifier
                                                .background(rowBg)
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(uIdx + 1),
                                                56.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(uReport.previousMonthBalance),
                                                125.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(uReport.currentMonthReceived),
                                                145.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(uReport.totalReceived),
                                                120.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                unresolvedCellText,
                                                220.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                uReport.remarks.ifBlank { "-" },
                                                190.dp
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val planReports = reports.filter { InspectionPresets.isDesignApprovalCategory(it.reportCategory) }
                        if (planReports.isNotEmpty()) {
                            Text(
                                text = "নকশা অনুমোদন ছক (ডানে-বামে স্ক্রল করুন):",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.tertiary)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        TableHeaderCell("ক্রমিক", 56.dp)
                                        TableHeaderCell("১. Submitted", 110.dp)
                                        TableHeaderCell("২. Approved", 110.dp)
                                        TableHeaderCell("৩. Rejected", 110.dp)
                                        TableHeaderCell("৪. Completed", 150.dp)
                                        TableHeaderCell("৫. WIP", 100.dp)
                                        TableHeaderCell("৬. Sent For Feedback", 140.dp)
                                        TableHeaderCell("৭. ≤ ৪৫ দিন", 110.dp)
                                        TableHeaderCell("৮. > ৪৫ দিন", 110.dp)
                                        TableHeaderCell("মন্তব্য", 160.dp)
                                    }
                                    planReports.forEachIndexed { pIdx, pReport ->
                                        val rowBg = if (pIdx % 2 == 0) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .background(rowBg)
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pIdx + 1),
                                                56.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planSubmitted),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planApproved),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planRejected),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                "${InspectionPresets.toBanglaDigits(pReport.planCompleted)} টি",
                                                150.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planWorkInProgress),
                                                100.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planSentForFeedback),
                                                140.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planCompletedWithin45Days),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(pReport.planCompletedMoreThan45Days),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                pReport.remarks.ifBlank { "-" },
                                                160.dp
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val occupancyReports = reports.filter { InspectionPresets.isOccupancyCertificateCategory(it.reportCategory) }
                        if (occupancyReports.isNotEmpty()) {
                            Text(
                                text = "অকুপেন্সী সনদ ছক (ডানে-বামে স্ক্রল করুন):",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        TableHeaderCell("ক্রমিক", 56.dp)
                                        TableHeaderCell("পূর্ববর্তী জের", 100.dp)
                                        TableHeaderCell("বর্তমান প্রাপ্তি", 110.dp)
                                        TableHeaderCell("জেরসহ মোট আবেদন", 130.dp)
                                        TableHeaderCell("পরিদর্শনকৃত ভবন", 120.dp)
                                        TableHeaderCell("সনদ প্রদান", 100.dp)
                                        TableHeaderCell("সনদ অপ্রদান", 100.dp)
                                        TableHeaderCell("অপ্রদানের কারণ", 160.dp)
                                        TableHeaderCell("মন্তব্য", 140.dp)
                                    }
                                    occupancyReports.forEachIndexed { oIdx, oReport ->
                                        val rowBg = if (oIdx % 2 == 0) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                        val prevBal = if (oReport.occupancyPreviousMonthBalance > 0L) oReport.occupancyPreviousMonthBalance else oReport.previousMonthBalance
                                        val currRec = if (oReport.occupancyCurrentMonthReceived > 0L) oReport.occupancyCurrentMonthReceived else oReport.currentMonthReceived
                                        val totApp = if (oReport.occupancyTotalApplications > 0L) oReport.occupancyTotalApplications else oReport.totalReceived
                                        val inspBld = if (oReport.occupancyInspectedBuildings > 0L) oReport.occupancyInspectedBuildings else oReport.previousInspectionCount

                                        Row(
                                            modifier = Modifier
                                                .background(rowBg)
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(oIdx + 1),
                                                56.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(prevBal),
                                                100.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(currRec),
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                "${InspectionPresets.toBanglaDigits(totApp)} টি",
                                                130.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                "${InspectionPresets.toBanglaDigits(inspBld)} টি",
                                                120.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                "${InspectionPresets.toBanglaDigits(oReport.occupancyIssuedCount)} টি",
                                                100.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                "${InspectionPresets.toBanglaDigits(oReport.occupancyNotIssuedCount)} টি",
                                                100.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                oReport.occupancyNotIssuedReason.ifBlank { "-" },
                                                160.dp
                                            )
                                            TableBodyCell(
                                                oReport.remarks.ifBlank { "-" },
                                                140.dp
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val mobileCourtReports = reports.filter { InspectionPresets.isMobileCourtCategory(it.reportCategory) }
                        if (mobileCourtReports.isNotEmpty()) {
                            Text(
                                text = "মোবাইল কোর্ট বিবরণী ছক (ডানে-বামে স্ক্রল করুন):",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.error)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        TableHeaderCell("ক্রমিক", 56.dp)
                                        TableHeaderCell("মোবাইল কোর্টের তারিখ", 130.dp)
                                        TableHeaderCell("ম্যাজিস্ট্রেট এর নাম", 140.dp)
                                        TableHeaderCell("ভবন মালিকের নাম ও মোবাইল", 170.dp)
                                        TableHeaderCell("ভবনের ঠিকানা", 180.dp)
                                        TableHeaderCell("ভবনের ধরণ", 100.dp)
                                        TableHeaderCell("ব্যত্যয়ের ধরণ", 160.dp)
                                        TableHeaderCell("জরিমানা (টাকা)", 120.dp)
                                        TableHeaderCell("মিটার কর্তন", 110.dp)
                                        TableHeaderCell("অঙ্গীকারনামা", 110.dp)
                                        TableHeaderCell("মন্তব্য", 140.dp)
                                    }
                                    mobileCourtReports.forEachIndexed { mIdx, mReport ->
                                        val rowBg = if (mIdx % 2 == 0) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                        val mcDate = mReport.mobileCourtDate.ifBlank { mReport.inspectionDate }
                                        val ownerInfo = if (mReport.ownerMobile.isNotBlank()) {
                                            "${mReport.ownerName}\n(${InspectionPresets.toBanglaDigits(mReport.ownerMobile)})"
                                        } else {
                                            mReport.ownerName
                                        }
                                        val meterInfo = if (mReport.mobileCourtMeterCut) {
                                            "হ্যাঁ (${InspectionPresets.toBanglaDigits(mReport.mobileCourtMeterCount)}টি)"
                                        } else {
                                            "না"
                                        }
                                        val undertakingInfo = if (mReport.mobileCourtUndertakingSubmitted) {
                                            "দাখিলকৃত"
                                        } else {
                                            "দাখিল হয়নি"
                                        }

                                        Row(
                                            modifier = Modifier
                                                .background(rowBg)
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(mIdx + 1),
                                                56.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                InspectionPresets.toBanglaDigits(mcDate),
                                                130.dp,
                                                TextAlign.Center
                                            )
                                            TableBodyCell(
                                                mReport.magistrateName.ifBlank { "-" },
                                                140.dp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            TableBodyCell(
                                                ownerInfo.ifBlank { "-" },
                                                170.dp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            TableBodyCell(
                                                mReport.buildingAddress.ifBlank { "-" },
                                                180.dp
                                            )
                                            TableBodyCell(
                                                mReport.buildingType.ifBlank { InspectionPresets.BUILDING_TYPE_CONSTRUCTED },
                                                100.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                mReport.deviationType.ifBlank { "-" },
                                                160.dp
                                            )
                                            TableBodyCell(
                                                "৳ ${InspectionPresets.toBanglaDigits(mReport.mobileCourtFineAmount)}",
                                                120.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TableBodyCell(
                                                meterInfo,
                                                110.dp,
                                                TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            TableBodyCell(
                                                undertakingInfo,
                                                110.dp,
                                                TextAlign.Center
                                            )
                                            TableBodyCell(
                                                mReport.remarks.ifBlank { "-" },
                                                140.dp
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val buildingReports = reports.filter { InspectionPresets.isBuildingInspectionCategory(it.reportCategory) }
                        if (buildingReports.isNotEmpty()) {
                            Text(
                                text = "অফিসিয়াল ভবন পরিদর্শন ছক (ডানে-বামে স্ক্রল করুন):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Horizontal Scrollable Official 9-Column Table
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column {
                                    // Table Header
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        TableHeaderCell("ক্রমিক", 56.dp)
                                        TableHeaderCell("১. পরিদর্শনের তারিখ", 120.dp)
                                        TableHeaderCell("২. ভবন মালিকের নাম", 145.dp)
                                        TableHeaderCell("৩. ভবনের ঠিকানা", 180.dp)
                                        TableHeaderCell("মৌজার নাম", 120.dp)
                                        TableHeaderCell("থানার নাম", 120.dp)
                                        TableHeaderCell("৪. মালিকের মোবাইল", 125.dp)
                                        TableHeaderCell("৫. ভবনের ধরন (নির্মিত/নির্মাণাধীন)", 145.dp)
                                        TableHeaderCell("৬. আগে কতবার পরিদর্শন", 120.dp)
                                        TableHeaderCell("৭. অথরাইজড অফিসার পরিদর্শন?", 135.dp)
                                        TableHeaderCell("৮. ব্যত্যয়কৃত ভবনের ধরণ", 170.dp)
                                        TableHeaderCell("৯. গৃহীত ব্যবস্থা", 180.dp)
                                    }

                                    buildingReports.forEachIndexed { index, r ->
                                    val rowBg = if (index % 2 == 0) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }
                                    Row(
                                        modifier = Modifier
                                            .background(rowBg)
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableBodyCell(
                                            InspectionPresets.toBanglaDigits(index + 1),
                                            56.dp,
                                            TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TableBodyCell(
                                            InspectionPresets.toBanglaDigits(r.inspectionDate),
                                            120.dp,
                                            TextAlign.Center,
                                            fontWeight = FontWeight.Medium
                                        )
                                        TableBodyCell(r.ownerName, 145.dp, fontWeight = FontWeight.SemiBold)
                                        TableBodyCell(r.buildingAddress, 180.dp)
                                        TableBodyCell(r.mouzaName.ifBlank { "-" }, 120.dp, TextAlign.Center)
                                        TableBodyCell(r.thanaName.ifBlank { "-" }, 120.dp, TextAlign.Center)
                                        TableBodyCell(r.ownerMobile, 125.dp, TextAlign.Center)
                                        TableBodyCell(
                                            r.buildingType.ifBlank { InspectionPresets.BUILDING_TYPE_CONSTRUCTED },
                                            145.dp,
                                            TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TableBodyCell(
                                            "${InspectionPresets.toBanglaDigits(r.previousInspectionCount)} বার",
                                            120.dp,
                                            TextAlign.Center
                                        )
                                        TableBodyCell(
                                            if (r.authorizedOfficerInspected) "হ্যাঁ" else "না",
                                            135.dp,
                                            TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TableBodyCell(r.deviationType, 170.dp)
                                        TableBodyCell(
                                            if (r.remarks.isNotBlank()) "${r.actionTaken}\n(${r.remarks})" else r.actionTaken,
                                            180.dp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            // Breakdown Analytics Section
            if (reports.isNotEmpty()) {
                BreakdownCard(
                    title = "গৃহীত ব্যবস্থা অনুযায়ী সারসংক্ষেপ",
                    breakdown = stats.actionBreakdown,
                    total = stats.totalInspections,
                    barColor = MaterialTheme.colorScheme.primary
                )

                BreakdownCard(
                    title = "ব্যত্যয়কৃত ভবনের ধরণ অনুযায়ী বিশ্লেষণ",
                    breakdown = stats.deviationBreakdown,
                    total = stats.totalInspections,
                    barColor = MaterialTheme.colorScheme.secondary
                )

                BreakdownCard(
                    title = "ভবনের ধরণ অনুযায়ী পরিসংখ্যান",
                    breakdown = stats.buildingTypeBreakdown,
                    total = stats.totalInspections,
                    barColor = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp)
    )
}

@Composable
private fun TableBodyCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp)
    )
}

@Composable
private fun BreakdownCard(
    title: String,
    breakdown: Map<String, Int>,
    total: Int,
    barColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            breakdown.entries.sortedByDescending { it.value }.forEach { (label, count) ->
                val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${InspectionPresets.toBanglaDigits(count)} টি",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
