package com.example.data.model

enum class CompletionRateFilter(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class CompletionRateStats(
    val filter: CompletionRateFilter = CompletionRateFilter.MONTHLY,
    val doneCount: Int = 0,
    val missedCount: Int = 0,
    val totalScheduledCount: Int = 0,
    val completionPercent: Int = 0,
    val periodLabel: String = ""
)

data class TrendPoint(
    val dateString: String,
    val shortLabel: String,
    val completionPercent: Int,
    val completedCount: Int,
    val scheduledCount: Int,
    val metStreakThreshold: Boolean = false,
    val isPerfectDay: Boolean = false
)

data class MonthHeatmapDay(
    val dateString: String,
    val dayOfMonth: Int,
    val dayOfWeekIndex: Int, // 0 = Mon .. 6 = Sun
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val isFuture: Boolean = false,
    val completedCount: Int = 0,
    val scheduledCount: Int = 0,
    val level: Int = 0 // 0 = 0%, 1 = 1-39%, 2 = 40-69%, 3 = 70-99% (Streak), 4 = 100% (Perfect)
)

data class HabitWithLog(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val isScheduledToday: Boolean = true,
    val currentStreak: Int = 0,
    val totalCompletions: Int = 0
)

data class HeatmapCell(
    val dateString: String,
    val level: Int, // 0 = none, 1 = low (1-39%), 2 = medium (40-69%), 3 = streak (70-99%), 4 = perfect (100%)
    val completedCount: Int,
    val totalCount: Int
)

data class DayCompletion(
    val dayOfWeek: String, // Mon, Tue, etc.
    val dateString: String,
    val completedCount: Int,
    val targetCount: Int = 0
)

data class CategoryStat(
    val name: String,
    val count: Int,
    val completedCount: Int
)

data class ProgressStats(
    val completionRatePercent: Int = 0,
    val totalCompleted: Int = 0,
    val totalMissed: Int = 0,
    val totalScheduled: Int = 0,
    val currentStreakDays: Int = 0, // >= 70% daily scheduled habits completed
    val bestStreakDays: Int = 0,
    val perfectDaysCount: Int = 0,  // 100% daily scheduled habits completed (tracked separately)
    val perfectStreakDays: Int = 0,
    val livesCount: Int = UserProfileEntity.INITIAL_LIVES_COUNT,
    val maxLivesCount: Int = UserProfileEntity.MAX_LIVES_COUNT,
    val lifeRewardsEarned: Int = 0,
    val daysUntilNextLifeReward: Int = UserProfileEntity.STREAK_DAYS_FOR_LIFE_REWARD,
    val hasSufficientHistory: Boolean = false,
    val isNewUser: Boolean = true,
    val isHabitLimitUnlocked: Boolean = false,
    val maxHabitsAllowed: Int = UserProfileEntity.MAX_HABITS_LIMIT,
    val activeHabitCount: Int = 0,
    val weeklyProgress: List<DayCompletion> = emptyList(),
    val trendPoints: List<TrendPoint> = emptyList(),
    val heatmapGrid: List<List<HeatmapCell>> = emptyList(), // 7 rows (Mon-Sun) x columns
    val currentMonthHeatmap: List<MonthHeatmapDay> = emptyList(),
    val currentMonthTitle: String = "",
    val weeklyCompletionStats: CompletionRateStats = CompletionRateStats(CompletionRateFilter.WEEKLY),
    val monthlyCompletionStats: CompletionRateStats = CompletionRateStats(CompletionRateFilter.MONTHLY),
    val yearlyCompletionStats: CompletionRateStats = CompletionRateStats(CompletionRateFilter.YEARLY),
    val categoryStats: List<CategoryStat> = emptyList()
) {
    fun getStatsForFilter(filter: CompletionRateFilter): CompletionRateStats {
        return when (filter) {
            CompletionRateFilter.WEEKLY -> weeklyCompletionStats
            CompletionRateFilter.MONTHLY -> monthlyCompletionStats
            CompletionRateFilter.YEARLY -> yearlyCompletionStats
        }
    }
}

enum class FeedbackType {
    SUCCESS,
    ERROR,
    INFO
}

data class UiFeedbackMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val type: FeedbackType = FeedbackType.SUCCESS,
    val retryLabel: String? = null,
    val onRetry: (suspend () -> Unit)? = null
)
