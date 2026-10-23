package com.example.data.db

import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SampleDataPreloader {

    val defaultHabits = listOf(
        HabitEntity(
            id = 1,
            title = "Read",
            subtitle = "30 minutes",
            category = "Learning",
            iconName = "book",
            goalDuration = "30 minutes per day",
            frequency = "Daily",
            reminderTime = "8:30 AM",
            reminderEnabled = true,
            orderIndex = 0
        ),
        HabitEntity(
            id = 2,
            title = "Workout",
            subtitle = "45 minutes",
            category = "Health",
            iconName = "fitness",
            goalDuration = "45 minutes per day",
            frequency = "Daily",
            reminderTime = "7:00 AM",
            reminderEnabled = true,
            orderIndex = 1
        ),
        HabitEntity(
            id = 3,
            title = "Code",
            subtitle = "Build something every day",
            category = "Productivity",
            iconName = "code",
            goalDuration = "1 hour per day",
            frequency = "Daily",
            reminderTime = "9:00 AM",
            reminderEnabled = true,
            orderIndex = 2
        ),
        HabitEntity(
            id = 4,
            title = "Drink water",
            subtitle = "8 glasses",
            category = "Health",
            iconName = "water",
            goalDuration = "8 glasses per day",
            frequency = "Daily",
            reminderTime = "10:00 AM",
            reminderEnabled = true,
            orderIndex = 3
        ),
        HabitEntity(
            id = 5,
            title = "Sleep early",
            subtitle = "Before 11:00 PM",
            category = "Lifestyle",
            iconName = "moon",
            goalDuration = "Before 11:00 PM",
            frequency = "Daily",
            reminderTime = "10:30 PM",
            reminderEnabled = true,
            orderIndex = 4
        )
    )

    fun generateInitialLogs(todayDate: Date = Date()): List<HabitLogEntity> {
        val logs = mutableListOf<HabitLogEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.time = todayDate

        // Today's logs (matching prototype: 4 of 5 completed)
        // Habits 1 (Read), 2 (Workout), 3 (Code) completed = true
        // Habit 4 (Water), 5 (Sleep early) = false
        val todayStr = dateFormat.format(cal.time)
        logs.add(HabitLogEntity(habitId = 1, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 2, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 3, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 4, dateString = todayStr, completed = false))
        logs.add(HabitLogEntity(habitId = 5, dateString = todayStr, completed = false))

        // Past 180 days history for rich Heatmap and Weekly Progress
        // With a consistent 7-day streak leading up to today
        val randomSeed = java.util.Random(42)

        for (dayOffset in 1..180) {
            val pastCal = Calendar.getInstance()
            pastCal.time = todayDate
            pastCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val pastDateStr = dateFormat.format(pastCal.time)

            // Past 6 days (days 1 to 6 before today) were all completed to maintain the 7-day streak!
            val isStreakDay = dayOffset in 1..6

            defaultHabits.forEach { habit ->
                val completed = if (isStreakDay) {
                    // Code and Read completed every day in streak
                    if (habit.id == 3L || habit.id == 1L) true
                    else randomSeed.nextFloat() > 0.15f
                } else {
                    // General completion probability around 75-80%
                    randomSeed.nextFloat() > 0.22f
                }

                if (completed) {
                    logs.add(
                        HabitLogEntity(
                            habitId = habit.id,
                            dateString = pastDateStr,
                            completed = true,
                            completedAt = pastCal.timeInMillis
                        )
                    )
                }
            }
        }

        return logs
    }

    suspend fun seedDatabaseIfEmpty(dao: HabitDao) {
        val existingHabits = dao.getAllHabitsSync()
        if (existingHabits.isEmpty()) {
            dao.insertHabits(defaultHabits)
            val logs = generateInitialLogs()
            dao.insertLogs(logs)
        }
    }

    private suspend fun HabitDao.getAllHabitsSync(): List<HabitEntity> {
        var result: List<HabitEntity> = emptyList()
        // Simple one-shot fetch
        val list = this.getHabitByIdSync(1)
        if (list != null) return listOf(list)
        return emptyList()
    }
}
