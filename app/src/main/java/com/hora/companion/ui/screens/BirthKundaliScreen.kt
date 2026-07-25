package com.hora.companion.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.FolderOpen
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthKundaliScreen(
    navController: NavController,
    viewModel: BirthViewModel,
    location: Pair<Double, Double>?,
    locationName: String?,
    apiBase: String,
    sessionToken: String?,
    lang: String = "en",
    dashaLevel: Int = 3,
    savePath: String? = null
) {
    val state by viewModel.state.collectAsState()
    
    var nameInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var showChart by remember { mutableStateOf(false) }
    
    // For Location Searchable Dropdown
    var locationSearch by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLocName by remember { mutableStateOf(locationName ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val selectedL1 by viewModel.selectedL1.collectAsState()
    val selectedL2 by viewModel.selectedL2.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Info, 1: Kundali, 2: Dasha

    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.loadKundali(it) { success, msg ->
                if (success) {
                    showChart = true
                    // Update local UI state from the loaded ViewModel state
                    val loadedState = viewModel.state.value
                    nameInput = loadedState.inputName
                    selectedLocName = loadedState.inputLocationName ?: ""
                    
                    // Parse date and time from string if they are in standard formats
                    try {
                        val calDate = Calendar.getInstance()
                        calDate.time = sdfDate.parse(loadedState.inputDate)!!
                        selectedDate = calDate
                        
                        val calTime = Calendar.getInstance()
                        calTime.time = sdfTime.parse(loadedState.inputTime)!!
                        selectedTime = calTime
                    } catch (_: Exception) {}
                    
                    scope.launch { snackbarHostState.showSnackbar("Loaded: ${loadedState.inputName}") }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Load failed: $msg") }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchLocations()
    }
    
    LaunchedEffect(state.inputName) {
        if (state.inputName.isNotEmpty()) nameInput = state.inputName
    }

    val valueFontWeight = if (lang == "kn") FontWeight.Normal else FontWeight.Bold

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Birth Kundali", lang)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showChart) showChart = false else navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showChart) {
                        IconButton(onClick = {
                            viewModel.saveKundali(savePath) { success, msg ->
                                scope.launch { snackbarHostState.showSnackbar(if (success) msg ?: "Saved!" else "Save failed: $msg") }
                            }
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    } else {
                        IconButton(onClick = { fileLauncher.launch(arrayOf("application/json", "application/octet-stream")) }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Load")
                        }
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
                Button(
                    onClick = { fileLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(TranslationUtils.translate("Load Saved Details", lang))
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Place of Birth Searchable Dropdown
                val filteredLocations = state.locations.filter { it.name.contains(locationSearch, ignoreCase = true) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (expanded) locationSearch else selectedLocName,
                        onValueChange = { 
                            locationSearch = it
                            if (!expanded) expanded = true
                        },
                        label = { Text(TranslationUtils.translate("Place of Birth", lang)) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        placeholder = { Text(TranslationUtils.translate("Search Location", lang)) }
                    )
                    
                    if (filteredLocations.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredLocations.take(10).forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = {
                                        selectedLocName = loc.name
                                        locationSearch = loc.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (selectedDate != null && selectedTime != null) {
                            val finalLocName = if (selectedLocName.isNotEmpty()) selectedLocName else locationName
                            viewModel.fetchData(
                                lat = if (finalLocName == null) location?.first else null,
                                lon = if (finalLocName == null) location?.second else null,
                                location = finalLocName,
                                date = sdfDate.format(selectedDate!!.time),
                                time = sdfTime.format(selectedTime!!.time),
                                name = nameInput,
                                lang = lang,
                                apiBase = apiBase,
                                depth = dashaLevel,
                                sessionToken = sessionToken
                            )
                            showChart = true 
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(TranslationUtils.translate("Submit", lang))
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                                0 -> BirthInfoTab(state, viewModel, lang, valueFontWeight)
                                1 -> BirthKundaliTab(state, sessionToken)
                                2 -> BirthDashaTab(state, viewModel, selectedL1, selectedL2, valueFontWeight)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BirthInfoTab(state: BirthState, viewModel: BirthViewModel, lang: String, valueFontWeight: FontWeight) {
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
                Text(TranslationUtils.translate("Entered Details", lang), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Name:", lang))
                    Text(state.inputName.ifEmpty { "--" }, fontWeight = valueFontWeight)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Date:", lang))
                    Text(state.inputDate, fontWeight = valueFontWeight)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(TranslationUtils.translate("Time:", lang))
                    Text(state.inputTime, fontWeight = valueFontWeight)
                }
            }
        }

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
fun BirthKundaliTab(state: BirthState, sessionToken: String?) {
    val url = state.chartUrl ?: return
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .addHeader("Authorization", "Bearer $sessionToken")
                .build(),
            contentDescription = "Birth Kundali",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun BirthDashaTab(
    state: BirthState,
    viewModel: BirthViewModel,
    selectedL1: DashaPeriod?,
    selectedL2: DashaPeriod?,
    valueFontWeight: FontWeight
) {
    val timeline = state.dashaResponse?.timeline ?: return
    
    Column(modifier = Modifier.fillMaxSize()) {
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
                BirthDashaPeriodItem(period, canClick, valueFontWeight) {
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
fun BirthDashaPeriodItem(period: DashaPeriod, clickable: Boolean, valueFontWeight: FontWeight, onClick: () -> Unit) {
    val rawStart = period.start.split("T").first()
    val rawEnd = period.end.split("T").first()

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
