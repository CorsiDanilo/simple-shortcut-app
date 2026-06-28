# Simple Shortcut

A simple Android app that lets you create home screen shortcuts. Each shortcut, when tapped from the launcher, shows a dialog with a scrollable grid of app icons - tap one to launch it.

## Features

- 📌 **Create and pin** custom shortcuts to the home screen
- 🎨 **Choose** from 40 predefined Material icons
- 📱 **Select multiple apps** per shortcut
- ✏️ **Edit shortcuts** at any time (updates automatically without re-pinning)
- 🗑️ **Delete or re-pin** shortcuts from the list
- 🌍 **Italian and English localization** (per-app language support)
- 🔄 **In-app update checker**

## Installation

Download the latest APK from [Releases](https://github.com/CorsiDanilo/simple-shortcut-app/releases/latest) and install it on your Android device (Android 8.0+).

## Requirements

- Android 8.0 (API 26) or higher
- Launcher that supports pinned shortcuts (most modern launchers)

## Build

To build the app locally, run the following Gradle command:

```bash
./gradlew assembleDebug
```

## Architecture

The app is built using modern Android development practices:
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Data Storage**: Room Database
- **Dependency**: Navigation Component for Compose

## License

MIT
