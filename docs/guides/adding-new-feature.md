# Adding a New Feature

This guide walks through adding a new feature module to TrackMate, following established patterns.

## Overview

Each feature in TrackMate lives in its own module under `feature/`. This guide demonstrates adding a hypothetical "Settings" feature.

## Step 1: Create the Module

### 1.1 Create Directory Structure

```
feature/
└── settings/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   └── java/com/octopus/edu/feature/settings/
        │       ├── SettingsScreen.kt
        │       ├── SettingsViewModel.kt
        │       └── SettingsUiContract.kt
        └── test/
            └── java/com/octopus/edu/feature/settings/
                └── SettingsViewModelTest.kt
```

### 1.2 Create build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.feature.settings"
    compileSdk = rootProject.extra["compileSdkVersion"].toString().toInt()

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"].toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = rootProject.extra["sourceCompatibility"] as JavaVersion
        targetCompatibility = rootProject.extra["targetCompatibility"] as JavaVersion
    }

    kotlinOptions {
        jvmTarget = rootProject.extra["kotlinOptionsJVMTarget"].toString()
    }
}

dependencies {
    // Core modules
    implementation(project(":core:ui-common"))
    implementation(project(":core:domain"))
    implementation(project(":core:design"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

### 1.3 Create AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

### 1.4 Register in settings.gradle.kts

Add the module to the project:

```kotlin
include(":feature:settings")
```

## Step 2: Define the UI Contract

Create `SettingsUiContract.kt`:

```kotlin
package com.octopus.edu.feature.settings

import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState

object SettingsUiContract {

    data class UiState(
        val isLoading: Boolean = false,
        val isDarkMode: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val syncFrequency: SyncFrequency = SyncFrequency.AUTOMATIC,
        val userEmail: String? = null,
        val appVersion: String = ""
    ) : ViewState

    sealed interface Effect : ViewEffect {
        object NavigateToLogin : Effect
        data class ShowMessage(val message: String) : Effect
        object OpenPrivacyPolicy : Effect
    }

    sealed interface Event : ViewEvent {
        data class OnDarkModeToggled(val enabled: Boolean) : Event
        data class OnNotificationsToggled(val enabled: Boolean) : Event
        data class OnSyncFrequencySelected(val frequency: SyncFrequency) : Event
        object OnLogoutClicked : Event
        object OnPrivacyPolicyClicked : Event
        object OnClearCacheClicked : Event
    }

    enum class SyncFrequency {
        AUTOMATIC,
        HOURLY,
        DAILY,
        MANUAL
    }
}
```

### Key Points

- **UiState**: All data the UI needs to render
- **Effect**: One-shot events (navigation, toasts)
- **Event**: User interactions

## Step 3: Implement the ViewModel

Create `SettingsViewModel.kt`:

```kotlin
package com.octopus.edu.feature.settings

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.domain.repository.AuthRepository
import com.octopus.edu.core.domain.repository.SettingsRepository
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.settings.SettingsUiContract.Effect
import com.octopus.edu.feature.settings.SettingsUiContract.Event
import com.octopus.edu.feature.settings.SettingsUiContract.SyncFrequency
import com.octopus.edu.feature.settings.SettingsUiContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<UiState, Effect, Event>() {

    override fun getInitialState() = UiState()

    init {
        loadSettings()
    }

    override fun processEvent(event: Event) {
        when (event) {
            is Event.OnDarkModeToggled -> updateDarkMode(event.enabled)
            is Event.OnNotificationsToggled -> updateNotifications(event.enabled)
            is Event.OnSyncFrequencySelected -> updateSyncFrequency(event.frequency)
            Event.OnLogoutClicked -> logout()
            Event.OnPrivacyPolicyClicked -> Effect.OpenPrivacyPolicy.send()
            Event.OnClearCacheClicked -> clearCache()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch(dispatcherProvider.io) {
            setState { copy(isLoading = true) }

            val settings = settingsRepository.getSettings()
            val user = authRepository.getCurrentUser()

            setState {
                copy(
                    isLoading = false,
                    isDarkMode = settings.isDarkMode,
                    notificationsEnabled = settings.notificationsEnabled,
                    syncFrequency = settings.syncFrequency.toUiModel(),
                    userEmail = user?.email,
                    appVersion = BuildConfig.VERSION_NAME
                )
            }
        }
    }

    private fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch(dispatcherProvider.io) {
            settingsRepository.setDarkMode(enabled)
            setState { copy(isDarkMode = enabled) }
        }
    }

    private fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch(dispatcherProvider.io) {
            settingsRepository.setNotificationsEnabled(enabled)
            setState { copy(notificationsEnabled = enabled) }
        }
    }

    private fun updateSyncFrequency(frequency: SyncFrequency) {
        viewModelScope.launch(dispatcherProvider.io) {
            settingsRepository.setSyncFrequency(frequency.toDomain())
            setState { copy(syncFrequency = frequency) }
        }
    }

    private fun logout() {
        viewModelScope.launch(dispatcherProvider.io) {
            authRepository.signOut()
            Effect.NavigateToLogin.send()
        }
    }

    private fun clearCache() {
        viewModelScope.launch(dispatcherProvider.io) {
            settingsRepository.clearCache()
            Effect.ShowMessage("Cache cleared").send()
        }
    }
}
```

## Step 4: Create the UI

Create `SettingsScreen.kt`:

```kotlin
package com.octopus.edu.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.feature.settings.SettingsUiContract.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.NavigateToLogin -> {
                    onNavigateToLogin()
                    viewModel.markEffectAsConsumed()
                }
                is Effect.ShowMessage -> {
                    // Show snackbar (handled by scaffold)
                    viewModel.markEffectAsConsumed()
                }
                Effect.OpenPrivacyPolicy -> {
                    onOpenUrl("https://example.com/privacy")
                    viewModel.markEffectAsConsumed()
                }
                null -> {}
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        onEvent = viewModel::processEvent
    )
}

@Composable
private fun SettingsContent(
    uiState: UiState,
    onEvent: (Event) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Appearance Section
        item {
            SectionHeader("Appearance")
        }

        item {
            SettingsSwitch(
                title = "Dark Mode",
                checked = uiState.isDarkMode,
                onCheckedChange = { onEvent(Event.OnDarkModeToggled(it)) }
            )
        }

        // Notifications Section
        item {
            SectionHeader("Notifications")
        }

        item {
            SettingsSwitch(
                title = "Enable Notifications",
                checked = uiState.notificationsEnabled,
                onCheckedChange = { onEvent(Event.OnNotificationsToggled(it)) }
            )
        }

        // Sync Section
        item {
            SectionHeader("Sync")
        }

        item {
            SyncFrequencySelector(
                selected = uiState.syncFrequency,
                onSelected = { onEvent(Event.OnSyncFrequencySelected(it)) }
            )
        }

        // Account Section
        item {
            SectionHeader("Account")
        }

        item {
            uiState.userEmail?.let { email ->
                SettingsItem(title = "Logged in as", subtitle = email)
            }
        }

        item {
            SettingsButton(
                title = "Log Out",
                onClick = { onEvent(Event.OnLogoutClicked) }
            )
        }

        // About Section
        item {
            SectionHeader("About")
        }

        item {
            SettingsItem(title = "Version", subtitle = uiState.appVersion)
        }

        item {
            SettingsButton(
                title = "Privacy Policy",
                onClick = { onEvent(Event.OnPrivacyPolicyClicked) }
            )
        }

        item {
            SettingsButton(
                title = "Clear Cache",
                onClick = { onEvent(Event.OnClearCacheClicked) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsItem(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsButton(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = title)
    }
}

@Composable
private fun SyncFrequencySelector(
    selected: SyncFrequency,
    onSelected: (SyncFrequency) -> Unit
) {
    // Implementation with dropdown or radio buttons
}
```

## Step 5: Add Navigation

### 5.1 Update App Navigation

In `app/src/main/java/.../navigation/NavGraph.kt`:

```kotlin
composable(route = "settings") {
    SettingsScreen(
        onNavigateToLogin = {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        },
        onOpenUrl = { url ->
            // Open URL in browser
        }
    )
}
```

### 5.2 Add Navigation Action

From wherever settings is accessed (e.g., home screen):

```kotlin
IconButton(onClick = { navController.navigate("settings") }) {
    Icon(Icons.Default.Settings, contentDescription = "Settings")
}
```

## Step 6: Write Tests

Create `SettingsViewModelTest.kt`:

```kotlin
package com.octopus.edu.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.octopus.edu.core.testing.MainDispatcherRule
import com.octopus.edu.core.testing.TestDispatcherProvider
import com.octopus.edu.feature.settings.SettingsUiContract.*
import io.mockk.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    private fun createViewModel() = SettingsViewModel(
        settingsRepository = settingsRepository,
        authRepository = authRepository,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `OnDarkModeToggled updates state and saves setting`() = runTest {
        viewModel = createViewModel()

        viewModel.processEvent(Event.OnDarkModeToggled(true))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isDarkMode).isTrue()
        coVerify { settingsRepository.setDarkMode(true) }
    }

    @Test
    fun `OnLogoutClicked signs out and emits NavigateToLogin`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.processEvent(Event.OnLogoutClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(Effect.NavigateToLogin)
        }

        coVerify { authRepository.signOut() }
    }

    @Test
    fun `OnClearCacheClicked clears cache and shows message`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.processEvent(Event.OnClearCacheClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(Effect.ShowMessage::class.java)
        }

        coVerify { settingsRepository.clearCache() }
    }
}
```

## Step 7: Add to App Module (if needed)

If the feature needs app-level dependencies, add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":feature:settings"))
}
```

## Checklist

Before submitting your feature:

- [ ] Module registered in `settings.gradle.kts`
- [ ] UI Contract defined (UiState, Effect, Event)
- [ ] ViewModel extends `BaseViewModel`
- [ ] Screen handles effects with `LaunchedEffect`
- [ ] Navigation integrated
- [ ] Unit tests for ViewModel
- [ ] UI tests for critical flows
- [ ] KtLint passes (`./gradlew ktlintCheck`)
- [ ] Build passes (`./gradlew assembleDebug`)
- [ ] Tests pass (`./gradlew test`)

## Related Documentation

- [ADR-002: MVI Pattern](../decisions/002-mvi-pattern.md)
- [BaseViewModel Pattern](../patterns/base-viewmodel-pattern.md)
- [Testing Guide](testing-guide.md)
