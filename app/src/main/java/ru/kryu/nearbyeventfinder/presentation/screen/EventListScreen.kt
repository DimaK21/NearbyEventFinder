package ru.kryu.nearbyeventfinder.presentation.screen

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import ru.kryu.nearbyeventfinder.domain.model.Event
import ru.kryu.nearbyeventfinder.presentation.EventViewModel
import java.time.LocalDate

@Composable
fun EventListScreen(
    viewModel: EventViewModel = hiltViewModel(),
    onEventClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    RequestLocationPermission { granted ->
        permissionGranted = granted
    }

    Column {
        FilterBar(
            onDateSelected = { date -> viewModel.applyFilters(selectedDate = date) },
            onTypeSelected = { type -> viewModel.applyFilters(selectedType = type) },
            onDistanceSelected = { distance ->
                if (permissionGranted) {
                    getCurrentLocation(context) { location ->
                        if (location != null) {
                            viewModel.applyFilters(
                                maxDistance = distance,
                                currentLocation = location
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Местоположение не определено",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Разрешение на доступ к местоположению не предоставлено",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        if (state.isLoading) {
            Text(
                text = "Loading events...",
                modifier = Modifier.fillMaxSize(),
                fontSize = 18.sp
            )
        } else if (state.events.isEmpty()) {
            Text(
                text = "No events found",
                modifier = Modifier.fillMaxSize(),
                fontSize = 18.sp,
                color = Color.Red
            )
        } else {
            LazyColumn {
                items(state.filteredEvents.ifEmpty { state.events }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Type: ${event.type}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Date: ${event.date}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FilterBar(
    onDateSelected: (LocalDate?) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onDistanceSelected: (Float?) -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedDistance by remember { mutableStateOf<Float?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val eventTypes = listOf("Concert", "Workshop", "Meetup", "Other")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Фильтры",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text("Выбор даты", fontWeight = FontWeight.Bold)

        DatePickerButton(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Тип события", fontWeight = FontWeight.Bold)

        TextButton(onClick = { expanded = true }) {
            Text("Выбрать тип: ${selectedType ?: "Не выбрано"}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            eventTypes.forEach { type ->
                DropdownMenuItem(
                    onClick = {
                        selectedType = type
                        onTypeSelected(type)
                        expanded = false
                    },
                    text = { Text(text = type) }
                )
            }
        }

        // Выбор радиуса
        Slider(
            value = selectedDistance ?: 20f,
            onValueChange = { distance ->
                val roundedDistance = distance.toInt().toFloat()
                selectedDistance = roundedDistance
                onDistanceSelected(distance)
            },
            valueRange = 1f..50f
        )
        Text("Радиус: ${selectedDistance?.toInt() ?: 20} км")
    }
}

@Composable
fun DatePickerButton(selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(newDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    TextButton(onClick = { datePickerDialog.show() }) {
        Text("Выбрать дату: ${selectedDate?.toString() ?: "Не выбрано"}")
    }
}

fun getCurrentLocation(context: Context, onLocationRetrieved: (Location?) -> Unit) {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
        onLocationRetrieved(location)
    }.addOnFailureListener {
        onLocationRetrieved(null)
    }
}


@Composable
fun RequestLocationPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    } else {
        onPermissionResult(true)
    }
}
