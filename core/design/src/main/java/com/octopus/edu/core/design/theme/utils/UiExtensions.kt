package com.octopus.edu.core.design.theme.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dp.orZeroIfDarkTheme() = if (isSystemInDarkTheme()) 0.dp else this
