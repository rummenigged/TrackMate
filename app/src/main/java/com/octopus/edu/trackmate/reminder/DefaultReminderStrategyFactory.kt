package com.octopus.edu.trackmate.reminder

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import com.octopus.edu.core.domain.scheduler.ReminderStrategyFactory
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.trackmate.di.mapKey.ReminderStrategyKey
import com.octopus.edu.trackmate.di.mapKey.ReminderStrategyMapKey
import javax.inject.Inject

class DefaultReminderStrategyFactory
    @Inject
    constructor(
        strategies: Map<@JvmSuppressWildcards ReminderStrategyMapKey, @JvmSuppressWildcards ReminderStrategy>
    ) : ReminderStrategyFactory {
        private val strategyMap =
            strategies.mapKeys {
                ReminderStrategyKey(it.key.entry, it.key.type)
            }

        override fun getStrategy(
            entry: Entry,
            type: ReminderType
        ): ReminderStrategy? = strategyMap[ReminderStrategyKey(entry::class, type)]
    }
