package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitWithLog
import com.example.ui.components.CircularProgressGauge
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.HabitGreenContainer
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutline
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    userName: String,
    todayHabits: List<HabitWithLog>,
    onToggleHabit: (Long) -> Unit,
    onHabitClick: (Long) -> Unit,
    onAddHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = todayHabits.size
    val completedCount = todayHabits.count { it.isCompletedToday }
    val progressFloat = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedLinearProgress by animateFloatAsState(
        targetValue = progressFloat,
        label = "linear_progress"
    )

    val dateDisplay = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Greeting Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = HabitTextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = HabitTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🌱", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keep going! You're doing great.",
                        fontSize = 13.sp,
                        color = HabitTextSecondary
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = HabitTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Today's Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_progress_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, HabitOutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Gauge
                    CircularProgressGauge(
                        completed = completedCount,
                        total = totalCount,
                        size = 90.dp,
                        strokeWidth = 9.dp
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    // Text & Progress Bar
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Progress",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HabitTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$completedCount of $totalCount habits completed",
                            fontSize = 12.sp,
                            color = HabitTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = { animatedLinearProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = HabitGreenPrimary,
                            trackColor = HabitGreenContainer,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F6F4))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = HabitTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = dateDisplay,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = HabitTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Today's Habits Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Habits",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HabitTextPrimary
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(HabitGreenContainer)
                        .clickable { onAddHabitClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("add_habit_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = HabitGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitGreenPrimary
                    )
                }
            }
        }

        // Habit Items List
        items(todayHabits, key = { it.habit.id }) { item ->
            val habit = item.habit
            val isDone = item.isCompletedToday

            val checkBgColor by animateColorAsState(
                targetValue = if (isDone) HabitGreenPrimary else Color.Transparent,
                label = "check_bg"
            )
            val checkBorderColor by animateColorAsState(
                targetValue = if (isDone) HabitGreenPrimary else HabitOutline,
                label = "check_border"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHabitClick(habit.id) }
                    .testTag("habit_item_${habit.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, HabitOutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Habit Icon in soft circular badge
                    HabitIconBadge(
                        iconName = habit.iconName,
                        size = 46.dp,
                        iconSize = 22.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HabitTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (habit.subtitle.isNotBlank()) habit.subtitle else habit.goalDuration,
                            fontSize = 12.sp,
                            color = HabitTextSecondary
                        )
                    }

                    // Rounded Checkbox
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(checkBgColor)
                            .clickable { onToggleHabit(habit.id) }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isDone) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(checkBorderColor)
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.White)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
