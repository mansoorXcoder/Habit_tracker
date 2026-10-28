package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.CompletionRateFilter
import com.example.data.model.FeedbackType
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitWithLog
import com.example.data.model.ProgressStats
import com.example.data.model.UiFeedbackMessage
import com.example.data.model.UserProfileEntity
import com.example.data.repository.HabitRepository
import com.example.receiver.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = HabitRepository(database.habitDao(), viewModelScope, application)

    // Navigation state: 0 = Today, 1 = Progress, 2 = Habits, 3 = Profile
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    val selectedHabitId: StateFlow<Long?> = _selectedHabitId.asStateFlow()

    // Completion Rate Filter state (Weekly, Monthly, Yearly)
    private val _completionRateFilter = MutableStateFlow(CompletionRateFilter.MONTHLY)
    val completionRateFilter: StateFlow<CompletionRateFilter> = _completionRateFilter.asStateFlow()

    // Add / Edit Modal state
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _habitToEdit = MutableStateFlow<HabitEntity?>(null)
    val habitToEdit: StateFlow<HabitEntity?> = _habitToEdit.asStateFlow()

    // Calendar selected date (Format: YYYY-MM-DD)
    private val _selectedCalendarDate = MutableStateFlow(repository.getTodayDateString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    // UI Feedback (Success, Failure, Retry)
    private val _feedbackMessage = MutableStateFlow<UiFeedbackMessage?>(null)
    val feedbackMessage: StateFlow<UiFeedbackMessage?> = _feedbackMessage.asStateFlow()

    // Notification permission status
    private val _hasNotificationPermission = MutableStateFlow(
        NotificationHelper.hasNotificationPermission(application)
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    val userName: StateFlow<String> = userProfile
        .map { it.userName.ifBlank { "Mansoor" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Mansoor"
        )

    val todayHabits: StateFlow<List<HabitWithLog>> = repository.getTodayHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val progressStats: StateFlow<ProgressStats> = repository.getProgressStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProgressStats()
        )

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedDateHabits: StateFlow<List<HabitWithLog>> = _selectedCalendarDate
        .flatMapLatest { dateStr -> repository.getHabitsForDate(dateStr) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedHabitDetail: StateFlow<Pair<HabitEntity?, List<HabitLogEntity>>> = combine(
        _selectedHabitId,
        repository.allHabits,
        database.habitDao().getAllLogs()
    ) { habitId, habits, allLogs ->
        if (habitId == null) {
            Pair(null, emptyList())
        } else {
            val habit = habits.find { it.id == habitId }
            val logs = allLogs.filter { it.habitId == habitId }
            Pair(habit, logs)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(null, emptyList())
    )

    fun updateNotificationPermission(granted: Boolean) {
        _hasNotificationPermission.value = granted
        if (granted) {
            val profile = userProfile.value
            if (profile.morningReminderEnabled) {
                NotificationHelper.scheduleMorningReminder(
                    context = getApplication(),
                    userName = profile.userName,
                    customMessage = profile.customMorningMessage,
                    timeString = profile.morningReminderTime
                )
            }
        }
    }

    fun dismissFeedback() {
        _feedbackMessage.value = null
    }

    fun triggerRetry() {
        val retryFn = _feedbackMessage.value?.onRetry ?: return
        _feedbackMessage.value = null
        viewModelScope.launch {
            retryFn()
        }
    }

    private fun postSuccess(message: String) {
        _feedbackMessage.value = UiFeedbackMessage(
            message = message,
            type = FeedbackType.SUCCESS
        )
    }

    private fun postError(message: String, retryAction: (suspend () -> Unit)? = null) {
        _feedbackMessage.value = UiFeedbackMessage(
            message = message,
            type = FeedbackType.ERROR,
            retryLabel = if (retryAction != null) "Retry" else null,
            onRetry = retryAction
        )
    }

    fun selectTab(tabIndex: Int) {
        _selectedHabitId.value = null
        _selectedTab.value = tabIndex
    }

    fun selectCompletionRateFilter(filter: CompletionRateFilter) {
        _completionRateFilter.value = filter
    }

    fun selectHabit(habitId: Long) {
        _selectedHabitId.value = habitId
    }

    fun clearSelectedHabit() {
        _selectedHabitId.value = null
    }

    fun selectCalendarDate(dateString: String) {
        _selectedCalendarDate.value = dateString
    }

    fun toggleHabitToday(habitId: Long) {
        val todayStr = repository.getTodayDateString()
        toggleHabitForDate(habitId, todayStr)
    }

    fun toggleHabitForDate(habitId: Long, dateString: String) {
        viewModelScope.launch {
            val result = repository.toggleHabit(habitId, dateString)
            result.onFailure { err ->
                postError("Failed to update habit completion: ${err.localizedMessage ?: "Database error"}") {
                    toggleHabitForDate(habitId, dateString)
                }
            }
        }
    }

    fun openAddHabit() {
        val stats = progressStats.value
        if (!stats.isHabitLimitUnlocked && allHabits.value.size >= stats.maxHabitsAllowed) {
            postError(
                "Starter limit reached (${stats.maxHabitsAllowed} habits). Complete habits on 3 days to unlock unlimited habits!"
            )
            return
        }
        _habitToEdit.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditHabit(habit: HabitEntity) {
        _habitToEdit.value = habit
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _habitToEdit.value = null
    }

    fun saveHabit(
        id: Long = 0,
        title: String,
        subtitle: String,
        category: String,
        iconName: String,
        goalDuration: String,
        frequency: String,
        reminderTime: String,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val habit = HabitEntity(
                id = id,
                title = title.trim(),
                subtitle = subtitle.trim(),
                category = category.trim(),
                iconName = iconName.trim(),
                goalDuration = goalDuration.trim(),
                frequency = frequency.trim(),
                reminderTime = reminderTime.trim(),
                reminderEnabled = reminderEnabled
            )

            val saveResult: Result<Long> = if (id == 0L) {
                repository.addHabit(habit)
            } else {
                repository.updateHabit(habit).map { id }
            }

            saveResult.onSuccess { savedId ->
                val context = getApplication<Application>()
                if (reminderEnabled) {
                    val (hour, minute) = NotificationHelper.parseTimeString(reminderTime)
                    NotificationHelper.scheduleHabitReminder(
                        context = context,
                        habitId = savedId,
                        habitTitle = habit.title,
                        hourOfDay = hour,
                        minute = minute
                    )
                } else {
                    NotificationHelper.cancelReminder(context, savedId)
                }
                closeAddEditSheet()
                postSuccess(if (id == 0L) "Habit '${habit.title}' created & saved." else "Habit '${habit.title}' updated.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Could not save habit.") {
                    saveHabit(id, title, subtitle, category, iconName, goalDuration, frequency, reminderTime, reminderEnabled)
                }
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), habitId)
            repository.deleteHabit(habitId).onSuccess {
                if (_selectedHabitId.value == habitId) {
                    _selectedHabitId.value = null
                }
                closeAddEditSheet()
                postSuccess("Habit deleted.")
            }.onFailure { err ->
                postError("Failed to delete habit: ${err.localizedMessage ?: "Unknown error"}") {
                    deleteHabit(habitId)
                }
            }
        }
    }

    fun addCustomCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCategory(name.trim()).onSuccess {
                postSuccess("Category '${name.trim()}' added.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Failed to add category.")
            }
        }
    }

    fun deleteCustomCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id).onSuccess {
                postSuccess("Category removed.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Failed to remove category.")
            }
        }
    }

    fun updateUserName(newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.updateUserProfile { it.copy(userName = clean) }.onSuccess { profile ->
                if (profile.morningReminderEnabled) {
                    NotificationHelper.scheduleMorningReminder(
                        context = getApplication(),
                        userName = profile.userName,
                        customMessage = profile.customMorningMessage,
                        timeString = profile.morningReminderTime
                    )
                }
                postSuccess("Display name saved as '$clean'.")
            }.onFailure { err ->
                postError("Failed to save name: ${err.localizedMessage ?: "Error"}") {
                    updateUserName(clean)
                }
            }
        }
    }

    fun updateMorningSettings(
        enabled: Boolean,
        reminderTime: String,
        customMessage: String
    ) {
        viewModelScope.launch {
            repository.updateUserProfile {
                it.copy(
                    morningReminderEnabled = enabled,
                    morningReminderTime = reminderTime.trim(),
                    customMorningMessage = customMessage.trim()
                )
            }.onSuccess { updated ->
                val context = getApplication<Application>()
                if (enabled) {
                    NotificationHelper.scheduleMorningReminder(
                        context = context,
                        userName = updated.userName,
                        customMessage = updated.customMorningMessage,
                        timeString = updated.morningReminderTime
                    )
                    postSuccess("Morning reminder scheduled for ${updated.morningReminderTime}.")
                } else {
                    NotificationHelper.cancelMorningReminder(context)
                    postSuccess("Morning reminder disabled.")
                }
            }.onFailure { err ->
                postError("Failed to save morning reminder settings: ${err.localizedMessage ?: "Error"}") {
                    updateMorningSettings(enabled, reminderTime, customMessage)
                }
            }
        }
    }

    fun sendTestMorningNotification() {
        val context = getApplication<Application>()
        val profile = userProfile.value
        val sent = NotificationHelper.showImmediateMorningNotification(
            context = context,
            userName = profile.userName,
            message = profile.customMorningMessage
        )
        if (sent) {
            postSuccess("Morning notification sent!")
        } else {
            postError("Notification permission is disabled. Grant notification permission in settings to receive alerts.")
        }
    }

    fun dismissMorningBannerToday() {
        val todayStr = repository.getTodayDateString()
        viewModelScope.launch {
            repository.updateUserProfile { it.copy(morningBannerDismissedDate = todayStr) }
        }
    }

    fun syncAccountData(email: String, syncKey: String) {
        viewModelScope.launch {
            repository.backupManager.syncAccountData(email, syncKey).onSuccess { msg ->
                postSuccess(msg)
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Account sync failed.") {
                    syncAccountData(email, syncKey)
                }
            }
        }
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            repository.backupManager.createSnapshotJson().onSuccess { json ->
                onResult(json)
                postSuccess("Backup snapshot generated.")
            }.onFailure { err ->
                postError("Failed to export backup: ${err.localizedMessage ?: "Error"}")
            }
        }
    }

    fun importBackupJson(jsonString: String) {
        viewModelScope.launch {
            repository.backupManager.restoreFromSnapshotJson(jsonString).onSuccess { count ->
                postSuccess("Successfully recovered $count habits and completion history!")
            }.onFailure { err ->
                postError("Backup recovery failed: ${err.localizedMessage ?: "Invalid backup format"}") {
                    importBackupJson(jsonString)
                }
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData().onSuccess {
                _selectedHabitId.value = null
                _selectedCalendarDate.value = repository.getTodayDateString()
                postSuccess("All data reset to a clean slate.")
            }.onFailure { err ->
                postError("Failed to reset data: ${err.localizedMessage ?: "Error"}") {
                    resetAllData()
                }
            }
        }
    }
}
