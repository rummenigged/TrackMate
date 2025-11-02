package com.octopus.edu.core.design.reminder.model

import com.octopus.edu.core.ui.common.StringResource
import com.octopus.edu.trackmate.R

internal sealed class DateState(
    open val hour: String,
    open val minute: String
) {
    abstract fun resolveStringResArgs(): StringResource

    data class Today(
        override val hour: String,
        override val minute: String
    ) : DateState(hour, minute) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                resId = R.string.todayReminder,
                formatArgs = listOf(hour, minute),
            )
    }

    data class Tomorrow(
        override val hour: String,
        override val minute: String
    ) : DateState(hour, minute) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                resId = R.string.tomorrowReminder,
                formatArgs = listOf(hour, minute),
            )
    }

    data class NextWeekDay(
        override val hour: String,
        override val minute: String,
        val weekDay: String
    ) : DateState(hour, minute) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                resId = R.string.nextWeekDay,
                formatArgs = listOf(weekDay, hour, minute),
            )
    }

    data class NextDate(
        override val hour: String,
        override val minute: String,
        val day: String,
        val month: String
    ) : DateState(hour, minute) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                resId = R.string.nextDate,
                formatArgs = listOf(day, month, hour, minute),
            )
    }
}
