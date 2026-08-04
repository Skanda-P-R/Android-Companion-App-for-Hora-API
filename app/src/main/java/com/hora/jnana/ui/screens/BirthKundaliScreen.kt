package com.hora.jnana.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hora.jnana.models.DashaPeriod
import com.hora.jnana.utils.TranslationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    savePath: String? = null,
    chartStyle: String = "south"
) {
    val state by viewModel.state.collectAsState()
    
    var nameInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var showChart by remember { mutableStateOf(false) }
    var localChartStyle by remember { mutableStateOf(chartStyle) }
    
    // For Location Searchable Dropdown
    var locationSearch by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLocName by remember { mutableStateOf(locationName ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showStyleSelector by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val selectedL1 by viewModel.selectedL1.collectAsState()
    val selectedL2 by viewModel.selectedL2.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })

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
                    
                    localChartStyle = loadedState.chartStyle
                    
                    scope.launch { snackbarHostState.showSnackbar("Loaded: ${loadedState.inputName}") }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Load failed: $msg") }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.fetchLocations()
        nameInput = ""
        selectedDate = null
        selectedTime = null
        selectedLocName = locationName ?: ""
        locationSearch = ""
        showChart = false
    }
    
    LaunchedEffect(state.inputName) {
        nameInput = state.inputName
    }

    // Effect to handle chart style change after chart is shown
    LaunchedEffect(localChartStyle) {
        if (showChart && selectedDate != null && selectedTime != null) {
            // Only fetch if the style changed or we don't have a chart yet
            if (localChartStyle != state.chartStyle || (state.chartUrl == null && state.svgContent == null)) {
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
                    chartStyle = localChartStyle,
                    sessionToken = sessionToken
                )
            }
        }
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

    if (showStyleSelector) {
        BirthStyleSelectorDialog(
            onDismiss = { showStyleSelector = false },
            currentStyle = localChartStyle,
            onStyleSelected = { style ->
                localChartStyle = style
                showStyleSelector = false
            },
            lang = lang
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
                        IconButton(onClick = { showStyleSelector = true }) {
                            Icon(Icons.Default.Grid3x3, contentDescription = "Chart Style")
                        }
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
                                chartStyle = localChartStyle,
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
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                    ) {
                        Text(text = TranslationUtils.translate("Info", lang), modifier = Modifier.padding(16.dp))
                    }
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                    ) {
                        Text(text = TranslationUtils.translate("Kundali", lang), modifier = Modifier.padding(16.dp))
                    }
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } }
                    ) {
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
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            when (page) {
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
    val context = LocalContext.current
    val model = remember(state.chartUrl, state.svgContent) {
        if (!state.svgContent.isNullOrEmpty()) {
            state.svgContent.toByteArray()
        } else {
            ImageRequest.Builder(context)
                .data(state.chartUrl)
                .addHeader("Authorization", "Bearer $sessionToken")
                .build()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = model,
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

@Composable
fun BirthStyleSelectorDialog(
    onDismiss: () -> Unit,
    currentStyle: String,
    onStyleSelected: (String) -> Unit,
    lang: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    TranslationUtils.translate("Chart Style", lang),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf("north", "south", "east")
                    styles.forEach { style ->
                        BirthStyleOption(
                            style = style,
                            isSelected = style == currentStyle,
                            onClick = { onStyleSelected(style) },
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(TranslationUtils.translate("Cancel", lang))
                }
            }
        }
    }
}

@Composable
fun BirthStyleOption(
    style: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    lang: String,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = isSelected,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(48.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                when (style) {
                    "north" -> NorthIndianChartIcon(color = LocalContentColor.current)
                    "south" -> SouthIndianChartIcon(color = LocalContentColor.current)
                    "east" -> EastIndianChartIcon(color = LocalContentColor.current)
                }
            }
            Text(
                text = TranslationUtils.translate(style.replaceFirstChar { it.uppercase() }, lang),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

