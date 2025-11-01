package com.octopus.edu.core.design.theme.utils

interface Comparable<T> {
    fun areItemsTheSame(newItem: T): Boolean

    fun areContentsTheSame(newItem: T): Boolean
}
