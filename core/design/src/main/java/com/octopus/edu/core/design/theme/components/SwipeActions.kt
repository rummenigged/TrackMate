package com.octopus.edu.core.design.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SwipeActionsConfig(
    val threshold: Float,
    val icon: ImageVector,
    val disabledIconTint: Color,
    val swipeActionActivatedBackground: Color,
    val swipeActionDeactivatedBackground: Color,
    val backgroundShape: Shape = RoundedCornerShape(0.dp),
    val stayDismissed: Boolean,
    val onSwiped: () -> Unit
)

val DefaultSwipeActionsConfig: SwipeActionsConfig =
    SwipeActionsConfig(
        threshold = 0.4f,
        icon = Icons.Default.Menu,
        disabledIconTint = Color.Transparent,
        swipeActionActivatedBackground = Color.Transparent,
        swipeActionDeactivatedBackground = Color.Transparent,
        stayDismissed = false,
        onSwiped = {},
    )

@Composable
fun SwipActions(
    modifier: Modifier = Modifier,
    startActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    content: @Composable () -> Unit
) {
    var width by remember { mutableFloatStateOf(1f) }

    var willDismissDirection: SwipeToDismissBoxValue? by remember { mutableStateOf(null) }

    val dismissState =
        rememberSwipeToDismissBoxState(
            positionalThreshold = { totalDistance -> totalDistance * maxOf(startActionsConfig.threshold, endActionsConfig.threshold) },
            confirmValueChange = { value ->
                handlerDismissStateChanges(
                    value,
                    willDismissDirection,
                    startActionsConfig,
                    endActionsConfig,
                )
            },
        )

    LaunchedEffect(dismissState, startActionsConfig, endActionsConfig, width) {
        snapshotFlow { dismissState.requireOffset() }
            .collect { offset ->
                willDismissDirection =
                    when {
                        offset > width * startActionsConfig.threshold -> StartToEnd
                        offset < -width * endActionsConfig.threshold -> EndToStart
                        else -> null
                    }
            }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(willDismissDirection) {
        if (willDismissDirection != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    SwipeToDismissBox(
        modifier =
            modifier
                .onSizeChanged { width = it.width.toFloat() },
        state = dismissState,
        enableDismissFromStartToEnd = startActionsConfig != DefaultSwipeActionsConfig,
        enableDismissFromEndToStart = endActionsConfig != DefaultSwipeActionsConfig,
        backgroundContent = {
            SwipeActionsBackground(
                state = dismissState,
                dismissDirection = willDismissDirection,
                startActionsConfig = startActionsConfig,
                endActionsConfig = endActionsConfig,
            )
        },
    ) {
        content()
    }
}

@Composable
private fun SwipeActionsBackground(
    state: SwipeToDismissBoxState,
    dismissDirection: SwipeToDismissBoxValue?,
    startActionsConfig: SwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        modifier =
            modifier.clip(
                if (state.dismissDirection == StartToEnd) {
                    startActionsConfig.backgroundShape
                } else {
                    endActionsConfig.backgroundShape
                },
            ),
        targetState = Pair(state.dismissDirection, (dismissDirection != null)),
        transitionSpec = {
            fadeIn(
                tween(0),
                initialAlpha = if (targetState.second) 1f else 0f,
            ) togetherWith
                fadeOut(
                    tween(0),
                    targetAlpha = if (targetState.second) .7f else 0f,
                )
        },
        label = "",
    ) { (direction, willDismiss) ->
        SwipeActionBackgroundContent(
            direction = direction,
            willDismiss = willDismiss,
            startActionsConfig = startActionsConfig,
            endActionsConfig = endActionsConfig,
        )
    }
}

@Composable
private fun SwipeActionBackgroundContent(
    direction: SwipeToDismissBoxValue,
    willDismiss: Boolean,
    startActionsConfig: SwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig,
    modifier: Modifier = Modifier,
) {
    val revealSize = remember { Animatable(if (willDismiss) 0f else 1f) }
    val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }

    LaunchedEffect(Unit) {
        if (willDismiss) {
            revealSize.snapTo(0f)
            launch {
                revealSize.animateTo(1f, animationSpec = tween(400))
            }
            iconSize.snapTo(.8f)
            iconSize.animateTo(
                1.45f,
                spring(dampingRatio = Spring.DampingRatioHighBouncy),
            )
            iconSize.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioLowBouncy),
            )
        }
    }

    val backgroundColor =
        when (direction) {
            StartToEnd ->
                if (willDismiss) {
                    startActionsConfig.swipeActionActivatedBackground
                } else {
                    startActionsConfig.swipeActionDeactivatedBackground
                }
            EndToStart ->
                if (willDismiss) {
                    endActionsConfig.swipeActionActivatedBackground
                } else {
                    endActionsConfig.swipeActionDeactivatedBackground
                }
            else -> Color.Transparent
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(
                    ProgressiveCircleShape(
                        progress = revealSize.value,
                        start = direction == StartToEnd,
                    ),
                ).background(backgroundColor),
    ) {
        SwipeActionIcon(
            modifier =
                Modifier
                    .align(
                        when (direction) {
                            StartToEnd -> Alignment.CenterStart
                            else -> Alignment.CenterEnd
                        },
                    ),
            dismissDirection = direction,
            willDismiss = willDismiss,
            iconSizeAnimatable = iconSize,
            startActionsConfig = startActionsConfig,
            endActionsConfig = endActionsConfig,
        )
    }
}

@Composable
private fun SwipeActionIcon(
    dismissDirection: SwipeToDismissBoxValue?,
    willDismiss: Boolean,
    iconSizeAnimatable: Animatable<Float, AnimationVector1D>,
    startActionsConfig: SwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .scale(iconSizeAnimatable.value)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (10 * (1f - iconSizeAnimatable.value)).roundToInt(),
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        when (dismissDirection) {
            StartToEnd -> {
                Image(
                    painter = rememberVectorPainter(image = startActionsConfig.icon),
                    colorFilter =
                        if (willDismiss) {
                            null
                        } else {
                            ColorFilter.tint(
                                startActionsConfig.disabledIconTint,
                            )
                        },
                    contentDescription = null,
                )
            }
            EndToStart -> {
                Image(
                    painter = rememberVectorPainter(image = endActionsConfig.icon),
                    colorFilter =
                        if (willDismiss) {
                            null
                        } else {
                            ColorFilter.tint(
                                endActionsConfig.disabledIconTint,
                            )
                        },
                    contentDescription = null,
                )
            }

            else -> {}
        }
    }
}

private fun handlerDismissStateChanges(
    dismissValue: SwipeToDismissBoxValue,
    willDismissDirection: SwipeToDismissBoxValue?,
    startActionsConfig: SwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig
) = when {
    willDismissDirection == StartToEnd && dismissValue == StartToEnd -> {
        startActionsConfig.onSwiped()
        startActionsConfig.stayDismissed
    }

    willDismissDirection == EndToStart && dismissValue == EndToStart -> {
        endActionsConfig.onSwiped()
        endActionsConfig.stayDismissed
    }
    else -> false
}
