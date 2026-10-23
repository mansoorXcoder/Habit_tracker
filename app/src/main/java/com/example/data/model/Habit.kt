package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val category: String = "Productivity", // Health, Productivity, Learning, Lifestyle
    val iconName: String = "code",        // book, fitness, code, water, moon, etc.
    val goalDuration: String = "1 hour per day",
    val frequency: String = "Daily",       // Daily, Weekdays, Weekends
    val reminderTime: String = "9:00 AM",
    val reminderEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
