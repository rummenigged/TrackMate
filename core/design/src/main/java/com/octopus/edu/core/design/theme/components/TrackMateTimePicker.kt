package com.octopus.edu.core.design.theme.components

import android.icu.util.Calendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.octopus.edu.core.design.theme.TrackMateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMateTimePicker(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    currentTime: Calendar = Calendar.getInstance(),
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = true,
        )

    TimePickerDialog(
        onDismiss = onDismiss,
        onConfirm = { onTimeSelected(timePickerState.hour, timePickerState.minute) },
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() },
    )
}

@PreviewLightDark
@Composable
private fun TrackMateTimePickerPreview() {
    TrackMateTheme {
        TrackMateTimePicker(
            onDismiss = {},
            onTimeSelected = { hour, minute -> },
        )
    }
}
