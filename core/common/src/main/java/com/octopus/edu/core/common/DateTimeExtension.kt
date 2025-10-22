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

/**
 * Converts this epoch millisecond value to an Instant.
 *
 * @return An Instant representing the same point on the timeline as this epoch millisecond value.
 */
fun Long.toInstant(): Instant = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toInstant()

/**
 * Converts the receiver date at the start of its day (system default time zone) to epoch milliseconds.
 *
 * @return The number of milliseconds since 1970-01-01T00:00:00Z representing this date at start of day in the system default zone.
 */
fun LocalDate.toEpocMilliseconds(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
         * Converts this LocalTime to epoch milliseconds using 1970-01-01 as the reference date.
         *
         * The resulting value corresponds to the instant of this time on 1970-01-01 in the system default time zone.
         *
         * @return Milliseconds since the Unix epoch for this LocalTime on 1970-01-01 in the system default time zone.
         */
        fun LocalTime.toEpochMilli(): Long =
    this
        .atDate(LocalDate.of(1970, 1, 1)) // reference epoch date
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()