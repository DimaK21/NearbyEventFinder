package ru.kryu.nearbyeventfinder.data.converter

import ru.kryu.nearbyeventfinder.data.network.EventDto
import ru.kryu.nearbyeventfinder.data.network.LocationDto
import ru.kryu.nearbyeventfinder.data.storage.EventEntity
import ru.kryu.nearbyeventfinder.domain.model.Event
import ru.kryu.nearbyeventfinder.domain.model.Location

fun LocationDto.toDomain() = Location(
    lat = lat,
    lng = lng,
)

fun Location.toDto() = LocationDto(
    lat = lat,
    lng = lng,
)

fun EventDto.toDomain() = Event(
    id = id,
    name = name,
    type = type,
    description = description,
    location = location.toDomain(),
    date = date,
)

fun Event.toDto() = EventDto(
    id = id,
    name = name,
    type = type,
    description = description,
    location = location.toDto(),
    date = date,
)

fun Event.toEntity() = EventEntity(
    id = id,
    name = name,
    type = type,
    description = description,
    lat = location.lat,
    lng = location.lng,
    date = date,
)

fun EventEntity.toDomain() = Event(
    id = id,
    name = name,
    type = type,
    description = description,
    location = Location(
        lat = lat,
        lng = lng
    ),
    date = date,
)
