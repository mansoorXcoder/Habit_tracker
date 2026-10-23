package com.example.data.model

data class HabitWithLog(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int = 0,
    val totalCompletions: Int = 0
)

data class HeatmapCell(
    val dateString: String,
    val level: Int, // 0 = none, 1 = low, 2 = medium, 3 = high
    val completedCount: Int,
    val totalCount: Int
)

data class DayCompletion(
    val dayOfWeek: String, // Mon, Tue, etc.
    val dateString: String,
    val completedCount: Int,
    val targetCount: Int = 5
)

data class CategoryStat(
    val name: String,
    val count: Int,
    val completedCount: Int
)

data class ProgressStats(
    val completionRatePercent: Int = 80,
    val totalCompleted: Int = 40,
    val totalMissed: Int = 10,
    val totalScheduled: Int = 50,
    val currentStreakDays: Int = 7,
    val bestStreakDays: Int = 12,
    val weeklyProgress: List<DayCompletion> = emptyList(),
    val heatmapGrid: List<List<HeatmapCell>> = emptyList(), // 7 rows (Mon-Sun) x ~40 columns
    val categoryStats: List<CategoryStat> = emptyList()
)
