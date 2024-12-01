package ru.kryu.nearbyeventfinder.data.network

data class EventDto(
    val id: Int,
    val name: String,
    val type: String, // Тип мероприятия
    val description: String,
    val location: LocationDto,
    val date: String, // ISO 8601 формат: "2024-12-15T18:00:00"
)