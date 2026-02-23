# ADR-005: Strategy Pattern for Reminders

## Status

Accepted

## Context

TrackMate supports reminders for both Tasks and Habits, with two delivery mechanisms: notifications and alarms. This creates a matrix of behaviors:

|           | Notification | Alarm |
|-----------|--------------|-------|
| **Task**  | TaskNotificationReminderStrategy | TaskAlarmReminderStrategy |
| **Habit** | HabitNotificationReminderStrategy | HabitAlarmReminderStrategy |

We needed a way to:

- Select the correct reminder behavior based on entry type and reminder type
- Allow easy addition of new entry types or reminder mechanisms
- Inject the appropriate strategy via Hilt
- Keep reminder scheduling logic testable

## Decision

We implemented the **Strategy Pattern** using Hilt's `@IntoMap` multi-binding feature with a custom map key annotation.

### Domain Contracts

```kotlin
// core/domain/.../scheduler/ReminderStrategy.kt

interface ReminderStrategy {
    suspend fun schedule(entry: Entry)
    suspend fun cancel(entryId: String)
}

enum class ReminderType {
    NOTIFICATION,
    ALARM
}
```

### Custom Map Key

```kotlin
// app/.../di/mapKey/ReminderStrategyMapKey.kt

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ReminderStrategyMapKey(
    val entry: KClass<out Entry>,
    val type: ReminderType
)
```

### Strategy Implementations

```kotlin
// app/.../reminder/TaskNotificationReminderStrategy.kt

class TaskNotificationReminderStrategy(
    private val scheduler: ReminderScheduler
) : ReminderStrategy {

    override suspend fun schedule(entry: Entry) {
        require(entry is Task) { "Expected Task but got ${entry::class.simpleName}" }
        scheduler.scheduleNotification(
            entryId = entry.id,
            title = entry.title,
            triggerAt = entry.reminderTime()
        )
    }

    override suspend fun cancel(entryId: String) {
        scheduler.cancelNotification(entryId)
    }
}

// Similar implementations for:
// - TaskAlarmReminderStrategy
// - HabitNotificationReminderStrategy
// - HabitAlarmReminderStrategy
```

### Hilt Module

```kotlin
// app/.../di/ReminderStrategyModule.kt

@Module
@InstallIn(SingletonComponent::class)
object ReminderStrategyModule {

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Task::class, type = ReminderType.NOTIFICATION)
    fun provideTaskNotificationReminderStrategy(
        @Named("TaskNotificationReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = TaskNotificationReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Habit::class, type = ReminderType.NOTIFICATION)
    fun provideHabitNotificationReminderStrategy(
        @Named("HabitNotificationReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = HabitNotificationReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Task::class, type = ReminderType.ALARM)
    fun provideTaskAlarmReminderStrategy(
        @Named("TaskAlarmReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = TaskAlarmReminderStrategy(scheduler)

    @Provides
    @IntoMap
    @ReminderStrategyMapKey(entry = Habit::class, type = ReminderType.ALARM)
    fun provideHabitAlarmReminderStrategy(
        @Named("HabitAlarmReminderScheduler") scheduler: ReminderScheduler
    ): ReminderStrategy = HabitAlarmReminderStrategy(scheduler)
}
```

### Strategy Selection

```kotlin
// Injection point
class ReminderManager @Inject constructor(
    private val strategies: Map<ReminderStrategyMapKey, @JvmSuppressWildcards ReminderStrategy>
) {
    suspend fun scheduleReminder(entry: Entry) {
        val reminderType = entry.reminderType ?: return
        val key = ReminderStrategyMapKey(entry::class, reminderType)
        val strategy = strategies[key]
            ?: throw IllegalStateException("No strategy for ${entry::class.simpleName} + $reminderType")

        strategy.schedule(entry)
    }

    suspend fun cancelReminder(entry: Entry) {
        val reminderType = entry.reminderType ?: return
        val key = ReminderStrategyMapKey(entry::class, reminderType)
        strategies[key]?.cancel(entry.id)
    }
}
```

### Usage Example

```kotlin
// In ViewModel or UseCase
class SaveEntryUseCase @Inject constructor(
    private val entryRepository: EntryRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(entry: Entry) {
        entryRepository.saveEntry(entry)

        if (entry.reminder != null && entry.reminder != Reminder.None) {
            reminderManager.scheduleReminder(entry)
        }
    }
}
```

## Consequences

### Positive

- **Open/Closed Principle**: New entry types or reminder types require only new strategy + module binding
- **Single Responsibility**: Each strategy handles one specific combination
- **Type Safety**: Custom map key ensures compile-time correctness
- **Testability**: Strategies are easily mocked or faked
- **Hilt Integration**: Leverages Hilt's built-in multi-binding

### Negative

- **Boilerplate**: Each combination requires its own class and provider method
- **Runtime lookup**: Strategy selection happens at runtime via map lookup
- **Custom annotation**: Requires understanding Hilt's advanced features

### Neutral

- **Explicit bindings**: All combinations must be explicitly provided (no default fallback)

## Adding a New Strategy

To add support for a new entry type (e.g., `Milestone`):

1. Create domain model:
   ```kotlin
   data class Milestone(...) : Entry()
   ```

2. Create strategy implementations:
   ```kotlin
   class MilestoneNotificationReminderStrategy(...) : ReminderStrategy
   class MilestoneAlarmReminderStrategy(...) : ReminderStrategy
   ```

3. Add Hilt bindings:
   ```kotlin
   @Provides
   @IntoMap
   @ReminderStrategyMapKey(entry = Milestone::class, type = ReminderType.NOTIFICATION)
   fun provideMilestoneNotificationStrategy(...): ReminderStrategy = ...
   ```

## Testing

```kotlin
@Test
fun `ReminderManager schedules correct strategy for Task notification`() = runTest {
    val mockTaskNotificationStrategy = mockk<ReminderStrategy>(relaxed = true)
    val strategies = mapOf(
        ReminderStrategyMapKey(Task::class, NOTIFICATION) to mockTaskNotificationStrategy
    )
    val manager = ReminderManager(strategies)

    val task = Task.mock("1").copy(reminderType = NOTIFICATION)
    manager.scheduleReminder(task)

    coVerify { mockTaskNotificationStrategy.schedule(task) }
}

@Test
fun `ReminderManager throws when no strategy found`() = runTest {
    val manager = ReminderManager(emptyMap())
    val task = Task.mock("1").copy(reminderType = NOTIFICATION)

    assertThrows<IllegalStateException> {
        manager.scheduleReminder(task)
    }
}
```

## Related Decisions

- [ADR-001: Modular Clean Architecture](001-modular-clean-architecture.md)

## References

- [Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
- [Hilt Multi-bindings](https://dagger.dev/hilt/multibindings)
- [Custom Map Keys](https://dagger.dev/dev-guide/multibindings#custom-keys)
