package com.hora.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.companion.utils.TranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    location: Pair<Double, Double>,
    locationName: String?,
    locationMode: String,
    lang: String = "en"
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Hora Companion", lang)) },
                actions = {
                    IconButton(onClick = { 
                        if (locationMode == "gps") {
                            viewModel.refresh(context, location.first, location.second, null)
                        } else {
                            viewModel.refresh(context, null, null, locationName)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

            LocationCard(locationName, locationMode, lang) {
                navController.navigate("locations")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = TranslationUtils.translate("Current Hora", lang), 
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = state.horaSymbol + " " + state.hora, 
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = TranslationUtils.translate("Remaining", lang) + ": " + state.remaining, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SummaryCard(state, lang)

            Spacer(modifier = Modifier.height(16.dp))

            val menuItems = listOf(
                MenuItem(TranslationUtils.translate("Panchanga", lang), Icons.Default.CalendarMonth, "panchanga_detail"),
                MenuItem(TranslationUtils.translate("Hora", lang), Icons.Default.Schedule, "hora_detail"),
                MenuItem(TranslationUtils.translate("Solar & Celestial", lang), Icons.Default.WbSunny, "solar_celestial"),
                MenuItem(TranslationUtils.translate("Muhurta", lang), Icons.Default.Timer, "muhurta"),
                MenuItem(TranslationUtils.translate("Transit Kundali", lang), Icons.Default.Map, "transit_kundali"),
                MenuItem(TranslationUtils.translate("Birth Kundali", lang), Icons.Default.Person, "birth_kundali")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(menuItems.size) { index ->
                    MenuCard(menuItems[index]) {
                        navController.navigate(menuItems[index].route)
                    }
                }
            }
        }
    }

    LaunchedEffect(location, locationName, locationMode) {
        if (locationMode == "gps") {
            viewModel.refresh(context, location.first, location.second, null)
        } else {
            viewModel.refresh(context, null, null, locationName)
        }
    }
}

@Composable
fun LocationCard(name: String?, mode: String, lang: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (mode == "gps") TranslationUtils.translate("Current Location", lang) else name ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (mode == "gps") {
                    Text("GPS Tracking", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun MenuCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SummaryCard(state: PanchangaState, lang: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(
                    TranslationUtils.translate("Tithi", lang), 
                    state.tithi
                )
                InfoItem(
                    TranslationUtils.translate("Vara", lang), 
                    state.vara
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(TranslationUtils.translate("Sunrise", lang), state.sunrise)
                InfoItem(TranslationUtils.translate("Sunset", lang), state.sunset)
            }
        }
    }
}

@Composable
fun ColumnScope.InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RowScope.InfoItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
