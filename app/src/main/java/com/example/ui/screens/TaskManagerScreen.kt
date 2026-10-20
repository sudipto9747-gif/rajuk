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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AssignedTask
import com.example.data.model.InspectionPresets
import com.example.data.model.UserAccount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerScreen(
    isAdmin: Boolean,
    allTasks: List<AssignedTask>,
    myTasks: List<AssignedTask>,
    allUserAccounts: List<UserAccount>,
    isSaving: Boolean,
    onCreateTask: (title: String, desc: String, user: UserAccount, due: String, prio: String, addr: String) -> Unit,
    onUpdateTask: (AssignedTask) -> Unit,
    onUpdateStatus: (taskId: String, newStatus: String, note: String) -> Unit,
    onDeleteTask: (taskId: String) -> Unit
) {
    var statusFilter by rememberSaveable { mutableStateOf("all") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAssigneeFilter by rememberSaveable { mutableStateOf<String?>(null) }

    var isCreateDialogOpen by rememberSaveable { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<AssignedTask?>(null) }
    var taskPendingDelete by remember { mutableStateOf<AssignedTask?>(null) }
    var taskStatusUpdateTarget by remember { mutableStateOf<AssignedTask?>(null) }

    val displayTasks = if (isAdmin) allTasks else myTasks

    val pendingCount = displayTasks.count { it.status == "pending" }
    val inProgressCount = displayTasks.count { it.status == "in_progress" }
    val completedCount = displayTasks.count { it.status == "completed" }

    val filteredTasks = displayTasks.filter { task ->
        val matchesStatus = when (statusFilter) {
            "pending" -> task.status == "pending"
            "in_progress" -> task.status == "in_progress"
            "completed" -> task.status == "completed"
            else -> true
        }
        val matchesAssignee = selectedAssigneeFilter == null || task.assignedToUserId == selectedAssigneeFilter
        val q = searchQuery.trim().lowercase(Locale.getDefault())
        val matchesQuery = q.isEmpty() ||
            task.title.lowercase().contains(q) ||
            task.description.lowercase().contains(q) ||
            task.assignedToName.lowercase().contains(q) ||
            task.buildingAddress.lowercase().contains(q)

        matchesStatus && matchesAssignee && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { isCreateDialogOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("admin_add_task_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("নতুন টাস্ক দিন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("task_manager_header_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isAdmin) "টাস্ক ম্যানেজার ও মনিটরিং (Task Manager)" else "আমার দায়িত্ব ও টাস্ক (My Tasks)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (isAdmin) {
                                            "যেকোনো পরিদর্শককে নির্দিষ্ট ভবনের পরিদর্শন বা ব্যবস্থা গ্রহণের টাস্ক দিন ও অগ্রগতি মনিটর করুন।"
                                        } else {
                                            "অ্যাডমিন কর্তৃক আপনাকে অর্পিত পরিদর্শন ও কার্যক্রম সংক্রান্ত টাস্কের তালিকা।"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Summary counters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TaskStatusBadge(
                                    label = "অপেক্ষমাণ",
                                    count = pendingCount,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                TaskStatusBadge(
                                    label = "চলমান",
                                    count = inProgressCount,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                TaskStatusBadge(
                                    label = "সম্পন্ন",
                                    count = completedCount,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Search field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("টাস্কের নাম, পরিদর্শক বা এলাকা খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("task_search_input")
                    )
                }

                // Filter chips
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
                            label = { Text("সকল (${InspectionPresets.toBanglaDigits(displayTasks.size)})") }
                        )
                        FilterChip(
                            selected = statusFilter == "pending",
                            onClick = { statusFilter = "pending" },
                            label = { Text("অপেক্ষমাণ (${InspectionPresets.toBanglaDigits(pendingCount)})") }
                        )
                        FilterChip(
                            selected = statusFilter == "in_progress",
                            onClick = { statusFilter = "in_progress" },
                            label = { Text("চলমান (${InspectionPresets.toBanglaDigits(inProgressCount)})") }
                        )
                        FilterChip(
                            selected = statusFilter == "completed",
                            onClick = { statusFilter = "completed" },
                            label = { Text("সম্পন্ন (${InspectionPresets.toBanglaDigits(completedCount)})") }
                        )
                    }
                }

                if (filteredTasks.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "কোনো টাস্ক পাওয়া যায়নি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isAdmin) {
                                    Text(
                                        text = "নতুন টাস্ক দিতে নিচের 'নতুন টাস্ক দিন' বাটনে ক্লিক করুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredTasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            isAdmin = isAdmin,
                            onUpdateStatusClick = { taskStatusUpdateTarget = task },
                            onEditClick = { taskToEdit = task },
                            onDeleteClick = { taskPendingDelete = task }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Dialog: Create Task (Admin only)
    if (isCreateDialogOpen) {
        CreateOrEditTaskDialog(
            initialTask = null,
            approvedUsers = allUserAccounts.filter { it.approvalStatus == "approved" }
                .ifEmpty { allUserAccounts.filter { it.approvalStatus != "rejected" } },
            isSaving = isSaving,
            onDismiss = { isCreateDialogOpen = false },
            onSave = { title, desc, user, due, prio, addr ->
                onCreateTask(title, desc, user, due, prio, addr)
                isCreateDialogOpen = false
            }
        )
    }

    // Dialog: Edit Task (Admin only)
    if (taskToEdit != null) {
        val editing = taskToEdit!!
        CreateOrEditTaskDialog(
            initialTask = editing,
            approvedUsers = allUserAccounts.filter { it.approvalStatus == "approved" }
                .ifEmpty { allUserAccounts.filter { it.approvalStatus != "rejected" } },
            isSaving = isSaving,
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, user, due, prio, addr ->
                onUpdateTask(
                    editing.copy(
                        title = title,
                        description = desc,
                        assignedToUserId = user.userId,
                        assignedToName = user.inspectorName.ifBlank { user.phoneNumber },
                        assignedToDesignation = user.designation.ifBlank { "ইমারত পরিদর্শক" },
                        assignedToPhone = user.phoneNumber,
                        dueDate = due,
                        priority = prio,
                        buildingAddress = addr
                    )
                )
                taskToEdit = null
            }
        )
    }

    // Dialog: Change Task Status
    if (taskStatusUpdateTarget != null) {
        val target = taskStatusUpdateTarget!!
        var newStatus by rememberSaveable(target.id) { mutableStateOf(target.status) }
        var completionNote by rememberSaveable(target.id) { mutableStateOf(target.completionNote) }

        AlertDialog(
            onDismissRequest = { taskStatusUpdateTarget = null },
            title = { Text("টাস্ক স্ট্যাটাস পরিবর্তন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("টাস্ক: ${target.title}", fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = newStatus == "pending",
                            onClick = { newStatus = "pending" },
                            label = { Text("অপেক্ষমাণ") }
                        )
                        FilterChip(
                            selected = newStatus == "in_progress",
                            onClick = { newStatus = "in_progress" },
                            label = { Text("চলমান") }
                        )
                        FilterChip(
                            selected = newStatus == "completed",
                            onClick = { newStatus = "completed" },
                            label = { Text("সম্পন্ন") }
                        )
                    }

                    OutlinedTextField(
                        value = completionNote,
                        onValueChange = { completionNote = it },
                        label = { Text("অগ্রগতি / মন্তব্যের বিবরণ") },
                        placeholder = { Text("যেমন: পরিদর্শন সম্পন্ন হয়েছে, নোটিশ জারি করা হয়েছে...") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(target.id, newStatus, completionNote)
                        taskStatusUpdateTarget = null
                    },
                    enabled = !isSaving
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskStatusUpdateTarget = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Delete Task Confirmation
    if (taskPendingDelete != null) {
        val deleting = taskPendingDelete!!
        AlertDialog(
            onDismissRequest = { taskPendingDelete = null },
            title = { Text("টাস্ক মুছে ফেলবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text("টাস্ক: \"${deleting.title}\"\nএই টাস্কটি স্থায়ীভাবে মুছে ফেলা হবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTask(deleting.id)
                        taskPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskPendingDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun TaskStatusBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
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

@Composable
private fun TaskItemCard(
    task: AssignedTask,
    isAdmin: Boolean,
    onUpdateStatusClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val (statusText, statusBg, statusFg) = when (task.status) {
        "completed" -> Triple("সম্পন্ন", Color(0xFF2E7D32).copy(alpha = 0.12f), Color(0xFF2E7D32))
        "in_progress" -> Triple("চলমান", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        else -> Triple("অপেক্ষমাণ", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    val (prioText, prioColor) = when (task.priority) {
        "High" -> "উচ্চ অগ্রাধিকার (জরুরি)" to MaterialTheme.colorScheme.error
        "Low" -> "কম অগ্রাধিকার" to MaterialTheme.colorScheme.outline
        else -> "সাধারণ অগ্রাধিকার" to MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("task_card_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = prioText,
                        style = MaterialTheme.typography.labelSmall,
                        color = prioColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Description
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Location
            if (task.buildingAddress.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.buildingAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Assignee & Due Date Row
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
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "পরিদর্শক: ${task.assignedToName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (task.dueDate.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "শেষ তারিখ: ${InspectionPresets.toBanglaDigits(task.dueDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Completion note
            if (task.completionNote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "অগ্রগতি নোট: ${task.completionNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUpdateStatusClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.status == "completed") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("স্ট্যাটাস আপডেট", fontWeight = FontWeight.Bold)
                }

                if (isAdmin) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Task", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrEditTaskDialog(
    initialTask: AssignedTask?,
    approvedUsers: List<UserAccount>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, user: UserAccount, due: String, prio: String, addr: String) -> Unit
) {
    val isEditing = initialTask != null
    var title by rememberSaveable { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialTask?.description.orEmpty()) }
    var buildingAddress by rememberSaveable { mutableStateOf(initialTask?.buildingAddress.orEmpty()) }
    var priority by rememberSaveable { mutableStateOf(initialTask?.priority ?: "Medium") }
    var dueDate by rememberSaveable {
        mutableStateOf(initialTask?.dueDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var selectedUser by remember {
        mutableStateOf(approvedUsers.find { it.userId == initialTask?.assignedToUserId } ?: approvedUsers.firstOrNull())
    }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var userDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "টাস্ক সম্পাদনা করুন" else "নতুন টাস্ক দিন (Assign Task)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("টাস্কের শিরোনাম (Title) *") },
                    placeholder = { Text("যেমন: ভবন নং ১২ সরেজমিনে পরিদর্শন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                // User dropdown
                ExposedDropdownMenuBox(
                    expanded = userDropdownExpanded,
                    onExpandedChange = { userDropdownExpanded = !userDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.let {
                            "${it.inspectorName.ifBlank { it.phoneNumber }} (${it.designation.ifBlank { "ইমারত পরিদর্শক" }})"
                        } ?: "কোন পরিদর্শক নির্বাচিত নেই",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("পরিদর্শক নির্বাচন করুন (Assign To) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = userDropdownExpanded,
                        onDismissRequest = { userDropdownExpanded = false }
                    ) {
                        approvedUsers.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(user.inspectorName.ifBlank { user.phoneNumber }, fontWeight = FontWeight.Bold)
                                        Text("${user.designation.ifBlank { "ইমারত পরিদর্শক" }} • ${user.phoneNumber}", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    selectedUser = user
                                    userDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Building Address
                OutlinedTextField(
                    value = buildingAddress,
                    onValueChange = { buildingAddress = it },
                    label = { Text("সংশ্লিষ্ট ভবনের ঠিকানা / এলাকা (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: প্লট-৪৫, রোড-৭, গুলশান-২") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("শেষ তারিখ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick Date", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = priorityDropdownExpanded,
                    onExpandedChange = { priorityDropdownExpanded = !priorityDropdownExpanded }
                ) {
                    val prioLabel = when (priority) {
                        "High" -> "উচ্চ অগ্রাধিকার (জরুরি)"
                        "Low" -> "কম অগ্রাধিকার"
                        else -> "সাধারণ অগ্রাধিকার"
                    }
                    OutlinedTextField(
                        value = prioLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("অগ্রাধিকার (Priority)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = priorityDropdownExpanded,
                        onDismissRequest = { priorityDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("উচ্চ অগ্রাধিকার (জরুরি)") },
                            onClick = { priority = "High"; priorityDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("সাধারণ অগ্রাধিকার") },
                            onClick = { priority = "Medium"; priorityDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("কম অগ্রাধিকার") },
                            onClick = { priority = "Low"; priorityDropdownExpanded = false }
                        )
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("কাজের বিবরণ ও সুনির্দিষ্ট নির্দেশনা") },
                    placeholder = { Text("যেমন: ভবনের সেটব্যাক ও অতিরিক্ত তলা পর্যবেক্ষণ করে রিপোর্ট দিন...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "টাস্কের শিরোনাম প্রদান করুন"
                        return@Button
                    }
                    if (selectedUser == null) {
                        validationError = "একজন পরিদর্শক নির্বাচন করুন"
                        return@Button
                    }
                    onSave(title, description, selectedUser!!, dueDate, priority, buildingAddress)
                },
                enabled = !isSaving
            ) {
                Text(if (isEditing) "আপডেট করুন" else "টাস্ক এসাইন করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) { Text("ঠিক আছে") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("বাতিল") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
