package com.octopus.edu.core

import com.octopus.edu.core.common.SystemAppClock
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SystemAppClockTest {
    @Test
    fun `nowInstant with default clock returns current time`() {
        // Given
        val appClock = SystemAppClock()
        val before = Instant.now()

        // When
        val result = appClock.nowInstant()
        val after = Instant.now()

        // Then
        assertNotNull(result)
        assertTrue(result >= before)
        assertTrue(result <= after)
    }

    @Test
    fun `nowInstant uses the provided fixed delegate clock`() {
        // Given
        val fixedInstant = Instant.parse("2023-11-20T10:15:30.00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        val appClock = SystemAppClock(delegate = fixedClock)

        // When
        val result = appClock.nowInstant()

        // Then
        assertEquals(fixedInstant, result)
    }

    @Test
    fun `nowEpocMillis uses the provided fixed delegate clock`() {
        // Given
        val fixedInstant = Instant.parse("2023-11-20T10:15:30.123Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        val appClock = SystemAppClock(delegate = fixedClock)

        // When
        val result = appClock.nowEpocMillis()

        // Then
        assertEquals(fixedInstant.toEpochMilli(), result)
    }

    @Test
    fun `nowLocalDate uses the provided fixed delegate clock`() {
        // Given
        val fixedInstant = Instant.parse("2023-11-20T10:15:30.00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        val appClock = SystemAppClock(delegate = fixedClock)
        val expectedDate = fixedInstant.atZone(ZoneId.systemDefault()).toLocalDate()

        // When
        val result = appClock.nowLocalDate()

        // Then
        assertEquals(expectedDate, result)
    }

    @Test
    fun `nowLocalTime uses the provided fixed delegate clock`() {
        // Given
        val fixedInstant = Instant.parse("2023-11-20T10:15:30.00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        val appClock = SystemAppClock(delegate = fixedClock)
        val expectedTime = fixedInstant.atZone(ZoneId.systemDefault()).toLocalTime()

        // When
        val result = appClock.nowLocalTime()

        // Then
        assertEquals(expectedTime, result)
    }
}
