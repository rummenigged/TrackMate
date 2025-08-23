package com.octopus.edu.core.domain.model

sealed class Recurrence {
    object Daily : Recurrence()

    object Weekly : Recurrence()

    object Custom : Recurrence()

    object None : Recurrence()
}
