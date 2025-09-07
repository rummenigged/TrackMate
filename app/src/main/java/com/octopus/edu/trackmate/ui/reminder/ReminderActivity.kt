package com.octopus.edu.trackmate.ui.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateOvalButton
import com.octopus.edu.core.design.theme.primaryAndSecondaryGradient
import com.octopus.edu.core.design.theme.utils.LaunchedEffectAndCollectLatest
import com.octopus.edu.trackmate.R
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEffect
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEffect.Dismiss
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEffect.ShowError
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiEvent
import com.octopus.edu.trackmate.ui.reminder.ReminderActivityUiContract.UiState
import com.octopus.edu.trackmate.ui.reminder.model.OffsetState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@AndroidEntryPoint
class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider {
                TrackMateTheme {
                    ReminderScreen(
                        onDismissAction = { finish() },
                    )
                }
            }
        }
    }

    @Composable
    private fun ReminderScreen(
        onDismissAction: () -> Unit,
        modifier: Modifier = Modifier,
        reminderViewModel: ReminderViewModel = hiltViewModel()
    ) {
        val uiState by reminderViewModel.uiState.collectAsStateWithLifecycle()

        ReminderContent(
            modifier = modifier,
            uiState = uiState,
            onEvent = reminderViewModel::processEvent,
        )

        EffectHandle(
            effectFlow = reminderViewModel.effect,
            onEvent = reminderViewModel::processEvent,
            onDismissAction = onDismissAction,
        )
    }

    @Composable
    private fun EffectHandle(
        effectFlow: Flow<UiEffect?>,
        onEvent: (UiEvent) -> Unit,
        onDismissAction: () -> Unit,
    ) {
        LaunchedEffectAndCollectLatest(
            effectFlow,
            onEffectConsumed = { onEvent(UiEvent.MarkEffectAsConsumed) },
        ) { effect ->
            when (effect) {
                Dismiss, ShowError -> {
                    onDismissAction()
                }
            }
        }
    }

    @Composable
    private fun ReminderContent(
        uiState: UiState,
        onEvent: (UiEvent) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryAndSecondaryGradient())
                    .padding(horizontal = 24.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ) {
                uiState.entry?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                uiState.formattedDate?.resolveStringResArgs()?.let { res ->
                    Text(
                        text = stringResource(res.resId, *res.formatArgs.toTypedArray()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                uiState.offset?.let { offset ->
                    val offsetText =
                        when (offset) {
                            is OffsetState.NoOffset -> stringResource(R.string.no_offset)
                            else ->
                                pluralStringResource(
                                    offset.resolveStringResArgs().resId,
                                    (uiState.offset?.offset ?: 0L).toInt(),
                                    *offset.resolveStringResArgs().formatArgs.toTypedArray(),
                                )
                        }
                    Text(
                        text = offsetText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }

                TrackMateOvalButton(
                    text = stringResource(R.string.dismiss),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                    onClick = { onEvent(UiEvent.OnDismiss) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                )
            }
        }
    }

    @PreviewLightDark
    @Composable
    private fun ReminderScreenPreview() {
        TrackMateTheme {
            ReminderContent(
                UiState(
                    entry = "Entry Reminder Test",
                    date = LocalDateTime.now(),
                ),
                onEvent = {},
            )
        }
    }
}
