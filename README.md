# Hora Companion Android (Beta v0.3.0)

A native Android application providing real-time astrological (Panchanga) and Hora data by consuming a dedicated Flask REST API. Designed with high-density Home Screen widgets and full Kannada language support.

## Key Features

- **Modern Grid Navigation**: A clean, 2-column square matrix on the Home screen for quick access to all astrological tools.
- **Real-time Hora**: Tracks the current astrological hour with remaining time, upcoming Hora planet, and historical/future lookups.
- **Full Panchanga Detail**: Comprehensive view of Samvatsara, Ayana, Rutu, Masa, Paksha, Tithi, Nakshatra, Yoga, Karana, and Vara with exact end times.
- **Solar & Celestial Insights**: Human-readable data for Sunrise, Sunset, Solar Noon, durations, and Sun/Moon Rasi positions.
- **Muhurta Timings**: Dedicated view for Rahu Kalam, Gulika, Yamaganda, and Abhijit Muhurta for any selected date.
- **Birth & Transit Kundali**: View real-time transit charts or generate Janma Kundali (Birth Charts) with custom name, date, and time inputs.
- **Advanced Location Registry**: Switch between automatic GPS tracking and a searchable manual location database with A-Z indexing and custom entry support.
- **High-Density Widgets**: Android Home Screen widgets built with Jetpack Glance, featuring a realistic selection picker and curved modern previews.
- **Multilingual Support**: Full English and Kannada support for all UI labels and backend-driven data values.
- **Security**: Secure, device-bound passwordless authentication tied to unique hardware identifiers.

## Security & Authentication

Introduced in v0.2.0, the app implements a robust security layer:
- **Device-Bound Identity**: Uses a stable hardware identifier (`ANDROID_ID`) so your login persists even after app reinstalls.
- **Passwordless Flow**: Authenticate using just your username; the app handles hardware verification automatically.
- **Bearer Token Auth**: All API communication is secured using JWT/Bearer tokens with automatic session invalidation.
- **Privacy Guard**: Network logging is strictly limited to Debug builds; production logs are completely silenced.

## Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Widgets**: Jetpack Glance
- **Networking**: Retrofit + OkHttp (with custom Interceptors) + Moshi (Kotlin Reflection)
- **Async**: Kotlin Coroutines & Flow
- **Security**: Android Keystore & Encrypted Storage
- **Persistence**: DataStore (Preferences) & Internal Storage
- **Location**: Google Play Services (Fused Location Provider)
- **Image Loading**: Coil

## Quick Start

1. **Clone the repository** and open the project in Android Studio (Ladybug or newer recommended).
2. **Sync Gradle** and ensure you have Android SDK 34 installed.
3. **Run the app** and sign in with your registered username.
4. **Grant Location Permissions** to allow the app to fetch local astrological data.

## Documentation

For more detailed information, please refer to the following documents:

- [**Session Security Plan**](docs/SESSION_SECURITY_PLAN.md): Details on the device-bound authentication and security architecture.
- [**Architecture Overview**](docs/ARCHITECTURE.md): Technical details on the app's structure and design patterns.
- [**Widget Guide**](docs/WIDGETS.md): Instructions on adding and configuring Home Screen widgets.
- [**Original Specification**](docs/SPECIFICATION.md): The initial project requirements and goals.

---
*Built for the Hora API ecosystem.*
