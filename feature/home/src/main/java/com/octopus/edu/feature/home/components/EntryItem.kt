package com.octopus.edu.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.EntryCard
import com.octopus.edu.core.design.theme.components.SwipActions
import com.octopus.edu.core.design.theme.components.SwipeActionsConfig
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.core.ui.common.extensions.rememberMaxTextWidthDp
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.getRecurrenceAsStringRes

@Composable
internal fun EntryItem(
    entry: Entry,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onItemSwipedFromStartToEnd: (Entry) -> Unit = {},
    onItemSwipedFromEndToStart: (Entry) -> Unit = {},
) = trace("EntryItem") {
    val allDayLabel = stringResource(R.string.all_day)
    val timeToDisplay =
        entry.time?.let { time ->
            stringResource(R.string.formatted_time_hh_mm, time.hour, time.minute)
        } ?: allDayLabel
    val timeMaxWidth =
        rememberMaxTextWidthDp(
            timeToDisplay,
            allDayLabel,
        )

    ConstraintLayout(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
    ) {
        val (time, icon, topLine, bottomLine, card) = createRefs()

        Text(
            text = timeToDisplay,
            style = typography.labelMedium,
            color = colorScheme.onSurface,
            modifier =
                Modifier
                    .width(timeMaxWidth)
                    .constrainAs(time) {
                        start.linkTo(parent.start)
                        top.linkTo(card.top)
                        bottom.linkTo(card.bottom)
                    },
        )

        if (!isFirstItem) {
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.2f))
                        .constrainAs(topLine) {
                            start.linkTo(icon.start)
                            top.linkTo(parent.top)
                            end.linkTo(icon.end)
                            bottom.linkTo(icon.top, margin = 2.dp)
                        },
            )
        }

        val entryIcon =
            remember(entry) {
                when (entry) {
                    is Habit -> R.drawable.ic_autorenew_habit_16
                    is Task -> R.drawable.ic_circle_task_16
                }
            }

        Icon(
            painter = painterResource(entryIcon),
            contentDescription = null,
            tint = colorScheme.primary,
            modifier =
                Modifier
                    .constrainAs(icon) {
                        start.linkTo(time.end, margin = 8.dp)
                        top.linkTo(time.top)
                        bottom.linkTo(time.bottom)
                    },
        )

        if (!isLastItem) {
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.2f))
                        .constrainAs(bottomLine) {
                            start.linkTo(icon.start)
                            top.linkTo(icon.bottom, margin = 2.dp)
                            end.linkTo(icon.end)
                            bottom.linkTo(parent.bottom)
                        },
            )
        }

        SwipActions(
            modifier =
                Modifier
                    .height(IntrinsicSize.Min)
                    .constrainAs(card) {
                        start.linkTo(icon.end, margin = 4.dp)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
            startActionsConfig =
                SwipeActionsConfig(
                    icon = Icons.Rounded.Check,
                    threshold = 0.4f,
                    disabledIconTint = colorScheme.onSurface,
                    swipeActionActivatedBackground = colorScheme.errorContainer,
                    swipeActionDeactivatedBackground = colorScheme.surfaceContainer,
                    backgroundShape = shapes.medium,
                    stayDismissed = true,
                    onSwiped = { onItemSwipedFromStartToEnd(entry) },
                ),
            endActionsConfig =
                SwipeActionsConfig(
                    icon = Icons.Rounded.Delete,
                    threshold = 0.4f,
                    disabledIconTint = colorScheme.onSurface,
                    swipeActionActivatedBackground = colorScheme.errorContainer,
                    swipeActionDeactivatedBackground = colorScheme.surfaceContainer,
                    backgroundShape = shapes.medium,
                    stayDismissed = true,
                    onSwiped = { onItemSwipedFromEndToStart(entry) },
                ),
        ) {
            EntryCard {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = entry.title,
                        style = typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (entry is Habit) {
                        entry.recurrence?.let { recurrence ->
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = stringResource(getRecurrenceAsStringRes(recurrence)),
                                style = typography.labelSmall,
                                color = colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TaskItemPreview() {
    TrackMateTheme {
        Column(modifier = Modifier.background(color = colorScheme.surface)) {
            EntryItem(Task.mock("2"))
        }
    }
}

@PreviewLightDark
@Composable
private fun HabitItemPreview() {
    TrackMateTheme {
        Column(modifier = Modifier.background(color = colorScheme.surface)) {
            EntryItem(Habit.mock("2"))
        }
    }
}
