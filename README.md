# Hora Companion Android

A native Android application providing real-time astrological (Panchanga) and Hora data by consuming a dedicated Flask REST API. Designed with high-density Home Screen widgets and full Kannada language support.

## Key Features

- **Real-time Hora**: Tracks the current astrological hour with remaining time and upcoming Hora planet.
- **Full Panchanga**: Detailed view of Tithi, Nakshatra, Yoga, Karana, and Vara.
- **Transit Kundali**: Zoomable and pannable view of the current astrological chart (PNG).
- **High-Density Widgets**: Android Home Screen widgets built with Jetpack Glance, mirroring the layout and information density of optimized iOS Scriptable widgets.
- **Multilingual**: Toggle between English and Kannada for all UI elements and dynamic astrological data.
- **Offline Reliability**: Caches the last successful server response; shows a timestamp and error message if the network is unavailable.
- **Background Sync**: Automatic data updates every 15 minutes via WorkManager.

## Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Widgets**: Jetpack Glance
- **Networking**: Retrofit + OkHttp + Moshi
- **Async**: Kotlin Coroutines & Flow
- **Persistence**: DataStore (Preferences) & Internal Storage (JSON/PNG Caching)
- **Location**: Google Play Services (Fused Location Provider)
- **Image Loading**: Coil

## Quick Start

1. **Clone the repository** and open the `android-app` folder in Android Studio (Iguana or newer recommended).
2. **Sync Gradle** and ensure you have Android SDK 34 installed.
3. **Run the app** on a physical device or emulator with Google Play Services.
4. **Grant Location Permissions** when prompted to allow the app to fetch local astrological data.

## Documentation

For more detailed information, please refer to the following documents:

- [**Architecture Overview**](docs/ARCHITECTURE.md): Technical details on the app's structure and design patterns.
- [**Widget Guide**](docs/WIDGETS.md): Instructions on adding and configuring Home Screen widgets.
- [**Original Specification**](docs/SPECIFICATION.md): The initial project requirements and goals.

---
*Built for the Hora API ecosystem.*
