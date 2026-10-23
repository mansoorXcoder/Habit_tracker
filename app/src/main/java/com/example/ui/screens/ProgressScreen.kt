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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProgressStats
import com.example.ui.components.DonutRateChart
import com.example.ui.components.HeatmapView
import com.example.ui.components.WeeklyBarChartView
import com.example.ui.theme.CatHealthBg
import com.example.ui.theme.CatHealthText
import com.example.ui.theme.CatLearningBg
import com.example.ui.theme.CatLearningText
import com.example.ui.theme.CatLifestyleBg
import com.example.ui.theme.CatLifestyleText
import com.example.ui.theme.CatProductivityBg
import com.example.ui.theme.CatProductivityText
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    progressStats: ProgressStats,
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val monthYearDisplay = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Progress Header with Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = HabitTextPrimary
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = HabitTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthYearDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HabitTextPrimary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HabitTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Daily Activity Heatmap
        item {
            HeatmapView(
                heatmapGrid = progressStats.heatmapGrid,
                modifier = Modifier.testTag("activity_heatmap")
            )
        }

        // Weekly Progress Bar Chart
        item {
            WeeklyBarChartView(
                weeklyData = progressStats.weeklyProgress,
                modifier = Modifier.testTag("weekly_bar_chart")
            )
        }

        // Completion Rate & Current Streak (2 Cards side by side)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Completion Rate Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("completion_rate_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, HabitOutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = HabitGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Completion Rate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HabitTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DonutRateChart(
                                percentage = progressStats.completionRatePercent,
                                size = 68.dp,
                                strokeWidth = 8.dp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(HabitGreenPrimary, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Completed", fontSize = 9.sp, color = HabitTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalCompleted}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HabitTextPrimary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFCBD5E1), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Missed", fontSize = 9.sp, color = HabitTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalMissed}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HabitTextPrimary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF94A3B8), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Total", fontSize = 9.sp, color = HabitTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalScheduled}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HabitTextPrimary)
                                }
                            }
                        }
                    }
                }

                // Current Streak Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("current_streak_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, HabitOutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = HabitGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HabitTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${progressStats.currentStreakDays}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = HabitTextPrimary
                            )
                            Text(
                                text = "days",
                                fontSize = 11.sp,
                                color = HabitTextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Streak Progress Dots (7 dots)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (i in 1..7) {
                                    val isFilled = i <= progressStats.currentStreakDays
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                if (isFilled) HabitGreenPrimary else Color(0xFFE2EBE4),
                                                CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Keep it up!",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = HabitGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // Habit Categories Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_categories_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, HabitOutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = null,
                            tint = HabitGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Habit Categories",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HabitTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip("Health", CatHealthBg, CatHealthText) { onCategoryClick("Health") }
                        CategoryChip("Productivity", CatProductivityBg, CatProductivityText) { onCategoryClick("Productivity") }
                        CategoryChip("Learning", CatLearningBg, CatLearningText) { onCategoryClick("Learning") }
                        CategoryChip("Lifestyle", CatLifestyleBg, CatLifestyleText) { onCategoryClick("Lifestyle") }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    bg: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
