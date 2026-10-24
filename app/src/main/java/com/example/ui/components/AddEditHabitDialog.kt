package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitBottomSheet(
    habitToEdit: HabitEntity?,
    availableCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        subtitle: String,
        category: String,
        iconName: String,
        goalDuration: String,
        frequency: String,
        reminderTime: String,
        reminderEnabled: Boolean
    ) -> Unit,
    onDelete: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(habitToEdit?.title ?: "") }
    var subtitle by remember { mutableStateOf(habitToEdit?.subtitle ?: "") }
    var category by remember { mutableStateOf(habitToEdit?.category ?: "Productivity") }
    var iconName by remember { mutableStateOf(habitToEdit?.iconName ?: "code") }
    var goalDuration by remember { mutableStateOf(habitToEdit?.goalDuration ?: "30 minutes") }
    var frequency by remember { mutableStateOf(habitToEdit?.frequency ?: "Daily") }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: "9:00 AM") }
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: true) }

    // Dialog state for Time Picker
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Selected icon category filter
    var selectedIconGroupIndex by remember { mutableIntStateOf(0) }

    val categoryNames = if (availableCategories.isNotEmpty()) {
        availableCategories.map { it.name }
    } else {
        listOf("Productivity", "Health", "Learning", "Lifestyle")
    }

    if (category !in categoryNames && categoryNames.isNotEmpty()) {
        category = categoryNames.first()
    }

    val frequencies = listOf("Daily", "Weekdays", "Weekends")
    val durationPresets = listOf("5 min", "10 min", "15 min", "20 min", "30 min", "45 min", "60 min")

    // Contextual duration suggestions based on habit title
    val contextualSuggestions = remember(title) {
        val lower = title.lowercase()
        when {
            "run" in lower || "jog" in lower -> listOf("20 min", "30 min", "45 min")
            "read" in lower || "book" in lower -> listOf("15 min", "30 min", "1 hour")
            "code" in lower || "study" in lower -> listOf("45 min", "1 hour", "2 hours")
            "water" in lower || "drink" in lower -> listOf("8 glasses", "2 liters", "1 gallon")
            "meditat" in lower || "breath" in lower -> listOf("5 min", "10 min", "15 min")
            "workout" in lower || "gym" in lower -> listOf("30 min", "45 min", "1 hour")
            else -> emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MonoWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (habitToEdit == null) "New Habit" else "Edit Habit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonoBlack
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MonoBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Name (High contrast input colors)
            Text(text = "Habit Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Morning Run, Read Books, Code", color = MonoGrayMedium) },
                modifier = Modifier.fillMaxWidth().testTag("habit_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonoBlack,
                    unfocusedBorderColor = MonoBorder,
                    focusedTextColor = MonoBlack,
                    unfocusedTextColor = MonoBlack,
                    cursorColor = MonoBlack,
                    focusedContainerColor = MonoWhite,
                    unfocusedContainerColor = MonoWhite
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Motivation Note / Subtitle
            Text(text = "Motivation Note / Subtitle", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                placeholder = { Text("e.g. Build something every day", color = MonoGrayMedium) },
                modifier = Modifier.fillMaxWidth().testTag("habit_subtitle_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonoBlack,
                    unfocusedBorderColor = MonoBorder,
                    focusedTextColor = MonoBlack,
                    unfocusedTextColor = MonoBlack,
                    cursorColor = MonoBlack,
                    focusedContainerColor = MonoWhite,
                    unfocusedContainerColor = MonoWhite
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection (Dynamic Categories)
            Text(text = "Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryNames) { cat ->
                    val isSelected = cat.equals(category, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MonoBlack else MonoWhite)
                            .border(BorderStroke(1.dp, if (isSelected) MonoBlack else MonoBorder), RoundedCornerShape(16.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Categorized Icon Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Icon", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
                Text(
                    text = HabitIcons.iconGroups[selectedIconGroupIndex].categoryName,
                    fontSize = 11.sp,
                    color = MonoGrayMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Icon Group selector tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(HabitIcons.iconGroups.indices.toList()) { index ->
                    val isSelected = index == selectedIconGroupIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MonoBlack else MonoSurfaceVariant)
                            .clickable { selectedIconGroupIndex = index }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = HabitIcons.iconGroups[index].categoryName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Icons in currently selected group
            val currentGroupIcons = HabitIcons.iconGroups[selectedIconGroupIndex].iconKeys
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentGroupIcons) { iconKey ->
                    val isSelected = iconKey == iconName
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MonoBlack else MonoSurfaceVariant)
                            .border(BorderStroke(1.dp, if (isSelected) MonoBlack else MonoBorder), RoundedCornerShape(12.dp))
                            .clickable { iconName = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = HabitIcons.getIconVector(iconKey),
                            contentDescription = iconKey,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Goal Duration & Smart Suggestions
            Text(text = "Goal Duration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
            Spacer(modifier = Modifier.height(6.dp))

            // Contextual suggestions if relevant
            if (contextualSuggestions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    contextualSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MonoSurfaceVariant)
                                .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(10.dp))
                                .clickable { goalDuration = suggestion }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "★ $suggestion", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MonoBlack)
                        }
                    }
                }
            }

            // Quick preset chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(durationPresets) { preset ->
                    val isSelected = preset == goalDuration
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MonoBlack else MonoWhite)
                            .border(BorderStroke(1.dp, if (isSelected) MonoBlack else MonoBorder), RoundedCornerShape(10.dp))
                            .clickable { goalDuration = preset }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = goalDuration,
                onValueChange = { goalDuration = it },
                placeholder = { Text("e.g. 30 minutes, 1 hour, 8 glasses", color = MonoGrayMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonoBlack,
                    unfocusedBorderColor = MonoBorder,
                    focusedTextColor = MonoBlack,
                    unfocusedTextColor = MonoBlack,
                    cursorColor = MonoBlack
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            Text(text = "Frequency", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MonoSurfaceVariant)
                    .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                frequencies.forEach { freq ->
                    val isSelected = freq == frequency
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MonoBlack else Color.Transparent)
                            .clickable { frequency = freq }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = freq,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Daily Reminder Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MonoSurfaceVariant),
                border = BorderStroke(1.dp, MonoBorder)
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
                        modifier = Modifier.clickable { showTimePickerDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MonoBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daily Reminder",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reminderTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MonoBlack
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Change time", tint = MonoGrayMedium, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MonoWhite,
                            checkedTrackColor = MonoBlack,
                            uncheckedThumbColor = MonoGrayLight,
                            uncheckedTrackColor = MonoBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            habitToEdit?.id ?: 0L,
                            title.trim(),
                            subtitle.trim(),
                            category,
                            iconName,
                            goalDuration.trim(),
                            frequency,
                            reminderTime,
                            reminderEnabled
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_habit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonoBlack,
                    contentColor = MonoWhite,
                    disabledContainerColor = MonoBorder,
                    disabledContentColor = MonoGrayLight
                )
            ) {
                Text(
                    text = if (habitToEdit == null) "Create Habit" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (habitToEdit != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onDelete(habitToEdit.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_habit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Delete Habit", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Interactive Time Picker Dialog
    if (showTimePickerDialog) {
        CustomTimePickerDialog(
            initialTime = reminderTime,
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { newTime ->
                reminderTime = newTime
                showTimePickerDialog = false
            }
        )
    }
}

@Composable
fun CustomTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    // Parse initial time e.g. "9:00 AM" or "10:30 PM"
    var selectedHour by remember {
        val parts = initialTime.split(" ")
        val timeParts = parts.getOrNull(0)?.split(":")
        val h = timeParts?.getOrNull(0)?.toIntOrNull() ?: 9
        mutableIntStateOf(if (h in 1..12) h else 9)
    }

    var selectedMinute by remember {
        val parts = initialTime.split(" ")
        val timeParts = parts.getOrNull(0)?.split(":")
        val m = timeParts?.getOrNull(1)?.toIntOrNull() ?: 0
        mutableIntStateOf(m)
    }

    var isAm by remember {
        val isPm = initialTime.contains("PM", ignoreCase = true)
        mutableStateOf(!isPm)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder Time", fontWeight = FontWeight.Bold, color = MonoBlack) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Time Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "$selectedHour:${if (selectedMinute < 10) "0$selectedMinute" else selectedMinute}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonoBlack
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // AM / PM Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoSurfaceVariant)
                            .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAm) MonoBlack else Color.Transparent)
                                .clickable { isAm = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("AM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isAm) MonoWhite else MonoBlack)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isAm) MonoBlack else Color.Transparent)
                                .clickable { isAm = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("PM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isAm) MonoWhite else MonoBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hour selectors (1 to 12)
                Text("Hour", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MonoGrayMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..12).toList()) { hour ->
                        val isSelected = hour == selectedHour
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MonoBlack else MonoSurfaceVariant)
                                .clickable { selectedHour = hour },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$hour",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MonoWhite else MonoBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Minute selectors (00, 15, 30, 45)
                Text("Minute", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MonoGrayMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0, 15, 30, 45).forEach { min ->
                        val isSelected = min == selectedMinute
                        val label = if (min == 0) "00" else "$min"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MonoBlack else MonoSurfaceVariant)
                                .clickable { selectedMinute = min }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MonoWhite else MonoBlack
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amPm = if (isAm) "AM" else "PM"
                    val minStr = if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute"
                    onTimeSelected("$selectedHour:$minStr $amPm")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
            ) {
                Text("Set Time", color = MonoWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MonoBlack)
            }
        }
    )
}
