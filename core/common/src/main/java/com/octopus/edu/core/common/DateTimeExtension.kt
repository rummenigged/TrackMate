package com.octopus.edu.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun LocalDate.isToday(): Boolean = this == LocalDate.now()

fun LocalDate.isTomorrow(): Boolean = this == LocalDate.now().plusDays(1)

fun LocalDate.isYesterday(): Boolean = this == LocalDate.now().minusDays(1)

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun Long.toLocalTime(): LocalTime = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime()

fun Long.toInstant(): Instant = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toInstant()

fun LocalDate.toEpocMilliseconds(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalTime.toEpochMilli(): Long =
    this
        .atDate(LocalDate.of(1970, 1, 1)) // reference epoch date
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
