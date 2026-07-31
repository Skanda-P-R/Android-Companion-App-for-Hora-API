# HoraJnana Widgets

The app provides high-density widgets for the Android Home Screen, built with Jetpack Glance. These widgets provide critical astrological data at a glance, mirroring the depth of traditional Panchangas in a modern mobile format.

## Available Widgets

### 1. Hora & Panchanga (Medium/Large)
A sophisticated 3-column widget designed for maximum information density.
- **Header**: Current Hora symbol, planet name, and time remaining.
- **Sub-header**: Exact end time of the current Hora and the planet for the next interval.
- **Grid (3 Columns)**:
    - **Column 1**: Ayana, Rutu, and Masa.
    - **Column 2**: Tithi, Nakshatra, and Sunrise/Sunset.
    - **Column 3**: Rahu Kalam, Yamaganda, and Abhijit Muhurta.
- **Footer**: Zodiac signs (Rasi) for the Moon and Sun.

## Widget Selection Experience
The app features a customized **Widget Picker**:
- **Descriptive Labels**: "Hora & Panchanga".
- **Realistic Previews**: See actual screenshots of the widgets before adding them.
- **Modern Design**: Previews feature curved corners to match the Android 12+ aesthetic.

## Configuration & Usage

### Location Support
Widgets automatically use the primary location saved in the app:
- **GPS Mode**: Updates based on your current tracking.
- **Manual Mode**: Uses the specific saved location you've selected from the registry.

### Language Support
All widget content (labels and data) automatically reflects the language choice (English or Kannada) set in the app.

### Refresh Logic
1.  **Automatic**: Updated via `WorkManager` approximately every 15 minutes.
2.  **Manual**: Refreshing the app's dashboard forces an immediate widget update.
3.  **Offline**: Displays the last cached data with specific error messages if updates fail.

## How to Add
1. Long-press on your Android home screen.
2. Select **Widgets**.
3. Find **HoraJnana**.
4. Drag the **Hora** widget to your screen.
5. (Recommended) Resize the Hora widget to fill a 4-column width for the clearest alignment.
