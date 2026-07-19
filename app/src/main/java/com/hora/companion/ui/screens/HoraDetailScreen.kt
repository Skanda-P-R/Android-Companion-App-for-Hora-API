package com.hora.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.companion.utils.TranslationUtils
import com.hora.companion.repository.HoraRepository
import com.hora.companion.utils.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraDetailScreen(
    navController: NavController,
    repo: HoraRepository,
    location: Pair<Double, Double>?,
    locationName: String?,
    lang: String = "en"
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    var state by remember { mutableStateOf(PanchangaState(isLoading = true)) }
    
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val displayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun fetchData(force: Boolean = false) {
        scope.launch {
            state = PanchangaState(isLoading = true)
            val res = repo.fetchHora(
                lat = location?.first,
                lon = location?.second,
                location = locationName,
                date = sdfDate.format(selectedDate.time),
                time = sdfTime.format(selectedTime.time),
                lang = lang,
                force = force
            )
            state = if (res.isSuccess) {
                repo.mergeToState(horaJson = res.getOrNull())
            } else {
                state.copy(isLoading = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    LaunchedEffect(selectedDate, selectedTime, location, locationName) {
        fetchData()
    }

    var remainingDisplay by remember { mutableStateOf(state.remaining) }

    LaunchedEffect(state.horaEndsAt, state.remaining) {
        while (true) {
            val endsAtStr = state.horaEndsAt
            val today = Calendar.getInstance()
            val isCurrentTime = sdfDate.format(selectedDate.time) == sdfDate.format(today.time) && 
                               Math.abs(selectedTime.timeInMillis - today.timeInMillis) < 600000 // within 10 mins

            if (endsAtStr != null && isCurrentTime) {
                try {
                    val endsAt = ZonedDateTime.parse(endsAtStr).toInstant().toEpochMilli()
                    val now = System.currentTimeMillis()
                    if (now < endsAt) {
                        val diffMinutes = (endsAt - now) / 60000
                        remainingDisplay = if (diffMinutes > 0) "$diffMinutes min" else "< 1 min"
                    } else {
                        remainingDisplay = "0 min"
                        if (NetworkUtils.isOnline(context)) {
                            fetchData(force = false)
                        }
                    }
                } catch (e: Exception) {
                    remainingDisplay = state.remaining
                }
            } else {
                remainingDisplay = state.remaining
            }
            delay(10000)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it
                        selectedDate = cal
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimeSelectorDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { h, m ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, m)
                selectedTime = cal
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Hora", lang)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f).padding(4.dp).clickable { showDatePicker = true }) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Text(displayDate.format(selectedDate.time), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Card(modifier = Modifier.weight(1f).padding(4.dp).clickable { showTimePicker = true }) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Text(sdfTime.format(selectedTime.time), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(TranslationUtils.translate("Current Hora", lang), style = MaterialTheme.typography.titleMedium)
                        Text(state.horaSymbol + " " + state.hora, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            DetailItem(TranslationUtils.translate("Ends", lang), state.horaEnds)
                            DetailItem(TranslationUtils.translate("Remaining", lang), remainingDisplay)
                        }
                        if (remainingDisplay == "0 min" && !NetworkUtils.isOnline(context)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = TranslationUtils.translate("Updated", lang) + ": " + state.lastUpdated,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(TranslationUtils.translate("Timeline", lang), style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(TranslationUtils.translate("Next Hora", lang), state.horaNext)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
