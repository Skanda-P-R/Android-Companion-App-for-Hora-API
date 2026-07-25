# Android Companion App Specification (v0.5.0)

## Objective

Build a native Android application (Kotlin + Jetpack Compose) that consumes the Hora Flask REST API. The app aims to provide a high-density, user-friendly mobile experience for accessing Vedic astrological data.

## Stack

-   Kotlin
-   Jetpack Compose (Material 3)
-   Jetpack Glance (Home Screen Widgets)
-   Retrofit + OkHttp (REST Networking)
-   Coil (SVG Image Loading)
-   WorkManager (Background Updates)
-   DataStore Preferences (Persistence)

## Backend API Support

The app consumes the following endpoints:
- `GET /api/v1/all`: Aggregate response for dashboard summary.
- `GET /api/v1/panchanga`: Detailed limbs with transition times.
- `GET /api/v1/hora`: Real-time and historical planetary hour data.
- `GET /api/v1/muhurta`: Calculated intervals (Rahu, Gulika, etc.).
- `GET /api/v1/dasha`: Vimshottari Dasha timeline and Balance.
- `GET /api/v1/kundali/svg`: Real-time Transit Kundali Vector.
- `GET /api/v1/kundali/birth/svg`: Personalized Janma Kundali Vector.
- `GET /api/v1/locations`: Saved location registry.
- `POST /api/v1/locations`: Custom location addition.
- `DELETE /api/v1/locations/{name}`: Custom location removal.

## Core Screens

1.  **Home**: Modern grid navigation providing a Hora countdown and Panchanga summary.
2.  **Panchanga Detail**: Full view of all limbs and calendar details with date navigation.
3.  **Hora Detail**: Current hora info with precise date/time selector support.
4.  **Solar & Celestial**: Human-readable solar events and Sun/Moon rasi positions.
5.  **Muhurta**: Timings for auspicious and inauspicious intervals.
6.  **Transit Kundali**: Multi-tab interface (Info, Kundali, Dasha) with SVG visualization and timeline explorer.
7.  **Birth Kundali**: Natal chart generation with integrated Dasha details, SVG rendering, and a secure local save/load system with searchable birth place support.
8.  **Locations**: Searchable registry with A-Z scrolling and multi-select deletion.
9.  **Settings**: Application configuration (Language, API URL, Dasha depth, Session, and Kundali Save Path).

## Widgets
- **Hora & Panchanga (Medium)**: 3-column high-density view of the Vedic day.
- **Transit Kundali (Small)**: Dynamic visualization of current planetary positions.
- **Widget Picker**: Enhanced selection experience with descriptive labels and screenshots.
