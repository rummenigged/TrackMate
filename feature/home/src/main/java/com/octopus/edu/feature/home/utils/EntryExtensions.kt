package com.octopus.edu.feature.home.utils

import java.time.LocalDate

fun LocalDate.isToday(): Boolean = this == LocalDate.now()

fun LocalDate.isTomorrow(): Boolean = this == LocalDate.now().plusDays(1)

fun LocalDate.isYesterday(): Boolean = this == LocalDate.now().minusDays(1)
