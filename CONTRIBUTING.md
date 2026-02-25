# Contributing to TrackMate

This guide covers the development workflow, coding standards, and contribution process.

## Development Setup

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17 (bundled with Android Studio recommended)
- Android SDK 36
- Firebase CLI (optional, for emulator)

### Initial Setup

1. **Clone and open**
   ```bash
   git clone https://github.com/your-org/trackmate.git
   ```
   Open the project in Android Studio.

2. **Firebase configuration**

   For production builds, place your `google-services.json` in the `app/` directory.

   For local development, you can use the Firebase emulator:
   ```bash
   firebase emulators:start --only auth,firestore
   ```

3. **Verify build**
   ```bash
   ./gradlew assembleDebug
   ./gradlew test
   ```

### IDE Configuration

Android Studio should automatically pick up project settings. Verify:

- **Kotlin version**: 2.2.21
- **Java version**: 17
- **Gradle JDK**: 17

## Code Style

### KtLint

The project uses [KtLint](https://pinterest.github.io/ktlint/) for code formatting. It runs automatically on all modules.

**Check formatting:**
```bash
./gradlew ktlintCheck
```

**Auto-format:**
```bash
./gradlew ktlintFormat
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `EntryRepository` |
| Functions | camelCase | `getEntriesVisibleOn()` |
| Constants | SCREAMING_SNAKE | `DB_SYNC_CONCURRENCY` |
| Packages | lowercase | `com.octopus.edu.core.domain` |
| Files | PascalCase (match class) | `EntryRepository.kt` |

### Architecture Conventions

1. **ViewModels** extend `BaseViewModel<UiState, Effect, Event>`
2. **Repository interfaces** live in `:core:domain`
3. **Repository implementations** live in `:core:data:*`
4. **Domain models** are immutable data classes
5. **UI state** is a single immutable data class per screen

### Import Organization

Imports are organized automatically by KtLint:

1. Standard library
2. Third-party libraries
3. Project imports

Wildcard imports are prohibited.

## Branching Strategy

```
main (production)
  │
  └── develop (integration)
        │
        ├── feature/feature-name
        ├── bugfix/bug-description
        └── refactor/refactor-description
```

### Branch Types

| Prefix | Purpose | Base | Merges To |
|--------|---------|------|-----------|
| `feature/` | New functionality | `develop` | `develop` |
| `bugfix/` | Bug fixes | `develop` | `develop` |
| `refactor/` | Code improvements | `develop` | `develop` |
| `hotfix/` | Production fixes | `main` | `main` and `develop` |

### Workflow

1. Create branch from `develop`
2. Make changes with atomic commits
3. Open PR to `develop`
4. After review and CI pass, squash merge
5. `develop` periodically merges to `main` for releases

## Commit Messages

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for semantic versioning.

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description | Version Bump |
|------|-------------|--------------|
| `feat` | New feature | Minor |
| `fix` | Bug fix | Patch |
| `docs` | Documentation only | None |
| `style` | Formatting, no code change | None |
| `refactor` | Code change, no feature/fix | None |
| `perf` | Performance improvement | Patch |
| `test` | Adding tests | None |
| `chore` | Build, CI, tooling | None |

### Scopes

Common scopes match module names:

- `app`, `domain`, `data-entry`, `database`, `network`
- `ui-common`, `design`, `common`
- `home`, `history`, `analytics`, `signIn`

### Examples

```bash
# Feature
feat(home): add swipe-to-complete gesture for tasks

# Bug fix
fix(data-entry): resolve sync conflict when offline edit precedes server update

# Refactor
refactor(ui-common): extract common state handling to BaseViewModel

# Documentation
docs: add ADR for error classification strategy
```

### Breaking Changes

Add `BREAKING CHANGE:` footer or `!` after type:

```
feat(domain)!: rename Entry.dueDate to Entry.targetDate

BREAKING CHANGE: Entry.dueDate has been renamed to Entry.targetDate.
Update all usages accordingly.
```

## Pull Request Process

### Before Opening a PR

1. **Run checks locally**
   ```bash
   ./gradlew ktlintCheck test
   ```

2. **Rebase on latest develop**
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

3. **Write meaningful commits**
   Follow conventional commit format.

### PR Template

The PR template (`.github/PULL_REQUEST_TEMPLATE.md`) includes:

- Description of changes
- Type of change (feature, fix, etc.)
- Testing performed
- Checklist items

### Review Process

1. Request review from at least one team member
2. Address feedback with new commits (don't force push during review)
3. Once approved, squash merge to `develop`

### CI Checks

All PRs must pass:

- [ ] KtLint formatting check
- [ ] Unit tests (`./gradlew test`)
- [ ] Build succeeds (`./gradlew assembleDebug`)

## Testing Requirements

### Test Coverage

All new code should include tests:

| Component | Test Type | Location |
|-----------|-----------|----------|
| Domain models | Unit | `core/domain/src/test/` |
| ViewModels | Unit | `feature/*/src/test/` |
| Repositories | Unit | `core/data/*/src/test/` |
| Database | Instrumented | `core/data/database/src/androidTest/` |

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific module
./gradlew :core:domain:test

# With coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Utilities

Use test utilities from `:core:testing`:

```kotlin
// Test dispatchers
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

// Mock data
val testTask = Task.mock("1")
```

See [Testing Guide](docs/guides/testing-guide.md) for detailed patterns.

## Adding New Features

For guidance on adding new features, see:

- [Adding a New Feature](docs/guides/adding-new-feature.md)
- [ADR-002: MVI Pattern](docs/decisions/002-mvi-pattern.md)
- [BaseViewModel Pattern](docs/patterns/base-viewmodel-pattern.md)

## Questions?

If you have questions about contributing:

1. Check existing documentation in `docs/`
2. Look for similar patterns in the codebase
3. Open a discussion issue for architectural questions
