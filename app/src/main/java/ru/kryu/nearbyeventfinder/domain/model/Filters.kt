package ru.kryu.nearbyeventfinder.domain.model

data class Filters(
    val type: String = "all",
    val date: String = "",
    val radius: Int = 20
)
