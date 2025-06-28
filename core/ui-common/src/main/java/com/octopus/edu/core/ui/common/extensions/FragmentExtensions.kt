package com.octopus.edu.core.ui.common.extensions

import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.octopus.edu.core.design.theme.TrackMateTheme

@Composable
private fun TrackMateThemeWithCompositionsLocals(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider {
        TrackMateTheme(
            darkTheme = isSystemInDarkTheme,
            dynamicColor = true,
        ) { content() }
    }
}

fun Fragment.setContent(content: @Composable (View) -> Unit) =
    ComposeView(context = requireContext()).apply {
        setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TrackMateThemeWithCompositionsLocals(content = {
                content(this)
            })
        }
    }
