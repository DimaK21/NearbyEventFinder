package ru.kryu.nearbyeventfinder.data.network

import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Url

interface EventApi {
    @GET
    suspend fun getEvents(@Url url: String): Flow<List<EventDto>>
}