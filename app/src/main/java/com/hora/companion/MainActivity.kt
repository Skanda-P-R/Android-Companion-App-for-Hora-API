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
import com.hora.companion.ui.screens.*
import com.hora.companion.workers.HoraUpdateWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleBackgroundUpdates()
        setContent {
            MaterialTheme {
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
    val factory = ViewModelFactory(activity)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val dataStoreManager = DataStoreManager(activity)
    val locationState by dataStoreManager.locationFlow.collectAsState(initial = null)
    val langState by dataStoreManager.langFlow.collectAsState(initial = "en")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            activity.fetchLocation(dataStoreManager)
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            activity.fetchLocation(dataStoreManager)
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                location = locationState ?: (12.9716 to 77.5946),
                lang = langState
            )
        }
        composable("panchanga") {
            PanchangaScreen(
                navController = navController,
                state = homeViewModel.state.collectAsState().value,
                lang = langState
            )
        }
        composable("kundali") {
            KundaliScreen(
                navController = navController,
                location = locationState ?: (12.9716 to 77.5946),
                lang = langState
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                dataStoreManager = dataStoreManager
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
