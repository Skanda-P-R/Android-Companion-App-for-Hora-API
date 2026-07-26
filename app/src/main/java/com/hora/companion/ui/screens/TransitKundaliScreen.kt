package com.hora.companion.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hora.companion.models.DashaPeriod
import com.hora.companion.utils.TranslationUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitKundaliScreen(
    navController: NavController,
    viewModel: TransitViewModel,
    location: Pair<Double, Double>?,
    locationName: String?,
    apiBase: String,
    sessionToken: String?,
    lang: String = "en",
    dashaLevel: Int = 3,
    chartStyle: String = "south"
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val state by viewModel.state.collectAsState()
    val selectedL1 by viewModel.selectedL1.collectAsState()
    val selectedL2 by viewModel.selectedL2.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Info, 1: Kundali, 2: Dasha

    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val displayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val valueFontWeight = if (lang == "kn") FontWeight.Normal else FontWeight.Bold

    // Initial and on-change fetch
    LaunchedEffect(selectedDate, selectedTime, location, locationName, dashaLevel, chartStyle) {
        viewModel.fetchData(
            lat = location?.first,
            lon = location?.second,
            location = locationName,
            date = sdfDate.format(selectedDate.time),
            time = sdfTime.format(selectedTime.time),
            lang = lang,
            apiBase = apiBase,
            depth = dashaLevel,
            chartStyle = chartStyle,
            sessionToken = sessionToken
        )
    }

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
            // Date & Time Selectors
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

            // Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text(text = TranslationUtils.translate("Info", lang), modifier = Modifier.padding(16.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text(text = TranslationUtils.translate("Kundali", lang), modifier = Modifier.padding(16.dp))
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text(text = TranslationUtils.translate("Dasha", lang), modifier = Modifier.padding(16.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Crossfade(targetState = activeTab, label = "TabTransition") { tab ->
                        when (tab) {
                            0 -> InfoTab(state, viewModel, lang, valueFontWeight)
                            1 -> KundaliTab(state, sessionToken)
                            2 -> DashaTab(state, viewModel, selectedL1, selectedL2, valueFontWeight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTab(state: TransitState, viewModel: TransitViewModel, lang: String, valueFontWeight: FontWeight) {
    val dasha = state.dashaResponse ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(TranslationUtils.translate("Moon Information", lang), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Rashi:", lang))
                    Text(dasha.moon.rasi, fontWeight = valueFontWeight)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Nakshatra:", lang))
                    Text(dasha.moon.nakshatra, fontWeight = valueFontWeight)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(TranslationUtils.translate("Balance of Dasha", lang), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Lord:", lang))
                    Text(dasha.dashaBalance.lord, fontWeight = valueFontWeight)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Remaining:", lang))
                    Text(
                        viewModel.formatDecimalYears(dasha.dashaBalance.remainingYears),
                        fontWeight = valueFontWeight
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(TranslationUtils.translate("Active Dasha", lang), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ActiveDashaRow(TranslationUtils.translate("Mahadasha", lang), dasha.activeDasha.mahadasha, valueFontWeight)
                dasha.activeDasha.antardasha?.let { 
                    ActiveDashaRow(TranslationUtils.translate("Antardasha", lang), it, valueFontWeight) 
                }
                dasha.activeDasha.pratyantardasha?.let { 
                    ActiveDashaRow(TranslationUtils.translate("Pratyantardasha", lang), it, valueFontWeight) 
                }
            }
        }
    }
}

@Composable
fun ActiveDashaRow(label: String, value: String, valueFontWeight: FontWeight) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = valueFontWeight)
    }
}

@Composable
fun KundaliTab(state: TransitState, sessionToken: String?) {
    val url = state.chartUrl ?: return
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .addHeader("Authorization", "Bearer $sessionToken")
                .build(),
            contentDescription = "Transit Kundali",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun DashaTab(
    state: TransitState,
    viewModel: TransitViewModel,
    selectedL1: DashaPeriod?,
    selectedL2: DashaPeriod?,
    valueFontWeight: FontWeight
) {
    val timeline = state.dashaResponse?.timeline ?: return
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Level 1 Breadcrumb
        if (selectedL1 != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    text = "L1: ${selectedL1.lord}",
                    modifier = Modifier.clickable { viewModel.selectL1(null) }.padding(8.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                if (selectedL2 != null) {
                    Text(" > ", modifier = Modifier.align(Alignment.CenterVertically))
                    Text(
                        text = "L2: ${selectedL2.lord}",
                        modifier = Modifier.clickable { viewModel.selectL2(null) }.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val displayList = when {
            selectedL2 != null -> selectedL2.subPeriods
            selectedL1 != null -> selectedL1.subPeriods
            else -> timeline
        }

        val canClick = selectedL2 == null

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayList.forEach { period ->
                DashaPeriodItem(period, canClick, valueFontWeight) {
                    if (period.level == 1) {
                        viewModel.selectL1(period)
                    } else if (period.level == 2) {
                        viewModel.selectL2(period)
                    }
                }
            }
        }
    }
}

@Composable
fun DashaPeriodItem(period: DashaPeriod, clickable: Boolean, valueFontWeight: FontWeight, onClick: () -> Unit) {
    val rawStart = period.start.split("T").first()
    val rawEnd = period.end.split("T").first()

    // Reformat YYYY-MM-DD to DD-MM-YYYY
    val formatRaw = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatDisplay = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    
    val startDate = try {
        val date = formatRaw.parse(rawStart)
        if (date != null) formatDisplay.format(date) else rawStart
    } catch (_: Exception) { rawStart }

    val endDate = try {
        val date = formatRaw.parse(rawEnd)
        if (date != null) formatDisplay.format(date) else rawEnd
    } catch (_: Exception) { rawEnd }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = period.lord,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = valueFontWeight,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$startDate to $endDate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
