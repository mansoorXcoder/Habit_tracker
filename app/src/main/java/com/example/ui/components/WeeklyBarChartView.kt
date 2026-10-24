package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayCompletion
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite

@Composable
fun WeeklyBarChartView(
    weeklyData: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MonoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoBlack
                    )
                }

                Text(
                    text = "This Week",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MonoGrayMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Area with Y axis labels and Bars
            val maxTarget = (weeklyData.maxOfOrNull { it.targetCount } ?: 5).coerceAtLeast(1).toFloat()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis labels (max, half, 0)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "${maxTarget.toInt()}", fontSize = 10.sp, color = MonoGrayMedium)
                    Text(text = "${(maxTarget / 2).toInt()}", fontSize = 10.sp, color = MonoGrayMedium)
                    Text(text = "0", fontSize = 10.sp, color = MonoGrayMedium)
                }

                // Columns for each day
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { day ->
                        val ratio = if (maxTarget > 0) (day.completedCount.toFloat() / maxTarget).coerceIn(0f, 1f) else 0f
                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(durationMillis = 600),
                            label = "bar_ratio"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Bar Container
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MonoSurfaceVariant),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (animatedRatio > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedRatio)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MonoBlack)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = day.dayOfWeek,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MonoGrayMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
