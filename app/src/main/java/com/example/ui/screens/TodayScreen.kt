package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitWithLog
import com.example.data.model.ProgressStats
import com.example.data.model.UserProfileEntity
import com.example.ui.components.CircularProgressGauge
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.AccentHeart
import com.example.ui.theme.AccentHeartBg
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
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    userName: String,
    userProfile: UserProfileEntity = UserProfileEntity(),
    todayHabits: List<HabitWithLog>,
    progressStats: ProgressStats = ProgressStats(),
    onToggleHabit: (Long) -> Unit,
    onHabitClick: (Long) -> Unit,
    onAddHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDismissMorningBanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalCount = todayHabits.size
    val completedCount = todayHabits.count { it.isCompletedToday }
    val progressFloat = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val todayPercent = (progressFloat * 100f).roundToInt()
    val animatedLinearProgress by animateFloatAsState(
        targetValue = progressFloat,
        label = "linear_progress"
    )

    val now = Date()
    val dateDisplay = SimpleDateFormat("MMM d, yyyy", Locale.US).format(now)
    val todayDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hourOfDay < 12 -> "Good Morning,"
        hourOfDay < 17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }

    val showMorningBanner = userProfile.morningBannerDismissedDate != todayDateKey

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

                // Greeting Header + Lives & Streak Pill + Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MonoGrayMedium
                        )
                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Recovery Lives Badge (Initial = 10)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AccentHeartBg)
                                .border(BorderStroke(1.dp, AccentHeart.copy(alpha = 0.35f)), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("home_lives_badge"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Recovery Lives",
                                tint = AccentHeart,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progressStats.livesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                        }

                        // Streak Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MonoSurfaceVariant)
                                .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("home_streak_badge"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MonoBlack,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progressStats.currentStreakDays}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                        }

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MonoBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Daily Start Notification & Greeting Card
            if (showMorningBanner) {
                item {
                    val streakGoalCount = if (totalCount > 0) {
                        kotlin.math.ceil(totalCount * UserProfileEntity.STREAK_THRESHOLD_RATIO).toInt().coerceAtLeast(1)
                    } else {
                        0
                    }
                    val remainingForStreak = (streakGoalCount - completedCount).coerceAtLeast(0)
                    val dailySummaryLine = when {
                        totalCount == 0 -> "Start your day by adding between ${UserProfileEntity.MIN_HABITS_COUNT} and ${UserProfileEntity.MAX_HABITS_LIMIT} daily habits."
                        remainingForStreak == 0 -> "Daily streak goal reached ($completedCount/$totalCount done) · Keep going for a 100% Perfect Day!"
                        else -> "$totalCount habits scheduled today · Complete $remainingForStreak more to secure your ${progressStats.currentStreakDays + 1}-day streak."
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("morning_message_banner"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MonoSurfaceVariant),
                        border = BorderStroke(1.dp, MonoBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MonoBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = MonoWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daily Start · $dateDisplay (${userProfile.morningReminderTime})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoBlack
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = userProfile.customMorningMessage.ifBlank {
                                        "Small consistent actions build lasting discipline. Focus on your top priorities first today."
                                    },
                                    fontSize = 12.sp,
                                    color = MonoBlack,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dailySummaryLine,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MonoGrayMedium,
                                    lineHeight = 15.sp
                                )
                            }

                            IconButton(
                                onClick = onDismissMorningBanner,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss daily start message",
                                    tint = MonoGrayMedium,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Today's Progress Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_progress_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressGauge(
                            completed = completedCount,
                            total = totalCount,
                            size = 90.dp,
                            strokeWidth = 9.dp
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Progress",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$completedCount of $totalCount habits completed ($todayPercent%)",
                                fontSize = 12.sp,
                                color = MonoGrayMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { animatedLinearProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MonoBlack,
                                trackColor = MonoSurfaceVariant,
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Streak Eligibility Status (70% vs 100%)
                            val streakStatusText = when {
                                totalCount == 0 -> "Add a habit to start your streak"
                                todayPercent >= 100 -> "★ 100% Perfect Day & Daily Streak unlocked!"
                                todayPercent >= 70 -> "✓ 70% Daily Streak qualified! Complete all for a Perfect Day"
                                else -> "Reach 70% today to qualify for your daily streak"
                            }
                            Text(
                                text = streakStatusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MonoSurfaceVariant)
                                        .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MonoGrayMedium,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = dateDisplay,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MonoBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Today's Habits Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Habits",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonoBlack
                        )
                        Text(
                            text = "${todayHabits.size}/${progressStats.maxHabitsAllowed} habits (${UserProfileEntity.MIN_HABITS_COUNT}–${UserProfileEntity.MAX_HABITS_LIMIT} entries)",
                            fontSize = 11.sp,
                            color = MonoGrayMedium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MonoBlack)
                            .clickable { onAddHabitClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("add_habit_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Habit",
                            tint = MonoWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MonoWhite
                        )
                    }
                }
            }

            // Empty state when no habits exist
            if (todayHabits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MonoWhite),
                        border = BorderStroke(1.dp, MonoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MonoGrayLight
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No habits scheduled for today",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Add' to set up your first daily routine.",
                                fontSize = 12.sp,
                                color = MonoGrayMedium
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddHabitClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                            ) {
                                Text("Add Habit", color = MonoWhite)
                            }
                        }
                    }
                }
            }

            // Habit Items List (Fixed layout: displays BOTH description AND time pills cleanly without overlap)
            items(todayHabits, key = { it.habit.id }) { item ->
                val habit = item.habit
                val isDone = item.isCompletedToday

                val checkBgColor by animateColorAsState(
                    targetValue = if (isDone) MonoBlack else MonoWhite,
                    label = "check_bg"
                )
                val checkBorderColor by animateColorAsState(
                    targetValue = if (isDone) MonoBlack else MonoBorder,
                    label = "check_border"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHabitClick(habit.id) }
                        .testTag("habit_item_${habit.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HabitIconBadge(
                            iconName = habit.iconName,
                            size = 46.dp,
                            iconSize = 22.dp,
                            isSelected = isDone
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        // Structured Column displaying Title, Description, AND Duration + Reminder Time together without overlap
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = habit.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (habit.subtitle.isNotBlank()) {
                                Text(
                                    text = habit.subtitle,
                                    fontSize = 12.sp,
                                    color = MonoGrayMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Goal duration badge
                                if (habit.goalDuration.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MonoSurfaceVariant)
                                            .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = MonoGrayMedium,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = habit.goalDuration,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MonoBlack
                                        )
                                    }
                                }

                                // Reminder time badge
                                if (habit.reminderEnabled && habit.reminderTime.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MonoSurfaceVariant)
                                            .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MonoGrayMedium,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = habit.reminderTime,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MonoBlack
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Accessible 48dp minimum touch target wrapper around checkbox
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleHabit(habit.id) }
                                .testTag("toggle_habit_${habit.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(checkBgColor)
                                    .border(BorderStroke(1.5.dp, checkBorderColor), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = MonoWhite,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
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
}
