package com.octopus.edu.trackmate.ui.reminder

import com.octopus.edu.core.common.isToday
import com.octopus.edu.core.common.isTomorrow
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState
import com.octopus.edu.trackmate.ui.reminder.model.DateState
import com.octopus.edu.trackmate.ui.reminder.model.OffsetState
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

internal object ReminderActivityUiContract {
    data class UiState(
        val entry: String? = null,
        val date: LocalDateTime? = null,
        val reminder: Reminder? = null
    ) : ViewState {
        val formattedDate: DateState?
            get() = resolveFormattedDate()

        val offset: OffsetState?
            get() = resolveOffsetAsStringRes()

        private fun resolveFormattedDate(): DateState? =
            date?.let { date ->
                when {
                    Duration.between(LocalDateTime.now(), date).toDays() <= 7 -> {
                        when {
                            date.toLocalDate()?.isToday() == true ->
                                DateState.Today(
                                    String.format(Locale.getDefault(), "%02d", date.hour),
                                    String.format(Locale.getDefault(), "%02d", date.minute),
                                )

                            date.toLocalDate()?.isTomorrow() == true ->
                                DateState.Tomorrow(
                                    String.format(Locale.getDefault(), "%02d", date.hour),
                                    String.format(Locale.getDefault(), "%02d", date.minute),
                                )

                            else ->
                                DateState.NextWeekDay(
                                    String.format(Locale.getDefault(), "%02d", date.hour),
                                    String.format(Locale.getDefault(), "%02d", date.minute),
                                    date.dayOfWeek
                                        ?.getDisplayName(
                                            TextStyle.FULL_STANDALONE,
                                            Locale.getDefault(),
                                        ).orEmpty(),
                                )
                        }
                    }

                    else ->
                        DateState.NextDate(
                            String.format(Locale.getDefault(), "%02d", date.hour),
                            String.format(Locale.getDefault(), "%02d", date.minute),
                            date.dayOfMonth.toString(),
                            date.month
                                ?.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.getDefault(),
                                ).orEmpty(),
                        )
                }
            }

        private fun resolveOffsetAsStringRes(): OffsetState? =
            date?.let {
                val offsetInMinutes = Duration.between(LocalDateTime.now(), it).toMinutes()

                if (offsetInMinutes < 0) return@let null // No negative offsets

                when (offsetInMinutes) {
                    0L -> OffsetState.NoOffset
                    in 1L..58L -> OffsetState.MinuteOffset(offsetInMinutes)
                    else -> {
                        val baseHour = offsetInMinutes / 60L
                        val remainderMinutes = offsetInMinutes % 60L

                        when (remainderMinutes) {
                            0L -> OffsetState.HourOffset(baseHour) // Exactly X hours
                            1L -> OffsetState.HourOffset(baseHour) // X hours and 1 minute
                            59L -> OffsetState.HourOffset(baseHour + 1) // 1 minute to next hour
                            else -> null
                        }
                    }
                }
            }
    }

    sealed interface UiEvent : ViewEvent {
        data object OnDismiss : UiEvent

        data object MarkEffectAsConsumed : UiEvent
    }

    sealed interface UiEffect : ViewEffect {
        data object Dismiss : UiEffect

        data object ShowError : UiEffect
    }
}
