package com.octopus.edu.core.design.theme.components.snackBar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TrackMateErrorSnackBar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    TrackMateSnackBar(
        modifier = modifier,
        data = data,
        containerColor = colorScheme.errorContainer,
        textColor = colorScheme.onErrorContainer,
        actionTextColor = colorScheme.onErrorContainer,
    )
}

@Composable
fun DefaultSnackBar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    TrackMateSnackBar(
        modifier = modifier,
        data = data,
        containerColor = colorScheme.inverseSurface,
        textColor = colorScheme.inverseOnSurface,
        actionTextColor = colorScheme.inversePrimary,
    )
}

@Composable
private fun TrackMateSnackBar(
    data: SnackbarData,
    containerColor: Color,
    textColor: Color,
    actionTextColor: Color,
    modifier: Modifier = Modifier,
) {
    val durationMillis =
        when (data.visuals.duration) {
            SnackbarDuration.Indefinite -> Long.MAX_VALUE
            SnackbarDuration.Long -> 9500L
            SnackbarDuration.Short -> 3500L
        }

    var progress by remember { mutableFloatStateOf(0F) }

    LaunchedEffect(data) {
        if (durationMillis != Long.MAX_VALUE) {
            val frameDuration = 16L
            val steps = (durationMillis / frameDuration).toInt()
            repeat(steps) {
                progress = it / steps.toFloat()
                delay(frameDuration)
            }
            progress = 1f
            data.dismiss()
        }
    }

    Snackbar(
        modifier = modifier.padding(bottom = 8.dp),
        shape = shapes.small,
        containerColor = containerColor,
        content = {
            Column {
                Text(
                    text = data.visuals.message,
                    color = textColor,
                    style = typography.titleSmall,
                )

                LinearProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    progress = { progress },
                    color = textColor,
                    trackColor = textColor.copy(alpha = 0.2f),
                )
            }
        },
        action = {
            if (data.visuals.actionLabel != null) {
                TextButton(
                    onClick = {
                        data.performAction()
                    },
                ) {
                    Text(
                        text = data.visuals.actionLabel.orEmpty(),
                        color = actionTextColor,
                        style = typography.titleSmall,
                    )
                }
            }
        },
    )
}

@Composable
fun TrackMateSnackBarHost(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        hostState = snackBarHostState,
        snackbar = { data ->
            when ((data.visuals as? TrackMateSnackBarVisuals)?.type ?: SnackBarType.DEFAULT) {
                SnackBarType.DEFAULT -> DefaultSnackBar(data)
                SnackBarType.ERROR -> TrackMateErrorSnackBar(data)
            }
        },
    )
}
