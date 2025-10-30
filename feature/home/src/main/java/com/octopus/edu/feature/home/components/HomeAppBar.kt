package com.octopus.edu.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.util.trace
import com.octopus.edu.core.design.theme.TrackMateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeAppBar(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = colorScheme.primary,
) = trace("HomeAppBar") {
    TopAppBar(
        modifier = modifier.background(color = background),
        title = {
            Text(
                text = title,
                style = typography.titleMedium,
            )
        },
    )
}

@PreviewLightDark
@Composable
private fun HomeAppBarPreview() {
    TrackMateTheme {
        HomeAppBar(
            title = "July, 16",
        )
    }
}
