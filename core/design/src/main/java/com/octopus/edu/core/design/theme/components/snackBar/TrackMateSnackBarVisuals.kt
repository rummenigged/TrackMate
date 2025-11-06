package com.octopus.edu.core.design.theme.components.snackBar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals

enum class SnackBarType { DEFAULT, ERROR }

data class TrackMateSnackBarVisuals(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean,
    val type: SnackBarType
) : SnackbarVisuals

suspend fun SnackbarHostState.showSnackBar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    type: SnackBarType = SnackBarType.DEFAULT
): SnackbarResult = showSnackbar(TrackMateSnackBarVisuals(actionLabel, duration, message, withDismissAction, type))
