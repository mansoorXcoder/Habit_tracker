package com.example.data.repository

import android.content.Context
import com.example.data.db.HabitDao
import com.example.data.db.SampleDataPreloader
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryStat
import com.example.data.model.CompletionRateFilter
import com.example.data.model.CompletionRateStats
import com.example.data.model.DayCompletion
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitWithLog
import com.example.data.model.HeatmapCell
import com.example.data.model.MonthHeatmapDay
import com.example.data.model.ProgressStats
import com.example.data.model.TrendPoint
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HabitRepository(
    private val habitDao: HabitDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    context: Context? = null,
    autoInit: Boolean = true
) {
    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val MAX_SUBTITLE_LENGTH = 200
        const val MAX_CATEGORY_LENGTH = 50
        const val MAX_DURATION_LENGTH = 50

        fun validateHabitFields(title: String, subtitle: String, category: String, goalDuration: String) {
            require(title.isNotEmpty()) { "Habit title cannot be empty." }
            require(title.length <= MAX_TITLE_LENGTH) {
                "Habit title cannot exceed $MAX_TITLE_LENGTH characters."
            }
            require(subtitle.length <= MAX_SUBTITLE_LENGTH) {
                "Habit description cannot exceed $MAX_SUBTITLE_LENGTH characters."
            }
            require(category.isNotEmpty() && category.length <= MAX_CATEGORY_LENGTH) {
                "Category name must be between 1 and $MAX_CATEGORY_LENGTH characters."
            }
            require(goalDuration.length <= MAX_DURATION_LENGTH) {
                "Goal duration cannot exceed $MAX_DURATION_LENGTH characters."
            }
        }
    }

    val backupManager = BackupRecoveryManager(habitDao, context)

    init {
        if (autoInit) {
            scope.launch {
                backupManager.tryAutoRecoverIfDatabaseCleared()
                SampleDataPreloader.seedDatabaseIfEmpty(habitDao)
            }
        }
    }

    private fun newDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun newDayOfWeekFormat() = SimpleDateFormat("EEE", Locale.US)
    private fun newShortDateFormat() = SimpleDateFormat("MMM d", Locale.US)
    private fun newMonthYearFormat() = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getTodayDateString(now: Date = Date()): String = newDateFormat().format(now)

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allCategories: Flow<List<CategoryEntity>> = habitDao.getAllCategories()
    val userProfile: Flow<UserProfileEntity?> = habitDao.getUserProfile()

    fun getHabitById(id: Long): Flow<HabitEntity?> = habitDao.getHabitById(id)
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

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
                val currentStreak = calculateSingleHabitStreak(habit, habitLogs, dateString)
                val isScheduled = isHabitScheduledOnDate(habit, dateString, includeBeforeCreated = true)

                HabitWithLog(
                    habit = habit,
                    isCompletedToday = isDone,
                    isScheduledToday = isScheduled,
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
            habitDao.getAllLogs(),
            habitDao.getUserProfile()
        ) { habits, allLogs, profile ->
            val effectiveProfile = profile ?: UserProfileEntity()
            val stats = calculateDynamicProgressStats(habits, allLogs, effectiveProfile, referenceDate)
            stats
        }
    }

    fun isHabitScheduledOnDate(
        habit: HabitEntity,
        dateString: String,
        earliestLogDate: String? = null,
        includeBeforeCreated: Boolean = false
    ): Boolean {
        val df = newDateFormat()
        val targetCal = parseDateToNormalizedCalendar(dateString, df) ?: return true

        if (!includeBeforeCreated) {
            val createdDateStr = df.format(Date(habit.createdAt))
            val effectiveStartStr = if (earliestLogDate != null && earliestLogDate < createdDateStr) {
                earliestLogDate
            } else {
                createdDateStr
            }
            if (dateString < effectiveStartStr) {
                return false
            }
        }

        val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
        return when (habit.frequency.trim().lowercase()) {
            "weekdays" -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            "weekends" -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            else -> true // "Daily"
        }
    }

    fun calculateSingleHabitStreak(
        habit: HabitEntity,
        logs: List<HabitLogEntity>,
        targetDateStr: String
    ): Int {
        val df = newDateFormat()
        val completedDates = logs.filter { it.completed }.map { it.dateString }.toSet()
        if (completedDates.isEmpty()) return 0

        val cal = parseDateToNormalizedCalendar(targetDateStr, df) ?: return 0
        var streak = 0

        val todayStr = df.format(cal.time)
        if (completedDates.contains(todayStr)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            // Skip backwards over unscheduled days (e.g. weekend for a Weekdays habit)
            var skippedUnscheduled = 0
            while (skippedUnscheduled < 7 && !isHabitScheduledOnDate(habit, df.format(cal.time), includeBeforeCreated = true)) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                skippedUnscheduled++
            }
            if (!completedDates.contains(df.format(cal.time))) {
                return 0
            }
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var safetyCounter = 0
        while (safetyCounter < 1000) {
            safetyCounter++
            val checkStr = df.format(cal.time)
            if (!isHabitScheduledOnDate(habit, checkStr, includeBeforeCreated = true)) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                continue
            }
            if (completedDates.contains(checkStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateDynamicProgressStats(
        habits: List<HabitEntity>,
        allLogs: List<HabitLogEntity>,
        profile: UserProfileEntity = UserProfileEntity(),
        referenceDate: Date = Date()
    ): ProgressStats {
        val df = newDateFormat()
        val dayFormat = newDayOfWeekFormat()
        val shortFormat = newShortDateFormat()
        val monthYearFormat = newMonthYearFormat()

        val todayCal = normalizeCalendar(referenceDate)
        val todayStr = df.format(todayCal.time)

        val validHabitIds = habits.map { it.id }.toSet()
        val validLogs = allLogs.filter { it.habitId in validHabitIds }
        val completedLogs = validLogs.filter { it.completed }
        val logsByDate = validLogs.groupBy { it.dateString }
        val earliestLogByHabit = completedLogs
            .groupBy { it.habitId }
            .mapValues { entry -> entry.value.minOf { it.dateString } }

        val activeLoggedDates = completedLogs.map { it.dateString }.distinct().sorted()
        val hasSufficientHistory = completedLogs.isNotEmpty()
        val isNewUser = activeLoggedDates.size < 7

        // Streak & Recovery Lives Evaluation
        val streakEval = evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logsByDate,
            earliestLogByHabit = earliestLogByHabit,
            existingRewardedMilestones = profile.getRewardedMilestones(),
            todayStr = todayStr
        )

        //Persist newly earned milestones or updated lives asynchronously if changed
        if (streakEval.allRewardedMilestones != profile.getRewardedMilestones() ||
            streakEval.livesRemaining != profile.livesCount
        ) {
            scope.launch {
                val latestProfile = habitDao.getUserProfileSync() ?: profile
                val mergedMilestones = latestProfile.getRewardedMilestones() + streakEval.allRewardedMilestones
                val updatedLives = (UserProfileEntity.INITIAL_LIVES_COUNT + mergedMilestones.size - streakEval.consumedLifeDates.size)
                    .coerceIn(0, latestProfile.maxLivesCount.coerceAtLeast(99))
                habitDao.insertOrUpdateUserProfile(
                    latestProfile.copy(
                        livesCount = updatedLives,
                        rewardedMilestonesCsv = mergedMilestones.joinToString(","),
                        consumedLifeDatesCsv = streakEval.consumedLifeDates.joinToString(","),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Support between 3 and 10 active habits
        val maxHabitsAllowed = UserProfileEntity.MAX_HABITS_LIMIT
        val isHabitLimitUnlocked = habits.size < maxHabitsAllowed

        if (habits.isEmpty()) {
            val emptyWeekly = CompletionRateStats(CompletionRateFilter.WEEKLY, 0, 0, 0, 0, "This Week")
            val emptyMonthly = CompletionRateStats(CompletionRateFilter.MONTHLY, 0, 0, 0, 0, monthYearFormat.format(todayCal.time))
            val emptyYearly = CompletionRateStats(CompletionRateFilter.YEARLY, 0, 0, 0, 0, "${todayCal.get(Calendar.YEAR)}")
            return ProgressStats(
                completionRatePercent = 0,
                totalCompleted = 0,
                totalMissed = 0,
                totalScheduled = 0,
                currentStreakDays = 0,
                bestStreakDays = 0,
                perfectDaysCount = 0,
                perfectStreakDays = 0,
                livesCount = profile.livesCount,
                maxLivesCount = profile.maxLivesCount,
                lifeRewardsEarned = profile.getRewardedMilestones().size,
                daysUntilNextLifeReward = UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD,
                hasSufficientHistory = false,
                isNewUser = true,
                isHabitLimitUnlocked = isHabitLimitUnlocked,
                maxHabitsAllowed = maxHabitsAllowed,
                activeHabitCount = 0,
                weeklyProgress = buildWeeklyDays(habits, logsByDate, earliestLogByHabit, todayCal, df, dayFormat),
                trendPoints = emptyList(),
                heatmapGrid = buildFullHeatmapGrid(habits, logsByDate, earliestLogByHabit, todayCal, df),
                currentMonthHeatmap = buildCurrentMonthHeatmap(habits, logsByDate, earliestLogByHabit, todayCal, df),
                currentMonthTitle = monthYearFormat.format(todayCal.time),
                weeklyCompletionStats = emptyWeekly,
                monthlyCompletionStats = emptyMonthly,
                yearlyCompletionStats = emptyYearly,
                categoryStats = emptyList()
            )
        }

        // 1. Weekly Progress Bar Data (Current Week Mon..Sun)
        val weeklyList = buildWeeklyDays(habits, logsByDate, earliestLogByHabit, todayCal, df, dayFormat)

        // 2. Line Chart Trend Points (Past 14 days up to today)
        val trendPoints = buildTrendPoints(habits, logsByDate, earliestLogByHabit, todayCal, df, shortFormat)

        // 3. Full GitHub-style Heatmap Grid & Current Month Calendar Heatmap
        val heatmapGrid = buildFullHeatmapGrid(habits, logsByDate, earliestLogByHabit, todayCal, df)
        val currentMonthHeatmap = buildCurrentMonthHeatmap(habits, logsByDate, earliestLogByHabit, todayCal, df)

        // 4. Filtered Completion Rate Stats (Weekly, Monthly, Yearly)
        val weeklyStats = calculateCompletionStatsForFilter(
            filter = CompletionRateFilter.WEEKLY,
            habits = habits,
            logsByDate = logsByDate,
            earliestLogByHabit = earliestLogByHabit,
            referenceDate = todayCal.time
        )
        val monthlyStats = calculateCompletionStatsForFilter(
            filter = CompletionRateFilter.MONTHLY,
            habits = habits,
            logsByDate = logsByDate,
            earliestLogByHabit = earliestLogByHabit,
            referenceDate = todayCal.time
        )
        val yearlyStats = calculateCompletionStatsForFilter(
            filter = CompletionRateFilter.YEARLY,
            habits = habits,
            logsByDate = logsByDate,
            earliestLogByHabit = earliestLogByHabit,
            referenceDate = todayCal.time
        )

        // 5. Category breakdown
        val categoryStats = habits.groupBy { it.category }.map { (catName, catHabits) ->
            val catIds = catHabits.map { it.id }.toSet()
            val completedInCat = completedLogs.count { it.habitId in catIds }
            CategoryStat(
                name = catName,
                count = catHabits.size,
                completedCount = completedInCat
            )
        }

        return ProgressStats(
            completionRatePercent = monthlyStats.completionPercent,
            totalCompleted = monthlyStats.doneCount,
            totalMissed = monthlyStats.missedCount,
            totalScheduled = monthlyStats.totalScheduledCount,
            currentStreakDays = streakEval.currentStreakDays,
            bestStreakDays = streakEval.bestStreakDays,
            perfectDaysCount = streakEval.perfectDaysCount,
            perfectStreakDays = streakEval.perfectStreakDays,
            livesCount = streakEval.livesRemaining,
            maxLivesCount = profile.maxLivesCount,
            lifeRewardsEarned = streakEval.allRewardedMilestones.size,
            daysUntilNextLifeReward = streakEval.daysUntilNextReward,
            hasSufficientHistory = hasSufficientHistory,
            isNewUser = isNewUser,
            isHabitLimitUnlocked = isHabitLimitUnlocked,
            maxHabitsAllowed = maxHabitsAllowed,
            activeHabitCount = habits.size,
            weeklyProgress = weeklyList,
            trendPoints = trendPoints,
            heatmapGrid = heatmapGrid,
            currentMonthHeatmap = currentMonthHeatmap,
            currentMonthTitle = monthYearFormat.format(todayCal.time),
            weeklyCompletionStats = weeklyStats,
            monthlyCompletionStats = monthlyStats,
            yearlyCompletionStats = yearlyStats,
            categoryStats = categoryStats
        )
    }

    data class StreakEvaluationResult(
        val currentStreakDays: Int,
        val bestStreakDays: Int,
        val perfectDaysCount: Int,
        val perfectStreakDays: Int,
        val livesRemaining: Int,
        val daysUntilNextReward: Int,
        val allRewardedMilestones: Set<String>,
        val consumedLifeDates: Set<String>
    )

    fun evaluateStreaksAndLives(
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        existingRewardedMilestones: Set<String>,
        todayStr: String
    ): StreakEvaluationResult {
        val df = newDateFormat()
        val rewardedMilestones = existingRewardedMilestones.toMutableSet()
        val consumedDates = mutableSetOf<String>()

        if (habits.isEmpty()) {
            return StreakEvaluationResult(
                currentStreakDays = 0,
                bestStreakDays = 0,
                perfectDaysCount = 0,
                perfectStreakDays = 0,
                livesRemaining = UserProfileEntity.INITIAL_LIVES_COUNT + rewardedMilestones.size,
                daysUntilNextReward = UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD,
                allRewardedMilestones = rewardedMilestones,
                consumedLifeDates = emptySet()
            )
        }

        val completedDates = logsByDate
            .filter { (_, logs) -> logs.any { it.completed } }
            .keys
            .sorted()

        if (completedDates.isEmpty()) {
            return StreakEvaluationResult(
                currentStreakDays = 0,
                bestStreakDays = 0,
                perfectDaysCount = 0,
                perfectStreakDays = 0,
                livesRemaining = UserProfileEntity.INITIAL_LIVES_COUNT + rewardedMilestones.size,
                daysUntilNextReward = UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD,
                allRewardedMilestones = rewardedMilestones,
                consumedLifeDates = emptySet()
            )
        }

        val firstDateStr = completedDates.first()
        val startCal = parseDateToNormalizedCalendar(firstDateStr, df) ?: normalizeCalendar(Date())
        val endCal = parseDateToNormalizedCalendar(todayStr, df) ?: normalizeCalendar(Date())

        var runningStreak = 0
        var bestStreak = 0
        var consecutiveUnassisted = 0
        var streakBlockStart = firstDateStr

        var perfectDaysCount = 0
        var runningPerfectStreak = 0

        val cursor = startCal.clone() as Calendar
        while (!cursor.after(endCal)) {
            val dStr = df.format(cursor.time)
            val scheduledHabits = getScheduledHabitsForEvaluation(habits, dStr, earliestLogByHabit)
            val scheduledCount = scheduledHabits.size

            if (scheduledCount > 0) {
                val scheduledIds = scheduledHabits.map { it.id }.toSet()
                val completedCount = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0
                val ratio = completedCount.toFloat() / scheduledCount.toFloat()

                val meetsStreakThreshold = ratio >= UserProfileEntity.STREAK_THRESHOLD_RATIO
                val isPerfectDay = completedCount >= scheduledCount

                if (isPerfectDay) {
                    perfectDaysCount++
                    runningPerfectStreak++
                } else if (dStr < todayStr) {
                    runningPerfectStreak = 0
                }

                if (meetsStreakThreshold) {
                    if (runningStreak == 0) {
                        streakBlockStart = dStr
                    }
                    runningStreak++
                    consecutiveUnassisted++
                    if (runningStreak > bestStreak) {
                        bestStreak = runningStreak
                    }

                    if (consecutiveUnassisted > 0 &&
                        consecutiveUnassisted % UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD == 0
                    ) {
                        val milestoneKey = "period_${streakBlockStart}_m${consecutiveUnassisted}"
                        rewardedMilestones.add(milestoneKey)
                    }
                } else if (dStr < todayStr) {
                    // Missed day in the past after user started tracking
                    consecutiveUnassisted = 0
                    val availableLives = UserProfileEntity.INITIAL_LIVES_COUNT + rewardedMilestones.size - consumedDates.size
                    if (runningStreak > 0 && availableLives > 0) {
                        // Consume 1 recovery life to protect streak
                        consumedDates.add(dStr)
                    } else {
                        runningStreak = 0
                    }
                }
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        val livesRemaining = (UserProfileEntity.INITIAL_LIVES_COUNT + rewardedMilestones.size - consumedDates.size)
            .coerceAtLeast(0)
        val daysUntilNextReward = UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD -
            (consecutiveUnassisted % UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD)

        return StreakEvaluationResult(
            currentStreakDays = runningStreak,
            bestStreakDays = maxOf(bestStreak, runningStreak),
            perfectDaysCount = perfectDaysCount,
            perfectStreakDays = runningPerfectStreak,
            livesRemaining = livesRemaining,
            daysUntilNextReward = daysUntilNextReward,
            allRewardedMilestones = rewardedMilestones,
            consumedLifeDates = consumedDates
        )
    }

    private fun getScheduledHabitsForEvaluation(
        habits: List<HabitEntity>,
        dateStr: String,
        earliestLogByHabit: Map<Long, String>
    ): List<HabitEntity> {
        val strictScheduled = habits.filter {
            isHabitScheduledOnDate(it, dateStr, earliestLogByHabit[it.id], includeBeforeCreated = false)
        }
        if (strictScheduled.isNotEmpty()) return strictScheduled
        return habits.filter {
            isHabitScheduledOnDate(it, dateStr, earliestLogByHabit[it.id], includeBeforeCreated = true)
        }
    }

    fun calculateCompletionStatsForFilter(
        filter: CompletionRateFilter,
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        referenceDate: Date = Date()
    ): CompletionRateStats {
        val df = newDateFormat()
        val shortFormat = newShortDateFormat()
        val monthYearFormat = newMonthYearFormat()
        val todayCal = normalizeCalendar(referenceDate)
        val startCal = todayCal.clone() as Calendar

        val periodLabel = when (filter) {
            CompletionRateFilter.WEEKLY -> {
                val dow = startCal.get(Calendar.DAY_OF_WEEK)
                val diffFromMon = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
                startCal.add(Calendar.DAY_OF_YEAR, -diffFromMon)
                "${shortFormat.format(startCal.time)} – ${shortFormat.format(todayCal.time)}"
            }
            CompletionRateFilter.MONTHLY -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                monthYearFormat.format(todayCal.time)
            }
            CompletionRateFilter.YEARLY -> {
                startCal.set(Calendar.MONTH, Calendar.JANUARY)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                "Year ${todayCal.get(Calendar.YEAR)}"
            }
        }

        if (habits.isEmpty()) {
            return CompletionRateStats(filter, 0, 0, 0, 0, periodLabel)
        }

        var doneCount = 0
        var totalScheduled = 0

        val cursor = startCal.clone() as Calendar
        // Stop at todayCal so future days in an incomplete period are NEVER counted as missed
        while (!cursor.after(todayCal)) {
            val dStr = df.format(cursor.time)
            val scheduledHabits = habits.filter { habit ->
                isHabitScheduledOnDate(
                    habit = habit,
                    dateString = dStr,
                    earliestLogDate = earliestLogByHabit[habit.id],
                    includeBeforeCreated = false
                )
            }
            val scheduledIds = scheduledHabits.map { it.id }.toSet()
            val completedOnDay = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0

            doneCount += completedOnDay
            totalScheduled += scheduledHabits.size

            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        val missedCount = (totalScheduled - doneCount).coerceAtLeast(0)
        val completionPercent = if (totalScheduled > 0) {
            ((doneCount.toFloat() / totalScheduled.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        return CompletionRateStats(
            filter = filter,
            doneCount = doneCount,
            missedCount = missedCount,
            totalScheduledCount = totalScheduled,
            completionPercent = completionPercent,
            periodLabel = periodLabel
        )
    }

    fun calculateHeatmapLevel(completed: Int, scheduled: Int): Int {
        if (completed <= 0 || scheduled <= 0) return 0
        val ratio = completed.toFloat() / scheduled.toFloat()
        return when {
            ratio >= 1.0f -> 4 // 100% Perfect Day
            ratio >= 0.70f -> 3 // 70-99% Streak Qualifying
            ratio >= 0.40f -> 2 // 40-69% Medium
            else -> 1           // 1-39% Low
        }
    }

    private fun buildCurrentMonthHeatmap(
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        todayCal: Calendar,
        df: SimpleDateFormat
    ): List<MonthHeatmapDay> {
        val result = mutableListOf<MonthHeatmapDay>()
        val todayStr = df.format(todayCal.time)
        val displayMonth = todayCal.get(Calendar.MONTH)

        val cal = todayCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val diffFromMon = if (firstDow == Calendar.SUNDAY) 6 else firstDow - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -diffFromMon)

        // Generate 6 weeks (42 cells) or 5 weeks if month fits cleanly
        for (week in 0 until 6) {
            val weekFirstInMonth = cal.get(Calendar.MONTH) == displayMonth
            if (week == 5 && !weekFirstInMonth) break
            for (dowIndex in 0 until 7) {
                val dStr = df.format(cal.time)
                val isCurrentMonth = cal.get(Calendar.MONTH) == displayMonth
                val isFuture = dStr > todayStr
                val scheduled = getScheduledHabitsForEvaluation(habits, dStr, earliestLogByHabit)
                val scheduledIds = scheduled.map { it.id }.toSet()
                val completed = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0
                val level = if (isFuture) 0 else calculateHeatmapLevel(completed, scheduled.size)

                result.add(
                    MonthHeatmapDay(
                        dateString = dStr,
                        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                        dayOfWeekIndex = dowIndex,
                        isCurrentMonth = isCurrentMonth,
                        isToday = dStr == todayStr,
                        isFuture = isFuture,
                        completedCount = completed,
                        scheduledCount = scheduled.size,
                        level = level
                    )
                )
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return result
    }

    private fun buildFullHeatmapGrid(
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        todayCal: Calendar,
        df: SimpleDateFormat
    ): List<List<HeatmapCell>> {
        val grid = List(7) { mutableListOf<HeatmapCell>() }
        val heatCal = todayCal.clone() as Calendar
        heatCal.add(Calendar.DAY_OF_YEAR, -180)
        val dow = heatCal.get(Calendar.DAY_OF_WEEK)
        val diffFromMon = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        heatCal.add(Calendar.DAY_OF_YEAR, -diffFromMon)

        while (!heatCal.after(todayCal)) {
            val dStr = df.format(heatCal.time)
            val dayOfWeek = heatCal.get(Calendar.DAY_OF_WEEK)
            val rowIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            val scheduled = getScheduledHabitsForEvaluation(habits, dStr, earliestLogByHabit)
            val scheduledIds = scheduled.map { it.id }.toSet()
            val completed = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0
            val level = calculateHeatmapLevel(completed, scheduled.size)

            grid[rowIndex].add(
                HeatmapCell(
                    dateString = dStr,
                    level = level,
                    completedCount = completed,
                    totalCount = scheduled.size
                )
            )
            heatCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return grid
    }

    private fun buildWeeklyDays(
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        todayCal: Calendar,
        df: SimpleDateFormat,
        dayFormat: SimpleDateFormat
    ): List<DayCompletion> {
        val list = mutableListOf<DayCompletion>()
        val cal = todayCal.clone() as Calendar
        val currentDow = cal.get(Calendar.DAY_OF_WEEK)
        val diffFromMon = if (currentDow == Calendar.SUNDAY) 6 else currentDow - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -diffFromMon)

        for (i in 0..6) {
            val dStr = df.format(cal.time)
            val dayName = dayFormat.format(cal.time)
            val scheduled = getScheduledHabitsForEvaluation(habits, dStr, earliestLogByHabit)
            val scheduledIds = scheduled.map { it.id }.toSet()
            val completed = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0
            list.add(
                DayCompletion(
                    dayOfWeek = dayName,
                    dateString = dStr,
                    completedCount = completed,
                    targetCount = scheduled.size
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun buildTrendPoints(
        habits: List<HabitEntity>,
        logsByDate: Map<String, List<HabitLogEntity>>,
        earliestLogByHabit: Map<Long, String>,
        todayCal: Calendar,
        df: SimpleDateFormat,
        shortFormat: SimpleDateFormat
    ): List<TrendPoint> {
        val points = mutableListOf<TrendPoint>()
        val cal = todayCal.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -13) // 14-day trend window ending today

        for (i in 0 until 14) {
            val dStr = df.format(cal.time)
            val label = shortFormat.format(cal.time)
            val scheduled = getScheduledHabitsForEvaluation(habits, dStr, earliestLogByHabit)
            val scheduledIds = scheduled.map { it.id }.toSet()
            val completed = logsByDate[dStr]?.count { it.completed && it.habitId in scheduledIds } ?: 0
            val pct = if (scheduled.isNotEmpty()) {
                ((completed.toFloat() / scheduled.size.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
            } else {
                0
            }
            points.add(
                TrendPoint(
                    dateString = dStr,
                    shortLabel = label,
                    completionPercent = pct,
                    completedCount = completed,
                    scheduledCount = scheduled.size,
                    metStreakThreshold = pct >= 70,
                    isPerfectDay = scheduled.isNotEmpty() && completed >= scheduled.size
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return points
    }

    suspend fun toggleHabit(habitId: Long, dateString: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val currentLog = habitDao.getLogForHabitAndDateSync(habitId, dateString)
            val newCompletedState = if (currentLog == null) {
                habitDao.insertLog(
                    HabitLogEntity(
                        habitId = habitId,
                        dateString = dateString,
                        completed = true,
                        completedAt = System.currentTimeMillis()
                    )
                )
                true
            } else {
                val toggled = !currentLog.completed
                habitDao.insertLog(
                    currentLog.copy(
                        completed = toggled,
                        completedAt = System.currentTimeMillis()
                    )
                )
                toggled
            }
            backupManager.createSnapshotJson()
            newCompletedState
        }
    }

    suspend fun addHabit(habit: HabitEntity): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanTitle = habit.title.trim()
            val cleanSubtitle = habit.subtitle.trim()
            val cleanCategory = habit.category.trim().ifEmpty { "Productivity" }
            val cleanDuration = habit.goalDuration.trim()

            validateHabitFields(cleanTitle, cleanSubtitle, cleanCategory, cleanDuration)

            // Check duplicate habit title
            val duplicate = habitDao.getHabitByTitleSync(cleanTitle)
            if (duplicate != null && duplicate.id != habit.id) {
                throw IllegalArgumentException("A habit named '$cleanTitle' already exists.")
            }

            // Enforce 3 to 10 habit entry limit (maximum 10 habits)
            val currentHabits = habitDao.getAllHabitsList()
            if (currentHabits.size >= UserProfileEntity.MAX_HABITS_LIMIT) {
                throw IllegalStateException(
                    "You can track between ${UserProfileEntity.MIN_HABITS_COUNT} and ${UserProfileEntity.MAX_HABITS_LIMIT} habits (maximum ${UserProfileEntity.MAX_HABITS_LIMIT} reached)."
                )
            }

            val id = habitDao.insertHabit(
                habit.copy(
                    title = cleanTitle,
                    subtitle = cleanSubtitle,
                    category = cleanCategory,
                    goalDuration = cleanDuration
                )
            )
            backupManager.createSnapshotJson()
            id
        }
    }

    suspend fun updateHabit(habit: HabitEntity): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanTitle = habit.title.trim()
            val cleanSubtitle = habit.subtitle.trim()
            val cleanCategory = habit.category.trim().ifEmpty { "Productivity" }
            val cleanDuration = habit.goalDuration.trim()

            validateHabitFields(cleanTitle, cleanSubtitle, cleanCategory, cleanDuration)

            val duplicate = habitDao.getHabitByTitleSync(cleanTitle)
            if (duplicate != null && duplicate.id != habit.id) {
                throw IllegalArgumentException("Another habit named '$cleanTitle' already exists.")
            }

            // Preserve original createdAt timestamp when editing an existing habit
            val existing = habitDao.getHabitByIdSync(habit.id)
            val preservedCreatedAt = existing?.createdAt ?: habit.createdAt
            habitDao.updateHabit(
                habit.copy(
                    title = cleanTitle,
                    subtitle = cleanSubtitle,
                    category = cleanCategory,
                    goalDuration = cleanDuration,
                    createdAt = preservedCreatedAt
                )
            )
            backupManager.createSnapshotJson()
            Unit
        }
    }

    suspend fun deleteHabit(habitId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            habitDao.deleteHabitById(habitId)
            habitDao.deleteLogsForHabit(habitId)
            backupManager.createSnapshotJson()
            Unit
        }
    }

    suspend fun addCategory(name: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanName = name.trim()
            require(cleanName.isNotEmpty()) { "Category name cannot be empty." }
            require(cleanName.length <= MAX_CATEGORY_LENGTH) {
                "Category name cannot exceed $MAX_CATEGORY_LENGTH characters."
            }
            val existing = habitDao.getCategoryByNameSync(cleanName)
            if (existing != null) {
                throw IllegalArgumentException("Category '$cleanName' already exists.")
            }
            val id = habitDao.insertCategory(CategoryEntity(name = cleanName, isDefault = false))
            backupManager.createSnapshotJson()
            id
        }
    }

    suspend fun deleteCategory(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            habitDao.deleteCategoryById(categoryId)
            backupManager.createSnapshotJson()
            Unit
        }
    }

    suspend fun updateUserProfile(transform: (UserProfileEntity) -> UserProfileEntity): Result<UserProfileEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val current = habitDao.getUserProfileSync() ?: UserProfileEntity()
                val updated = transform(current).copy(updatedAt = System.currentTimeMillis())
                habitDao.insertOrUpdateUserProfile(updated)
                backupManager.createSnapshotJson()
                updated
            }
        }

    suspend fun resetAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val currentProfile = habitDao.getUserProfileSync() ?: UserProfileEntity()
            habitDao.clearAllLogs()
            habitDao.clearAllHabits()
            habitDao.clearAllCategories()
            habitDao.insertCategories(SampleDataPreloader.defaultCategories)
            habitDao.insertOrUpdateUserProfile(
                currentProfile.copy(
                    livesCount = UserProfileEntity.INITIAL_LIVES_COUNT,
                    rewardedMilestonesCsv = "",
                    consumedLifeDatesCsv = "",
                    hasSeededInitialData = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            backupManager.clearMirrors()
            backupManager.createSnapshotJson()
            Unit
        }
    }

    private fun normalizeCalendar(date: Date): Calendar {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun parseDateToNormalizedCalendar(dateStr: String, df: SimpleDateFormat): Calendar? {
        return try {
            val parsed = df.parse(dateStr) ?: return null
            normalizeCalendar(parsed)
        } catch (_: Exception) {
            null
        }
    }
}
