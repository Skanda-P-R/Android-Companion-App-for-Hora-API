package com.hora.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.companion.utils.TranslationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchangaScreen(navController: NavController, state: PanchangaState, lang: String = "en") {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationUtils.translate("Panchanga", lang)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PanchangaSection(TranslationUtils.translate("Limbs", lang), listOf(
                TranslationUtils.translate("Tithi", lang) to state.tithi,
                TranslationUtils.translate("Nakshatra", lang) to state.nakshatra,
                TranslationUtils.translate("Yoga", lang) to state.yoga,
                TranslationUtils.translate("Karana", lang) to state.karana,
                TranslationUtils.translate("Vara", lang) to state.vara
            ))

            Spacer(modifier = Modifier.height(16.dp))

            PanchangaSection(TranslationUtils.translate("Calendar", lang), listOf(
                TranslationUtils.translate("Samvatsara", lang) to state.samvatsara,
                TranslationUtils.translate("Ayana", lang) to state.ayana,
                TranslationUtils.translate("Rutu", lang) to state.rutu,
                TranslationUtils.translate("Masa", lang) to state.masa,
                TranslationUtils.translate("Paksha", lang) to state.paksha
            ))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PanchangaSection(TranslationUtils.translate("Hora", lang), listOf(
                TranslationUtils.translate("Current Hora", lang) to state.hora,
                TranslationUtils.translate("Next Hora", lang) to state.horaNext,
                TranslationUtils.translate("Remaining", lang) to state.remaining,
                TranslationUtils.translate("Ends", lang) to state.horaEnds
            ))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PanchangaSection(TranslationUtils.translate("Solar & Celestial", lang), listOf(
                TranslationUtils.translate("Sunrise", lang) to state.sunrise,
                TranslationUtils.translate("Sunset", lang) to state.sunset,
                TranslationUtils.translate("Moon Rasi", lang) to state.moonRasi,
                TranslationUtils.translate("Sun Rasi", lang) to state.sunRasi
            ))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PanchangaSection(TranslationUtils.translate("Timings", lang), listOf(
                TranslationUtils.translate("Abhijit", lang) to state.abhijit,
                TranslationUtils.translate("Rahu Kalam", lang) to state.rahuKalam,
                TranslationUtils.translate("Yamaganda", lang) to state.yamaganda
            ))
        }
    }
}

@Composable
fun PanchangaSection(title: String, items: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                }
                if (items.last() != (label to value)) {
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
