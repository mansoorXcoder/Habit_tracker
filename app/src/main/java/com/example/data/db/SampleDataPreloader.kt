package com.example.data.db

import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.data.model.UserProfileEntity

object SampleDataPreloader {

    val defaultCategories = listOf(
        CategoryEntity(name = "Productivity", isDefault = true),
        CategoryEntity(name = "Health", isDefault = true),
        CategoryEntity(name = "Learning", isDefault = true),
        CategoryEntity(name = "Lifestyle", isDefault = true),
        CategoryEntity(name = "Fitness", isDefault = true),
        CategoryEntity(name = "Mindfulness", isDefault = true)
    )

    // Limited to UserProfileEntity.NEW_USER_MAX_HABITS (3) starter habits for new users, with zero fake logs.
    val defaultStarterHabits = listOf(
        HabitEntity(
            id = 1,
            title = "Read",
            subtitle = "Read 20 pages of a book",
            category = "Learning",
            iconName = "book",
            goalDuration = "30 min",
            frequency = "Daily",
            reminderTime = "8:30 AM",
            reminderEnabled = true,
            orderIndex = 0
        ),
        HabitEntity(
            id = 2,
            title = "Workout",
            subtitle = "Strength & mobility training",
            category = "Fitness",
            iconName = "fitness",
            goalDuration = "45 min",
            frequency = "Daily",
            reminderTime = "7:00 AM",
            reminderEnabled = true,
            orderIndex = 1
        ),
        HabitEntity(
            id = 3,
            title = "Drink Water",
            subtitle = "Stay hydrated throughout the day",
            category = "Health",
            iconName = "water",
            goalDuration = "8 glasses",
            frequency = "Daily",
            reminderTime = "10:00 AM",
            reminderEnabled = true,
            orderIndex = 2
        )
    )

    suspend fun seedDatabaseIfEmpty(dao: HabitDao) {
        val existingCategories = dao.getAllCategoriesSync()
        if (existingCategories.isEmpty()) {
            dao.insertCategories(defaultCategories)
        } else {
            // Ensure all standard categories exist without duplicating
            val existingNames = existingCategories.map { it.name.lowercase() }.toSet()
            val missing = defaultCategories.filter { it.name.lowercase() !in existingNames }
            if (missing.isNotEmpty()) {
                dao.insertCategories(missing)
            }
        }

        val existingProfile = dao.getUserProfileSync()
        if (existingProfile == null) {
            val existingHabits = dao.getAllHabitsList()
            if (existingHabits.isEmpty()) {
                dao.insertHabits(defaultStarterHabits)
            }
            dao.insertOrUpdateUserProfile(
                UserProfileEntity(
                    id = 1,
                    livesCount = UserProfileEntity.INITIAL_LIVES_COUNT,
                    hasSeededInitialData = true
                )
            )
        }
    }
}
