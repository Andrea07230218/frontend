package com.example.thelastone.data.repo.impl

import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.AgeBand
import com.example.thelastone.data.model.DaySchedule
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.model.TripVisibility
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.data.repo.TripStats // 👈 確保 import 正確
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    // 您其他的依賴注入，例如 Firestore, RemoteDataSource 等
) : TripRepository {

    private var formForPreview: TripForm? = null

    override suspend fun createTrip(form: TripForm): Trip {
        println("TripRepositoryImpl: 正在建立一個**假**的預覽行程: ${form.name}")

        val daysList = try {
            val start = LocalDate.parse(form.startDate)
            val end = LocalDate.parse(form.endDate)
            val numOfDays = ChronoUnit.DAYS.between(start, end) + 1
            (0 until numOfDays.toInt()).map {
                DaySchedule(
                    date = start.plusDays(it.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    activities = emptyList()
                )
            }
        } catch (e: Exception) {
            listOf(DaySchedule(date = form.startDate, activities = emptyList()))
        }

        return Trip(
            id = "preview_trip_id_${System.currentTimeMillis()}",
            createdBy = "preview_user",
            name = form.name,
            locations = form.locations,
            totalBudget = form.totalBudget,
            startDate = form.startDate,
            endDate = form.endDate,
            activityStart = form.activityStart,
            activityEnd = form.activityEnd,
            avgAge = form.avgAge,
            transportPreferences = form.transportPreferences,
            useGmapsRating = form.useGmapsRating,
            styles = form.styles,
            visibility = form.visibility,
            members = emptyList(),
            days = daysList
        )
    }

    override suspend fun saveTrip(trip: Trip): Trip {
        println("TripRepositoryImpl: 假裝正在儲存行程到遠端: ${trip.name}")
        return trip
    }

    override suspend fun getMyTrips(): List<Trip> {
        return emptyList()
    }

    override fun observeMyTrips(): Flow<List<Trip>> {
        return flowOf(emptyList())
    }

    override suspend fun getPublicTrips(): List<Trip> {
        return emptyList()
    }

    override fun observePublicTrips(): Flow<List<Trip>> {
        return flowOf(emptyList())
    }

    override suspend fun getTripDetail(tripId: String): Trip {
        TODO("尚未實作 getTripDetail 的假資料")
    }

    override fun observeTripDetail(tripId: String): Flow<Trip> {
        TODO("尚未實作 observeTripDetail 的假資料")
    }

    override suspend fun addActivity(tripId: String, dayIndex: Int, activity: Activity) {}
    override suspend fun updateActivity(tripId: String, dayIndex: Int, activityIndex: Int, updated: Activity) {}
    override suspend fun removeActivity(tripId: String, dayIndex: Int, activityIndex: Int) {}
    override suspend fun deleteTrip(tripId: String) {}
    override suspend fun addMembers(tripId: String, userIds: List<String>) {}

    override suspend fun getTripStatsFor(userId: String): TripStats {
        return TripStats(0, 0) // ✅ 現在 TripStats 被正確識別
    }

    override fun setTripFormForPreview(form: TripForm) {
        this.formForPreview = form
    }

    override fun getTripFormForPreview(): TripForm? {
        val form = this.formForPreview
        this.formForPreview = null
        return form
    }
}

