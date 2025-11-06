package com.octopus.edu.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

interface AppClock {
    fun nowInstant(): Instant

    fun nowEpocMillis(): Long = nowInstant().toEpochMilli()

    fun nowLocalDate(): LocalDate = nowInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    fun nowLocalTime(): LocalTime = nowInstant().atZone(ZoneId.systemDefault()).toLocalTime()
}
