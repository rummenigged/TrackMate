package com.octopus.edu.core.ui.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

data class StringResource(
    @param:PluralsRes @param:StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList()
)
