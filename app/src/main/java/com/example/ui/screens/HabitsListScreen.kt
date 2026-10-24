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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite

@Composable
fun HabitsListScreen(
    habits: List<HabitEntity>,
    categories: List<CategoryEntity>,
    onHabitClick: (Long) -> Unit,
    onAddHabitClick: () -> Unit,
    onEditHabitClick: (HabitEntity) -> Unit,
    onDeleteHabitClick: (Long) -> Unit,
    onAddCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    val categoryNames = listOf("All") + categories.map { it.name }

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

            // Header: Title & "+ Add Habit" Button
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
                        color = MonoBlack
                    )
                    Text(
                        text = "${habits.size} active routines",
                        fontSize = 13.sp,
                        color = MonoGrayMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MonoBlack)
                        .clickable { onAddHabitClick() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("habits_tab_add_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = MonoWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Habit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Management Row (+ Add Category button & Category chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Category Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MonoSurfaceVariant)
                        .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(16.dp))
                        .clickable {
                            newCategoryName = ""
                            showAddCategoryDialog = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Category", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Scrollable Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryNames) { cat ->
                        val isSelected = cat == selectedCategoryFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MonoBlack else MonoWhite)
                                .border(BorderStroke(1.dp, if (isSelected) MonoBlack else MonoBorder), RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
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
            }
        }

        // Empty State
        if (filteredHabits.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MonoGrayLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (habits.isEmpty()) "No habits yet" else "No habits in this category",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build a consistent routine by creating your first habit.",
                        fontSize = 12.sp,
                        color = MonoGrayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddHabitClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Habit", color = MonoWhite)
                    }
                }
            }
        }

        // Habit Cards
        items(filteredHabits, key = { it.id }) { habit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHabitClick(habit.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MonoWhite),
                border = BorderStroke(1.dp, MonoBorder),
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
                                color = MonoBlack
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MonoSurfaceVariant)
                                    .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = habit.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MonoBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = habit.goalDuration,
                            fontSize = 12.sp,
                            color = MonoGrayMedium
                        )

                        if (habit.subtitle.isNotBlank()) {
                            Text(
                                text = habit.subtitle,
                                fontSize = 11.sp,
                                color = MonoGrayLight
                            )
                        }
                    }

                    // Actions: Edit & Delete buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Reminder pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MonoSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (habit.reminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (habit.reminderEnabled) MonoBlack else MonoGrayLight,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = habit.reminderTime,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (habit.reminderEnabled) MonoBlack else MonoGrayMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { onEditHabitClick(habit) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MonoBlack, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { habitToDelete = habit },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New Habit Category", fontWeight = FontWeight.Bold, color = MonoBlack) },
            text = {
                Column {
                    Text("Create a custom category to organize your routines:", fontSize = 12.sp, color = MonoGrayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Finances, Creativity, Reading") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonoBlack,
                            unfocusedBorderColor = MonoBorder,
                            focusedTextColor = MonoBlack,
                            unfocusedTextColor = MonoBlack
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategoryClick(newCategoryName.trim())
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                ) {
                    Text("Add", color = MonoWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }

    // Delete Habit Confirmation Dialog
    if (habitToDelete != null) {
        val habit = habitToDelete!!
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete '${habit.title}'?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this habit? Its past history will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabitClick(habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }
}
