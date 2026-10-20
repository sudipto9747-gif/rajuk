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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AppOption
import com.example.data.model.InspectionPresets
import java.util.Locale

data class DisplayOptionItem(
    val id: String,
    val group: String,
    val name: String,
    val subGroup: String = "",
    val isCustom: Boolean,
    val originalAppOption: AppOption? = null
)

@Composable
fun OptionManagerScreen(
    customOptions: List<AppOption>,
    isSaving: Boolean,
    onAddOption: (group: String, name: String, subGroup: String) -> Unit,
    onUpdateOption: (option: AppOption, newName: String, newSubGroup: String) -> Unit,
    onDeleteOption: (optionId: String) -> Unit
) {
    var selectedGroup by rememberSaveable { mutableStateOf("category") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
    var optionToEdit by remember { mutableStateOf<DisplayOptionItem?>(null) }
    var optionPendingDelete by remember { mutableStateOf<DisplayOptionItem?>(null) }

    // Aggregate options for the selected group (combining presets and custom options)
    val currentGroupOptions: List<DisplayOptionItem> = remember(selectedGroup, customOptions) {
        val customs = customOptions.filter { it.group == selectedGroup }
        if (customs.isNotEmpty()) {
            customs.map {
                DisplayOptionItem(
                    id = it.id,
                    group = it.group,
                    name = it.name,
                    subGroup = it.subGroup,
                    isCustom = true,
                    originalAppOption = it
                )
            }
        } else {
            // Default preset items if not yet customized
            val defaults = when (selectedGroup) {
                "category" -> InspectionPresets.enterDataCategories.map {
                    DisplayOptionItem(id = "default_${it.hashCode()}", group = "category", name = it, isCustom = false)
                }
                "building_type" -> InspectionPresets.buildingTypes.map {
                    DisplayOptionItem(id = "default_${it.hashCode()}", group = "building_type", name = it, isCustom = false)
                }
                "deviation_type" -> {
                    val c = InspectionPresets.deviationTypesConstructed.map {
                        DisplayOptionItem(id = "default_${it.hashCode()}", group = "deviation_type", name = it, subGroup = "constructed", isCustom = false)
                    }
                    val u = InspectionPresets.deviationTypesUnderConstruction.map {
                        DisplayOptionItem(id = "default_u_${it.hashCode()}", group = "deviation_type", name = it, subGroup = "under_construction", isCustom = false)
                    }
                    (c + u).distinctBy { "${it.name}_${it.subGroup}" }
                }
                "action_taken" -> {
                    val c = InspectionPresets.actionTakenTypesConstructed.map {
                        DisplayOptionItem(id = "default_${it.hashCode()}", group = "action_taken", name = it, subGroup = "constructed", isCustom = false)
                    }
                    val u = InspectionPresets.actionTakenTypesUnderConstruction.map {
                        DisplayOptionItem(id = "default_u_${it.hashCode()}", group = "action_taken", name = it, subGroup = "under_construction", isCustom = false)
                    }
                    (c + u).distinctBy { "${it.name}_${it.subGroup}" }
                }
                "unresolved_duration" -> InspectionPresets.unresolvedDurationOptions.map {
                    DisplayOptionItem(id = "default_${it.hashCode()}", group = "unresolved_duration", name = it, isCustom = false)
                }
                else -> emptyList()
            }
            defaults
        }
    }

    val filteredList = currentGroupOptions.filter { item ->
        val q = searchQuery.trim().lowercase(Locale.getDefault())
        q.isEmpty() || item.name.lowercase().contains(q)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("admin_add_option_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("নতুন অপশন যোগ করুন", fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.fillMaxWidth().testTag("option_manager_header_card")
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
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "অপশন কাস্টমাইজার (App Options Manager)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "এডমিন যে কোন অপশন Add, Edit, Delete করতে পারবেন। পরিদর্শকগণ ফর্ম ও ফিল্টারে তাৎক্ষণিক দেখতে পাবেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Group selector chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedGroup == "category",
                            onClick = { selectedGroup = "category" },
                            label = { Text("১. ক্যাটাগরি অপশন") }
                        )
                        FilterChip(
                            selected = selectedGroup == "building_type",
                            onClick = { selectedGroup = "building_type" },
                            label = { Text("২. ভবনের ধরণ") }
                        )
                        FilterChip(
                            selected = selectedGroup == "deviation_type",
                            onClick = { selectedGroup = "deviation_type" },
                            label = { Text("৩. ব্যত্যয়ের ধরণ") }
                        )
                        FilterChip(
                            selected = selectedGroup == "action_taken",
                            onClick = { selectedGroup = "action_taken" },
                            label = { Text("৪. গৃহীত ব্যবস্থা") }
                        )
                        FilterChip(
                            selected = selectedGroup == "unresolved_duration",
                            onClick = { selectedGroup = "unresolved_duration" },
                            label = { Text("৫. অনিষ্পন্ন সময়কাল") }
                        )
                    }
                }

                // Search field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("অপশন খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("option_search_input")
                    )
                }

                if (filteredList.isEmpty()) {
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
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "এই গ্রুপে কোনো অপশন নেই",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "নতুন অপশন যোগ করতে নিচের 'নতুন অপশন যোগ করুন' বাটনে চাপুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            modifier = Modifier.fillMaxWidth().testTag("option_item_card_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.isCustom) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "কাস্টম",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "ডিফল্ট",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (item.subGroup.isNotBlank()) {
                                        val subLabel = when (item.subGroup) {
                                            "constructed" -> "নির্মিত ভবনের জন্য প্রযোজ্য"
                                            "under_construction" -> "নির্মাণাধীন ভবনের জন্য প্রযোজ্য"
                                            else -> "উভয় ভবনের জন্য"
                                        }
                                        Text(
                                            text = subLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { optionToEdit = item }
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Option", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { optionPendingDelete = item }
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Option", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Dialog: Add Option
    if (isAddDialogOpen) {
        CreateOrEditOptionDialog(
            isEditing = false,
            initialName = "",
            initialGroup = selectedGroup,
            initialSubGroup = "general",
            isSaving = isSaving,
            onDismiss = { isAddDialogOpen = false },
            onSave = { group, name, subGroup ->
                onAddOption(group, name, subGroup)
                isAddDialogOpen = false
            }
        )
    }

    // Dialog: Edit Option
    if (optionToEdit != null) {
        val editing = optionToEdit!!
        CreateOrEditOptionDialog(
            isEditing = true,
            initialName = editing.name,
            initialGroup = editing.group,
            initialSubGroup = editing.subGroup.ifBlank { "general" },
            isSaving = isSaving,
            onDismiss = { optionToEdit = null },
            onSave = { group, newName, newSub ->
                if (editing.originalAppOption != null) {
                    onUpdateOption(editing.originalAppOption, newName, newSub)
                } else {
                    // For default option being edited, add it as custom
                    onAddOption(group, newName, newSub)
                }
                optionToEdit = null
            }
        )
    }

    // Dialog: Delete Option
    if (optionPendingDelete != null) {
        val deleting = optionPendingDelete!!
        AlertDialog(
            onDismissRequest = { optionPendingDelete = null },
            title = { Text("অপশন মুছে ফেলবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text("অপশন: \"${deleting.name}\"\nএই অপশনটি তালিকা হতে মুছে ফেলা হবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleting.originalAppOption != null) {
                            onDeleteOption(deleting.originalAppOption.id)
                        }
                        optionPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { optionPendingDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrEditOptionDialog(
    isEditing: Boolean,
    initialName: String,
    initialGroup: String,
    initialSubGroup: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (group: String, name: String, subGroup: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var group by rememberSaveable { mutableStateOf(initialGroup) }
    var subGroup by rememberSaveable { mutableStateOf(initialSubGroup) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var subGroupDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    val groupLabel = when (group) {
        "category" -> "১. ক্যাটাগরি অপশন (Report Category)"
        "building_type" -> "২. ভবনের ধরণ (Building Type)"
        "deviation_type" -> "৩. ব্যত্যয়ের ধরণ (Deviation Type)"
        "action_taken" -> "৪. গৃহীত ব্যবস্থা (Action Taken)"
        else -> "৫. অনিষ্পন্ন সময়কাল (Unresolved Duration)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "অপশন সম্পাদনা করুন" else "নতুন অপশন যোগ করুন",
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

                // Group dropdown
                ExposedDropdownMenuBox(
                    expanded = groupDropdownExpanded,
                    onExpandedChange = { if (!isEditing) groupDropdownExpanded = !groupDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = groupLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("অপশনের গ্রুপ (Option Group) *") },
                        trailingIcon = { if (!isEditing) ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = groupDropdownExpanded,
                        onDismissRequest = { groupDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("১. ক্যাটাগরি অপশন (Report Category)") },
                            onClick = { group = "category"; groupDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("২. ভবনের ধরণ (Building Type)") },
                            onClick = { group = "building_type"; groupDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("৩. ব্যত্যয়ের ধরণ (Deviation Type)") },
                            onClick = { group = "deviation_type"; groupDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("৪. গৃহীত ব্যবস্থা (Action Taken)") },
                            onClick = { group = "action_taken"; groupDropdownExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("৫. অনিষ্পন্ন সময়কাল (Unresolved Duration)") },
                            onClick = { group = "unresolved_duration"; groupDropdownExpanded = false }
                        )
                    }
                }

                // Option Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("অপশনের নাম (Option Name / Label) *") },
                    placeholder = { Text("যেমন: নতুন ভবনের ধরণ বা ব্যত্যয়...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("option_name_input")
                )

                // Subgroup dropdown if deviation or action
                if (group == "deviation_type" || group == "action_taken") {
                    val subLabel = when (subGroup) {
                        "constructed" -> "নির্মিত ভবন"
                        "under_construction" -> "নির্মাণাধীন ভবন"
                        else -> "উভয় (সাধারণ)"
                    }
                    ExposedDropdownMenuBox(
                        expanded = subGroupDropdownExpanded,
                        onExpandedChange = { subGroupDropdownExpanded = !subGroupDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = subLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("প্রযোজ্য ভবনের ধরণ (Sub-group)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subGroupDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = subGroupDropdownExpanded,
                            onDismissRequest = { subGroupDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("নির্মিত ভবন") },
                                onClick = { subGroup = "constructed"; subGroupDropdownExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("নির্মাণাধীন ভবন") },
                                onClick = { subGroup = "under_construction"; subGroupDropdownExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("উভয় (সাধারণ)") },
                                onClick = { subGroup = "general"; subGroupDropdownExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        validationError = "অনুগ্রহ করে অপশনের নাম প্রদান করুন"
                        return@Button
                    }
                    onSave(group, name.trim(), subGroup.trim())
                },
                enabled = !isSaving
            ) {
                Text(if (isEditing) "আপডেট করুন" else "যোগ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
