package ru.kryu.nearbyeventfinder.data

import android.location.Location
import ru.kryu.nearbyeventfinder.data.converter.toDomain
import ru.kryu.nearbyeventfinder.data.converter.toEntity
import ru.kryu.nearbyeventfinder.data.network.EventApi
import ru.kryu.nearbyeventfinder.data.storage.EventDao
import ru.kryu.nearbyeventfinder.domain.api.EventRepository
import ru.kryu.nearbyeventfinder.domain.model.Event
import java.time.LocalDate
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
) : EventRepository {

    private var cachedEvents: List<Event> = emptyList()

    override suspend fun getEvents(): List<Event> {
        cachedEvents = try {
            val url = URL
            val events = eventApi.getEvents(url).map { it.toDomain() }
            eventDao.insertEvents(events.map { it.toEntity() })
            events
        } catch (e: Exception) {
            eventDao.getAllEvents().map { it.toDomain() }
        }
        return cachedEvents
    }

    override fun filterEvents(
        selectedDate: LocalDate?,
        selectedType: String?,
        maxDistance: Float?,
        currentLocation: Location?
    ): List<Event> {
        return cachedEvents.filter { event ->
            // Фильтр по дате
            val isDateMatch = selectedDate?.let {
                val eventDate = LocalDate.parse(event.date.substringBefore("T"))
                eventDate == it
            } ?: true

            // Фильтр по типу
            val isTypeMatch = selectedType?.let { type ->
                event.type.equals(type, ignoreCase = true)
            } ?: true

            // Фильтр по расстоянию
            val isDistanceMatch = maxDistance?.let { distance ->
                currentLocation?.let { location ->
                    val eventLocation = Location("").apply {
                        latitude = event.location.lat
                        longitude = event.location.lng
                    }
                    location.distanceTo(eventLocation) / 1000 <= distance
                } ?: true
            } ?: true

            isDateMatch && isTypeMatch && isDistanceMatch
        }
    }

    companion object {
        const val URL =
            "https://gist.githubusercontent.com/DimaK21/b69522fffe46cd189d1a7f7f80fabcc9/raw/99760402dde1adfd4102f7b44d7c1e23e583c745/gistfile1.json"
    }
}