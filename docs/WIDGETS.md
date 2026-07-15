# Hora Companion Widgets

The app provides two high-density widgets for the Android Home Screen, built with Jetpack Glance. These widgets are designed to match the information density of the original iPhone Scriptable widgets.

## Available Widgets

### 1. Hora & Panchanga (Medium)
A detailed 2-column widget that provides a comprehensive view of the current astrological state.
- **Header**: Current Hora symbol/planet and the time remaining.
- **Sub-header**: Exact end time of the current Hora and the planet for the next Hora.
- **Body (Left)**: Tithi, Rahu Kalam, and Yamaganda.
- **Body (Right)**: Nakshatra, Abhijit, and Sunrise/Sunset.
- **Footer**: Zodiac signs (Rasi) for the Moon and Sun with icons.

### 2. Kundali Chart (Small)
A compact widget displaying the generated Kundali PNG for the current location.

## Configuration & Usage

### Location
Widgets automatically use the last location saved by the main application in `DataStore`. Ensure you have granted location permissions and opened the app at least once to populate this data.

### Language Support
The widgets automatically reflect the language choice (English or Kannada) set in the **Settings** screen of the app.

### Refresh Logic
1.  **Automatic**: The app schedules a background task via `WorkManager` that attempts to refresh data every 15 minutes.
2.  **Manual**: Clicking the **Refresh** button inside the app immediately triggers an update for all active home screen widgets.
3.  **Offline**: If the device is offline, widgets will display the last successfully cached data.

## How to Add
1. Long-press on your Android home screen.
2. Select **Widgets**.
3. Find **Hora Companion**.
4. Drag either the **HoraWidget** or **KundaliWidget** to your screen.
5. (Recommended) Resize the Hora widget to a 4x2 or larger grid for the best viewing experience.
