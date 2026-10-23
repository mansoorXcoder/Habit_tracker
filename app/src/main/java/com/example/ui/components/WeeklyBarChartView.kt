package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayCompletion
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary

@Composable
fun WeeklyBarChartView(
    weeklyData: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    var selectedWeekFilter by remember { mutableStateOf("This Week") }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, HabitOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Dropdown Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = HabitGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitTextPrimary
                    )
                }

                // Dropdown selector pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1F5F2))
                            .clickable { menuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWeekFilter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HabitTextSecondary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = HabitTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("This Week") },
                            onClick = {
                                selectedWeekFilter = "This Week"
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last Week") },
                            onClick = {
                                selectedWeekFilter = "Last Week"
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Area with Y axis labels and Bars
            val maxTarget = 5f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis labels (5, 4, 2, 0)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "5", fontSize = 10.sp, color = HabitTextSecondary)
                    Text(text = "4", fontSize = 10.sp, color = HabitTextSecondary)
                    Text(text = "2", fontSize = 10.sp, color = HabitTextSecondary)
                    Text(text = "0", fontSize = 10.sp, color = HabitTextSecondary)
                }

                // Columns for each day
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = if (weeklyData.isNotEmpty()) weeklyData else listOf(
                        DayCompletion("Mon", "", 3),
                        DayCompletion("Tue", "", 4),
                        DayCompletion("Wed", "", 5),
                        DayCompletion("Thu", "", 5),
                        DayCompletion("Fri", "", 4),
                        DayCompletion("Sat", "", 4),
                        DayCompletion("Sun", "", 5)
                    )

                    days.forEach { day ->
                        val ratio = (day.completedCount.toFloat() / maxTarget).coerceIn(0f, 1f)
                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(durationMillis = 600),
                            label = "bar_ratio"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Bar Container (height ~ 90dp)
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F2)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedRatio)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HabitGreenPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = day.dayOfWeek,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = HabitTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
