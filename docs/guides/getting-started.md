# Getting Started

This guide walks you through setting up TrackMate for local development.

## Prerequisites

Before you begin, ensure you have the following installed:

| Requirement | Version | Notes |
|-------------|---------|-------|
| Android Studio | Ladybug (2024.2.1)+ | Download from [developer.android.com](https://developer.android.com/studio) |
| JDK | 17 | Bundled with Android Studio recommended |
| Android SDK | 36 | Install via SDK Manager |
| Git | Any recent | For version control |
| Firebase CLI | Latest | Optional, for emulator |

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/trackmate.git
cd trackmate
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File > Open**
3. Navigate to the cloned `trackmate` directory
4. Click **OK**

Android Studio will:
- Detect the Gradle project
- Download dependencies
- Configure the project

This may take several minutes on first run.

### 3. Configure Firebase

TrackMate uses Firebase for authentication and cloud storage. You have two options:

#### Option A: Firebase Emulator (Recommended for Development)

No Firebase project needed. Use local emulators:

1. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Start emulators:
   ```bash
   firebase emulators:start --only auth,firestore
   ```

3. The app will automatically connect to emulators in debug builds.

#### Option B: Real Firebase Project

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable Authentication (Email/Password provider)
3. Enable Cloud Firestore
4. Download `google-services.json`
5. Place it in the `app/` directory:
   ```
   TrackMate/
   └── app/
       └── google-services.json  ← Place here
   ```

### 4. Verify Build

Run a debug build to verify everything is configured correctly:

```bash
./gradlew assembleDebug
```

Expected output:
```
BUILD SUCCESSFUL in Xm Xs
```

### 5. Run Tests

Verify the test suite passes:

```bash
./gradlew test
```

## Running the App

### On Emulator

1. Open **Device Manager** (Tools > Device Manager)
2. Create a virtual device:
   - Phone: Pixel 6 or similar
   - System Image: API 34 (Android 14)
3. Click **Play** to start the emulator
4. In Android Studio, click **Run** (green play button)

### On Physical Device

1. Enable **Developer Options** on your device
2. Enable **USB Debugging**
3. Connect via USB
4. Select your device from the dropdown
5. Click **Run**

## Project Structure Overview

```
TrackMate/
├── app/                    # Main application module
├── core/
│   ├── domain/            # Business models & interfaces
│   ├── data/
│   │   ├── data-entry/    # Entry repository
│   │   ├── data-auth/     # Auth repository
│   │   └── database/      # Room database
│   ├── network/           # API clients
│   ├── common/            # Utilities
│   ├── ui-common/         # BaseViewModel, shared UI
│   ├── design/            # Design system
│   └── testing/           # Test utilities
├── feature/
│   ├── home/              # Home screen
│   ├── history/           # History screen
│   ├── analytics/         # Analytics screen
│   └── signIn/            # Sign-in flow
└── docs/                  # Documentation
```

## Common Tasks

### Build Debug APK

```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK

```bash
./gradlew assembleRelease
```
Requires signing configuration in `keystore.properties`.

### Run All Tests

```bash
./gradlew test
```

### Run Specific Module Tests

```bash
./gradlew :core:domain:test
./gradlew :feature:home:test
```

### Check Code Style

```bash
./gradlew ktlintCheck
```

### Auto-format Code

```bash
./gradlew ktlintFormat
```

### Clean Build

```bash
./gradlew clean
```

## IDE Configuration

### Recommended Plugins

- **Kotlin** (bundled)
- **Android** (bundled)
- **KtLint** - Code style checking
- **GitToolBox** - Enhanced Git integration

### Code Style

The project uses KtLint. Import the code style:

1. **File > Settings > Editor > Code Style > Kotlin**
2. Click **Set from...** > **Predefined Style** > **Kotlin Style Guide**

### Live Templates

Add useful live templates for faster development:

| Abbreviation | Expands To |
|--------------|------------|
| `vms` | ViewModel state property |
| `vme` | ViewModel effect property |
| `usecase` | Use case class template |

## Troubleshooting

### Gradle Sync Failed

**Problem:** Gradle sync fails with dependency errors.

**Solution:**
1. **File > Invalidate Caches / Restart**
2. Delete `.gradle` folder in project root
3. Re-sync

### Firebase Configuration Error

**Problem:** `google-services.json` not found.

**Solution:**
- Ensure the file is in `app/` directory
- Or use Firebase emulators (no config needed)

### Build Fails with JDK Error

**Problem:** JDK version mismatch.

**Solution:**
1. **File > Project Structure > SDK Location**
2. Set **JDK location** to JDK 17 path
3. Or set in `gradle.properties`:
   ```properties
   org.gradle.java.home=/path/to/jdk17
   ```

### Tests Fail with Dispatcher Error

**Problem:** Tests fail with "Module with the Main dispatcher had failed to initialize".

**Solution:** Ensure tests use `TestDispatcherProvider`:
```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

### Emulator Network Issues

**Problem:** App can't connect to Firebase emulators.

**Solution:**
1. Verify emulators are running: `firebase emulators:start`
2. Check `10.0.2.2` is used (Android emulator's localhost)
3. Ensure ports aren't blocked: 9099 (Auth), 8080 (Firestore)

## Next Steps

Now that you're set up:

1. Read the [Architecture Overview](../../ARCHITECTURE.md)
2. Explore the [ADRs](../decisions/) for design decisions
3. Follow the [Adding a New Feature](adding-new-feature.md) guide
4. Review the [Testing Guide](testing-guide.md)

## Getting Help

- Check existing [documentation](../)
- Search closed issues on GitHub
- Open a new issue for bugs or questions
