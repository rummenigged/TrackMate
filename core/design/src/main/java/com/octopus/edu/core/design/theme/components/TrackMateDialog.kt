package com.octopus.edu.core.design.theme.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun TrackMateDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes confirmText: Int,
    @StringRes dismissText: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(confirmText))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(dismissText))
            }
        },
        title = {
            Text(text = stringResource(title), style = MaterialTheme.typography.headlineSmall)
        },
        text = content,
    )
}
