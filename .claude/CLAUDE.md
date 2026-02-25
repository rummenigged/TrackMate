# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Model Selection

- **Sonnet**: Default for most coding tasks - implementing features, fixing bugs, writing tests, refactoring, code reviews
- **Opus**: Use for complex architectural decisions, multi-module design changes, evaluating trade-offs, and problems requiring deep reasoning across the codebase

## Build Commands

```bash
# Build
./gradlew assembleDebug           # Debug APK
./gradlew assembleRelease         # Release APK (requires keystore.properties)

# Testing
./gradlew test                    # All unit tests
./gradlew :core:domain:test       # Single module tests
./gradlew testDebugUnitTest jacocoTestReport  # Tests with coverage

# Code Quality
./gradlew ktlintCheck             # Check Kotlin formatting
./gradlew ktlintFormat            # Auto-format Kotlin code
./gradlew lintDebug               # Android Lint

# Pre-PR checks
./gradlew ktlintCheck test        # Run before opening PRs
```

## Architecture

See [ARCHITECTURE.md](../ARCHITECTURE.md) for an overview and [docs/architecture/ARCHITECTURE_REFERENCE.md](../docs/architecture/ARCHITECTURE_REFERENCE.md) for comprehensive details including:
- Module structure and dependencies
- MVI pattern with BaseViewModel
- Offline-first data flow
- Error classification and retry policies
- KMP migration roadmap

## ViewModel Patterns

All ViewModels extend `BaseViewModel<UiState, Effect, Event>` implementing MVI pattern:
- **UiState**: Single immutable state exposed via `StateFlow`
- **Effect**: One-shot side effects (navigation, toasts) - must call `markEffectAsConsumed()`
- **Event**: User actions processed through `processEvent()`

Key methods: `setState { copy(...) }`, `setEffect(effect)`, `Effect.send()`

See [docs/patterns/base-viewmodel-pattern.md](../docs/patterns/base-viewmodel-pattern.md) for implementation details and [docs/decisions/002-mvi-pattern.md](../docs/decisions/002-mvi-pattern.md) for design rationale.

## Code Conventions

- **Conventional Commits**: `feat(scope):`, `fix(scope):`, `refactor(scope):` - drives semantic versioning
- **KtLint**: Auto-applied to all modules, no wildcard imports
- **Repository pattern**: Interfaces in `:core:domain`, implementations in `:core:data:*`
- **Scopes**: Match module names (domain, data-entry, home, etc.)

## Testing

See [docs/guides/testing-guide.md](../docs/guides/testing-guide.md) for comprehensive testing patterns including ViewModel testing with Turbine, repository testing with fakes, and Konsist architecture tests.

Test utilities in `:core:testing`: `MainDispatcherRule`, `TestDispatcherProvider`, `Task.mock()`, `Habit.mock()`.

## Configuration

- **SDK**: compileSdk 36, minSdk 28, targetSdk 36
- **Java/Kotlin**: 17 / 2.2
- **Firebase**: Debug builds can use emulator (`firebase emulators:start --only auth,firestore`)
- **Sync concurrency**: Limited to 4 via `DB_SYNC_CONCURRENCY`

## Documentation

- ADRs in `docs/decisions/` (modular architecture, MVI, offline-first sync, error handling, reminder strategies)
- Patterns in `docs/patterns/` (BaseViewModel, error handling)
- Guides in `docs/guides/` (getting started, testing, adding features)