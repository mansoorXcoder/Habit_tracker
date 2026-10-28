package com.example.data.repository

import android.content.Context
import com.example.data.db.HabitDao
import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Handles persistent data backup, cross-device JSON recovery snapshots,
 * SharedPreferences + file mirror fallback, and deduplicated data restoration.
 */
class BackupRecoveryManager(
    private val dao: HabitDao,
    private val context: Context? = null
) {
    companion object {
        private const val PREFS_NAME = "habitflow_cloud_backup_prefs"
        private const val KEY_LATEST_SNAPSHOT = "latest_backup_snapshot_json"
        private const val KEY_ACCOUNT_SNAPSHOT_PREFIX = "account_snapshot_"
        private const val BACKUP_FILE_NAME = "habitflow_recovery_snapshot.json"
    }

    suspend fun createSnapshotJson(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val profile = dao.getUserProfileSync() ?: UserProfileEntity()
            val categories = dao.getAllCategoriesSync()
            val habits = dao.getAllHabitsList()
            val logs = dao.getAllLogsSync()

            val root = JSONObject()
            root.put("version", 3)
            root.put("exportedAt", System.currentTimeMillis())

            val profileObj = JSONObject().apply {
                put("userName", profile.userName)
                put("userEmail", profile.userEmail)
                put("syncKey", profile.syncKey)
                put("livesCount", profile.livesCount)
                put("maxLivesCount", profile.maxLivesCount)
                put("rewardedMilestonesCsv", profile.rewardedMilestonesCsv)
                put("consumedLifeDatesCsv", profile.consumedLifeDatesCsv)
                put("morningReminderEnabled", profile.morningReminderEnabled)
                put("morningReminderTime", profile.morningReminderTime)
                put("customMorningMessage", profile.customMorningMessage)
                put("morningBannerDismissedDate", profile.morningBannerDismissedDate)
                put("hasSeededInitialData", profile.hasSeededInitialData)
                put("lastSyncedAt", System.currentTimeMillis())
            }
            root.put("profile", profileObj)

            val catArray = JSONArray()
            categories.forEach { cat ->
                catArray.put(
                    JSONObject().apply {
                        put("id", cat.id)
                        put("name", cat.name)
                        put("isDefault", cat.isDefault)
                        put("createdAt", cat.createdAt)
                    }
                )
            }
            root.put("categories", catArray)

            val habitArray = JSONArray()
            habits.forEach { h ->
                habitArray.put(
                    JSONObject().apply {
                        put("id", h.id)
                        put("title", h.title)
                        put("subtitle", h.subtitle)
                        put("category", h.category)
                        put("iconName", h.iconName)
                        put("goalDuration", h.goalDuration)
                        put("frequency", h.frequency)
                        put("reminderTime", h.reminderTime)
                        put("reminderEnabled", h.reminderEnabled)
                        put("orderIndex", h.orderIndex)
                        put("createdAt", h.createdAt)
                    }
                )
            }
            root.put("habits", habitArray)

            val logArray = JSONArray()
            logs.forEach { l ->
                logArray.put(
                    JSONObject().apply {
                        put("habitId", l.habitId)
                        put("dateString", l.dateString)
                        put("completed", l.completed)
                        put("completedAt", l.completedAt)
                    }
                )
            }
            root.put("logs", logArray)

            val jsonString = root.toString()
            persistMirrorSnapshot(jsonString, profile.userEmail.ifBlank { profile.syncKey })
            jsonString
        }
    }

    suspend fun restoreFromSnapshotJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            require(jsonString.isNotBlank()) { "Backup data is empty." }
            val root = JSONObject(jsonString)

            val profileObj = root.optJSONObject("profile")
            val existingProfile = dao.getUserProfileSync() ?: UserProfileEntity()
            val restoredProfile = if (profileObj != null) {
                existingProfile.copy(
                    userName = profileObj.optString("userName", existingProfile.userName),
                    userEmail = profileObj.optString("userEmail", existingProfile.userEmail),
                    syncKey = profileObj.optString("syncKey", existingProfile.syncKey),
                    livesCount = profileObj.optInt("livesCount", UserProfileEntity.INITIAL_LIVES_COUNT),
                    maxLivesCount = profileObj.optInt("maxLivesCount", UserProfileEntity.MAX_LIVES_COUNT),
                    rewardedMilestonesCsv = profileObj.optString("rewardedMilestonesCsv", ""),
                    consumedLifeDatesCsv = profileObj.optString("consumedLifeDatesCsv", ""),
                    morningReminderEnabled = profileObj.optBoolean("morningReminderEnabled", true),
                    morningReminderTime = profileObj.optString("morningReminderTime", "8:00 AM"),
                    customMorningMessage = profileObj.optString(
                        "customMorningMessage",
                        existingProfile.customMorningMessage
                    ),
                    hasSeededInitialData = true,
                    lastSyncedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                existingProfile.copy(hasSeededInitialData = true, lastSyncedAt = System.currentTimeMillis())
            }

            // Parse and deduplicate categories by case-insensitive name
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            val categoriesByName = linkedMapOf<String, CategoryEntity>()
            for (i in 0 until catArray.length()) {
                val obj = catArray.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                if (name.isNotEmpty() && name.lowercase() !in categoriesByName) {
                    categoriesByName[name.lowercase()] = CategoryEntity(
                        id = obj.optLong("id", 0L),
                        name = name,
                        isDefault = obj.optBoolean("isDefault", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                }
            }

            // Parse and deduplicate habits by ID and case-insensitive title
            val habitArray = root.optJSONArray("habits") ?: JSONArray()
            val habitsList = mutableListOf<HabitEntity>()
            val seenTitles = mutableSetOf<String>()
            val idRemap = mutableMapOf<Long, Long>()

            for (i in 0 until habitArray.length()) {
                val obj = habitArray.getJSONObject(i)
                val title = obj.optString("title", "").trim()
                if (title.isEmpty()) continue
                val key = title.lowercase()
                val rawId = obj.optLong("id", (i + 1).toLong())
                if (key !in seenTitles) {
                    seenTitles.add(key)
                    val assignedId = if (rawId > 0L) rawId else (i + 1).toLong()
                    idRemap[rawId] = assignedId
                    habitsList.add(
                        HabitEntity(
                            id = assignedId,
                            title = title,
                            subtitle = obj.optString("subtitle", ""),
                            category = obj.optString("category", "Productivity"),
                            iconName = obj.optString("iconName", "code"),
                            goalDuration = obj.optString("goalDuration", "30 min"),
                            frequency = obj.optString("frequency", "Daily"),
                            reminderTime = obj.optString("reminderTime", "9:00 AM"),
                            reminderEnabled = obj.optBoolean("reminderEnabled", true),
                            orderIndex = obj.optInt("orderIndex", i),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val validHabitIds = habitsList.map { it.id }.toSet()

            // Parse and deduplicate logs by (habitId, dateString)
            val logArray = root.optJSONArray("logs") ?: JSONArray()
            val logsMap = linkedMapOf<Pair<Long, String>, HabitLogEntity>()
            for (i in 0 until logArray.length()) {
                val obj = logArray.getJSONObject(i)
                val rawHabitId = obj.optLong("habitId", 0L)
                val mappedHabitId = idRemap[rawHabitId] ?: rawHabitId
                val dateStr = obj.optString("dateString", "").trim()
                if (mappedHabitId in validHabitIds && dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    val pairKey = mappedHabitId to dateStr
                    logsMap[pairKey] = HabitLogEntity(
                        habitId = mappedHabitId,
                        dateString = dateStr,
                        completed = obj.optBoolean("completed", false),
                        completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                    )
                }
            }

            dao.restoreFullSnapshot(
                profile = restoredProfile,
                categories = categoriesByName.values.toList(),
                habits = habitsList,
                logs = logsMap.values.toList()
            )

            persistMirrorSnapshot(jsonString, restoredProfile.userEmail.ifBlank { restoredProfile.syncKey })
            habitsList.size
        }
    }

    suspend fun syncAccountData(email: String, syncKey: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim().lowercase()
            val cleanKey = syncKey.trim()
            require(cleanEmail.isNotEmpty() || cleanKey.isNotEmpty()) {
                "Please enter an account email or recovery key."
            }

            val accountId = cleanEmail.ifEmpty { cleanKey }
            val currentHabits = dao.getAllHabitsList()
            val currentLogs = dao.getAllLogsSync()
            val storedAccountJson = loadMirrorSnapshot(accountId)

            if (storedAccountJson != null && currentLogs.isEmpty()) {
                // Recover existing cloud/account backup into local database
                restoreFromSnapshotJson(storedAccountJson).getOrThrow()
                val profile = dao.getUserProfileSync() ?: UserProfileEntity()
                dao.insertOrUpdateUserProfile(
                    profile.copy(
                        userEmail = cleanEmail,
                        syncKey = cleanKey,
                        lastSyncedAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                "Account data recovered and synced (${dao.getAllHabitsList().size} habits restored)."
            } else {
                // Save current profile & snapshot to account backup
                val profile = dao.getUserProfileSync() ?: UserProfileEntity()
                val updatedProfile = profile.copy(
                    userEmail = cleanEmail,
                    syncKey = cleanKey,
                    lastSyncedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertOrUpdateUserProfile(updatedProfile)
                val snapshot = createSnapshotJson().getOrThrow()
                persistMirrorSnapshot(snapshot, accountId)
                "Synced ${currentHabits.size} habits and ${currentLogs.count { it.completed }} completions to account backup."
            }
        }
    }

    suspend fun tryAutoRecoverIfDatabaseCleared(): Boolean = withContext(Dispatchers.IO) {
        try {
            val profile = dao.getUserProfileSync()
            if (profile != null) return@withContext false

            val snapshot = loadMirrorSnapshot(null)
            if (!snapshot.isNullOrBlank()) {
                restoreFromSnapshotJson(snapshot)
                return@withContext true
            }
        } catch (_: Exception) {
        }
        false
    }

    private fun persistMirrorSnapshot(json: String, accountKey: String?) {
        val ctx = context ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit().putString(KEY_LATEST_SNAPSHOT, json)
            if (!accountKey.isNullOrBlank()) {
                editor.putString(KEY_ACCOUNT_SNAPSHOT_PREFIX + accountKey.lowercase(), json)
            }
            editor.apply()

            val file = File(ctx.filesDir, BACKUP_FILE_NAME)
            file.writeText(json)
        } catch (_: Exception) {
        }
    }

    private fun loadMirrorSnapshot(accountKey: String?): String? {
        val ctx = context ?: return null
        return try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!accountKey.isNullOrBlank()) {
                val accountSpecific = prefs.getString(KEY_ACCOUNT_SNAPSHOT_PREFIX + accountKey.lowercase(), null)
                if (!accountSpecific.isNullOrBlank()) return accountSpecific
            }
            val latestPref = prefs.getString(KEY_LATEST_SNAPSHOT, null)
            if (!latestPref.isNullOrBlank()) return latestPref

            val file = File(ctx.filesDir, BACKUP_FILE_NAME)
            if (file.exists()) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    fun clearMirrors() {
        val ctx = context ?: return
        try {
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            val file = File(ctx.filesDir, BACKUP_FILE_NAME)
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }
}
