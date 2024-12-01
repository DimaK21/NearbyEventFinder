package ru.kryu.nearbyeventfinder.domain.api

import android.location.Location
import ru.kryu.nearbyeventfinder.domain.model.Event
import java.time.LocalDate

interface EventRepository {
    suspend fun getEvents(): List<Event>
    fun filterEvents(
        selectedDate: LocalDate? = null,
        selectedType: String? = null,
        maxDistance: Float? = null,
        currentLocation: Location? = null
    ): List<Event>
}