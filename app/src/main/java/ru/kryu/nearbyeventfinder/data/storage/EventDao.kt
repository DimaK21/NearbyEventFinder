package ru.kryu.nearbyeventfinder.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM event_table WHERE type LIKE :type AND date LIKE :date")
    suspend fun getFilteredEvents(type: String, date: String): List<EventEntity>

    @Query("SELECT * FROM event_table")
    suspend fun getAllEvents(): List<EventEntity>
}