package com.example.data.db

import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SampleDataPreloader {

    val defaultCategories = listOf(
        CategoryEntity(name = "Productivity", isDefault = true),
        CategoryEntity(name = "Health", isDefault = true),
        CategoryEntity(name = "Learning", isDefault = true),
        CategoryEntity(name = "Lifestyle", isDefault = true)
    )

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

        val todayStr = dateFormat.format(cal.time)
        logs.add(HabitLogEntity(habitId = 1, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 2, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 3, dateString = todayStr, completed = true))
        logs.add(HabitLogEntity(habitId = 4, dateString = todayStr, completed = false))
        logs.add(HabitLogEntity(habitId = 5, dateString = todayStr, completed = false))

        val randomSeed = java.util.Random(42)

        for (dayOffset in 1..180) {
            val pastCal = Calendar.getInstance()
            pastCal.time = todayDate
            pastCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val pastDateStr = dateFormat.format(pastCal.time)

            val isStreakDay = dayOffset in 1..6

            defaultHabits.forEach { habit ->
                val completed = if (isStreakDay) {
                    if (habit.id == 3L || habit.id == 1L) true
                    else randomSeed.nextFloat() > 0.15f
                } else {
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
        val existingCategories = dao.getAllCategoriesSync()
        if (existingCategories.isEmpty()) {
            dao.insertCategories(defaultCategories)
        }

        val existingHabits = dao.getAllHabitsList()
        if (existingHabits.isEmpty()) {
            dao.insertHabits(defaultHabits)
            val logs = generateInitialLogs()
            dao.insertLogs(logs)
        }
    }
}
