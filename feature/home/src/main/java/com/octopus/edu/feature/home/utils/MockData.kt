package com.octopus.edu.feature.home.utils

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock

internal fun mockEntryList(count: Int): List<Entry> =
    (1..count).map {
        if (it % 2 == 0) {
            Task.mock(it.toString())
        } else {
            Habit.mock(it.toString())
        }
    }
