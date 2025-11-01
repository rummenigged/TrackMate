package com.octopus.edu.core.design.theme.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.octopus.edu.core.design.theme.components.DefaultSwipeActionsConfig
import com.octopus.edu.core.design.theme.components.SwipeActionsConfig

data class TutorialAnimationConfig(
    val duration: Int,
    val delay: Int,
    val easing: Easing
)

val DefaultTutorialAnimationConfig: TutorialAnimationConfig
    get() =
        TutorialAnimationConfig(
            duration = 500,
            delay = 1000,
            easing = FastOutSlowInEasing,
        )

@Composable
fun TutorialAnimationHandler(
    startActionsConfig: SwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig,
    tutorialAnimationConfig: TutorialAnimationConfig,
    viewWidth: Float,
    block: (Float) -> Unit
) {
    val startActionsInfo by remember { mutableStateOf(startActionsConfig) }

    val threshold =
        if (startActionsInfo == DefaultSwipeActionsConfig) {
            endActionsConfig.threshold
        } else {
            startActionsInfo.threshold
        }

    val targetValue by remember { derivedStateOf { viewWidth * (threshold) / 2f } }

    TutorialSwipeAnimationHandler(targetValue, tutorialAnimationConfig) { value ->
        block(value)
    }
}

@Composable
private fun TutorialSwipeAnimationHandler(
    targetValue: Float,
    tutorialAnimationConfig: TutorialAnimationConfig,
    block: ((Float) -> Unit)
) {
    val startAnimationFloat = remember { Animatable(0f) }
    val endAnimationFloat = remember { Animatable(targetValue) }

    LaunchedEffect(startAnimationFloat) {
        val startAnimationResult =
            startAnimationFloat.animateTo(
                targetValue = targetValue,
                animationSpec =
                    tween(
                        durationMillis = tutorialAnimationConfig.duration,
                        easing = tutorialAnimationConfig.easing,
                        delayMillis = tutorialAnimationConfig.delay,
                    ),
            ) { block(value) }

        if (startAnimationResult.endReason == AnimationEndReason.Finished) {
            endAnimationFloat.animateTo(
                targetValue = 0f,
                animationSpec =
                    tween(
                        durationMillis = tutorialAnimationConfig.duration,
                        easing = tutorialAnimationConfig.easing,
                        delayMillis = tutorialAnimationConfig.delay,
                    ),
            ) { block(value) }
        }
    }
}
