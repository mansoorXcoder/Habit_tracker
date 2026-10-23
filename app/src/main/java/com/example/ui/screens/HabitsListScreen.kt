package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.HabitIcons
import com.example.ui.theme.HabitGreenContainer
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary

@Composable
fun HabitsListScreen(
    habits: List<HabitEntity>,
    onHabitClick: (Long) -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Health", "Productivity", "Learning", "Lifestyle")

    val filteredHabits = if (selectedCategoryFilter == "All") {
        habits
    } else {
        habits.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "All Habits",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HabitTextPrimary
                    )
                    Text(
                        text = "${habits.size} active routines",
                        fontSize = 13.sp,
                        color = HabitTextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(HabitGreenContainer)
                        .clickable { onAddHabitClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("habits_tab_add_button"),
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
                        text = "Add Habit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategoryFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) HabitGreenPrimary else Color.White)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else HabitTextSecondary
                        )
                    }
                }
            }
        }

        items(filteredHabits, key = { it.id }) { habit ->
            val (catBg, catText) = HabitIcons.getCategoryColors(habit.category)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHabitClick(habit.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, HabitOutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HabitIconBadge(
                        iconName = habit.iconName,
                        size = 46.dp,
                        iconSize = 22.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HabitTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(catBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = habit.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = catText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = habit.goalDuration,
                            fontSize = 12.sp,
                            color = HabitTextSecondary
                        )
                    }

                    // Reminder status pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (habit.reminderEnabled) HabitGreenContainer else Color(0xFFF1F5F2))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (habit.reminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (habit.reminderEnabled) HabitGreenPrimary else HabitTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = habit.reminderTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (habit.reminderEnabled) HabitGreenPrimary else HabitTextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
