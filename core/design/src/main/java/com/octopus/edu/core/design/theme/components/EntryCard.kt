package com.octopus.edu.core.design.theme.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import com.octopus.edu.core.design.theme.utils.orZeroIfDarkTheme

@Composable
fun EntryCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    backgroundColor: Color = colorScheme.surfaceContainer,
    shape: CornerBasedShape = shapes.medium,
    content: @Composable () -> Unit,
) = trace("EntryCard") {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.orZeroIfDarkTheme()),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun EntryCardPreview() {
}
