package ru.kryu.nearbyeventfinder.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.kryu.nearbyeventfinder.data.EventRepositoryImpl
import ru.kryu.nearbyeventfinder.domain.model.Event
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(EventListState())
    val state: StateFlow<EventListState> = _state

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getEvents().collect {
                _state.value = _state.value.copy(events = it, isLoading = false)
            }
        }
    }

    fun getEventById(eventId: Int): Event? {
        return _state.value.filteredEvents.find { it.id == eventId }
            ?: _state.value.events.find { it.id == eventId }
    }

    fun applyFilters(
        selectedDate: LocalDate? = null,
        selectedType: String? = null,
        maxDistance: Float? = null,
        currentLocation: Location? = null
    ) {
        viewModelScope.launch {
            repository.filterEvents(selectedDate, selectedType, maxDistance, currentLocation)
                .collect {
                    _state.value = _state.value.copy(filteredEvents = it)
                }
        }
    }
}

data class EventListState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false
)
