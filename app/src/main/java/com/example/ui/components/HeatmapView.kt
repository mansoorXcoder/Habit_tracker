package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.HeatmapCell
import com.example.data.model.MonthHeatmapDay
import com.example.ui.theme.Heatmap0
import com.example.ui.theme.Heatmap1
import com.example.ui.theme.Heatmap2
import com.example.ui.theme.Heatmap3
import com.example.ui.theme.Heatmap4
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite

fun getHeatmapLevelColor(level: Int): Color = when (level) {
    1 -> Heatmap1
    2 -> Heatmap2
    3 -> Heatmap3
    4 -> Heatmap4
    else -> Heatmap0
}

@Composable
fun HeatmapView(
    heatmapGrid: List<List<HeatmapCell>>,
    currentMonthCells: List<MonthHeatmapDay> = emptyList(),
    currentMonthTitle: String = "",
    isNewUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    // New users initially see the current month's calendar heatmap
    var showMonthCalendar by remember(isNewUser) {
        mutableStateOf(isNewUser && currentMonthCells.isNotEmpty())
    }
    var selectedCellSummary by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MonoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Title and View Switcher (Month Calendar vs Full Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Daily Activity Heatmap",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                        if (currentMonthTitle.isNotBlank() && showMonthCalendar) {
                            Text(
                                text = currentMonthTitle,
                                fontSize = 11.sp,
                                color = MonoGrayMedium
                            )
                        }
                    }
                }

                if (currentMonthCells.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MonoSurfaceVariant)
                            .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (showMonthCalendar) MonoBlack else Color.Transparent)
                                .clickable { showMonthCalendar = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("heatmap_mode_month"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Month view",
                                    tint = if (showMonthCalendar) MonoWhite else MonoBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Month",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (showMonthCalendar) MonoWhite else MonoBlack
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!showMonthCalendar) MonoBlack else Color.Transparent)
                                .clickable { showMonthCalendar = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("heatmap_mode_grid"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Grid view",
                                    tint = if (!showMonthCalendar) MonoWhite else MonoBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Grid",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!showMonthCalendar) MonoWhite else MonoBlack
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showMonthCalendar && currentMonthCells.isNotEmpty()) {
                CurrentMonthHeatmapGrid(
                    cells = currentMonthCells,
                    onCellClick = { day ->
                        val status = when {
                            day.isFuture -> "Upcoming"
                            day.level == 4 -> "100% Perfect Day"
                            day.level == 3 -> "≥70% Streak Qualified"
                            day.completedCount > 0 -> "Partial Completion"
                            else -> "No completions"
                        }
                        selectedCellSummary = "${day.dateString}: ${day.completedCount}/${day.scheduledCount} habits ($status)"
                    }
                )
            } else {
                GitHubContributionMatrix(
                    heatmapGrid = heatmapGrid,
                    onCellClick = { cell ->
                        selectedCellSummary = "${cell.dateString}: ${cell.completedCount}/${cell.totalCount} habits completed"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend + Selected Date Tooltip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCellSummary ?: "Tap any day to inspect activity",
                    fontSize = 10.sp,
                    color = MonoGrayMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = "0%", fontSize = 9.sp, color = MonoGrayMedium)
                    listOf(Heatmap0, Heatmap1, Heatmap2, Heatmap3, Heatmap4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(color, RoundedCornerShape(2.dp))
                                .border(BorderStroke(0.5.dp, MonoBorder), RoundedCornerShape(2.dp))
                        )
                    }
                    Text(text = "100%", fontSize = 9.sp, color = MonoGrayMedium)
                }
            }
        }
    }
}

@Composable
private fun CurrentMonthHeatmapGrid(
    cells: List<MonthHeatmapDay>,
    onCellClick: (MonthHeatmapDay) -> Unit
) {
    val weekHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val rows = cells.chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("current_month_heatmap_grid"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Weekday header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekHeaders.forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoGrayMedium
                    )
                }
            }
        }

        // Calendar weeks
        rows.forEach { weekCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until 7) {
                    val cell = weekCells.getOrNull(i)
                    if (cell == null) {
                        Spacer(modifier = Modifier.weight(1f).height(34.dp))
                    } else {
                        val targetBg = when {
                            !cell.isCurrentMonth -> MonoSurfaceVariant.copy(alpha = 0.45f)
                            cell.isFuture -> MonoSurfaceVariant
                            else -> getHeatmapLevelColor(cell.level)
                        }
                        val animatedBg by animateColorAsState(
                            targetValue = targetBg,
                            animationSpec = tween(durationMillis = 250),
                            label = "month_cell_bg"
                        )
                        val textColor = when {
                            !cell.isCurrentMonth -> MonoGrayLight
                            cell.level >= 3 && !cell.isFuture -> MonoWhite
                            else -> MonoBlack
                        }
                        val borderColor = when {
                            cell.isToday -> MonoBlack
                            else -> MonoBorder
                        }
                        val borderWidth = if (cell.isToday) 1.8.dp else 0.8.dp

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(animatedBg)
                                .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(8.dp))
                                .clickable { onCellClick(cell) }
                                .testTag("heatmap_day_${cell.dateString}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${cell.dayOfMonth}",
                                    fontSize = 11.sp,
                                    fontWeight = if (cell.isToday || cell.level >= 3) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                                if (cell.isCurrentMonth && !cell.isFuture && cell.completedCount > 0) {
                                    Text(
                                        text = "${cell.completedCount}/${cell.scheduledCount}",
                                        fontSize = 7.sp,
                                        color = textColor.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubContributionMatrix(
    heatmapGrid: List<List<HeatmapCell>>,
    onCellClick: (HeatmapCell) -> Unit
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(heatmapGrid) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("github_heatmap_matrix")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            dayLabels.forEach { label ->
                Box(modifier = Modifier.height(13.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = MonoGrayMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            if (heatmapGrid.isNotEmpty()) {
                val colCount = heatmapGrid.maxOfOrNull { it.size } ?: 0
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until 7) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (col in 0 until colCount) {
                                val cell = heatmapGrid.getOrNull(row)?.getOrNull(col)
                                val targetColor = getHeatmapLevelColor(cell?.level ?: 0)
                                val animatedColor by animateColorAsState(
                                    targetValue = targetColor,
                                    animationSpec = tween(durationMillis = 250),
                                    label = "matrix_cell_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(animatedColor)
                                        .border(BorderStroke(0.5.dp, MonoBorder), RoundedCornerShape(3.dp))
                                        .clickable(enabled = cell != null) {
                                            if (cell != null) onCellClick(cell)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
