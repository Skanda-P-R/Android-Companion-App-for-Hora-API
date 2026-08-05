package com.hora.jnana.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.jnana.utils.TranslationUtils
import com.hora.jnana.repository.HoraRepository
import com.hora.jnana.DataStoreManager
import com.hora.jnana.models.LocationData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    navController: NavController,
    repo: HoraRepository,
    dataStoreManager: DataStoreManager,
    lang: String = "en"
) {
    val scope = rememberCoroutineScope()
    var locations by remember { mutableStateOf<List<LocationData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedLocations by remember { mutableStateOf(setOf<String>()) }

    fun fetchLocations() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val res = repo.fetchLocations()
            if (res.isSuccess) {
                val map = res.getOrNull() ?: emptyMap()
                locations = map.map { (name, data) ->
                    LocationData(
                        name = name,
                        latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                        timezone = data["timezone"]?.toString(),
                        description = data["description"]?.toString()
                    )
                }.sortedBy { it.name }
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Failed to fetch locations"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchLocations()
    }

    val filteredLocations = if (searchQuery.isEmpty()) {
        locations
    } else {
        locations.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (selectedLocations.isEmpty()) {
                        Text(TranslationUtils.translate("Locations", lang))
                    } else {
                        Text("${selectedLocations.size} ${TranslationUtils.translate("Selected", lang)}")
                    }
                },
                navigationIcon = {
                    if (selectedLocations.isEmpty()) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = { selectedLocations = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    }
                },
                actions = {
                    if (selectedLocations.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(TranslationUtils.translate("Search", lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        LocationItem(
                            loc = LocationData(TranslationUtils.translate("Current Location", lang), 0.0, 0.0),
                            isCurrent = true,
                            isSelected = false,
                            onClick = {
                                if (selectedLocations.isEmpty()) {
                                    scope.launch {
                                        dataStoreManager.saveLocationMode("gps")
                                        navController.navigateUp()
                                    }
                                }
                            },
                            onLongClick = {}
                        )
                    }
                    items(filteredLocations) { loc ->
                        val isSelected = selectedLocations.contains(loc.name)
                        LocationItem(
                            loc = loc,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedLocations.isEmpty()) {
                                    scope.launch {
                                        dataStoreManager.saveLocation(loc.latitude, loc.longitude, loc.name, "manual")
                                        navController.navigateUp()
                                    }
                                } else {
                                    selectedLocations = if (isSelected) {
                                        selectedLocations - loc.name
                                    } else {
                                        selectedLocations + loc.name
                                    }
                                }
                            },
                            onLongClick = {
                                if (selectedLocations.isEmpty()) {
                                    selectedLocations = setOf(loc.name)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(TranslationUtils.translate("Confirm Delete", lang)) },
            text = { Text(TranslationUtils.translate("Are you sure you want to delete these locations from the backend?", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showDeleteConfirm = false
                            isLoading = true
                            var success = true
                            selectedLocations.forEach { name ->
                                val res = repo.deleteLocation(name)
                                if (!res.isSuccess) success = false
                            }
                            if (success) {
                                selectedLocations = emptySet()
                                fetchLocations()
                            } else {
                                errorMessage = "Failed to delete some locations"
                                fetchLocations()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(TranslationUtils.translate("Yes, Delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(TranslationUtils.translate("Cancel", lang))
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LocationItem(
    loc: LocationData, 
    isCurrent: Boolean = false, 
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(loc.name) },
        supportingContent = { 
            if (!isCurrent) Text("${loc.latitude}, ${loc.longitude}") else Text("Use GPS")
        },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        colors = if (isSelected) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ListItemDefaults.colors()
    )
    HorizontalDivider(thickness = 0.5.dp)
}
