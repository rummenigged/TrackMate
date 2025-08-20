package com.octopus.edu.core.design.theme.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme

@Composable
fun TrackMateOvalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    isEnabled: Boolean = true,
    colors: ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    elevation: ButtonElevation =
        ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp,
        )
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        colors = colors,
        elevation = elevation,
        enabled = isEnabled,
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@PreviewLightDark
@Composable
private fun TrackMateOvalButtonPreview() {
    TrackMateTheme {
        TrackMateOvalButton(
            text = "Text Button",
            onClick = {},
            modifier = Modifier.height(48.dp),
        )
    }
}
