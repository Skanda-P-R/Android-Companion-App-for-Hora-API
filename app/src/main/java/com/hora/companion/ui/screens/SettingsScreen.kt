package com.hora.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.companion.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, dataStoreManager: DataStoreManager) {
    val scope = rememberCoroutineScope()
    var apiUrl by remember { mutableStateOf("https://dannyboiii.pythonanywhere.com/") }
    val currentLang by dataStoreManager.langFlow.collectAsState(initial = "en")

    LaunchedEffect(Unit) {
        apiUrl = dataStoreManager.getApiBase() ?: "https://dannyboiii.pythonanywhere.com/"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentLang == "kn") "ಸೇಟಿಂಗ್ಸ್" else "Settings") },
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
        ) {
            Text(
                if (currentLang == "kn") "ಭಾಷೆ" else "Language", 
                style = MaterialTheme.typography.titleMedium
            )
            val options = listOf("en" to "English", "kn" to "ಕನ್ನಡ")
            Column(Modifier.selectableGroup()) {
                options.forEach { (code, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (code == currentLang),
                                onClick = { scope.launch { dataStoreManager.saveLang(code) } },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == currentLang),
                            onClick = null // null recommended for accessibility with selectable modifier
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "API ಸಂರಚನೆ" else "API Configuration", 
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                label = { Text("Base API URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    dataStoreManager.saveApiBase(apiUrl)
                }
            }) {
                Text(if (currentLang == "kn") "API URL ಉಳಿಸಿ" else "Save API URL")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                if (currentLang == "kn") "ಸ್ಥಳ" else "Location", 
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (currentLang == "kn") "ಅಪ್ಲಿಕೇಶನ್ ಸ್ವಯಂಚಾಲಿತವಾಗಿ GPS ಸ್ಥಳವನ್ನು ಬಳಸುತ್ತದೆ." else "The app currently uses GPS location automatically.", 
                 style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(if (currentLang == "kn") "ನಮ್ಮ ಬಗ್ಗೆ" else "About", style = MaterialTheme.typography.titleMedium)
            Text("Hora Companion v0.1.1", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
