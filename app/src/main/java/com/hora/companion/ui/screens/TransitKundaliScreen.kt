package com.hora.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hora.companion.utils.TranslationUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitKundaliScreen(
    navController: NavController,
    location: Pair<Double, Double>?,
    locationName: String?,
    apiBase: String,
    sessionToken: String?,
    lang: String = "en"
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    
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

    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val displayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Transit Kundali", lang)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Card(modifier = Modifier.weight(1f).padding(4.dp).clickable { showDatePicker = true }) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(displayDate.format(selectedDate.time), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(modifier = Modifier.weight(1f).padding(4.dp).clickable { showTimePicker = true }) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sdfTime.format(selectedTime.time), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            val apiLang = if (lang == "kn") "kan" else "en"
            val normalizedBase = if (apiBase.endsWith("/")) apiBase else "$apiBase/"
            val url = buildString {
                append("${normalizedBase}api/v1/kundali/chart?")
                if (locationName != null) {
                    append("location=${java.net.URLEncoder.encode(locationName, "UTF-8")}")
                } else if (location != null) {
                    append("lat=${location.first}&lon=${location.second}")
                } else {
                    // Fallback to default if everything is null
                    append("lat=12.9716&lon=77.5946")
                }
                append("&date=${sdfDate.format(selectedDate.time)}")
                append("&time=${sdfTime.format(selectedTime.time)}")
                append("&lang=$apiLang")
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                var isLoading by remember { mutableStateOf(true) }
                var isError by remember { mutableStateOf(false) }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .addHeader("Authorization", "Bearer $sessionToken")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Transit Kundali",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentScale = ContentScale.Fit,
                    onLoading = { isLoading = true; isError = false },
                    onSuccess = { isLoading = false; isError = false },
                    onError = { isLoading = false; isError = true }
                )
                if (isLoading) CircularProgressIndicator()
                if (isError) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading chart", color = MaterialTheme.colorScheme.error)
                        Text("Check API connection or session", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
