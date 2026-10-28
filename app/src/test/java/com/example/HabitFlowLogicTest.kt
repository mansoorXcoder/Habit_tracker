package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.HabitDao
import com.example.data.model.CompletionRateFilter
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitFlowLogicTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: HabitDao
    private lateinit var repository: HabitRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.habitDao()
        repository = HabitRepository(dao, CoroutineScope(Dispatchers.Unconfined), context, autoInit = false)
        repository.backupManager.clearMirrors()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `initial life count is 10 and new user progress charts are hidden when history is empty`() = runBlocking {
        dao.clearAllLogs()
        dao.clearAllHabits()
        val profile = UserProfileEntity()
        assertEquals(10, profile.livesCount)

        val habits = listOf(
            HabitEntity(id = 1, title = "Read", frequency = "Daily"),
            HabitEntity(id = 2, title = "Code", frequency = "Daily")
        )
        val stats = repository.calculateDynamicProgressStats(
            habits = habits,
            allLogs = emptyList(),
            profile = profile
        )

        assertEquals(10, stats.livesCount)
        assertFalse(stats.hasSufficientHistory)
        assertTrue(stats.isNewUser)
        assertEquals(0, stats.currentStreakDays)
        assertEquals(0, stats.perfectDaysCount)
    }

    @Test
    fun `70 percent threshold qualifies for daily streak while 100 percent is tracked separately`() {
        val todayStr = "2026-10-20"
        val createdMillis = dateFormat.parse("2026-10-01")!!.time
        val habits = (1L..4L).map { id ->
            HabitEntity(id = id, title = "Habit $id", frequency = "Daily", createdAt = createdMillis)
        }

        // Complete 3 of 4 habits (75% >= 70% threshold, but not 100%)
        val logs = listOf(
            HabitLogEntity(habitId = 1, dateString = todayStr, completed = true),
            HabitLogEntity(habitId = 2, dateString = todayStr, completed = true),
            HabitLogEntity(habitId = 3, dateString = todayStr, completed = true),
            HabitLogEntity(habitId = 4, dateString = todayStr, completed = false)
        )

        val eval75 = repository.evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logs.groupBy { it.dateString },
            earliestLogByHabit = emptyMap(),
            existingRewardedMilestones = emptySet(),
            todayStr = todayStr
        )

        assertEquals(1, eval75.currentStreakDays)
        assertEquals(0, eval75.perfectDaysCount)

        // Now complete all 4 of 4 habits (100% Perfect Day)
        val logs100 = logs.map { it.copy(completed = true) }
        val eval100 = repository.evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logs100.groupBy { it.dateString },
            earliestLogByHabit = emptyMap(),
            existingRewardedMilestones = emptySet(),
            todayStr = todayStr
        )

        assertEquals(1, eval100.currentStreakDays)
        assertEquals(1, eval100.perfectDaysCount)

        // Complete only 2 of 4 habits (50% < 70% threshold)
        val logs50 = logs.mapIndexed { index, log -> log.copy(completed = index < 2) }
        val eval50 = repository.evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logs50.groupBy { it.dateString },
            earliestLogByHabit = emptySet<Pair<Long, String>>().toMap(),
            existingRewardedMilestones = emptySet(),
            todayStr = todayStr
        )

        assertEquals(0, eval50.currentStreakDays)
        assertEquals(0, eval50.perfectDaysCount)
    }

    @Test
    fun `7 consecutive successful streak days awards 1 life and prevents duplicate rewards`() {
        val createdMillis = dateFormat.parse("2026-10-01")!!.time
        val habits = listOf(
            HabitEntity(id = 1, title = "Read", frequency = "Daily", createdAt = createdMillis)
        )

        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse("2026-10-01")!!
        }
        val logs = mutableListOf<HabitLogEntity>()
        var lastDateStr = ""
        for (day in 1..7) {
            val dStr = dateFormat.format(cal.time)
            lastDateStr = dStr
            logs.add(HabitLogEntity(habitId = 1, dateString = dStr, completed = true))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val firstEval = repository.evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logs.groupBy { it.dateString },
            earliestLogByHabit = mapOf(1L to "2026-10-01"),
            existingRewardedMilestones = emptySet(),
            todayStr = lastDateStr
        )

        assertEquals(7, firstEval.currentStreakDays)
        assertEquals(1, firstEval.allRewardedMilestones.size)
        assertEquals(11, firstEval.livesRemaining) // 10 initial + 1 reward = 11

        // Re-evaluate with existingRewardedMilestones populated: must NOT grant duplicate life
        val secondEval = repository.evaluateStreaksAndLives(
            habits = habits,
            logsByDate = logs.groupBy { it.dateString },
            earliestLogByHabit = mapOf(1L to "2026-10-01"),
            existingRewardedMilestones = firstEval.allRewardedMilestones,
            todayStr = lastDateStr
        )

        assertEquals(1, secondEval.allRewardedMilestones.size)
        assertEquals(11, secondEval.livesRemaining)
    }

    @Test
    fun `heatmap level mapping and current month cells map accurately`() {
        assertEquals(0, repository.calculateHeatmapLevel(0, 4))
        assertEquals(1, repository.calculateHeatmapLevel(1, 4)) // 25% -> Level 1
        assertEquals(2, repository.calculateHeatmapLevel(2, 4)) // 50% -> Level 2
        assertEquals(3, repository.calculateHeatmapLevel(3, 4)) // 75% -> Level 3 (Streak)
        assertEquals(4, repository.calculateHeatmapLevel(4, 4)) // 100% -> Level 4 (Perfect)
    }

    @Test
    fun `weekly completion rate excludes unscheduled weekend habits and future days`() {
        // Wednesday Oct 14, 2026 (Mon Oct 12 .. Wed Oct 14 = 3 weekdays elapsed)
        val wednesdayDate = dateFormat.parse("2026-10-14")!!
        val oct1Millis = dateFormat.parse("2026-10-01")!!.time

        val weekdayHabit = HabitEntity(
            id = 1,
            title = "Code",
            frequency = "Weekdays",
            createdAt = oct1Millis
        )
        val weekendHabit = HabitEntity(
            id = 2,
            title = "Hike",
            frequency = "Weekends",
            createdAt = oct1Millis
        )

        val logs = listOf(
            HabitLogEntity(habitId = 1, dateString = "2026-10-12", completed = true),
            HabitLogEntity(habitId = 1, dateString = "2026-10-13", completed = true)
            // 2026-10-14 not completed yet -> 1 missed out of 3 scheduled weekdays
            // weekendHabit has 0 scheduled days between Mon..Wed!
        )

        val weeklyStats = repository.calculateCompletionStatsForFilter(
            filter = CompletionRateFilter.WEEKLY,
            habits = listOf(weekdayHabit, weekendHabit),
            logsByDate = logs.groupBy { it.dateString },
            earliestLogByHabit = emptyMap(),
            referenceDate = wednesdayDate
        )

        assertEquals(3, weeklyStats.totalScheduledCount)
        assertEquals(2, weeklyStats.doneCount)
        assertEquals(1, weeklyStats.missedCount)
        assertEquals(67, weeklyStats.completionPercent)
    }

    @Test
    fun `habit limit allows between 3 and 10 habits and blocks the 11th habit while backup restores without duplicates`() = runBlocking {
        dao.clearAllLogs()
        dao.clearAllHabits()

        // Create 10 habits (within the 3 to 10 allowed range)
        val createdIds = mutableListOf<Long>()
        for (i in 1..10) {
            val res = repository.addHabit(HabitEntity(title = "Habit $i"))
            assertTrue("Habit $i should succeed", res.isSuccess)
            createdIds.add(res.getOrThrow())
        }
        assertEquals(10, dao.getAllHabitsList().size)

        // Duplicate habit name should fail
        val dup = repository.addHabit(HabitEntity(title = "habit 1"))
        assertTrue(dup.isFailure)

        // 11th habit should be blocked by the 10-habit maximum
        val h11Blocked = repository.addHabit(HabitEntity(title = "Habit 11"))
        assertTrue(h11Blocked.isFailure)

        // Toggle habit completion and export backup snapshot
        val firstHabitId = createdIds.first()
        repository.toggleHabit(firstHabitId, "2026-10-10")
        val snapshotJson = repository.backupManager.createSnapshotJson().getOrThrow()

        // Wipe database and restore from snapshot
        dao.clearAllHabits()
        dao.clearAllLogs()
        assertEquals(0, dao.getAllHabitsList().size)

        val restoredCount = repository.backupManager.restoreFromSnapshotJson(snapshotJson).getOrThrow()
        assertEquals(10, restoredCount)
        assertEquals(10, dao.getAllHabitsList().size)
        assertEquals(1, dao.getAllLogsSync().count { it.completed })
    }

    @Test
    fun `security audit fixes redact syncKey from JSON exports, enforce passkey verification, and validate input bounds`() = runBlocking {
        dao.clearAllLogs()
        dao.clearAllHabits()

        // SEC-09: Input validation bounds reject overly long habit titles (> 100 chars)
        val longTitle = "A".repeat(101)
        val invalidHabitResult = repository.addHabit(HabitEntity(title = longTitle))
        assertTrue(invalidHabitResult.isFailure)

        // Add a valid habit and save local backup with a secret passkey
        val h1 = repository.addHabit(HabitEntity(title = "Morning Run")).getOrThrow()
        repository.toggleHabit(h1, "2026-10-15")

        val secretPasskey = "MySuperSecretPasskey#2026"
        val syncSave = repository.backupManager.syncAccountData("user@example.com", secretPasskey)
        assertTrue(syncSave.isSuccess)

        // SEC-01: Stored profile syncKey must be a salted PBKDF2 hash, never plaintext
        val storedProfile = dao.getUserProfileSync()!!
        assertFalse(storedProfile.syncKey.contains(secretPasskey))
        assertTrue(storedProfile.syncKey.startsWith("pbkdf2:"))

        // SEC-01 & SEC-05: Exported JSON snapshot must NEVER contain syncKey or the plaintext passkey
        val exportedJson = repository.backupManager.createSnapshotJson().getOrThrow()
        assertFalse(exportedJson.contains("syncKey"))
        assertFalse(exportedJson.contains(secretPasskey))

        // SEC-04: Clear local logs/habits and attempt to recover with wrong passkey -> must fail
        dao.clearAllLogs()
        dao.clearAllHabits()
        val wrongKeyAttempt = repository.backupManager.syncAccountData("user@example.com", "WrongPasskey")
        assertTrue(wrongKeyAttempt.isFailure)

        // Recover with correct passkey -> must succeed
        val rightKeyAttempt = repository.backupManager.syncAccountData("user@example.com", secretPasskey)
        assertTrue(rightKeyAttempt.isSuccess)
        assertEquals(1, dao.getAllHabitsList().size)
    }

    @Test
    fun `HabitViewModel instantiates cleanly via AndroidViewModelFactory`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        val viewModel = factory.create(com.example.ui.HabitViewModel::class.java)
        assertEquals(0, viewModel.selectedTab.value)
    }
}
