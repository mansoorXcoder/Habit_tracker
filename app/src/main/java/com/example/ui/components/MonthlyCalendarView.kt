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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitLogEntity
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyCalendarView(
    habitLogs: List<HabitLogEntity>,
    selectedDate: String,
    onDateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    // Calendar state for month navigation
    var currentDisplayCal by remember {
        val cal = Calendar.getInstance()
        try {
            cal.time = dateFormat.parse(selectedDate) ?: Date()
        } catch (_: Exception) {
            cal.time = Date()
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val todayCal = Calendar.getInstance()
    val todayDateStr = dateFormat.format(todayCal.time)

    val completedDates = habitLogs.filter { it.completed }.map { it.dateString }.toSet()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MonoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Month Navigation (< Month Year >)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Completion History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoBlack
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MonoSurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newCal = currentDisplayCal.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            currentDisplayCal = newCal
                        },
                        modifier = Modifier.size(28.dp).testTag("cal_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = MonoBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = monthYearFormat.format(currentDisplayCal.time),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoBlack,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            val newCal = currentDisplayCal.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            currentDisplayCal = newCal
                        },
                        modifier = Modifier.size(28.dp).testTag("cal_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = MonoBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                            color = MonoGrayMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days Grid
            val cal = currentDisplayCal.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysFromMon = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMon)

            val displayMonth = currentDisplayCal.get(Calendar.MONTH)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 0 until 7) {
                            val isCurrentMonth = cal.get(Calendar.MONTH) == displayMonth
                            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                            val dateStr = dateFormat.format(cal.time)
                            val isCompleted = completedDates.contains(dateStr)
                            val isSelected = dateStr == selectedDate
                            val isToday = dateStr == todayDateStr

                            val cellModifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onDateClick(dateStr) }

                            Box(
                                modifier = when {
                                    isCompleted && isSelected -> cellModifier
                                        .background(MonoBlack, CircleShape)
                                        .border(2.dp, MonoGrayMedium, CircleShape)
                                    isCompleted -> cellModifier.background(MonoBlack, CircleShape)
                                    isSelected -> cellModifier.border(2.dp, MonoBlack, CircleShape)
                                    isToday -> cellModifier.background(MonoSurfaceVariant, CircleShape)
                                    else -> cellModifier
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isCompleted || isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCompleted -> Color.White
                                        isSelected -> MonoBlack
                                        isToday -> MonoBlack
                                        !isCurrentMonth -> MonoGrayLight
                                        else -> MonoBlack
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
