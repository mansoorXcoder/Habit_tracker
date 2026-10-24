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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.receiver.NotificationHelper
import com.example.ui.HabitViewModel
import com.example.ui.components.AddEditHabitBottomSheet
import com.example.ui.components.HabitBottomBar
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.HabitsListScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.HabitFlowTheme
import com.example.ui.theme.MonoBackgroundLight

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
    // Permission request for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val selectedTab by habitViewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedHabitId by habitViewModel.selectedHabitId.collectAsStateWithLifecycle()
    val userName by habitViewModel.userName.collectAsStateWithLifecycle()
    val isAddEditSheetOpen by habitViewModel.isAddEditSheetOpen.collectAsStateWithLifecycle()
    val habitToEdit by habitViewModel.habitToEdit.collectAsStateWithLifecycle()

    val todayHabits by habitViewModel.todayHabits.collectAsStateWithLifecycle()
    val progressStats by habitViewModel.progressStats.collectAsStateWithLifecycle()
    val allHabits by habitViewModel.allHabits.collectAsStateWithLifecycle()
    val allCategories by habitViewModel.allCategories.collectAsStateWithLifecycle()
    val habitDetailPair by habitViewModel.selectedHabitDetail.collectAsStateWithLifecycle()

    // Handle back button
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
                        todayHabits = todayHabits,
                        onToggleHabit = { habitId -> habitViewModel.toggleHabitToday(habitId) },
                        onHabitClick = { habitId -> habitViewModel.selectHabit(habitId) },
                        onAddHabitClick = { habitViewModel.openAddHabit() },
                        onSettingsClick = { habitViewModel.selectTab(3) }
                    )
                    1 -> ProgressScreen(
                        progressStats = progressStats
                    )
                    2 -> HabitsListScreen(
                        habits = allHabits,
                        categories = allCategories,
                        onHabitClick = { habitId -> habitViewModel.selectHabit(habitId) },
                        onAddHabitClick = { habitViewModel.openAddHabit() },
                        onEditHabitClick = { h -> habitViewModel.openEditHabit(h) },
                        onDeleteHabitClick = { id -> habitViewModel.deleteHabit(id) },
                        onAddCategoryClick = { name -> habitViewModel.addCustomCategory(name) }
                    )
                    3 -> ProfileScreen(
                        userName = userName,
                        onUpdateUserName = { newName -> habitViewModel.updateUserName(newName) },
                        onResetAllData = { habitViewModel.resetAllData() },
                        currentStreak = progressStats.currentStreakDays,
                        completionRate = progressStats.completionRatePercent,
                        totalHabits = allHabits.size
                    )
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
