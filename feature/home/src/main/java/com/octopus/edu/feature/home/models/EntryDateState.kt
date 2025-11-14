package com.octopus.edu.feature.home.models

import androidx.annotation.StringRes
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.feature.home.R
import java.time.LocalTime

data class ResolvedEntryDate(
    @field:StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList()
)

sealed class EntryDateState(
    open val time: LocalTime?,
    open val recurrence: Recurrence?,
) {
    abstract fun resolveStringResArgs(): ResolvedEntryDate

    class Today(
        override val time: LocalTime? = null,
        override val recurrence: Recurrence? = null,
    ) : EntryDateState(time, recurrence) {
        override fun resolveStringResArgs(): ResolvedEntryDate =
            ResolvedEntryDate(
                resId = R.string.today,
            )
    }

    data class Tomorrow(
        override val time: LocalTime? = null,
        override val recurrence: Recurrence? = null,
    ) : EntryDateState(time, recurrence) {
        override fun resolveStringResArgs(): ResolvedEntryDate =
            ResolvedEntryDate(
                resId = R.string.tomorrow,
            )
    }

    data class Yesterday(
        override val time: LocalTime? = null,
        override val recurrence: Recurrence? = null,
    ) : EntryDateState(time, recurrence) {
        override fun resolveStringResArgs(): ResolvedEntryDate =
            ResolvedEntryDate(
                resId = R.string.yesterday,
            )
    }

    data class DateLaterToday(
        override val time: LocalTime? = null,
        override val recurrence: Recurrence? = null,
        val daysLeft: Int,
        val month: String,
        val day: String
    ) : EntryDateState(time, recurrence) {
        override fun resolveStringResArgs(): ResolvedEntryDate =
            ResolvedEntryDate(
                resId = R.string.days_later,
                formatArgs = listOf(month, day, daysLeft),
            )
    }

    data class DateBeforeToday(
        override val time: LocalTime? = null,
        override val recurrence: Recurrence? = null,
        val daysOverdue: Int,
        val month: String,
        val day: String
    ) : EntryDateState(time, recurrence) {
        override fun resolveStringResArgs(): ResolvedEntryDate =
            ResolvedEntryDate(
                resId = R.string.days_before,
                formatArgs = listOf(month, day, daysOverdue),
            )
    }
}
