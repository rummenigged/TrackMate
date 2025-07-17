package com.octopus.edu.trackmate.di.mapKey

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.scheduler.ReminderType
import dagger.MapKey
import kotlin.reflect.KClass

data class ReminderStrategyKey(
    val entryClass: KClass<out Entry>,
    val reminderType: ReminderType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReminderStrategyKey) return false
        return entryClass == other.entryClass &&
            reminderType == other.reminderType
    }

    override fun hashCode(): Int {
        var result = entryClass.hashCode()
        result = 31 * result + reminderType.hashCode()
        return result
    }
}

@MapKey(unwrapValue = false)
annotation class ReminderStrategyMapKey(
    val entry: KClass<out Entry>,
    val type: ReminderType
)
