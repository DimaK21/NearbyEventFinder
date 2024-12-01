package ru.kryu.nearbyeventfinder.domain.api

import android.location.Location
import kotlinx.coroutines.flow.Flow
import ru.kryu.nearbyeventfinder.domain.model.Event
import java.time.LocalDate

interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    fun filterEvents(
        selectedDate: LocalDate? = null,
        selectedType: String? = null,
        maxDistance: Float? = null,
        currentLocation: Location? = null
    ): Flow<List<Event>>
}