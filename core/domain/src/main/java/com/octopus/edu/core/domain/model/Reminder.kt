package com.octopus.edu.core.domain.model

import java.time.Duration

sealed class Reminder {
    abstract val offset: Duration

    data object None : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object OnTime : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object FiveMinutesEarly : Reminder() {
        override val offset: Duration = Duration.ofMinutes(5)
    }

    data object ThirtyMinutesEarly : Reminder() {
        override val offset: Duration = Duration.ofMinutes(30)
    }

    data object OneHourEarly : Reminder() {
        override val offset: Duration = Duration.ofHours(1)
    }

    data object OnDay : Reminder() {
        override val offset: Duration = Duration.ZERO
    }

    data object DayEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(1)
    }

    data object TwoDaysEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(2)
    }

    data object ThreeDaysEarly : Reminder() {
        override val offset: Duration = Duration.ofDays(3)
    }
}
