package com.octopus.edu.core.ui.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

data class StringResource(
    @field:PluralsRes @field:StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList()
)
