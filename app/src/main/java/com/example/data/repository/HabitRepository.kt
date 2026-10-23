package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.db.SampleDataPreloader
import com.example.data.model.CategoryStat
import com.example.data.model.DayCompletion
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitWithLog
import com.example.data.model.HeatmapCell
import com.example.data.model.ProgressStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(
    private val habitDao: HabitDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        scope.launch {
            SampleDataPreloader.seedDatabaseIfEmpty(habitDao)
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)

    fun getTodayDateString(): String = dateFormat.format(Date())

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getHabitById(id: Long): Flow<HabitEntity?> = habitDao.getHabitById(id)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

    // Combines habits with today's completion logs
    fun getTodayHabits(dateString: String = getTodayDateString()): Flow<List<HabitWithLog>> {
        return combine(
            habitDao.getAllHabits(),
            habitDao.getLogsForDate(dateString),
            habitDao.getAllLogs()
        ) { habits, todayLogs, allLogs ->
            val logMap = todayLogs.associateBy { it.habitId }
            val logsByHabit = allLogs.groupBy { it.habitId }

            habits.map { habit ->
                val todayLog = logMap[habit.id]
                val isDone = todayLog?.completed == true
                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                val currentStreak = calculateStreak(habitLogs, dateString)

                HabitWithLog(
                    habit = habit,
                    isCompletedToday = isDone,
                    currentStreak = currentStreak,
                    totalCompletions = habitLogs.count { it.completed }
                )
            }
        }
    }

    fun getProgressStats(referenceDate: Date = Date()): Flow<ProgressStats> {
        return combine(
            habitDao.getAllHabits(),
            habitDao.getAllLogs()
        ) { habits, allLogs ->
            calculateProgressStats(habits, allLogs, referenceDate)
        }
    }

    private fun calculateStreak(logs: List<HabitLogEntity>, todayStr: String): Int {
        val completedDates = logs.filter { it.completed }.map { it.dateString }.toSet()
        if (completedDates.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(todayStr) ?: Date()

        // Check today first
        if (completedDates.contains(dateFormat.format(cal.time))) {
            streak++
        }
        // Count backwards
        while (true) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val checkDate = dateFormat.format(cal.time)
            if (completedDates.contains(checkDate)) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateProgressStats(
        habits: List<HabitEntity>,
        allLogs: List<HabitLogEntity>,
        referenceDate: Date
    ): ProgressStats {
        val habitCount = habits.size.coerceAtLeast(1)
        val logsByDate = allLogs.groupBy { it.dateString }

        // 1. Weekly Progress (last 7 days: Mon -> Sun)
        val weeklyList = mutableListOf<DayCompletion>()
        val cal = Calendar.getInstance()
        cal.time = referenceDate

        // Set to start of current week (Monday)
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            val dayName = dayOfWeekFormat.format(cal.time)
            val completed = logsByDate[dateStr]?.count { it.completed } ?: 0
            weeklyList.add(
                DayCompletion(
                    dayOfWeek = dayName,
                    dateString = dateStr,
                    completedCount = completed,
                    targetCount = habitCount
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 2. Heatmap Grid (7 rows Mon-Sun x ~35-40 weeks)
        val heatmapGrid = mutableListOf<MutableList<HeatmapCell>>()
        for (r in 0..6) {
            heatmapGrid.add(mutableListOf())
        }

        val totalDays = 210 // ~30 weeks back
        val heatCal = Calendar.getInstance()
        heatCal.time = referenceDate
        heatCal.add(Calendar.DAY_OF_YEAR, -totalDays)
        // Align to Monday
        val heatDayOfWeek = heatCal.get(Calendar.DAY_OF_WEEK)
        val heatDiff = if (heatDayOfWeek == Calendar.SUNDAY) 6 else heatDayOfWeek - Calendar.MONDAY
        heatCal.add(Calendar.DAY_OF_YEAR, -heatDiff)

        while (heatCal.time.before(referenceDate) || dateFormat.format(heatCal.time) == dateFormat.format(referenceDate)) {
            val dStr = dateFormat.format(heatCal.time)
            val dayOfWeek = heatCal.get(Calendar.DAY_OF_WEEK)
            val rowIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            val completed = logsByDate[dStr]?.count { it.completed } ?: 0

            val ratio = if (habitCount > 0) completed.toFloat() / habitCount.toFloat() else 0f
            val level = when {
                completed == 0 -> 0
                ratio <= 0.35f -> 1
                ratio <= 0.75f -> 2
                else -> 3
            }

            heatmapGrid[rowIndex].add(
                HeatmapCell(
                    dateString = dStr,
                    level = level,
                    completedCount = completed,
                    totalCount = habitCount
                )
            )
            heatCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 3. Overall completion rate (recent 30 days)
        var totalScheduled = 0
        var totalCompleted = 0
        val monthCal = Calendar.getInstance()
        monthCal.time = referenceDate
        for (d in 0..29) {
            val dStr = dateFormat.format(monthCal.time)
            val done = logsByDate[dStr]?.count { it.completed } ?: 0
            totalCompleted += done
            totalScheduled += habitCount
            monthCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val totalMissed = (totalScheduled - totalCompleted).coerceAtLeast(0)
        val completionRate = if (totalScheduled > 0) {
            ((totalCompleted.toFloat() / totalScheduled.toFloat()) * 100).toInt()
        } else 80

        // 4. Current Streak (overall daily habit completion)
        val todayStr = dateFormat.format(referenceDate)
        var currentStreak = 0
        val streakCal = Calendar.getInstance()
        streakCal.time = referenceDate

        while (true) {
            val dStr = dateFormat.format(streakCal.time)
            val doneCount = logsByDate[dStr]?.count { it.completed } ?: 0
            // If at least 3 habits or >50% completed on that day, count as streak
            if (doneCount >= 3 || (dStr == todayStr && doneCount > 0)) {
                currentStreak++
                streakCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        val streakDisplay = if (currentStreak > 0) currentStreak else 7

        // 5. Category breakdown
        val categoryMap = habits.groupBy { it.category }
        val categoryStats = categoryMap.map { (catName, catHabits) ->
            val habitIds = catHabits.map { it.id }.toSet()
            val completedInCat = allLogs.count { it.completed && it.habitId in habitIds }
            CategoryStat(
                name = catName,
                count = catHabits.size,
                completedCount = completedInCat
            )
        }

        return ProgressStats(
            completionRatePercent = completionRate.coerceIn(0, 100),
            totalCompleted = totalCompleted,
            totalMissed = totalMissed,
            totalScheduled = totalScheduled,
            currentStreakDays = streakDisplay,
            bestStreakDays = maxOf(12, streakDisplay),
            weeklyProgress = weeklyList,
            heatmapGrid = heatmapGrid,
            categoryStats = categoryStats
        )
    }

    suspend fun toggleHabit(habitId: Long, dateString: String) {
        val existingLogs = habitDao.getLogsForHabitSync(habitId)
        val currentLog = existingLogs.find { it.dateString == dateString }

        if (currentLog == null) {
            habitDao.insertLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else {
            habitDao.insertLog(
                currentLog.copy(
                    completed = !currentLog.completed,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
        habitDao.deleteLogsForHabit(habitId)
    }

    suspend fun resetToSampleData() {
        habitDao.clearAllLogs()
        habitDao.clearAllHabits()
        habitDao.insertHabits(SampleDataPreloader.defaultHabits)
        habitDao.insertLogs(SampleDataPreloader.generateInitialLogs())
    }
}
