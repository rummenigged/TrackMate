package com.octopus.edu.core.common

import java.time.Clock
import java.time.Instant

class SystemAppClock(
    private val delegate: Clock = Clock.systemUTC()
) : AppClock {
    override fun nowInstant(): Instant = Instant.now(delegate)
}
