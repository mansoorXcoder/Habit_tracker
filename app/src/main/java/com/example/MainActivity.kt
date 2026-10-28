package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FeedbackType
import com.example.receiver.NotificationHelper
import com.example.ui.HabitViewModel
import com.example.ui.components.AddEditHabitBottomSheet
import com.example.ui.components.HabitBottomBar
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.HabitsListScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentErrorBg
import com.example.ui.theme.HabitFlowTheme
import com.example.ui.theme.MonoBackgroundLight
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoWhite
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            HabitFlowTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(habitViewModel: HabitViewModel = viewModel()) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            habitViewModel.updateNotificationPermission(granted)
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val selectedTab by habitViewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedHabitId by habitViewModel.selectedHabitId.collectAsStateWithLifecycle()
    val userName by habitViewModel.userName.collectAsStateWithLifecycle()
    val userProfile by habitViewModel.userProfile.collectAsStateWithLifecycle()
    val completionRateFilter by habitViewModel.completionRateFilter.collectAsStateWithLifecycle()
    val feedbackMessage by habitViewModel.feedbackMessage.collectAsStateWithLifecycle()
    val hasNotificationPermission by habitViewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val isAddEditSheetOpen by habitViewModel.isAddEditSheetOpen.collectAsStateWithLifecycle()
    val habitToEdit by habitViewModel.habitToEdit.collectAsStateWithLifecycle()

    val todayHabits by habitViewModel.todayHabits.collectAsStateWithLifecycle()
    val progressStats by habitViewModel.progressStats.collectAsStateWithLifecycle()
    val allHabits by habitViewModel.allHabits.collectAsStateWithLifecycle()
    val allCategories by habitViewModel.allCategories.collectAsStateWithLifecycle()
    val habitDetailPair by habitViewModel.selectedHabitDetail.collectAsStateWithLifecycle()

    // Auto-dismiss success feedback after 3.5 seconds
    LaunchedEffect(feedbackMessage?.id) {
        val current = feedbackMessage ?: return@LaunchedEffect
        if (current.type == FeedbackType.SUCCESS) {
            delay(3500)
            habitViewModel.dismissFeedback()
        }
    }

    BackHandler(enabled = selectedHabitId != null || selectedTab != 0) {
        if (selectedHabitId != null) {
            habitViewModel.clearSelectedHabit()
        } else if (selectedTab != 0) {
            habitViewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MonoBackgroundLight,
        bottomBar = {
            HabitBottomBar(
                selectedTab = if (selectedHabitId != null) 2 else selectedTab,
                onTabSelected = { tabIndex ->
                    habitViewModel.selectTab(tabIndex)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MonoBackgroundLight)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            if (selectedHabitId != null) {
                val (habit, logs) = habitDetailPair
                HabitDetailScreen(
                    habit = habit,
                    logs = logs,
                    onBackClick = { habitViewModel.clearSelectedHabit() },
                    onEditHabitClick = { h -> habitViewModel.openEditHabit(h) },
                    onToggleDate = { dateStr ->
                        if (habit != null) {
                            habitViewModel.toggleHabitForDate(habit.id, dateStr)
                        }
                    },
                    onDeleteHabit = { id -> habitViewModel.deleteHabit(id) }
                )
            } else {
                when (selectedTab) {
                    0 -> TodayScreen(
                        userName = userName,
                        userProfile = userProfile,
                        todayHabits = todayHabits,
                        progressStats = progressStats,
                        onToggleHabit = { habitId -> habitViewModel.toggleHabitToday(habitId) },
                        onHabitClick = { habitId -> habitViewModel.selectHabit(habitId) },
                        onAddHabitClick = { habitViewModel.openAddHabit() },
                        onSettingsClick = { habitViewModel.selectTab(3) },
                        onDismissMorningBanner = { habitViewModel.dismissMorningBannerToday() }
                    )
                    1 -> ProgressScreen(
                        progressStats = progressStats,
                        selectedFilter = completionRateFilter,
                        onFilterSelected = { filter -> habitViewModel.selectCompletionRateFilter(filter) },
                        onNavigateToToday = { habitViewModel.selectTab(0) }
                    )
                    2 -> HabitsListScreen(
                        habits = allHabits,
                        categories = allCategories,
                        isHabitLimitUnlocked = progressStats.isHabitLimitUnlocked,
                        maxHabitsAllowed = progressStats.maxHabitsAllowed,
                        onHabitClick = { habitId -> habitViewModel.selectHabit(habitId) },
                        onAddHabitClick = { habitViewModel.openAddHabit() },
                        onEditHabitClick = { h -> habitViewModel.openEditHabit(h) },
                        onDeleteHabitClick = { id -> habitViewModel.deleteHabit(id) },
                        onAddCategoryClick = { name -> habitViewModel.addCustomCategory(name) }
                    )
                    3 -> ProfileScreen(
                        userName = userName,
                        userProfile = userProfile,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onUpdateUserName = { newName -> habitViewModel.updateUserName(newName) },
                        onUpdateMorningSettings = { enabled, time, msg ->
                            habitViewModel.updateMorningSettings(enabled, time, msg)
                        },
                        onSendTestNotification = { habitViewModel.sendTestMorningNotification() },
                        onSyncAccount = { email, key -> habitViewModel.syncAccountData(email, key) },
                        onExportBackup = { cb -> habitViewModel.exportBackupJson(cb) },
                        onImportBackup = { json -> habitViewModel.importBackupJson(json) },
                        onResetAllData = { habitViewModel.resetAllData() },
                        currentStreak = progressStats.currentStreakDays,
                        completionRate = progressStats.completionRatePercent,
                        totalHabits = allHabits.size,
                        livesCount = progressStats.livesCount,
                        perfectDaysCount = progressStats.perfectDaysCount
                    )
                }
            }

            // Floating Feedback Banner (Success, Failure, Retry)
            AnimatedVisibility(
                visible = feedbackMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val msg = feedbackMessage
                if (msg != null) {
                    val isError = msg.type == FeedbackType.ERROR
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) AccentErrorBg else MonoBlack
                        ),
                        border = BorderStroke(1.dp, if (isError) AccentError else MonoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isError) AccentError else MonoWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isError) MonoBlack else MonoWhite
                                )
                            }

                            if (msg.retryLabel != null && msg.onRetry != null) {
                                TextButton(
                                    onClick = { habitViewModel.triggerRetry() },
                                    modifier = Modifier.testTag("feedback_retry_button")
                                ) {
                                    Text(
                                        text = msg.retryLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isError) AccentError else MonoWhite
                                    )
                                }
                            }

                            IconButton(
                                onClick = { habitViewModel.dismissFeedback() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = if (isError) MonoBlack else MonoWhite,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddEditSheetOpen) {
        AddEditHabitBottomSheet(
            habitToEdit = habitToEdit,
            availableCategories = allCategories,
            onDismiss = { habitViewModel.closeAddEditSheet() },
            onSave = { id, title, subtitle, category, iconName, goal, freq, reminder, reminderEnabled ->
                habitViewModel.saveHabit(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    category = category,
                    iconName = iconName,
                    goalDuration = goal,
                    frequency = freq,
                    reminderTime = reminder,
                    reminderEnabled = reminderEnabled
                )
            },
            onDelete = { id -> habitViewModel.deleteHabit(id) }
        )
    }
}
