package com.hora.jnana.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hora.jnana.DataStoreManager
import kotlinx.coroutines.launch

@Composable
fun LocationPermissionScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    lang: String,
    onPermissionGranted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            scope.launch {
                dataStoreManager.savePrivacyAccepted(true)
                onPermissionGranted()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (lang == "kn") "ಸ್ಥಳದ ಅನುಮತಿ ಅಗತ್ಯವಿದೆ" else "Location Permission Required",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (lang == "kn") 
                    "ನಿಮ್ಮ ಪ್ರಸ್ತುತ ಸ್ಥಳಕ್ಕೆ ಅನುಗುಣವಾಗಿ ನಿಖರವಾದ ಪಂಚಾಂಗ ಮತ್ತು ಹೋರಾ ಸಮಯವನ್ನು ಲೆಕ್ಕಹಾಕಲು ನಮಗೆ ನಿಮ್ಮ ಸ್ಥಳದ ಅಗತ್ಯವಿದೆ.\n\nನಿಮ್ಮ ಸ್ಥಳವನ್ನು ಸರ್ವರ್‌ಗೆ ಕಳುಹಿಸಲಾಗುವುದಿಲ್ಲ ಮತ್ತು ಅಪ್ಲಿಕೇಶನ್‌ನಲ್ಲಿ ಮಾತ್ರ ಬಳಸಲಾಗುತ್ತದೆ."
                    else "To calculate accurate Panchanga and Hora timings for your exact location, we need access to your device's GPS.\n\nYour location data remains on your device and is never sent to our servers.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    launcher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (lang == "kn") "ಒಪ್ಪಿಕೊಳ್ಳಿ ಮತ್ತು ಅನುಮತಿ ನೀಡಿ" else "Accept & Grant Permission")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { navController.navigate("privacy_policy") }) {
                Text(if (lang == "kn") "ಗೌಪ್ಯತಾ ನೀತಿಯನ್ನು ಓದಿ" else "Read Privacy Policy")
            }
        }
    }
}
