package com.hora.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.companion.utils.TranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    location: Pair<Double, Double>,
    lang: String = "en"
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Hora Companion", lang)) },
                actions = {
                    IconButton(onClick = { viewModel.refresh(context, location.first, location.second) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

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
                        text = TranslationUtils.translate(state.hora, lang, "planet"), 
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = TranslationUtils.translate("Remaining", lang) + ": " + state.remaining, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = TranslationUtils.translate("Updated", lang) + ": " + state.lastUpdated, 
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SummaryCard(state, lang)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("panchanga") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(TranslationUtils.translate("Full Panchanga", lang))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("kundali") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(TranslationUtils.translate("View Kundali", lang))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(TranslationUtils.translate("Settings", lang))
            }
        }
    }

    LaunchedEffect(location) {
        viewModel.refresh(context, location.first, location.second)
    }
}

@Composable
fun SummaryCard(state: PanchangaState, lang: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(
                    TranslationUtils.translate("Tithi", lang), 
                    TranslationUtils.translate(state.tithi, lang, "tithi")
                )
                InfoItem(
                    TranslationUtils.translate("Vara", lang), 
                    TranslationUtils.translate(state.vara, lang, "vara")
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
