package com.hora.companion

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hora.companion.api.AuthService
import com.hora.companion.api.HoraApiService
import com.hora.companion.data.AuthRepository
import com.hora.companion.repository.HoraRepository
import com.hora.companion.security.DeviceUuidProvider
import com.hora.companion.ui.login.LoginViewModel
import com.hora.companion.ui.screens.*
import com.hora.companion.workers.HoraUpdateWorker
import com.hora.companion.ui.theme.HoraTheme
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleBackgroundUpdates()
        val dataStoreManager = DataStoreManager(this)
        setContent {
            val currentTheme by dataStoreManager.themeFlow.collectAsState(initial = "blue")
            val currentThemeMode by dataStoreManager.themeModeFlow.collectAsState(initial = "system")
            HoraTheme(themeName = currentTheme, themeMode = currentThemeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(this)
                }
            }
        }
    }

    private fun scheduleBackgroundUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<HoraUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "HoraUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun AppNavigation(activity: MainActivity) {
    val navController = rememberNavController()
    val dataStoreManager = remember { DataStoreManager(activity) }
    val authRepository = remember { AuthRepository(activity) }
    val uuidProvider = remember { DeviceUuidProvider(activity) }
    
    val apiBase by dataStoreManager.apiBaseFlow.collectAsState(initial = BuildConfig.BASE_URL)

    val logging = remember { 
        okhttp3.logging.HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            } else {
                okhttp3.logging.HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    val commonClient = remember {
        okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    val authService = remember(apiBase) { AuthService.create(baseUrl = apiBase, client = commonClient) }
    val apiService = remember(apiBase) { 
        HoraApiService.create(
            authRepository = authRepository,
            onSessionExpired = {
                activity.lifecycleScope.launch {
                    authRepository.clearSessionToken()
                    authRepository.notifySessionExpired()
                }
            },
            baseUrl = apiBase
        ) 
    }
    val horaRepository = remember(apiService) { HoraRepository(apiService, activity) }
    
    val factory = remember(horaRepository, authService) { 
        ViewModelFactory(activity, authRepository, authService, horaRepository) 
    }
    
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val transitViewModel: TransitViewModel = viewModel(factory = factory)
    val birthViewModel: BirthViewModel = viewModel(factory = factory)

    val locationState by dataStoreManager.locationFlow.collectAsState(initial = null)
    val locationName by dataStoreManager.locationNameFlow.collectAsState(initial = null)
    val locationMode by dataStoreManager.locationModeFlow.collectAsState(initial = "gps")
    val langState by dataStoreManager.langFlow.collectAsState(initial = "en")
    val chartStyleState by dataStoreManager.chartStyleFlow.collectAsState(initial = "south")
    val sessionToken by authRepository.sessionToken.collectAsState(initial = null)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            activity.fetchLocation(dataStoreManager)
        }
    }

    LaunchedEffect(locationMode) {
        if (locationMode == "gps") {
            val hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            
            if (!hasFine && !hasCoarse) {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            } else {
                activity.fetchLocation(dataStoreManager)
            }
        }
    }

    LaunchedEffect(Unit) {
        authRepository.sessionExpiredEvent.collect {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Determine start destination
    val startDest = if (sessionToken.isNullOrEmpty()) "login" else "home"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                uuidProvider = uuidProvider,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                location = locationState ?: (12.9716 to 77.5946),
                locationName = locationName,
                locationMode = locationMode,
                lang = langState
            )
        }
        composable("panchanga_detail") {
            PanchangaDetailScreen(
                navController = navController,
                repo = horaRepository,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                lang = langState
            )
        }
        composable("hora_detail") {
            HoraDetailScreen(
                navController = navController,
                repo = horaRepository,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                lang = langState
            )
        }
        composable("solar_celestial") {
            SolarCelestialScreen(
                navController = navController,
                repo = horaRepository,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                lang = langState
            )
        }
        composable("muhurta") {
            MuhurtaScreen(
                navController = navController,
                repo = horaRepository,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                lang = langState
            )
        }
        composable("transit_kundali") {
            val dashaLevel by dataStoreManager.dashaLevelFlow.collectAsState(initial = 3)
            TransitKundaliScreen(
                navController = navController,
                viewModel = transitViewModel,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                apiBase = apiBase,
                sessionToken = sessionToken,
                lang = langState,
                dashaLevel = dashaLevel,
                chartStyle = chartStyleState
            )
        }
        composable("birth_kundali") {
            val dashaLevel by dataStoreManager.dashaLevelFlow.collectAsState(initial = 3)
            val savePath by dataStoreManager.savePathFlow.collectAsState(initial = null)
            BirthKundaliScreen(
                navController = navController,
                viewModel = birthViewModel,
                location = locationState,
                locationName = if (locationMode == "manual") locationName else null,
                apiBase = apiBase,
                sessionToken = sessionToken,
                lang = langState,
                dashaLevel = dashaLevel,
                savePath = savePath,
                chartStyle = chartStyleState
            )
        }
        composable("locations") {
            LocationsScreen(
                navController = navController,
                repo = horaRepository,
                dataStoreManager = dataStoreManager,
                lang = langState
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                dataStoreManager = dataStoreManager,
                authRepository = authRepository,
                repo = horaRepository
            )
        }
    }
}

@SuppressLint("MissingPermission")
fun MainActivity.fetchLocation(dataStoreManager: DataStoreManager) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { location ->
            location?.let {
                lifecycleScope.launch {
                    dataStoreManager.saveLocation(it.latitude, it.longitude)
                }
            }
        }
}
