package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "dateString"], unique = true),
        Index(value = ["dateString"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // Format: YYYY-MM-DD
    val completed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
