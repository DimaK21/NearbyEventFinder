package ru.kryu.nearbyeventfinder.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.kryu.nearbyeventfinder.domain.model.Event
import ru.kryu.nearbyeventfinder.presentation.EventViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun EventDetailScreen(
    eventId: Int,
    viewModel: EventViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState().value
    val event = viewModel.getEventById(eventId)

    val userLocation = remember { mutableStateOf<Location?>(null) }
    val distance = remember { mutableStateOf<Float?>(null) }
    getCurrentLocation(context) { location ->
        userLocation.value = location
        event?.location?.let { eventLocation ->
            val eventLocationObj = Location("event").apply {
                latitude = eventLocation.lat
                longitude = eventLocation.lng
            }
            val dist = calculateDistanceToEvent(location, eventLocationObj)
            distance.value = dist / 1000f
        }
    }

    if (state.isLoading) {
        Text(
            text = "Loading...",
            modifier = Modifier.fillMaxSize(),
            fontSize = 18.sp,
            color = Color.Gray
        )
        return
    }

    if (event != null && distance.value != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = event.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Type: ${event.type}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Date: ${event.date}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = event.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Distance: ${String.format("%.2f", distance.value)} km",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    addEventToCalendar(
                        context = context,
                        event = event
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add to Calendar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        Text(
            text = "Event not found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Red,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun addEventToCalendar(context: Context, event: Event) {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val localDateTime = LocalDateTime.parse(event.date, formatter)
    val startMillis = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endMillis = startMillis + TimeUnit.HOURS.toMillis(2)

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.name)
        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        putExtra(
            CalendarContract.Events.EVENT_LOCATION,
            "${event.location.lat}, ${event.location.lng}"
        )
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
    }
}

fun calculateDistanceToEvent(userLocation: Location?, eventLocation: Location): Float {
    return userLocation?.distanceTo(eventLocation) ?: 0f
}

