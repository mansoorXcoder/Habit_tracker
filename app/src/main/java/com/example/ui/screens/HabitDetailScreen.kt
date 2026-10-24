package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.HabitLogEntity
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.MonthlyCalendarView
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitDetailScreen(
    habit: HabitEntity?,
    logs: List<HabitLogEntity>,
    onBackClick: () -> Unit,
    onEditHabitClick: (HabitEntity) -> Unit,
    onToggleDate: (String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (habit == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Habit not found", color = MonoGrayMedium)
        }
        return
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    var selectedCalendarDate by remember { mutableStateOf(dateFormat.format(Date())) }

    // 100% Dynamic Streak & Completion Calculations from actual stored logs
    val completedDates = logs.filter { it.completed }.map { it.dateString }.toSet()
    val totalCompletions = completedDates.size

    // Dynamic Current Streak calculation
    var currentStreak = 0
    val streakCal = Calendar.getInstance()
    val todayFormatted = dateFormat.format(streakCal.time)
    if (completedDates.contains(todayFormatted)) {
        currentStreak++
        streakCal.add(Calendar.DAY_OF_YEAR, -1)
    } else {
        streakCal.add(Calendar.DAY_OF_YEAR, -1)
        if (completedDates.contains(dateFormat.format(streakCal.time))) {
            currentStreak++
            streakCal.add(Calendar.DAY_OF_YEAR, -1)
        }
    }
    if (currentStreak > 0) {
        while (true) {
            if (completedDates.contains(dateFormat.format(streakCal.time))) {
                currentStreak++
                streakCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
    }

    // Dynamic Best Streak calculation
    val sortedDates = completedDates.sorted()
    var bestStreak = 0
    var tempStreak = 0
    var previousCal: Calendar? = null
    for (dStr in sortedDates) {
        val c = Calendar.getInstance()
        try {
            c.time = dateFormat.parse(dStr) ?: continue
        } catch (_: Exception) {
            continue
        }
        if (previousCal == null) {
            tempStreak = 1
        } else {
            val diffDays = ((c.timeInMillis - previousCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (diffDays == 1) {
                tempStreak++
            } else if (diffDays > 1) {
                tempStreak = 1
            }
        }
        if (tempStreak > bestStreak) {
            bestStreak = tempStreak
        }
        previousCal = c
    }
    bestStreak = maxOf(bestStreak, currentStreak)

    val isSelectedDateCompleted = completedDates.contains(selectedCalendarDate)
    val selectedDateFormatted = try {
        displayDateFormat.format(dateFormat.parse(selectedCalendarDate) ?: Date())
    } catch (_: Exception) {
        selectedCalendarDate
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("habit_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MonoBlack
                )
            }

            Text(
                text = "Habit Detail",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MonoBlack
            )

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("habit_detail_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = MonoBlack
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Habit") },
                        onClick = {
                            menuExpanded = false
                            onEditHabitClick(habit)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Habit", color = Color(0xFFDC2626)) },
                        onClick = {
                            menuExpanded = false
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Habit Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_detail_header_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HabitIconBadge(
                            iconName = habit.iconName,
                            size = 56.dp,
                            iconSize = 28.dp,
                            isRoundedRect = true
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = habit.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MonoSurfaceVariant)
                                        .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = habit.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MonoBlack
                                    )
                                }
                            }

                            if (habit.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = habit.subtitle,
                                    fontSize = 12.sp,
                                    color = MonoGrayMedium
                                )
                            }
                        }
                    }
                }
            }

            // Specs Card (Goal, Frequency, Reminder, Edit Habit)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                        SpecRow(icon = Icons.Default.Flag, label = "Goal", value = habit.goalDuration)
                        SpecRow(icon = Icons.Default.Repeat, label = "Frequency", value = habit.frequency)
                        SpecRow(icon = Icons.Default.Notifications, label = "Reminder", value = habit.reminderTime)
                        SpecClickableRow(
                            icon = Icons.Default.Edit,
                            label = "Edit Habit",
                            onClick = { onEditHabitClick(habit) }
                        )
                    }
                }
            }

            // Completion History Calendar
            item {
                MonthlyCalendarView(
                    habitLogs = logs,
                    selectedDate = selectedCalendarDate,
                    onDateClick = { clickedDate -> selectedCalendarDate = clickedDate },
                    modifier = Modifier.testTag("completion_history_calendar")
                )
            }

            // Date-Specific Completion Card (Shows status for selected date + toggle button)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoSurfaceVariant),
                    border = BorderStroke(1.dp, MonoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDateFormatted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSelectedDateCompleted) "Completed" else "Not completed",
                                fontSize = 11.sp,
                                color = if (isSelectedDateCompleted) MonoBlack else MonoGrayMedium,
                                fontWeight = if (isSelectedDateCompleted) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        Button(
                            onClick = { onToggleDate(selectedCalendarDate) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelectedDateCompleted) MonoWhite else MonoBlack,
                                contentColor = if (isSelectedDateCompleted) MonoBlack else MonoWhite
                            ),
                            border = BorderStroke(1.dp, MonoBorder)
                        ) {
                            Icon(
                                imageVector = if (isSelectedDateCompleted) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSelectedDateCompleted) "Mark Incomplete" else "Mark Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Streak History Card (100% Dynamic!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_history_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Streak History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3 stat circles: Current, Best, Total
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                StreakStatCircle(value = "$currentStreak", label = "Current")
                                StreakStatCircle(value = "$bestStreak", label = "Best")
                                StreakStatCircle(value = "$totalCompletions", label = "Total")
                            }

                            // Longest streak
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = MonoBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "Longest streak", fontSize = 10.sp, color = MonoGrayMedium)
                                    Text(
                                        text = "$bestStreak days",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Bottom Sticky Action Button: "Edit Habit" (Monochrome Black)
        Button(
            onClick = { onEditHabitClick(habit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .height(52.dp)
                .testTag("bottom_edit_habit_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
        ) {
            Text(
                text = "Edit Habit",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MonoWhite
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Habit?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${habit.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteHabit(habit.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }
}

@Composable
private fun SpecRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MonoGrayMedium, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 13.sp, color = MonoBlack)
        }
        Text(text = value, fontSize = 13.sp, color = MonoGrayMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SpecClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MonoGrayMedium, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 13.sp, color = MonoBlack)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MonoGrayMedium,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StreakStatCircle(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MonoSurfaceVariant)
                .border(BorderStroke(1.dp, MonoBorder), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MonoBlack
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = MonoGrayMedium)
    }
}
