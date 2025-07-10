package com.octopus.edu.core.ui.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp

@Composable
fun rememberMaxTextWidthDp(vararg strings: String): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(*strings) {
        with(density) {
            strings
                .maxOf { label ->
                    textMeasurer.measure(label).size.width
                }.toDp()
        }
    }
}

fun Modifier.noClickableOverlay(): Modifier =
    this.then(
        Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
    )
