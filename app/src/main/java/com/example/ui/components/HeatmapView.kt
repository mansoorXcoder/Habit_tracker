package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeatmapCell
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary
import com.example.ui.theme.Heatmap0
import com.example.ui.theme.Heatmap1
import com.example.ui.theme.Heatmap2
import com.example.ui.theme.Heatmap3

@Composable
fun HeatmapView(
    heatmapGrid: List<List<HeatmapCell>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HabitOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and Low -> High legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = HabitGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily Activity Heatmap",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitTextPrimary
                    )
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Low",
                        fontSize = 10.sp,
                        color = HabitTextSecondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.size(8.dp).background(Heatmap0, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Heatmap1, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Heatmap2, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Heatmap3, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "High",
                        fontSize = 10.sp,
                        color = HabitTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month labels + Grid
            val scrollState = rememberScrollState()
            LaunchedEffect(heatmapGrid) {
                scrollState.scrollTo(scrollState.maxValue)
            }

            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            Row(modifier = Modifier.fillMaxWidth()) {
                // Day of week labels on left
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 18.dp, end = 6.dp)
                ) {
                    dayLabels.forEach { label ->
                        Box(modifier = Modifier.height(12.dp), contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                color = HabitTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Scrollable contribution matrix
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    // Month labels
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct")
                        months.forEach { month ->
                            Text(
                                text = month,
                                fontSize = 10.sp,
                                color = HabitTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 7 rows of cells
                    if (heatmapGrid.isNotEmpty()) {
                        val colCount = heatmapGrid.maxOfOrNull { it.size } ?: 0
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (row in 0 until 7) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (col in 0 until colCount) {
                                        val cell = heatmapGrid.getOrNull(row)?.getOrNull(col)
                                        val color = when (cell?.level) {
                                            1 -> Heatmap1
                                            2 -> Heatmap2
                                            3 -> Heatmap3
                                            else -> Heatmap0
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(color, RoundedCornerShape(2.5.dp))
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
}
