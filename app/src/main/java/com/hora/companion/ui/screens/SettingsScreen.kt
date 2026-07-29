package com.hora.companion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.hora.companion.DataStoreManager
import com.hora.companion.data.AuthRepository
import com.hora.companion.ui.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.documentfile.provider.DocumentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController, 
    dataStoreManager: DataStoreManager,
    authRepository: AuthRepository,
    repo: com.hora.companion.repository.HoraRepository
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoggingOut by remember { mutableStateOf(false) }

    val currentLang by dataStoreManager.langFlow.collectAsState(initial = "en")
    val currentTheme by dataStoreManager.themeFlow.collectAsState(initial = "blue")
    val currentThemeMode by dataStoreManager.themeModeFlow.collectAsState(initial = "system")
    val currentDashaLevel by dataStoreManager.dashaLevelFlow.collectAsState(initial = 3)
    val currentSavePath by dataStoreManager.savePathFlow.collectAsState(initial = null)
    val currentChartStyle by dataStoreManager.chartStyleFlow.collectAsState(initial = "south")
    val context = LocalContext.current
    
    val userEmail by authRepository.userEmail.collectAsState(initial = null)
    val userName by authRepository.userName.collectAsState(initial = null)
    val userPicture by authRepository.userPicture.collectAsState(initial = null)

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Take persistable permission
            val contentResolver = context.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it, takeFlags)
            
            scope.launch {
                dataStoreManager.saveSavePath(it.toString())
            }
        }
    }

    val displayPath = remember(currentSavePath) {
        if (currentSavePath.isNullOrEmpty()) {
            if (currentLang == "kn") "ಡೀಫಾಲ್ಟ್: Documents/Kundalis" else "Default: Documents/Kundalis"
        } else {
            val uri = Uri.parse(currentSavePath)
            DocumentFile.fromTreeUri(context, uri)?.name ?: currentSavePath
        }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userPicture != null) {
                        AsyncImage(
                            model = userPicture,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName?.take(1)?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userEmail ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                if (currentLang == "kn") "ಭಾಷೆ" else "Language", 
                style = MaterialTheme.typography.titleMedium
            )
            val options = listOf("en" to "English", "kn" to "ಕನ್ನಡ")
            Column(Modifier.selectableGroup()) {
                options.forEach { (code, label) ->
                    LanguageOption(
                        label = label,
                        selected = (code == currentLang),
                        onClick = { scope.launch { dataStoreManager.saveLang(code) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "ಥೀಮ್ ಮೋಡ್" else "Theme Mode",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val modeOptions = listOf(
                    "light" to if (currentLang == "kn") "ಲೈಟ್" else "Light",
                    "dark" to if (currentLang == "kn") "ಡಾರ್ಕ್" else "Dark",
                    "system" to if (currentLang == "kn") "ಸಿಸ್ಟಂ" else "System"
                )
                modeOptions.forEach { (mode, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            scope.launch { dataStoreManager.saveThemeMode(mode) }
                        }
                    ) {
                        ThemeModeCircle(
                            mode = mode,
                            isSelected = (mode == currentThemeMode)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "ಅಪ್ಲಿಕೇಶನ್ ಬಣ್ಣ" else "App Colour Palette",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(theme.mainColor)
                            .border(
                                width = if (currentTheme == theme.colorName) 3.dp else 1.dp,
                                color = if (currentTheme == theme.colorName) Color.Red else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    dataStoreManager.saveTheme(theme.colorName)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "ದಶಾ ಮಟ್ಟಗಳು" else "Dasha Levels",
                style = MaterialTheme.typography.titleMedium
            )
            Row(Modifier.selectableGroup()) {
                listOf(1, 2, 3).forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = (currentDashaLevel == level),
                                onClick = { scope.launch { dataStoreManager.saveDashaLevel(level) } },
                                role = Role.RadioButton
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(selected = (currentDashaLevel == level), onClick = null)
                        Text(text = level.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "ಕುಂಡಲಿ ಚಾರ್ಟ್ ಶೈಲಿ" else "Kundali Chart Style",
                style = MaterialTheme.typography.titleMedium
            )
            Row(Modifier.selectableGroup()) {
                val chartOptions = listOf(
                    "north" to if (currentLang == "kn") "ಉತ್ತರ" else "North",
                    "south" to if (currentLang == "kn") "ದಕ್ಷಿಣ" else "South",
                    "east" to if (currentLang == "kn") "ಪೂರ್ವ" else "East"
                )
                chartOptions.forEach { (style, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = (currentChartStyle == style),
                                onClick = { scope.launch { dataStoreManager.saveChartStyle(style) } },
                                role = Role.RadioButton
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(selected = (currentChartStyle == style), onClick = null)
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                if (currentLang == "kn") "ಸ್ಥಳ" else "Location", 
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = { navController.navigate("locations") }) {
                Text(if (currentLang == "kn") "ಸ್ಥಳವನ್ನು ಬದಲಾಯಿಸಿ" else "Change Location")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (currentLang == "kn") "ಕುಂಡಲಿ ಉಳಿಸುವ ಸ್ಥಳ" else "Kundali Save Location",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = displayPath ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { folderPickerLauncher.launch(null) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentLang == "kn") "ಫೋಲ್ಡರ್ ಆಯ್ಕೆಮಾಡಿ" else "Select Save Folder")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(if (currentLang == "kn") "ನಮ್ಮ ಬಗ್ಗೆ" else "About", style = MaterialTheme.typography.titleMedium)
            Text("Hora Companion v0.6.1", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLoggingOut) return@Button
                    isLoggingOut = true
                    scope.launch {
                        val result = repo.logout()
                        if (result.isSuccess) {
                            authRepository.clearSessionToken()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            isLoggingOut = false
                            snackbarHostState.showSnackbar(
                                if (currentLang == "kn") "ಲಾಗ್ ಔಟ್ ವಿಫಲವಾಗಿದೆ. ದಯವಿಟ್ಟು ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ." 
                                else "Logout failed. Please try again."
                            )
                        }
                    }
                },
                enabled = !isLoggingOut,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (currentLang == "kn") "ಲಾಗ್ ಔಟ್" else "Logout")
                }
            }
        }
    }
}

@Composable
fun ThemeModeCircle(
    mode: String,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.Red else Color.Gray,
                shape = CircleShape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                "light" -> {
                    drawCircle(color = Color.White)
                    drawCircle(color = Color.LightGray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                }
                "dark" -> {
                    drawCircle(color = Color.Black)
                }
                "system" -> {
                    drawArc(
                        color = Color.White,
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true
                    )
                    drawArc(
                        color = Color.Black,
                        startAngle = 270f,
                        sweepAngle = 180f,
                        useCenter = true
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
