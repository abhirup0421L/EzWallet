package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DocumentItem
import com.example.data.model.UserProfile

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE documents ADD COLUMN thumbnailPath TEXT")
        db.execSQL("ALTER TABLE documents ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN viewMode TEXT NOT NULL DEFAULT 'BARS'")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE documents ADD COLUMN groupId INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN customBgColor TEXT")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN customSelectionColor TEXT")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN customFileColor TEXT")
    }
}

@Database(
    entities = [DocumentItem::class, UserProfile::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EzWalletDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: EzWalletDatabase? = null

        fun getInstance(context: Context): EzWalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EzWalletDatabase::class.java,
                    "ezwallet_database.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

