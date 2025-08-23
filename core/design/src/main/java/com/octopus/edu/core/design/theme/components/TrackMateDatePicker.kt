package com.octopus.edu.core.design.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.R
import com.octopus.edu.core.design.theme.TrackMateTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TrackMateDatePicker(
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = LocalDate.now(),
) {
    var currentMonth by remember {
        mutableStateOf(YearMonth.from(selectedDate))
    }
    val today = remember { LocalDate.now() }
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val daysInMonth = (1..lastDayOfMonth.dayOfMonth).map { currentMonth.atDay(it) }
    val leadingEmptyDays = (firstDayOfMonth.dayOfWeek.value % 7)
    val allDays = List(leadingEmptyDays) { null } + daysInMonth

    Column(modifier = modifier.background(color = colorScheme.surface)) {
        MonthPicker(
            currentMonth = currentMonth,
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach {
                Text(
                    text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }

        CalendarMonthGrid(
            days = allDays,
            selectedDate = selectedDate,
            today = today,
            onDateSelected = onDateSelected,
        )
    }
}

@Composable
private fun MonthPicker(
    currentMonth: YearMonth,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                currentMonth.month.name
                    .lowercase()
                    .replaceFirstChar { it.titlecase() },
            style = typography.headlineSmall,
            color = colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onPrevMonth() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.prev_month),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = { onNextMonth() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next_month),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    days: List<LocalDate?>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(300.dp).padding(top = 8.dp),
        userScrollEnabled = false,
    ) {
        items(days) { date ->
            Box(
                modifier =
                    Modifier
                        .aspectRatio(1f)
                        .clip(shape = shapes.medium)
                        .clickable(enabled = date != null) {
                            date?.let { onDateSelected(it) }
                        }.background(
                            when (date) {
                                selectedDate -> {
                                    colorScheme.primary
                                }

                                today -> {
                                    colorScheme.primary.copy(alpha = 0.1f)
                                }

                                else -> {
                                    Color.Transparent
                                }
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                val textColor =
                    when (date) {
                        selectedDate -> {
                            colorScheme.onPrimary
                        }

                        today -> {
                            colorScheme.primary
                        }

                        else -> {
                            colorScheme.onSurface
                        }
                    }

                val textStyle =
                    when (date) {
                        today, selectedDate -> {
                            typography.titleLarge
                        }

                        else -> {
                            typography.bodyLarge
                        }
                    }

                Text(
                    text = date?.dayOfMonth?.toString() ?: "",
                    color = textColor,
                    style = textStyle,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TrackMateDatePickerPreview() {
    TrackMateTheme {
        TrackMateDatePicker(onDateSelected = {})
    }
}
