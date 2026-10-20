package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.InspectionPresets
import com.example.data.model.UserAccount

@Composable
fun AdminApprovalPanelScreen(
    allAccounts: List<UserAccount>,
    isSaving: Boolean,
    onApprove: (UserAccount) -> Unit,
    onReject: (UserAccount) -> Unit,
    onUpdateUserRole: (UserAccount, newRole: String) -> Unit
) {
    var statusFilter by rememberSaveable { mutableStateOf("all") }

    val pendingCount = allAccounts.count { it.approvalStatus == "pending" }
    val approvedCount = allAccounts.count { it.approvalStatus == "approved" }
    val rejectedCount = allAccounts.count { it.approvalStatus == "rejected" }

    val filteredAccounts = when (statusFilter) {
        "pending" -> allAccounts.filter { it.approvalStatus == "pending" }
        "approved" -> allAccounts.filter { it.approvalStatus == "approved" }
        "rejected" -> allAccounts.filter { it.approvalStatus == "rejected" }
        else -> allAccounts
    }

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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "অ্যাডমিন অনুমোদন প্যানেল (User Registration Approval)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "ব্যবহারকারীরা ফোন নাম্বার ও পাসওয়ার্ড দিয়ে রেজিস্ট্রেশন করলে এখান থেকে অনুমোদন (Approve) বা বাতিল করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminCountBadge(
                                label = "অপেক্ষমাণ",
                                count = pendingCount,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            AdminCountBadge(
                                label = "অনুমোদিত",
                                count = approvedCount,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            AdminCountBadge(
                                label = "বাতিলকৃত",
                                count = rejectedCount,
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == "all",
                        onClick = { statusFilter = "all" },
                        label = { Text("সকল (${InspectionPresets.toBanglaDigits(allAccounts.size)})") }
                    )
                    FilterChip(
                        selected = statusFilter == "pending",
                        onClick = { statusFilter = "pending" },
                        label = { Text("অপেক্ষমাণ (${InspectionPresets.toBanglaDigits(pendingCount)})") }
                    )
                    FilterChip(
                        selected = statusFilter == "approved",
                        onClick = { statusFilter = "approved" },
                        label = { Text("অনুমোদিত (${InspectionPresets.toBanglaDigits(approvedCount)})") }
                    )
                    FilterChip(
                        selected = statusFilter == "rejected",
                        onClick = { statusFilter = "rejected" },
                        label = { Text("বাতিলকৃত (${InspectionPresets.toBanglaDigits(rejectedCount)})") }
                    )
                }
            }

            if (filteredAccounts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "এই ক্যাটাগরিতে কোনো রেজিস্ট্রেশন আবেদন নেই",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredAccounts,
                    key = { it.userId.ifBlank { it.id } }
                ) { account ->
                    UserRegistrationApprovalCard(
                        account = account,
                        isSaving = isSaving,
                        onApprove = { onApprove(account) },
                        onReject = { onReject(account) },
                        onUpdateRole = { newRole -> onUpdateUserRole(account, newRole) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun AdminCountBadge(
    label: String,
    count: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = InspectionPresets.toBanglaDigits(count),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun UserRegistrationApprovalCard(
    account: UserAccount,
    isSaving: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onUpdateRole: (newRole: String) -> Unit
) {
    var showRoleConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val targetRole = if (account.role == "admin") "inspector" else "admin"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_account_card_${account.userId}")
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (account.role == "admin") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (account.role == "admin") Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (account.role == "admin") MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = account.inspectorName.ifBlank { "নিবন্ধিত ফোন: ${account.phoneNumber}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (account.inspectorName.isBlank()) {
                                "লগইন করার পর ব্যবহারকারী নাম সেট করবেন"
                            } else {
                                "${account.designation.ifBlank { "ইমারত পরিদর্শক" }} ${if (account.officeName.isNotBlank()) "• ${account.officeName}" else ""}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val (badgeText, badgeBg, badgeFg, badgeIcon) = when (account.approvalStatus) {
                    "approved" -> Quad(
                        "অনুমোদিত",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                        Icons.Filled.CheckCircle
                    )
                    "rejected" -> Quad(
                        "বাতিলকৃত",
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer,
                        Icons.Filled.Block
                    )
                    else -> Quad(
                        "অপেক্ষমাণ",
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        Icons.Filled.HourglassTop
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg
                        )
                    }
                }
            }

            // Role indicator row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "বর্তমান রোল:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (account.role == "admin") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (account.role == "admin") "অ্যাডমিন (Admin)" else "ইমারত পরিদর্শক (Inspector)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (account.role == "admin") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Make Admin / Revert Button
                if (account.phoneNumber != "01767563095" && account.approvalStatus == "approved") {
                    OutlinedButton(
                        onClick = { showRoleConfirmDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (account.role == "admin") "পরিদর্শক বানান" else "অ্যাডমিন রোল দিন",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ফোন: ${account.phoneNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (account.email.isNotBlank()) {
                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (account.approvalStatus != "approved") {
                    Button(
                        onClick = onApprove,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_user_${account.userId}")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অনুমোদন দিন (Approve)", fontWeight = FontWeight.Bold)
                    }
                }

                if (account.approvalStatus != "rejected" && account.phoneNumber != "01767563095") {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_user_${account.userId}")
                    ) {
                        Icon(
                            Icons.Filled.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বাতিল করুন", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showRoleConfirmDialog) {
        val newRoleLabel = if (targetRole == "admin") "অ্যাডমিন" else "পরিদর্শক"
        val userName = account.inspectorName.ifBlank { account.phoneNumber }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRoleConfirmDialog = false },
            title = { Text("রোল পরিবর্তন নিশ্চিতকরণ", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "আপনি কি $userName-কে $newRoleLabel রোল প্রদান করতে চান?\n\n" +
                        if (targetRole == "admin") "অ্যাডমিন হিসেবে তিনি সকল ইউজার অনুমোদন, অপশন যোগ/এডিট/ডিলিট ও পরিদর্শকদের টাস্ক প্রদান ও মনিটর করতে পারবেন।"
                        else "পরিদর্শক হিসেবে তিনি সাধারণ পরিদর্শন তথ্য এন্ট্রি ও এডিট করতে পারবেন।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateRole(targetRole)
                        showRoleConfirmDialog = false
                    },
                    enabled = !isSaving
                ) {
                    Text("হ্যাঁ, রোল পরিবর্তন করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRoleConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
