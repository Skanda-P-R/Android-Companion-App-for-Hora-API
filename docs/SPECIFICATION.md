# Original Android Companion App Specification

## Objective

Build a native Android application (Kotlin + Jetpack Compose) that consumes the existing Flask REST API. Do not move any astrology logic to Android.

The visual baseline is the existing iPhone Scriptable widgets for Hora/Panchanga and Kundali. Match their layout, spacing, typography and information density as closely as possible.

## Stack

-   Kotlin
-   Jetpack Compose
-   Jetpack Glance (Widgets)
-   Retrofit + OkHttp
-   Coil
-   WorkManager
-   DataStore Preferences
-   Material 3

## Backend

Use only the existing endpoints: 
- `GET /api/v1/all`
- `GET /api/v1/kundali`
- `GET /api/v1/kundali/chart`

Base API URL is: `https://dannyboiii.pythonanywhere.com/`

## Core Screens

1.  **Home**: Current Hora, remaining time, and quick summary.
2.  **Panchanga**: Detailed limbs (Mirroring the iPhone medium widget).
3.  **Kundali**: Display backend PNG with pinch zoom and pan support.
4.  **Settings**: Language, API URL, and Location settings.

## Widgets
- Medium Hora widget matching the iPhone design. 
- Small Kundali widget displaying backend PNG.

## Offline
Cache the last JSON and PNG. Show last successful update.

## Networking
Use Retrofit. Respect backend Cache-Control headers.
