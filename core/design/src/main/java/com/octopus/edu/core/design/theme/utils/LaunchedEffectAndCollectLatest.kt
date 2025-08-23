package com.octopus.edu.core.design.theme.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

@Composable
fun <T> LaunchedEffectAndCollectLatest(
    flow: Flow<T?>,
    onEffectConsumed: () -> Unit,
    function: suspend (value: T) -> Unit
) {
    val effectFlow = rememberFunctionWithLifecycle(flow)

    LaunchedEffect(effectFlow) {
        effectFlow.mapNotNull { it }.collect { item ->
            function(item)
            onEffectConsumed
        }
    }
}
