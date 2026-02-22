# ADR-001: Modular Clean Architecture

## Status

Accepted

## Context

TrackMate is an R&D project demonstrating modern Android development practices. We needed an architecture that:

- Enables parallel development across teams
- Supports unit testing without Android dependencies
- Allows features to be developed and tested in isolation
- Provides clear boundaries for code ownership
- Scales as the application grows

## Decision

We adopted a **modular Clean Architecture** with the following structure:

### Module Categories

```
TrackMate/
├── app/                      # Application shell
├── core/
│   ├── domain/              # Business logic (pure Kotlin)
│   ├── data/
│   │   ├── data-entry/      # Entry repository implementation
│   │   ├── data-auth/       # Auth repository implementation
│   │   └── database/        # Room database
│   ├── network/             # Network clients
│   ├── common/              # Shared utilities
│   ├── ui-common/           # BaseViewModel, shared UI
│   ├── design/              # Design system
│   └── testing/             # Test utilities
└── feature/
    ├── home/                # Home screen
    ├── history/             # History screen
    ├── analytics/           # Analytics screen
    └── signIn/              # Sign-in flow
```

### Dependency Rules

1. **Feature modules** depend only on `core:ui-common` and `core:domain`
2. **Core UI** depends on `core:domain` and `core:design`
3. **Data modules** implement interfaces from `core:domain`
4. **Domain module** has no Android dependencies
5. **Common module** provides utilities, has no dependencies

```
feature:* → core:ui-common → core:domain ← core:data:*
                   ↓
             core:design
```

### Module Responsibilities

| Module | Responsibility |
|--------|----------------|
| `:app` | Hilt root, navigation, sync orchestration |
| `:core:domain` | Entities, repository interfaces, domain services |
| `:core:data:data-entry` | `EntryRepository` implementation |
| `:core:data:database` | Room `@Database`, DAOs, entities |
| `:core:network` | Retrofit/Firebase clients |
| `:core:ui-common` | `BaseViewModel`, shared composables |
| `:feature:*` | Screen-specific UI and ViewModels |

## Consequences

### Positive

- **Testability**: Domain module is pure Kotlin, enabling fast unit tests
- **Build times**: Incremental builds only recompile affected modules
- **Encapsulation**: Implementation details hidden behind module boundaries
- **Reusability**: Core modules can be shared across projects
- **Team scaling**: Teams can own specific modules

### Negative

- **Initial complexity**: More modules to set up and maintain
- **Navigation complexity**: Cross-feature navigation requires coordination
- **Dependency management**: Need to carefully manage module dependencies

### Neutral

- **Learning curve**: Developers must understand module boundaries
- **Refactoring**: Moving code between modules requires careful consideration

## Implementation Notes

### Gradle Configuration

Each module uses convention plugins for consistent configuration:

```kotlin
// core/domain/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.octopus.edu.core.domain"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

### Dependency Inversion

Repository interfaces live in `:core:domain`:

```kotlin
// core/domain/.../repository/EntryRepository.kt
interface EntryRepository {
    val pendingEntries: Flow<List<Entry>>
    suspend fun saveEntry(entry: Entry): ResultOperation<Unit>
    suspend fun getEntryById(id: String): ResultOperation<Entry>
}
```

Implementations live in `:core:data:data-entry`:

```kotlin
// core/data/data-entry/.../EntryRepositoryImpl.kt
internal class EntryRepositoryImpl @Inject constructor(
    private val entryStore: EntryStore,
    private val entryApi: EntryApi,
    // ...
) : EntryRepository {
    // Implementation
}
```

Hilt binds interface to implementation:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class EntryRepositoryModule {
    @Binds
    abstract fun bindEntryRepository(impl: EntryRepositoryImpl): EntryRepository
}
```

## Related Decisions

- [ADR-002: MVI Pattern](002-mvi-pattern.md)
- [ADR-003: Offline-First Sync Strategy](003-offline-first-sync-strategy.md)

## References

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Guide to Android app modularization](https://developer.android.com/topic/modularization)
