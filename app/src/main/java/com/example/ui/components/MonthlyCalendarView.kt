package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitLogEntity
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitOutlineVariant
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyCalendarView(
    habitLogs: List<HabitLogEntity>,
    onDateClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedDates = habitLogs.filter { it.completed }.map { it.dateString }.toSet()
    val todayCal = Calendar.getInstance()
    val todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val currentMonthStr = SimpleDateFormat("MMM yyyy", Locale.US).format(todayCal.time)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, HabitOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = HabitGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Completion History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HabitTextPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F2))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMonthStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = HabitTextSecondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HabitTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of week headers (Mon - Sun)
            val weekDayHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDayHeaders.forEach { dayName ->
                    Box(
                        modifier = Modifier.width(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HabitTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Days Grid
            // Build 5-6 rows matching the current month layout
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysFromMon = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY

            // Start from Monday of the first row
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMon)

            val currentMonth = todayCal.get(Calendar.MONTH)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (week in 0 until 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 0 until 7) {
                            val isCurrentMonth = cal.get(Calendar.MONTH) == currentMonth
                            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                            val dateStr = dateFormat.format(cal.time)
                            val isCompleted = completedDates.contains(dateStr)
                            val isToday = isCurrentMonth && dayNum == todayDayOfMonth

                            val cellModifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onDateClick(dateStr) }

                            Box(
                                modifier = when {
                                    isCompleted -> cellModifier.background(HabitGreenPrimary, CircleShape)
                                    isToday -> cellModifier.border(1.5.dp, HabitGreenPrimary, CircleShape)
                                    else -> cellModifier
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isCompleted || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCompleted -> Color.White
                                        isToday -> HabitGreenPrimary
                                        !isCurrentMonth -> Color(0xFFBAC5BD)
                                        else -> HabitTextPrimary
                                    }
                                )
                            }

                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                }
            }
        }
    }
}
