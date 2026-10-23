package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.HabitEntity
import com.example.ui.theme.HabitGreenContainer
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutline
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitBottomSheet(
    habitToEdit: HabitEntity?,
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
    var goalDuration by remember { mutableStateOf(habitToEdit?.goalDuration ?: "1 hour per day") }
    var frequency by remember { mutableStateOf(habitToEdit?.frequency ?: "Daily") }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: "9:00 AM") }
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: true) }

    val categories = listOf("Productivity", "Health", "Learning", "Lifestyle")
    val icons = listOf("code", "book", "fitness", "water", "moon", "run", "meditation", "music")
    val frequencies = listOf("Daily", "Weekdays", "Weekends")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
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
                    color = HabitTextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = HabitTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Title
            Text(text = "Habit Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Read, Workout, Code") },
                modifier = Modifier.fillMaxWidth().testTag("habit_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HabitGreenPrimary,
                    unfocusedBorderColor = HabitOutline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle / Note
            Text(text = "Subtitle / Motivation Note", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                placeholder = { Text("e.g. 30 minutes, Build something every day") },
                modifier = Modifier.fillMaxWidth().testTag("habit_subtitle_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HabitGreenPrimary,
                    unfocusedBorderColor = HabitOutline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(text = "Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.equals(category, ignoreCase = true)
                    val (catBg, catText) = HabitIcons.getCategoryColors(cat)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) catBg else Color(0xFFF3F6F4))
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) catText else HabitTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Selector
            Text(text = "Icon", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(icons) { iconKey ->
                    val isSelected = iconKey == iconName
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) HabitGreenPrimary else HabitGreenContainer)
                            .clickable { iconName = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = HabitIcons.getIconVector(iconKey),
                            contentDescription = iconKey,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) Color.White else HabitGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Duration & Frequency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Goal Target", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = goalDuration,
                        onValueChange = { goalDuration = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HabitGreenPrimary,
                            unfocusedBorderColor = HabitOutline
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Frequency", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HabitTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F6F4))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        frequencies.forEach { freq ->
                            val isSelected = freq == frequency
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { frequency = freq }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (freq == "Weekdays") "Wkday" else if (freq == "Weekends") "Wkend" else freq,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) HabitGreenPrimary else HabitTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Time and Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF7FAF7))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = HabitGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Reminder",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HabitTextPrimary
                        )
                        Text(
                            text = reminderTime,
                            fontSize = 12.sp,
                            color = HabitTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick time cycler button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HabitGreenContainer)
                            .clickable {
                                reminderTime = when (reminderTime) {
                                    "7:00 AM" -> "8:00 AM"
                                    "8:00 AM" -> "9:00 AM"
                                    "9:00 AM" -> "10:00 AM"
                                    "10:00 AM" -> "12:00 PM"
                                    "12:00 PM" -> "6:00 PM"
                                    "6:00 PM" -> "9:00 PM"
                                    "9:00 PM" -> "10:30 PM"
                                    else -> "7:00 AM"
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Change Time",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = HabitGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HabitGreenPrimary
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
                            goalDuration,
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
                colors = ButtonDefaults.buttonColors(containerColor = HabitGreenPrimary)
            ) {
                Text(
                    text = if (habitToEdit == null) "Create Habit" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Delete Habit", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
