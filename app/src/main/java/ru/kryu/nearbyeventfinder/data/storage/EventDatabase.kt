package ru.kryu.nearbyeventfinder.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EventEntity::class], version = 1)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}