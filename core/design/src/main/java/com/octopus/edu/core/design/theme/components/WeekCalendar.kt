package com.octopus.edu.core.design.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val startDate = today.with(DayOfWeek.MONDAY).minusDays(1)
    val basePage = Int.MAX_VALUE / 2

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { pageIndex ->
        val weekStart =
            if (pageIndex < basePage) {
                startDate.minusWeeks((basePage - pageIndex).toLong())
            } else {
                startDate.plusWeeks((pageIndex - basePage).toLong())
            }

        val daysOfWeek = (0..6).map { weekStart.plusDays(it.toLong()) }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            daysOfWeek.forEach { day ->
                DayItem(
                    day = day,
                    isSelectedDay = day == selectedDate,
                    isToday = day == today,
                    onItemClicked = { selectedDay -> onDateSelected(selectedDay) },
                )
            }
        }
    }
}

@Composable
private fun DayItem(
    day: LocalDate,
    onItemClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isSelectedDay: Boolean = false,
    isToday: Boolean = false,
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        val textColor =
            when {
                isSelectedDay -> {
                    colorScheme.onPrimary
                }

                isToday -> {
                    colorScheme.primary
                }

                else -> {
                    colorScheme.onSurface
                }
            }

        Text(
            modifier =
                Modifier
                    .clip(shapes.small)
                    .background(
                        when {
                            isSelectedDay -> {
                                colorScheme.primary
                            }

                            isToday -> {
                                colorScheme.primary.copy(alpha = 0.1f)
                            }

                            else -> {
                                Color.Transparent
                            }
                        },
                    ).clickable { onItemClicked(day) }
                    .padding(8.dp),
            text = day.dayOfMonth.toString(),
            style = typography.headlineSmall,
            color = textColor,
        )
    }
}

@PreviewLightDark
@Composable
private fun WeekCalendarPreview() {
    TrackMateTheme {
        WeekCalendar(
            selectedDate = LocalDate.now().plusDays(1),
            pagerState = rememberPagerState(initialPage = 0, pageCount = { Int.MAX_VALUE }),
            onDateSelected = {},
        )
    }
}
