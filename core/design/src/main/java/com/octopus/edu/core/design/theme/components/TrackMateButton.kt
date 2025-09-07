package com.octopus.edu.core.design.theme.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.R
import com.octopus.edu.core.design.theme.TrackMateTheme

@Composable
fun PrimaryIconButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    colors: ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.surfaceContainerHighest,
            disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.5f),
        )
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = colors,
        contentPadding = PaddingValues(all = 16.dp),
        enabled = isEnabled,
        onClick = { onClick() },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = Modifier.align(Alignment.CenterStart),
                painter = painterResource(id = iconRes),
                contentDescription = null,
            )

            Text(
                text,
                style = typography.titleMedium.copy(color = colorScheme.onPrimary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PrimaryIconButtonPreview() {
    TrackMateTheme {
        PrimaryIconButton(
            text = "Login",
            iconRes = R.drawable.ic_google_new,
            onClick = {},
        )
    }
}
