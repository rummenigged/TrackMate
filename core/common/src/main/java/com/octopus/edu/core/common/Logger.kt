package com.octopus.edu.core.common

import timber.log.Timber

object Logger {
    fun d(
        message: String,
        tag: String? = null,
    ) {
        val log = tag?.let { Timber.tag(it) } ?: Timber
        log.d(message)
    }

    fun w(
        message: String,
        tag: String? = null,
        throwable: Throwable? = null
    ) {
        val log = tag?.let { Timber.tag(it) } ?: Timber
        if (throwable != null) {
            log.w(throwable, message)
        } else {
            log.w(message)
        }
    }

    fun e(
        message: String,
        tag: String? = null,
        throwable: Throwable? = null
    ) {
        val log = tag?.let { Timber.tag(it) } ?: Timber
        if (throwable != null) {
            log.e(throwable, message)
        } else {
            log.e(message)
        }
    }
}
