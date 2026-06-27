# Voyagers: Offline-First Travel Companion & Safe Tracking Shield

Voyagers is a modern, local-first Android application designed to safeguard travelers by tracking journey progress in real time and automatically notifying designated guardians via SMS at key checkpoints. Built using Jetpack Compose, Room Database, Hilt dependency injection, Google Maps SDK, and native Android services.

---

## Key Features

- **Local-First Architecture**: No cloud servers, external databases, or third-party APIs (except for Google Map tiles). All profile settings, emergency contacts, active trip states, and alert logs are stored safely on-device in a Room database.
-  **Foreground Service Location Tracking**: Runs continuous location tracking inside a background service (`TrackingService.kt`) with an active foreground notification, ensuring tracking continues even when the app is minimized or the screen is off.
-  **Interactive Google Maps View**: Displays live-updating markers for the start location, destination, and current position. Renders a polyline route connecting the points and automatically centers the camera as the traveler moves.
-  **Native Geocoding**: Utilizes the built-in Android `Geocoder` API to translate textual address inputs (e.g. "Los Angeles", "New York") into geographical coordinates asynchronously off the main thread.
-  **Milestone Proximity Engine**: Computes distance relative to destination using Android's native `Location.distanceBetween`. Dispatches native SMS alerts to contacts upon crossing Quarterway, Halfway, Three-Quarterway milestones, and final Destination Arrival (within 500m).
-  **Route Simulation Mode**: Built-in interpolation simulator that generates a 100-step path between start and finish points. Travelers can adjust simulation speed (1x up to 100x speed) in real time using a custom slider.
-  **SMS History Logger**: Tracks and displays sent notifications, complete with status tags (`SENT` vs. `SIMULATED`), message body containing Google Maps coordinate links, and relative dispatch timestamps.

---

## Build & Installation Requirements

- **Android Studio Jellyfish / Ladybug** or newer.
- **Java 17** SDK.
- **Android Gradle Plugin (AGP)** `9.2.1` / Kotlin `2.0.21` / KSP `2.2.10-2.0.2`.

### 1. Verification and Compilation

To verify the compiler and build integrity:

```powershell
./gradlew compileDebugKotlin
```

### 2. Running on Emulators/Devices

Install the debug build directly onto your device:

```powershell
./gradlew installDebug
```

---

## Permissions Declared

The application declares the following permissions in its `AndroidManifest.xml` to support full tracking operations:

- `android.permission.INTERNET`: Loading map tiles and resolving locations.
- `android.permission.ACCESS_FINE_LOCATION` & `android.permission.ACCESS_COARSE_LOCATION`: Active location tracking.
- `android.permission.ACCESS_BACKGROUND_LOCATION`: Periodic location updates in the background.
- `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_LOCATION`: Running location services as foreground tasks.
- `android.permission.SEND_SMS`: Dispatching alerts to emergency contacts.
- `android.permission.POST_NOTIFICATIONS`: Displaying tracking notifications on Android 13+.
