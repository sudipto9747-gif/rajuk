package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.ui.viewmodel.DashboardViewState

/**
 * এডমিন আইডিতে Enter Data-এর পরিবর্তে প্রধান ড্যাশবোর্ড স্ক্রিন।
 * এর মাধ্যমে রাজউকের সকল পরিদর্শন, ৫টি ক্যাটাগরির তথ্য, মোবাইল কোর্ট, অকুপেন্সী,
 * টাস্ক মনিটরিং ও ইউজার স্ট্যাটাস একনজরে সহজে বোঝা যায়।
 */
@Composable
fun AdminDashboardOverviewScreen(
    data: DashboardViewState,
    onNavigateToViewData: () -> Unit,
    onNavigateToTaskManager: () -> Unit,
    onNavigateToUsersRoles: () -> Unit,
    onNavigateToOptionManager: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    val reports = data.allMonthReports
    val stats = data.stats
    val monthBangla = InspectionPresets.formatReportMonthBangla(data.selectedMonth)

    // Category-specific report filters
    val buildingReports = reports.filter { InspectionPresets.isBuildingInspectionCategory(it.reportCategory) }
    val unresolvedReports = reports.filter { InspectionPresets.isUnresolvedCategory(it.reportCategory) }
    val designReports = reports.filter { InspectionPresets.isDesignApprovalCategory(it.reportCategory) }
    val occupancyReports = reports.filter { InspectionPresets.isOccupancyCertificateCategory(it.reportCategory) }
    val mobileCourtReports = reports.filter { InspectionPresets.isMobileCourtCategory(it.reportCategory) }

    // Mobile Court summary calculations
    val totalFineAmount = mobileCourtReports.sumOf { it.mobileCourtFineAmount }
    val totalMetersCut = mobileCourtReports.filter { it.mobileCourtMeterCut }.sumOf {
        if (it.mobileCourtMeterCount > 0L) it.mobileCourtMeterCount else 1L
    }
    val undertakingCount = mobileCourtReports.count { it.mobileCourtUndertakingSubmitted }

    // Occupancy Certificate summary calculations
    val occupancyTotalIssued = occupancyReports.sumOf { it.occupancyIssuedCount }
    val occupancyTotalNotIssued = occupancyReports.sumOf { it.occupancyNotIssuedCount }
    val occupancyTotalReceived = occupancyReports.sumOf { it.occupancyTotalApplications }

    // User Accounts summary
    val pendingUsersCount = data.allUserAccounts.count { it.approvalStatus == "pending" }
    val approvedUsersCount = data.allUserAccounts.count { it.approvalStatus == "approved" }
    val adminUsersCount = data.allUserAccounts.count { it.role == "admin" }

    // Task Manager summary
    val pendingTasksCount = data.allTasks.count { it.status == "pending" }
    val inProgressTasksCount = data.allTasks.count { it.status == "in_progress" }
    val completedTasksCount = data.allTasks.count { it.status == "completed" }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Admin Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_overview_hero_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Dashboard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "অ্যাডমিন ড্যাশবোর্ড (Admin Overview)",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$monthBangla মাসের সার্বিক তথ্য সারসংক্ষেপ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "এডমিন অ্যাক্সেস",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Text(
                            text = "এই ড্যাশবোর্ডের মাধ্যমে রাজউকের পরিদর্শন, নোটিশ, নকশা অনুমোদন, অকুপেন্সী সনদ, মোবাইল কোর্টের জরিমানা ও পরিদর্শকদের টাস্ক সহজেই পর্যবেক্ষণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. High-Level Primary Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        title = "মোট রেকর্ড",
                        value = "${InspectionPresets.toBanglaDigits(reports.size)} টি",
                        subtitle = "$monthBangla মাস",
                        icon = Icons.Filled.Apartment,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "ব্যত্যয় চিহ্নিত",
                        value = "${InspectionPresets.toBanglaDigits(stats.deviatedBuildingCount)} টি",
                        subtitle = "নকশা বহির্ভূত ভবন",
                        icon = Icons.Filled.Warning,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        title = "অথরাইজড পরিদর্শন",
                        value = "${InspectionPresets.toBanglaDigits(stats.authorizedOfficerVisitedCount)} টি",
                        subtitle = "কর্মকর্তা সরেজমিনে",
                        icon = Icons.Filled.CheckCircle,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "অনুমোদন অপেক্ষা",
                        value = "${InspectionPresets.toBanglaDigits(pendingUsersCount)} জন",
                        subtitle = "নতুন রেজিস্ট্রেশন",
                        icon = Icons.Filled.HourglassTop,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Quick Action Hub (Task Manager, Users, Options, View Data, PDF)
            item {
                Text(
                    text = "প্রশাসনিক নিয়ন্ত্রণ ও নেভিগেশন (Administrative Actions)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminActionTile(
                            title = "টাস্ক ম্যানেজার",
                            subtitle = "টাস্ক এসাইন ও মনিটরিং (${InspectionPresets.toBanglaDigits(data.allTasks.size)})",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            badgeCount = pendingTasksCount,
                            badgeColor = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToTaskManager,
                            modifier = Modifier.weight(1f)
                        )
                        AdminActionTile(
                            title = "ইউজার ও রোল",
                            subtitle = "অনুমোদন ও অ্যাডমিন নিয়োগ",
                            icon = Icons.Filled.People,
                            badgeCount = pendingUsersCount,
                            badgeColor = MaterialTheme.colorScheme.error,
                            onClick = onNavigateToUsersRoles,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminActionTile(
                            title = "অপশন ম্যানেজার",
                            subtitle = "যে কোন অপশন যোগ, এডিট, ডিলিট",
                            icon = Icons.Filled.Settings,
                            badgeCount = data.customOptions.size,
                            badgeColor = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateToOptionManager,
                            modifier = Modifier.weight(1f)
                        )
                        AdminActionTile(
                            title = "বিস্তারিত তথ্য দেখুন",
                            subtitle = "সকল রেকর্ড ফিল্টার ও সার্চ",
                            icon = Icons.AutoMirrored.Filled.ListAlt,
                            badgeCount = reports.size,
                            badgeColor = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToViewData,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // PDF export button (Head office & Customized)
                    OutlinedCard(
                        onClick = onDownloadPdf,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_pdf_download_button")
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Column {
                                    Text(
                                        text = "পিডিএফ রিপোর্ট জেনারেটর (২ ধরনের রিপোর্ট)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "১. হেড অফিস স্ট্যান্ডার্ড রিপোর্ট | ২. কাস্টম কলাম ভিত্তিক রিপোর্ট",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 4. 5 Categories Detailed Breakdown Section
            item {
                Text(
                    text = "৫টি ক্যাটাগরির বিস্তারিত তথ্য সারসংক্ষেপ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Category 1: ভবন পরিদর্শন সম্পর্কিত রিপোর্ট
            item {
                CategoryBreakdownCard(
                    categoryTitle = "১. ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
                    count = buildingReports.size,
                    accentColor = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.Apartment,
                    metrics = listOf(
                        "নির্মিত ভবন" to "${InspectionPresets.toBanglaDigits(stats.constructedBuildingCount)} টি",
                        "নির্মাণাধীন ভবন" to "${InspectionPresets.toBanglaDigits(stats.underConstructionBuildingCount)} টি",
                        "ব্যত্যয় পাওয়া গেছে" to "${InspectionPresets.toBanglaDigits(stats.deviatedBuildingCount)} টি",
                        "পুনঃপরিদর্শন" to "${InspectionPresets.toBanglaDigits(stats.repeatInspectionCount)} টি"
                    )
                )
            }

            // Category 2: অনিষ্পন্ন রিপোর্ট
            item {
                CategoryBreakdownCard(
                    categoryTitle = "২. অনিষ্পন্ন রিপোর্ট",
                    count = unresolvedReports.size,
                    accentColor = MaterialTheme.colorScheme.error,
                    icon = Icons.Filled.PendingActions,
                    metrics = listOf(
                        "মোট এন্ট্রি" to "${InspectionPresets.toBanglaDigits(unresolvedReports.size)} টি",
                        "পূর্ববর্তী জের" to "${InspectionPresets.toBanglaDigits(unresolvedReports.sumOf { it.previousMonthBalance })} টি",
                        "মাসে প্রাপ্তি" to "${InspectionPresets.toBanglaDigits(unresolvedReports.sumOf { it.currentMonthReceived })} টি",
                        "মাসান্তে অনিষ্পন্ন" to "${InspectionPresets.toBanglaDigits(unresolvedReports.sumOf { it.unresolvedAtMonthEnd })} টি"
                    )
                )
            }

            // Category 3: নকশা অনুমোদন
            item {
                CategoryBreakdownCard(
                    categoryTitle = "৩. নকশা অনুমোদন",
                    count = designReports.size,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Filled.Engineering,
                    metrics = listOf(
                        "দাখিলকৃত নকশা" to "${InspectionPresets.toBanglaDigits(designReports.sumOf { it.planSubmitted })} টি",
                        "অনুমোদিত নকশা" to "${InspectionPresets.toBanglaDigits(designReports.sumOf { it.planApproved })} টি",
                        "প্রত্যাখ্যাত" to "${InspectionPresets.toBanglaDigits(designReports.sumOf { it.planRejected })} টি",
                        "চলমান কাজ" to "${InspectionPresets.toBanglaDigits(designReports.sumOf { it.planWorkInProgress })} টি"
                    )
                )
            }

            // Category 4: অকুপেন্সী সনদ
            item {
                CategoryBreakdownCard(
                    categoryTitle = "৪. অকুপেন্সী সনদ",
                    count = occupancyReports.size,
                    accentColor = Color(0xFF00796B),
                    icon = Icons.Filled.AssignmentTurnedIn,
                    metrics = listOf(
                        "জেরসহ মোট আবেদন" to "${InspectionPresets.toBanglaDigits(occupancyTotalReceived)} টি",
                        "সনদ প্রদান করা হয়েছে" to "${InspectionPresets.toBanglaDigits(occupancyTotalIssued)} টি",
                        "সনদ অপ্রদান সংখ্যা" to "${InspectionPresets.toBanglaDigits(occupancyTotalNotIssued)} টি",
                        "পরিদর্শনকৃত ভবন" to "${InspectionPresets.toBanglaDigits(occupancyReports.sumOf { it.occupancyInspectedBuildings })} টি"
                    )
                )
            }

            // Category 5: মোবাইল কোর্টের তথ্য
            item {
                CategoryBreakdownCard(
                    categoryTitle = "৫. মোবাইল কোর্টের তথ্য",
                    count = mobileCourtReports.size,
                    accentColor = Color(0xFFC2185B),
                    icon = Icons.Filled.Gavel,
                    metrics = listOf(
                        "পরিচালিত কোর্ট" to "${InspectionPresets.toBanglaDigits(mobileCourtReports.size)} টি",
                        "মোট জরিমানা" to "৳ ${InspectionPresets.toBanglaDigits(totalFineAmount)}",
                        "মিটার কাটা হয়েছে" to "${InspectionPresets.toBanglaDigits(totalMetersCut)} টি",
                        "অঙ্গীকার নামা দাখিল" to "${InspectionPresets.toBanglaDigits(undertakingCount)} টি"
                    )
                )
            }

            // 5. Task Manager Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_task_overview_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "টাস্ক মনিটরিং স্ট্যাটাস (Task Status)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "মোট: ${InspectionPresets.toBanglaDigits(data.allTasks.size)} টি",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskMiniBadge(
                                label = "অপেক্ষমাণ",
                                count = pendingTasksCount,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            TaskMiniBadge(
                                label = "চলমান",
                                count = inProgressTasksCount,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TaskMiniBadge(
                                label = "সম্পন্ন",
                                count = completedTasksCount,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun AdminActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = InspectionPresets.toBanglaDigits(badgeCount),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    categoryTitle: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    metrics: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${InspectionPresets.toBanglaDigits(count)} টি রেকর্ড",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                metrics.forEach { (label, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskMiniBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = InspectionPresets.toBanglaDigits(count),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
