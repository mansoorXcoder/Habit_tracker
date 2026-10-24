package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
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
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    progressStats: ProgressStats,
    modifier: Modifier = Modifier
) {
    val monthYearDisplay = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonoBlack
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MonoWhite)
                        .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MonoGrayMedium,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthYearDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MonoBlack
                    )
                }
            }
        }

        // Daily Activity Heatmap (Monochrome)
        item {
            HeatmapView(
                heatmapGrid = progressStats.heatmapGrid,
                modifier = Modifier.testTag("activity_heatmap")
            )
        }

        // Weekly Progress Bar Chart (Monochrome)
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
                // Completion Rate Card (Dynamic)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("completion_rate_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Completion Rate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
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
                                    Box(modifier = Modifier.size(6.dp).background(MonoBlack, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Done", fontSize = 9.sp, color = MonoGrayMedium)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalCompleted}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MonoBlack)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(MonoGrayLight, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Missed", fontSize = 9.sp, color = MonoGrayMedium)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalMissed}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MonoBlack)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(MonoBorder, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Total", fontSize = 9.sp, color = MonoGrayMedium)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${progressStats.totalScheduled}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MonoBlack)
                                }
                            }
                        }
                    }
                }

                // Current Streak Card (Dynamic)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("current_streak_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
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
                                tint = MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
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
                                color = MonoBlack
                            )
                            Text(
                                text = if (progressStats.currentStreakDays == 1) "day" else "days",
                                fontSize = 11.sp,
                                color = MonoGrayMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 7 Streak Progress Dots
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (i in 1..7) {
                                    val isFilled = i <= progressStats.currentStreakDays
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                if (isFilled) MonoBlack else MonoBorder,
                                                CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (progressStats.currentStreakDays > 0) "Keep it up!" else "Start a new streak!",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MonoBlack
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
