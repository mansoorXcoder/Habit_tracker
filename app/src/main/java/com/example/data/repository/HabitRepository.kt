package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.db.SampleDataPreloader
import com.example.data.model.CategoryEntity
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
    val allCategories: Flow<List<CategoryEntity>> = habitDao.getAllCategories()

    fun getHabitById(id: Long): Flow<HabitEntity?> = habitDao.getHabitById(id)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

    // Combines habits with logs for any given date (default today)
    fun getHabitsForDate(dateString: String = getTodayDateString()): Flow<List<HabitWithLog>> {
        return combine(
            habitDao.getAllHabits(),
            habitDao.getLogsForDate(dateString),
            habitDao.getAllLogs()
        ) { habits, dateLogs, allLogs ->
            val logMap = dateLogs.associateBy { it.habitId }
            val logsByHabit = allLogs.groupBy { it.habitId }

            habits.map { habit ->
                val dateLog = logMap[habit.id]
                val isDone = dateLog?.completed == true
                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                val currentStreak = calculateSingleHabitStreak(habitLogs, dateString)

                HabitWithLog(
                    habit = habit,
                    isCompletedToday = isDone,
                    currentStreak = currentStreak,
                    totalCompletions = habitLogs.count { it.completed }
                )
            }
        }
    }

    fun getTodayHabits(): Flow<List<HabitWithLog>> = getHabitsForDate(getTodayDateString())

    fun getProgressStats(referenceDate: Date = Date()): Flow<ProgressStats> {
        return combine(
            habitDao.getAllHabits(),
            habitDao.getAllLogs()
        ) { habits, allLogs ->
            calculateDynamicProgressStats(habits, allLogs, referenceDate)
        }
    }

    private fun calculateSingleHabitStreak(logs: List<HabitLogEntity>, targetDateStr: String): Int {
        val completedDates = logs.filter { it.completed }.map { it.dateString }.toSet()
        if (completedDates.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        cal.time = try { dateFormat.parse(targetDateStr) ?: Date() } catch (_: Exception) { Date() }

        val todayFormatted = dateFormat.format(cal.time)
        // If target date is completed, count it
        if (completedDates.contains(todayFormatted)) {
            streak++
        } else {
            // Check if yesterday was completed
            cal.add(Calendar.DAY_OF_YEAR, -1)
            if (!completedDates.contains(dateFormat.format(cal.time))) {
                return 0
            }
            streak++
        }

        // Count backward consecutively
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

    private fun calculateDynamicProgressStats(
        habits: List<HabitEntity>,
        allLogs: List<HabitLogEntity>,
        referenceDate: Date
    ): ProgressStats {
        val habitCount = habits.size
        val logsByDate = allLogs.groupBy { it.dateString }

        // If no habits exist, return completely empty state
        if (habitCount == 0) {
            return ProgressStats(
                completionRatePercent = 0,
                totalCompleted = 0,
                totalMissed = 0,
                totalScheduled = 0,
                currentStreakDays = 0,
                bestStreakDays = 0,
                weeklyProgress = emptyWeeklyList(referenceDate),
                heatmapGrid = emptyHeatmapGrid(referenceDate),
                categoryStats = emptyList()
            )
        }

        // 1. Weekly Progress (last 7 days: Mon -> Sun)
        val weeklyList = mutableListOf<DayCompletion>()
        val cal = Calendar.getInstance()
        cal.time = referenceDate

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

        // 2. Heatmap Grid (7 rows Mon-Sun x ~30 weeks back)
        val heatmapGrid = mutableListOf<MutableList<HeatmapCell>>()
        for (r in 0..6) {
            heatmapGrid.add(mutableListOf())
        }

        val totalDays = 210
        val heatCal = Calendar.getInstance()
        heatCal.time = referenceDate
        heatCal.add(Calendar.DAY_OF_YEAR, -totalDays)
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
                ratio <= 0.70f -> 2
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

        // 3. Dynamic Completion Rate (over past 30 days of actual tracking)
        var totalScheduled = 0
        var totalCompletedInPeriod = 0
        val monthCal = Calendar.getInstance()
        monthCal.time = referenceDate
        for (d in 0..29) {
            val dStr = dateFormat.format(monthCal.time)
            val done = logsByDate[dStr]?.count { it.completed } ?: 0
            totalCompletedInPeriod += done
            totalScheduled += habitCount
            monthCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val totalMissed = (totalScheduled - totalCompletedInPeriod).coerceAtLeast(0)
        val completionRate = if (totalScheduled > 0) {
            ((totalCompletedInPeriod.toFloat() / totalScheduled.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // 4. Dynamic Overall Streak (Current & Best)
        val todayStr = dateFormat.format(referenceDate)
        var currentStreak = 0
        val streakCal = Calendar.getInstance()
        streakCal.time = referenceDate

        val todayDone = logsByDate[todayStr]?.count { it.completed } ?: 0
        if (todayDone > 0) {
            currentStreak++
            streakCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            streakCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayDone = logsByDate[dateFormat.format(streakCal.time)]?.count { it.completed } ?: 0
            if (yesterdayDone == 0) {
                currentStreak = 0
            } else {
                currentStreak++
                streakCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        if (currentStreak > 0) {
            while (true) {
                val dStr = dateFormat.format(streakCal.time)
                val doneCount = logsByDate[dStr]?.count { it.completed } ?: 0
                if (doneCount > 0) {
                    currentStreak++
                    streakCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Calculate Best Streak across all dates
        val activeDates = logsByDate.filter { it.value.any { log -> log.completed } }.keys.sorted()
        var bestStreak = 0
        var tempStreak = 0
        var previousCal: Calendar? = null

        for (dateStr in activeDates) {
            val c = Calendar.getInstance()
            try {
                c.time = dateFormat.parse(dateStr) ?: continue
            } catch (_: Exception) {
                continue
            }

            if (previousCal == null) {
                tempStreak = 1
            } else {
                val diffDays = ((c.timeInMillis - previousCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            previousCal = c
        }
        bestStreak = maxOf(bestStreak, currentStreak)

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
            completionRatePercent = completionRate,
            totalCompleted = totalCompletedInPeriod,
            totalMissed = totalMissed,
            totalScheduled = totalScheduled,
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            weeklyProgress = weeklyList,
            heatmapGrid = heatmapGrid,
            categoryStats = categoryStats
        )
    }

    private fun emptyWeeklyList(referenceDate: Date): List<DayCompletion> {
        val list = mutableListOf<DayCompletion>()
        val cal = Calendar.getInstance()
        cal.time = referenceDate
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        val diff = if (currentDay == Calendar.SUNDAY) 6 else currentDay - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -diff)
        for (i in 0..6) {
            list.add(DayCompletion(dayOfWeekFormat.format(cal.time), dateFormat.format(cal.time), 0, 0))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun emptyHeatmapGrid(referenceDate: Date): List<List<HeatmapCell>> {
        val grid = mutableListOf<MutableList<HeatmapCell>>()
        for (r in 0..6) grid.add(mutableListOf())
        val cal = Calendar.getInstance()
        cal.time = referenceDate
        cal.add(Calendar.DAY_OF_YEAR, -210)
        while (cal.time.before(referenceDate) || dateFormat.format(cal.time) == dateFormat.format(referenceDate)) {
            val dStr = dateFormat.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val rowIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            grid[rowIndex].add(HeatmapCell(dStr, 0, 0, 0))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return grid
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

    suspend fun addCategory(name: String): Long {
        return habitDao.insertCategory(CategoryEntity(name = name.trim(), isDefault = false))
    }

    suspend fun deleteCategory(categoryId: Long) {
        habitDao.deleteCategoryById(categoryId)
    }

    // Reset All Data: Wipes habits, logs, and custom categories back to clean blank slate
    suspend fun resetAllData() {
        habitDao.clearAllLogs()
        habitDao.clearAllHabits()
        habitDao.clearAllCategories()
        habitDao.insertCategories(SampleDataPreloader.defaultCategories)
    }
}
