package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitWithLog
import com.example.data.model.ProgressStats
import com.example.data.repository.HabitRepository
import com.example.receiver.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = HabitRepository(database.habitDao(), viewModelScope)

    // Current navigation state
    private val _selectedTab = MutableStateFlow(0) // 0: Today, 1: Progress, 2: Habits, 3: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    val selectedHabitId: StateFlow<Long?> = _selectedHabitId.asStateFlow()

    private val _userName = MutableStateFlow("Mansoor")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Add / Edit Modal state
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _habitToEdit = MutableStateFlow<HabitEntity?>(null)
    val habitToEdit: StateFlow<HabitEntity?> = _habitToEdit.asStateFlow()

    // Calendar selected date (Format: YYYY-MM-DD)
    private val _selectedCalendarDate = MutableStateFlow(repository.getTodayDateString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

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

    // Dynamic habits for whatever calendar date is currently clicked/selected
    val selectedDateHabits: StateFlow<List<HabitWithLog>> = _selectedCalendarDate
        .flatMapLatest { dateStr -> repository.getHabitsForDate(dateStr) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active habit detail (combining selected habit with all its logs)
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

    fun selectTab(tabIndex: Int) {
        _selectedHabitId.value = null
        _selectedTab.value = tabIndex
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
        viewModelScope.launch {
            repository.toggleHabit(habitId, repository.getTodayDateString())
        }
    }

    fun toggleHabitForDate(habitId: Long, dateString: String) {
        viewModelScope.launch {
            repository.toggleHabit(habitId, dateString)
        }
    }

    fun openAddHabit() {
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
                title = title,
                subtitle = subtitle,
                category = category,
                iconName = iconName,
                goalDuration = goalDuration,
                frequency = frequency,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )

            val savedId = if (id == 0L) {
                repository.addHabit(habit)
            } else {
                repository.updateHabit(habit)
                id
            }

            // Schedule or cancel reminder
            val context = getApplication<Application>()
            if (reminderEnabled) {
                val parts = reminderTime.split(" ")
                val timeParts = parts.getOrNull(0)?.split(":")
                var hour = timeParts?.getOrNull(0)?.toIntOrNull() ?: 9
                val minute = timeParts?.getOrNull(1)?.toIntOrNull() ?: 0
                val isPm = parts.getOrNull(1)?.equals("PM", ignoreCase = true) == true
                if (isPm && hour < 12) hour += 12
                if (!isPm && hour == 12) hour = 0

                NotificationHelper.scheduleHabitReminder(
                    context = context,
                    habitId = savedId,
                    habitTitle = title,
                    hourOfDay = hour,
                    minute = minute
                )
            } else {
                NotificationHelper.cancelReminder(context, savedId)
            }

            closeAddEditSheet()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), habitId)
            repository.deleteHabit(habitId)
            if (_selectedHabitId.value == habitId) {
                _selectedHabitId.value = null
            }
        }
    }

    fun addCustomCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCategory(name.trim())
        }
    }

    fun deleteCustomCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun updateUserName(newName: String) {
        _userName.value = newName
    }

    // Reset All Data: Clears database back to clean blank slate with default categories
    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _selectedHabitId.value = null
            _selectedCalendarDate.value = repository.getTodayDateString()
        }
    }
}
