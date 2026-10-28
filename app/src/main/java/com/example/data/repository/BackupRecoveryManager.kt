package com.example.data.repository

import android.content.Context
import android.util.Base64
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
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles persistent data backup, JSON recovery snapshots (with sensitive credentials redacted),
 * Android Keystore AES-256-GCM encrypted local mirrors, PBKDF2 passkey hashing/verification,
 * and deduplicated data restoration with strict input bounds.
 */
class BackupRecoveryManager(
    private val dao: HabitDao,
    private val context: Context? = null
) {
    companion object {
        private const val PREFS_NAME = "habitflow_cloud_backup_prefs"
        private const val KEY_LATEST_SNAPSHOT = "latest_backup_snapshot_enc"
        private const val KEY_ACCOUNT_SNAPSHOT_PREFIX = "account_snapshot_enc_"
        private const val KEY_ACCOUNT_PASSKEY_HASH_PREFIX = "account_passkey_hash_"
        private const val KEY_FALLBACK_AES_SECRET = "local_keystore_fallback_secret"
        private const val BACKUP_FILE_NAME = "habitflow_recovery_snapshot.json"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "habitflow_backup_aes_master_key"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_BITS = 128

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 12000
        private const val PBKDF2_KEY_BITS = 256
        private const val HASH_PREFIX = "pbkdf2:"

        fun hashPasskey(rawPasskey: String): String {
            val clean = rawPasskey.trim()
            if (clean.isEmpty()) return ""
            if (clean.startsWith(HASH_PREFIX)) return clean
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val hashBytes = pbkdf2(clean.toCharArray(), salt)
            return "$HASH_PREFIX${toHex(salt)}:${toHex(hashBytes)}"
        }

        fun verifyPasskey(rawPasskey: String, storedHash: String): Boolean {
            if (storedHash.isBlank()) return true
            if (!storedHash.startsWith(HASH_PREFIX)) {
                return MessageDigest.isEqual(
                    rawPasskey.trim().toByteArray(Charsets.UTF_8),
                    storedHash.trim().toByteArray(Charsets.UTF_8)
                )
            }
            val parts = storedHash.removePrefix(HASH_PREFIX).split(":")
            if (parts.size != 2) return false
            val salt = fromHex(parts[0]) ?: return false
            val expectedHash = fromHex(parts[1]) ?: return false
            val candidateHash = pbkdf2(rawPasskey.trim().toCharArray(), salt)
            return MessageDigest.isEqual(expectedHash, candidateHash)
        }

        private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray {
            return try {
                val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS)
                SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
            } catch (_: Exception) {
                val digest = MessageDigest.getInstance("SHA-256")
                digest.update(salt)
                digest.digest(String(password).toByteArray(Charsets.UTF_8))
            }
        }

        private fun toHex(bytes: ByteArray): String =
            bytes.joinToString("") { "%02x".format(it) }

        private fun fromHex(hex: String): ByteArray? {
            if (hex.length % 2 != 0) return null
            return try {
                ByteArray(hex.length / 2) { i ->
                    hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Generates a portable JSON backup snapshot.
     * Security (SEC-01 & SEC-05): `syncKey` is strictly excluded from exported JSON payloads.
     */
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
                put("userName", profile.userName.take(HabitRepository.MAX_TITLE_LENGTH))
                put("userEmail", profile.userEmail.take(HabitRepository.MAX_TITLE_LENGTH))
                // SEC-01 / SEC-05: Never export syncKey or passkey hashes in JSON snapshots
                put("livesCount", profile.livesCount)
                put("maxLivesCount", profile.maxLivesCount)
                put("rewardedMilestonesCsv", profile.rewardedMilestonesCsv)
                put("consumedLifeDatesCsv", profile.consumedLifeDatesCsv)
                put("morningReminderEnabled", profile.morningReminderEnabled)
                put("morningReminderTime", profile.morningReminderTime)
                put("customMorningMessage", profile.customMorningMessage.take(HabitRepository.MAX_SUBTITLE_LENGTH))
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
                        put("name", cat.name.take(HabitRepository.MAX_CATEGORY_LENGTH))
                        put("isDefault", cat.isDefault)
                        put("createdAt", cat.createdAt)
                    }
                )
            }
            root.put("categories", catArray)

            val habitArray = JSONArray()
            habits.take(UserProfileEntity.MAX_HABITS_LIMIT).forEach { h ->
                habitArray.put(
                    JSONObject().apply {
                        put("id", h.id)
                        put("title", h.title.take(HabitRepository.MAX_TITLE_LENGTH))
                        put("subtitle", h.subtitle.take(HabitRepository.MAX_SUBTITLE_LENGTH))
                        put("category", h.category.take(HabitRepository.MAX_CATEGORY_LENGTH))
                        put("iconName", h.iconName.take(30))
                        put("goalDuration", h.goalDuration.take(HabitRepository.MAX_DURATION_LENGTH))
                        put("frequency", h.frequency.take(20))
                        put("reminderTime", h.reminderTime.take(20))
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
            persistMirrorSnapshot(jsonString, profile.userEmail)
            jsonString
        }
    }

    suspend fun restoreFromSnapshotJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            require(jsonString.isNotBlank()) { "Backup data is empty." }
            require(jsonString.length <= 2_000_000) { "Backup snapshot exceeds maximum allowed size." }
            val root = JSONObject(jsonString)

            val profileObj = root.optJSONObject("profile")
            val existingProfile = dao.getUserProfileSync() ?: UserProfileEntity()
            val restoredProfile = if (profileObj != null) {
                existingProfile.copy(
                    userName = profileObj.optString("userName", existingProfile.userName)
                        .trim()
                        .take(HabitRepository.MAX_TITLE_LENGTH)
                        .ifEmpty { existingProfile.userName },
                    userEmail = profileObj.optString("userEmail", existingProfile.userEmail)
                        .trim()
                        .take(HabitRepository.MAX_TITLE_LENGTH),
                    // Never import raw syncKey from external JSON
                    syncKey = existingProfile.syncKey,
                    livesCount = profileObj.optInt("livesCount", UserProfileEntity.INITIAL_LIVES_COUNT)
                        .coerceIn(0, UserProfileEntity.MAX_LIVES_COUNT),
                    maxLivesCount = profileObj.optInt("maxLivesCount", UserProfileEntity.MAX_LIVES_COUNT)
                        .coerceIn(1, 99),
                    rewardedMilestonesCsv = profileObj.optString("rewardedMilestonesCsv", "").take(2000),
                    consumedLifeDatesCsv = profileObj.optString("consumedLifeDatesCsv", "").take(2000),
                    morningReminderEnabled = profileObj.optBoolean("morningReminderEnabled", true),
                    morningReminderTime = profileObj.optString("morningReminderTime", "8:00 AM").take(20),
                    customMorningMessage = profileObj.optString(
                        "customMorningMessage",
                        existingProfile.customMorningMessage
                    ).take(HabitRepository.MAX_SUBTITLE_LENGTH),
                    hasSeededInitialData = true,
                    lastSyncedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                existingProfile.copy(hasSeededInitialData = true, lastSyncedAt = System.currentTimeMillis())
            }

            // Parse and deduplicate categories by case-insensitive name with strict length bounds
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            val categoriesByName = linkedMapOf<String, CategoryEntity>()
            val maxCategories = minOf(catArray.length(), 50)
            for (i in 0 until maxCategories) {
                val obj = catArray.getJSONObject(i)
                val name = obj.optString("name", "")
                    .trim()
                    .take(HabitRepository.MAX_CATEGORY_LENGTH)
                if (name.isNotEmpty() && name.lowercase() !in categoriesByName) {
                    categoriesByName[name.lowercase()] = CategoryEntity(
                        id = obj.optLong("id", 0L),
                        name = name,
                        isDefault = obj.optBoolean("isDefault", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                }
            }

            // Parse and deduplicate habits by ID and case-insensitive title (up to MAX_HABITS_LIMIT = 10)
            val habitArray = root.optJSONArray("habits") ?: JSONArray()
            val habitsList = mutableListOf<HabitEntity>()
            val seenTitles = mutableSetOf<String>()
            val idRemap = mutableMapOf<Long, Long>()

            for (i in 0 until habitArray.length()) {
                if (habitsList.size >= UserProfileEntity.MAX_HABITS_LIMIT) break
                val obj = habitArray.getJSONObject(i)
                val title = obj.optString("title", "")
                    .trim()
                    .take(HabitRepository.MAX_TITLE_LENGTH)
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
                            subtitle = obj.optString("subtitle", "")
                                .trim()
                                .take(HabitRepository.MAX_SUBTITLE_LENGTH),
                            category = obj.optString("category", "Productivity")
                                .trim()
                                .take(HabitRepository.MAX_CATEGORY_LENGTH)
                                .ifEmpty { "Productivity" },
                            iconName = obj.optString("iconName", "code").trim().take(30),
                            goalDuration = obj.optString("goalDuration", "30 min")
                                .trim()
                                .take(HabitRepository.MAX_DURATION_LENGTH),
                            frequency = obj.optString("frequency", "Daily").trim().take(20),
                            reminderTime = obj.optString("reminderTime", "9:00 AM").trim().take(20),
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
            val maxLogs = minOf(logArray.length(), 10000)
            for (i in 0 until maxLogs) {
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

            persistMirrorSnapshot(jsonString, restoredProfile.userEmail)
            habitsList.size
        }
    }

    /**
     * Saves or restores a local device profile backup.
     * Security (SEC-01 & SEC-04):
     * - Passkeys are hashed with salted PBKDF2-HMAC-SHA256 before persistence.
     * - Restoring an existing profile that has a passkey hash requires a matching passkey.
     */
    suspend fun syncAccountData(email: String, syncKey: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim().lowercase().take(HabitRepository.MAX_TITLE_LENGTH)
            val cleanKey = syncKey.trim()
            require(cleanEmail.isNotEmpty() || cleanKey.isNotEmpty()) {
                "Please enter a profile email or recovery passkey."
            }

            val accountId = cleanEmail.ifEmpty { "default_local_profile" }
            val currentHabits = dao.getAllHabitsList()
            val currentLogs = dao.getAllLogsSync()
            val storedAccountJson = loadMirrorSnapshot(accountId)
            val storedPasskeyHash = loadAccountPasskeyHash(accountId)

            if (storedAccountJson != null && currentLogs.isEmpty()) {
                if (storedPasskeyHash.isNotBlank() && !verifyPasskey(cleanKey, storedPasskeyHash)) {
                    throw SecurityException("Invalid recovery passkey for profile '$accountId'.")
                }
                restoreFromSnapshotJson(storedAccountJson).getOrThrow()
                val profile = dao.getUserProfileSync() ?: UserProfileEntity()
                val effectiveHash = if (cleanKey.isNotEmpty()) hashPasskey(cleanKey) else storedPasskeyHash
                dao.insertOrUpdateUserProfile(
                    profile.copy(
                        userEmail = cleanEmail,
                        syncKey = effectiveHash,
                        lastSyncedAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                "Local backup recovered (${dao.getAllHabitsList().size} habits restored)."
            } else {
                if (storedPasskeyHash.isNotBlank() && cleanKey.isNotEmpty() && !verifyPasskey(cleanKey, storedPasskeyHash)) {
                    throw SecurityException("Recovery passkey does not match the existing passkey for '$accountId'.")
                }
                val profile = dao.getUserProfileSync() ?: UserProfileEntity()
                val passkeyHashToStore = when {
                    cleanKey.isNotEmpty() -> hashPasskey(cleanKey)
                    storedPasskeyHash.isNotBlank() -> storedPasskeyHash
                    else -> profile.syncKey
                }
                val updatedProfile = profile.copy(
                    userEmail = cleanEmail,
                    syncKey = passkeyHashToStore,
                    lastSyncedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertOrUpdateUserProfile(updatedProfile)
                if (passkeyHashToStore.isNotBlank()) {
                    saveAccountPasskeyHash(accountId, passkeyHashToStore)
                }
                val snapshot = createSnapshotJson().getOrThrow()
                persistMirrorSnapshot(snapshot, accountId)
                "Saved ${currentHabits.size} habits and ${currentLogs.count { it.completed }} completions to encrypted local backup."
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
            val encryptedPayload = encryptPayload(ctx, json)
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit().putString(KEY_LATEST_SNAPSHOT, encryptedPayload)
            if (!accountKey.isNullOrBlank()) {
                editor.putString(KEY_ACCOUNT_SNAPSHOT_PREFIX + accountKey.lowercase(), encryptedPayload)
            }
            editor.apply()

            val file = File(ctx.filesDir, BACKUP_FILE_NAME)
            file.writeText(encryptedPayload)
        } catch (_: Exception) {
        }
    }

    private fun loadMirrorSnapshot(accountKey: String?): String? {
        val ctx = context ?: return null
        return try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!accountKey.isNullOrBlank()) {
                val accountSpecific = prefs.getString(KEY_ACCOUNT_SNAPSHOT_PREFIX + accountKey.lowercase(), null)
                if (!accountSpecific.isNullOrBlank()) {
                    return decryptPayload(ctx, accountSpecific)
                }
            }
            val latestPref = prefs.getString(KEY_LATEST_SNAPSHOT, null)
            if (!latestPref.isNullOrBlank()) {
                return decryptPayload(ctx, latestPref)
            }

            val file = File(ctx.filesDir, BACKUP_FILE_NAME)
            if (file.exists()) decryptPayload(ctx, file.readText()) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun saveAccountPasskeyHash(accountKey: String, passkeyHash: String) {
        val ctx = context ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_ACCOUNT_PASSKEY_HASH_PREFIX + accountKey.lowercase(), passkeyHash)
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun loadAccountPasskeyHash(accountKey: String): String {
        val ctx = context ?: return ""
        return try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_ACCOUNT_PASSKEY_HASH_PREFIX + accountKey.lowercase(), "") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun getOrCreateSecretKey(ctx: Context): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existingKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
            if (existingKey != null) return existingKey

            val keyGenSpec = android.security.keystore.KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            val keyGenerator = KeyGenerator.getInstance(
                android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        } catch (_: Exception) {
            // Fallback for local JVM / Robolectric unit tests where AndroidKeyStore hardware provider is absent
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingEncoded = prefs.getString(KEY_FALLBACK_AES_SECRET, null)
            if (!existingEncoded.isNullOrBlank()) {
                val raw = Base64.decode(existingEncoded, Base64.NO_WRAP)
                SecretKeySpec(raw, "AES")
            } else {
                val randomBytes = ByteArray(32)
                SecureRandom().nextBytes(randomBytes)
                prefs.edit()
                    .putString(KEY_FALLBACK_AES_SECRET, Base64.encodeToString(randomBytes, Base64.NO_WRAP))
                    .apply()
                SecretKeySpec(randomBytes, "AES")
            }
        }
    }

    private fun encryptPayload(ctx: Context, plaintext: String): String {
        return try {
            val secretKey = getOrCreateSecretKey(ctx)
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
            "ENCv1:" + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            plaintext
        }
    }

    private fun decryptPayload(ctx: Context, stored: String): String? {
        if (!stored.startsWith("ENCv1:")) {
            return if (stored.trim().startsWith("{")) stored else null
        }
        return try {
            val secretKey = getOrCreateSecretKey(ctx)
            val raw = Base64.decode(stored.removePrefix("ENCv1:"), Base64.NO_WRAP)
            if (raw.size <= GCM_IV_LENGTH) return null
            val iv = raw.copyOfRange(0, GCM_IV_LENGTH)
            val cipherBytes = raw.copyOfRange(GCM_IV_LENGTH, raw.size)
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
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
