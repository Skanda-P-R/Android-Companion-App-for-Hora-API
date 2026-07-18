package com.hora.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun BirthKundaliScreen(
    navController: NavController,
    location: Pair<Double, Double>?,
    locationName: String?,
    apiBase: String,
    sessionToken: String?,
    lang: String = "en"
) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var showChart by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Birth Kundali", lang)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showChart) showChart = false else navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!showChart) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(TranslationUtils.translate("Name", lang)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text(
                            text = selectedDate?.let { sdfDate.format(it.time) } ?: TranslationUtils.translate("Birth Date", lang),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text(
                            text = selectedTime?.let { sdfTime.format(it.time) } ?: TranslationUtils.translate("Birth Time", lang),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { if (selectedDate != null && selectedTime != null) showChart = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(TranslationUtils.translate("Submit", lang))
                }
            }
        } else {
            val apiLang = if (lang == "kn") "kan" else "en"
            val normalizedBase = if (apiBase.endsWith("/")) apiBase else "$apiBase/"
            val url = buildString {
                append("${normalizedBase}api/v1/kundali/birth/chart?")
                if (locationName != null) {
                    append("location=${java.net.URLEncoder.encode(locationName, "UTF-8")}")
                } else if (location != null) {
                    append("lat=${location.first}&lon=${location.second}")
                } else {
                    append("lat=12.9716&lon=77.5946")
                }
                append("&date=${sdfDate.format(selectedDate!!.time)}")
                append("&time=${sdfTime.format(selectedTime!!.time)}")
                if (name.isNotEmpty()) append("&name=${java.net.URLEncoder.encode(name, "UTF-8")}")
                append("&lang=$apiLang")
            }

            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                var isLoading by remember { mutableStateOf(true) }
                var isError by remember { mutableStateOf(false) }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .addHeader("Authorization", "Bearer $sessionToken")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Birth Kundali",
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
                        Text("Check details or session", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
