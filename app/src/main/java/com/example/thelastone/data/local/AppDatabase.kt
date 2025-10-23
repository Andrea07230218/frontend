package com.example.thelastone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // 👈 1. Import TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.thelastone.data.model.Trip // 👈 2. Import Trip

@Database(
    entities = [
        MessageEntity::class,
        SavedPlaceEntity::class,
        Trip::class // 👈 3. 將 Trip 加入 entities
    ],
    version = 4, // 👈 4. 升級版本 3 -> 4
    exportSchema = false
)
@TypeConverters(Converters::class) // 👈 5. 註冊 TypeConverters
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun tripDao(): TripDao // 👈 6. 加入 TripDao

    companion object {
        // (MIGRATION_1_2 和 MIGRATION_2_3 保持不變)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            /* ... (你的 1 -> 2 程式碼) ... */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_places(
                        placeId TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        address TEXT,
                        lat REAL NOT NULL,
                        lng REAL NOT NULL,
                        rating REAL,
                        userRatingsTotal INTEGER,
                        photoUrl TEXT,
                        savedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_saved_places_placeId ON saved_places(placeId)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            /* ... (你的 2 -> 3 程式碼) ... */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_places ADD COLUMN openingHoursJson TEXT")
                db.execSQL("ALTER TABLE saved_places ADD COLUMN openNow INTEGER")
                db.execSQL("ALTER TABLE saved_places ADD COLUMN openStatusText TEXT")
            }
        }

        // 👈 7. 新增 MIGRATION_3_4 來建立 trips 資料表
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 使用反引號 ` ` 包住資料表/欄位名稱 (好習慣)
                // 根據 Trip.kt 明確定義 NULL / NOT NULL
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trips` (
                        `id` TEXT NOT NULL, 
                        `createdBy` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `locations` TEXT NOT NULL, 
                        `totalBudget` INTEGER,          -- 可為空的 Int? 對應到 INTEGER NULL
                        `startDate` TEXT NOT NULL, 
                        `endDate` TEXT NOT NULL, 
                        `activityStart` TEXT,           -- 可為空的 String? 對應到 TEXT NULL
                        `activityEnd` TEXT,             -- 可為空的 String? 對應到 TEXT NULL
                        `avgAge` TEXT NOT NULL,         -- Enum 透過 Converter 對應到 TEXT NOT NULL
                        `transportPreferences` TEXT NOT NULL, -- List 透過 Converter 對應到 TEXT NOT NULL
                        `useGmapsRating` INTEGER NOT NULL, -- Boolean 對應到 INTEGER NOT NULL (0 或 1)
                        `styles` TEXT NOT NULL,         -- List 透過 Converter 對應到 TEXT NOT NULL
                        `visibility` TEXT NOT NULL,     -- Enum 透過 Converter 對應到 TEXT NOT NULL
                        `members` TEXT NOT NULL,        -- List 透過 Converter 對應到 TEXT NOT NULL
                        `days` TEXT NOT NULL,           -- List 透過 Converter 對應到 TEXT NOT NULL
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                // 索引另外建立 (非必須，但有助效能)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_trips_id` ON `trips`(`id`)")
            }
        }
    }
}