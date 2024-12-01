package ru.kryu.nearbyeventfinder.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_table")
data class EventEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val date: String,
)
