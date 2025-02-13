# The Weather App

The Weather App is a modern Android application built to provide weekly ( 7 days ) weather updates using the [OpenWeather API](https://openweathermap.org/api). It features a clean architecture, leveraging MVVM, a single activity, and various modern libraries and tools to ensure robust functionality, seamless UI, and testability.

---

## Features

- **Architecture**: MVVM with Use Cases, Repositories, and State/Event handling.
- **UI**: Built with Jetpack Compose for a modern and interactive user interface.
- **Data Storage**: Uses Room Database for offline persistence and DataStore for user preferences.
- **Asynchronous Handling**: Powered by Kotlin Coroutines for efficient threading.
- **Dependency Injection**: Utilizes Dagger Hilt for simplified DI.
- **Testing**: Includes 4 unit tests to ensure code reliability (located in the `test` folder).
- **Compatibility**: Thoroughly tested on the following devices:
  - Pixel 9 (API 30)
  - Pixel 6 Pro (API 35)
  - Pixel 8 (API VanilaIceCream)
  - Pixel 9 Pro (API 34)
  - Lenovo Z6 Pro
- **Supported Versions**: Android 9 (Pie, API 28) and above.

---

## Setup Instructions

### Clone or Download the Repository

1. **Clone the repository**  
   Open a terminal and run:
   ```bash
   git clone https://github.com/yourusername/yourrepository.git
   cd yourrepository

2. **Download the ZIP file**
   - Click the green `Code` button on the repository page.
   - Select `Download ZIP`.
   - Extract the downloaded ZIP file to your preferred location.
   - Open Android Studio.
   - Click on `File > Open`.
   - Navigate to the project folder and select it.
   - Allow Android Studio to sync the Gradle files.
   - Run the app on an emulator or connected device.

3. **Install Directly on a Device**
   - Use the provided debug bundle (APK).
   - Download the `.apk` file.
   - Transfer the APK to your Android device.
   - On your device, enable **Install unknown apps** for the file manager app (if not already enabled).
   - Open the APK and install the app.

---

## Debugging and Running Tests

To run the included unit tests:

1. Open the `test` folder in the project.
2. Right-click on any test file and select `Run <test_name>`.
3. View the test results in the Run window.

---

## Libraries Used

Below is the list of libraries integrated into the app:

- **Jetpack Components**: Lifecycle, ViewModel, Compose, DataStore, Room
- **Networking**: Retrofit, OkHttp
- **Dependency Injection**: Dagger Hilt
- **Testing**: JUnit, Mockk, Robolectric, Espresso
- **Coroutines**: Kotlin Coroutines for threading and concurrency
- **Accompanist**: For managing runtime permissions
- **Play Services Location**: To fetch user location data

---

## Supported Platforms

- **Minimum SDK**: 28 (Android 9, Pie)
- **Target SDK**: 35

---
