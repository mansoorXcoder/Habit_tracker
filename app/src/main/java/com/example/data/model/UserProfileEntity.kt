package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Mansoor",
    val userEmail: String = "",
    val syncKey: String = "",
    val livesCount: Int = INITIAL_LIVES_COUNT,
    val maxLivesCount: Int = MAX_LIVES_COUNT,
    val rewardedMilestonesCsv: String = "", // Tracks milestones (e.g. "7,14,21") or period end dates already rewarded
    val consumedLifeDatesCsv: String = "",  // Tracks missed dates where a recovery life was consumed
    val morningReminderEnabled: Boolean = true,
    val morningReminderTime: String = "8:00 AM",
    val customMorningMessage: String = "Small daily habits build unstoppable momentum.",
    val morningBannerDismissedDate: String = "",
    val hasSeededInitialData: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val INITIAL_LIVES_COUNT = 10
        const val MAX_LIVES_COUNT = 15
        const val NEW_USER_MAX_HABITS = 3
        const val STREAK_DAYS_FOR_LIFE_REWARD = 7
        const val STREAK_THRESHOLD_RATIO = 0.70f
    }

    fun getRewardedMilestones(): Set<String> =
        rewardedMilestonesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun getConsumedLifeDates(): Set<String> =
        consumedLifeDatesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
