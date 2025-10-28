// 檔案路徑：data/local/TripDao.kt
package com.example.thelastone.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thelastone.data.model.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    /**
     * 插入一個行程。如果 ID 已經存在，就把它蓋掉 (OnConflictStrategy.REPLACE)。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    /**
     * 根據 ID 觀察一個行程的詳細資料。
     * (這會修復 TripDetailViewModel 的閃退)
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeTripDetail(tripId: String): Flow<Trip?>

    /**
     * 觀察所有 visibility (可見度) 為 "PUBLIC" 的行程。
     * (這會修復 ExploreViewModel 的閃退)
     */
    @Query("SELECT * FROM trips WHERE visibility = 'PUBLIC'")
    fun observePublicTrips(): Flow<List<Trip>>
    @Query("SELECT * FROM trips") // 或者根據 user ID 過濾? e.g., "SELECT * FROM trips WHERE createdBy = :userId"
    fun observeMyTrips(/* userId: String? */): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripByIdBlocking(tripId: String): Trip? // 用於 getTripDetail
}