package ru.kryu.nearbyeventfinder.data.network

import retrofit2.http.GET
import retrofit2.http.Url

interface EventApi {
    @GET
    suspend fun getEvents(@Url url: String): List<EventDto>
}