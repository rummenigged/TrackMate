package com.octopus.edu.trackmate.ui.reminder.model

import com.octopus.edu.core.ui.common.StringResource
import com.octopus.edu.trackmate.R

internal sealed class OffsetState(
    open val offset: Long
) {
    abstract fun resolveStringResArgs(): StringResource

    data object NoOffset : OffsetState(0) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                R.string.no_offset,
            )
    }

    data class HourOffset(
        override val offset: Long
    ) : OffsetState(offset) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                R.plurals.hourly_offset,
                listOf(offset.toString()),
            )
    }

    data class MinuteOffset(
        override val offset: Long
    ) : OffsetState(offset) {
        override fun resolveStringResArgs(): StringResource =
            StringResource(
                R.plurals.minutely_offset,
                listOf(offset.toString()),
            )
    }
}
