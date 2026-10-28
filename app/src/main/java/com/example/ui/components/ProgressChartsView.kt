package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionRateFilter
import com.example.data.model.CompletionRateStats
import com.example.data.model.TrendPoint
import com.example.ui.theme.Heatmap2
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite

@Composable
fun CompletionTrendLineChart(
    trendPoints: List<TrendPoint>,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = if (trendPoints.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "line_chart_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("completion_trend_line_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MonoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Completion Trend Over Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                        Text(
                            text = "14-day daily completion rate (dashed line = 70% streak goal)",
                            fontSize = 10.sp,
                            color = MonoGrayMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Y-Axis labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "100%", fontSize = 9.sp, color = MonoGrayMedium)
                    Text(text = "70%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MonoBlack)
                    Text(text = "0%", fontSize = 9.sp, color = MonoGrayMedium)
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val w = size.width
                        val h = size.height
                        val topPad = 8.dp.toPx()
                        val bottomPad = 8.dp.toPx()
                        val usableH = (h - topPad - bottomPad).coerceAtLeast(1f)

                        // Horizontal baseline (0%) and top line (100%)
                        drawLine(
                            color = MonoBorder,
                            start = Offset(0f, topPad),
                            end = Offset(w, topPad),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = MonoBorder,
                            start = Offset(0f, topPad + usableH),
                            end = Offset(w, topPad + usableH),
                            strokeWidth = 1.dp.toPx()
                        )

                        // 70% Streak Threshold Reference Line
                        val y70 = topPad + usableH * (1f - 0.70f)
                        drawLine(
                            color = MonoGrayLight,
                            start = Offset(0f, y70),
                            end = Offset(w, y70),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        if (trendPoints.isNotEmpty()) {
                            val count = trendPoints.size
                            val stepX = if (count > 1) w / (count - 1).toFloat() else w / 2f

                            val points = trendPoints.mapIndexed { index, pt ->
                                val x = if (count > 1) index * stepX else w / 2f
                                val normalized = (pt.completionPercent.coerceIn(0, 100) / 100f) * animProgress
                                val y = topPad + usableH * (1f - normalized)
                                Offset(x, y)
                            }

                            // Area fill under the line
                            if (points.size > 1) {
                                val fillPath = Path().apply {
                                    moveTo(points.first().x, topPad + usableH)
                                    points.forEach { lineTo(it.x, it.y) }
                                    lineTo(points.last().x, topPad + usableH)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    color = MonoBlack.copy(alpha = 0.08f)
                                )

                                // Main trend line
                                val linePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                drawPath(
                                    path = linePath,
                                    color = MonoBlack,
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            // Data nodes
                            points.forEachIndexed { idx, offset ->
                                val pt = trendPoints[idx]
                                drawCircle(
                                    color = MonoWhite,
                                    radius = 4.5.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = if (pt.metStreakThreshold) MonoBlack else MonoGrayLight,
                                    radius = 3.2.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // X-Axis Date Labels (First, Middle, Last)
                    if (trendPoints.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val first = trendPoints.first().shortLabel
                            val mid = trendPoints[trendPoints.size / 2].shortLabel
                            val last = trendPoints.last().shortLabel
                            Text(text = first, fontSize = 9.sp, color = MonoGrayMedium)
                            Text(text = mid, fontSize = 9.sp, color = MonoGrayMedium)
                            Text(text = last, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MonoBlack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedVsMissedPieChart(
    completedCount: Int,
    missedCount: Int,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    val total = (completedCount + missedCount).coerceAtLeast(0)
    val completedFraction = if (total > 0) (completedCount.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedCompletedFraction by animateFloatAsState(
        targetValue = completedFraction,
        animationSpec = tween(durationMillis = 800),
        label = "pie_completed_fraction"
    )
    val completedPercent = if (total > 0) ((completedCount.toFloat() / total.toFloat()) * 100f).toInt() else 0
    val missedPercent = if (total > 0) (100 - completedPercent).coerceAtLeast(0) else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("completed_vs_missed_pie_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MonoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Completed vs. Missed Habits",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonoBlack
                    )
                }

                if (periodLabel.isNotBlank()) {
                    Text(
                        text = periodLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MonoGrayMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut / Pie Canvas
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(116.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Missed segment background ring
                        drawArc(
                            color = MonoBorder,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Completed segment
                        if (animatedCompletedFraction > 0f) {
                            drawArc(
                                color = MonoBlack,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedCompletedFraction,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedPercent%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                        Text(
                            text = "Done",
                            fontSize = 10.sp,
                            color = MonoGrayMedium
                        )
                    }
                }

                // Breakdown Legend
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MonoBlack, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Completed ($completedPercent%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                            Text(
                                text = "$completedCount habits done",
                                fontSize = 11.sp,
                                color = MonoGrayMedium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MonoBorder, RoundedCornerShape(3.dp))
                                .border(BorderStroke(1.dp, Heatmap2), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Missed ($missedPercent%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                            Text(
                                text = "$missedCount habits missed",
                                fontSize = 11.sp,
                                color = MonoGrayMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionRateAnalyticsCard(
    selectedFilter: CompletionRateFilter,
    stats: CompletionRateStats,
    onFilterSelected: (CompletionRateFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("completion_rate_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        border = BorderStroke(1.dp, MonoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header + Weekly / Monthly / Yearly Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Completion Rate",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonoBlack
                    )
                    if (stats.periodLabel.isNotBlank()) {
                        Text(
                            text = stats.periodLabel,
                            fontSize = 11.sp,
                            color = MonoGrayMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonoSurfaceVariant)
                        .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionRateFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MonoBlack else Color.Transparent)
                                .clickable { onFilterSelected(filter) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("filter_${filter.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MonoWhite else MonoBlack
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress Indicator + 3 Metric Boxes (Done, Missed, Total Scheduled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonutRateChart(
                    percentage = stats.completionPercent,
                    size = 96.dp,
                    strokeWidth = 10.dp,
                    modifier = Modifier.testTag("overall_completion_gauge")
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompletionMetricRow(
                        label = "Done habits",
                        value = "${stats.doneCount}",
                        dotColor = MonoBlack,
                        testTag = "stat_done_habits"
                    )
                    CompletionMetricRow(
                        label = "Missed habits",
                        value = "${stats.missedCount}",
                        dotColor = MonoGrayLight,
                        testTag = "stat_missed_habits"
                    )
                    CompletionMetricRow(
                        label = "Total scheduled habits",
                        value = "${stats.totalScheduledCount}",
                        dotColor = MonoBorder,
                        testTag = "stat_total_scheduled_habits"
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionMetricRow(
    label: String,
    value: String,
    dotColor: Color,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MonoSurfaceVariant)
            .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
                    .border(BorderStroke(0.5.dp, MonoBlack), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MonoGrayMedium
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MonoBlack
        )
    }
}
