package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionRateFilter
import com.example.data.model.ProgressStats
import com.example.ui.components.CompletedVsMissedPieChart
import com.example.ui.components.CompletionRateAnalyticsCard
import com.example.ui.components.CompletionTrendLineChart
import com.example.ui.components.HeatmapView
import com.example.ui.components.WeeklyBarChartView
import com.example.ui.theme.AccentHeart
import com.example.ui.theme.AccentHeartBg
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
    selectedFilter: CompletionRateFilter = CompletionRateFilter.MONTHLY,
    onFilterSelected: (CompletionRateFilter) -> Unit = {},
    onNavigateToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val monthYearDisplay = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
    val activeFilterStats = progressStats.getStatsForFilter(selectedFilter)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWideScreen = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (isWideScreen) 840.dp else 600.dp)
                .padding(horizontal = if (isWideScreen) 28.dp else 20.dp),
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
                    Column {
                        Text(
                            text = "Progress & Analytics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                        Text(
                            text = "Real-time insights from your completion history",
                            fontSize = 12.sp,
                            color = MonoGrayMedium
                        )
                    }

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

            // Streak, 100% Perfect Days & Recovery Lives Summary Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_streak_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 70% Daily Streak
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = MonoBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Daily Streak (≥70%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MonoGrayMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${progressStats.currentStreakDays} days",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack
                                )
                                Text(
                                    text = "Best: ${progressStats.bestStreakDays} days",
                                    fontSize = 10.sp,
                                    color = MonoGrayMedium
                                )
                            }

                            // 100% Perfect Days (tracked separately)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MonoBlack,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Perfect Days (100%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MonoGrayMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${progressStats.perfectDaysCount}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack,
                                    modifier = Modifier.testTag("perfect_days_count")
                                )
                                Text(
                                    text = "All habits done",
                                    fontSize = 10.sp,
                                    color = MonoGrayMedium
                                )
                            }

                            // Recovery Lives (Initial = 10, +1 every 7 streak days)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = AccentHeart,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Recovery Lives",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MonoGrayMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AccentHeartBg)
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${progressStats.livesCount} lives",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoBlack,
                                        modifier = Modifier.testTag("progress_lives_count")
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+1 life in ${progressStats.daysUntilNextLifeReward}d streak",
                                    fontSize = 10.sp,
                                    color = MonoGrayMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 7-day Life Reward Progress Dots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MonoSurfaceVariant)
                                .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Life Reward Cycle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MonoBlack
                            )
                            val cycleCompleted = 7 - progressStats.daysUntilNextLifeReward
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                for (i in 1..7) {
                                    val isFilled = i <= cycleCompleted
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isFilled) MonoBlack else MonoBorder,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Completion Rate Page / Card with Weekly, Monthly, Yearly Filters (§5)
            item {
                CompletionRateAnalyticsCard(
                    selectedFilter = selectedFilter,
                    stats = activeFilterStats,
                    onFilterSelected = onFilterSelected
                )
            }

            // Daily Activity Heatmap
            item {
                HeatmapView(
                    heatmapGrid = progressStats.heatmapGrid,
                    currentMonthCells = progressStats.currentMonthHeatmap,
                    currentMonthTitle = progressStats.currentMonthTitle,
                    isNewUser = progressStats.isNewUser,
                    modifier = Modifier.testTag("activity_heatmap")
                )
            }

            // Hide progress charts when there is insufficient completion history (§2 & §4)
            if (!progressStats.hasSufficientHistory) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("progress_empty_state_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MonoWhite),
                        border = BorderStroke(1.dp, MonoBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MonoGrayLight,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Not Enough Completion History Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Progress charts are hidden until you complete your first habit. Check off a habit today to unlock your Completion Trend Line Chart, Completed vs. Missed Breakdown, and Weekly Bar Chart!",
                                fontSize = 12.sp,
                                color = MonoGrayMedium,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNavigateToToday,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MonoBlack),
                                modifier = Modifier.testTag("empty_progress_go_today_button")
                            ) {
                                Text("Complete Today's Habits", color = MonoWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // 1. Line Chart Displaying Completion Trends Over Time (§4)
                item {
                    CompletionTrendLineChart(
                        trendPoints = progressStats.trendPoints
                    )
                }

                // 2. Pie / Donut Chart Showing Completed vs. Missed Habits (§4)
                item {
                    CompletedVsMissedPieChart(
                        completedCount = activeFilterStats.doneCount,
                        missedCount = activeFilterStats.missedCount,
                        periodLabel = activeFilterStats.periodLabel
                    )
                }

                // 3. Weekly Progress Bar Chart
                item {
                    WeeklyBarChartView(
                        weeklyData = progressStats.weeklyProgress,
                        modifier = Modifier.testTag("weekly_bar_chart")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
