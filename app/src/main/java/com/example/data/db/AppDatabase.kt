package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        CategoryEntity::class,
        UserProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `userName` TEXT NOT NULL,
                        `userEmail` TEXT NOT NULL,
                        `syncKey` TEXT NOT NULL,
                        `livesCount` INTEGER NOT NULL,
                        `maxLivesCount` INTEGER NOT NULL,
                        `rewardedMilestonesCsv` TEXT NOT NULL,
                        `consumedLifeDatesCsv` TEXT NOT NULL,
                        `morningReminderEnabled` INTEGER NOT NULL,
                        `morningReminderTime` TEXT NOT NULL,
                        `customMorningMessage` TEXT NOT NULL,
                        `morningBannerDismissedDate` TEXT NOT NULL,
                        `hasSeededInitialData` INTEGER NOT NULL,
                        `lastSyncedAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitflow.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
