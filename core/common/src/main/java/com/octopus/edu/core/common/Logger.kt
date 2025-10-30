package com.octopus.edu.core.common

import timber.log.Timber

object Logger {
    private val ANONYMOUS_CLASS_PATTERN = Regex("\\$\\d+$")

    private fun getCallerTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size > 3) {
            val callerElement = stackTrace[3]
            val tag = callerElement.className.substringAfterLast('.')
            return tag
                .replace(ANONYMOUS_CLASS_PATTERN, "")
                .substringBefore('$')
                .ifEmpty { "Logger" }
        }
        return "Logger"
    }

    fun d(
        message: String,
        tag: String? = null,
    ) {
        val finalTag = tag ?: getCallerTag()
        Timber.tag(finalTag).d(message)
    }

    fun w(
        message: String,
        tag: String? = null,
        throwable: Throwable? = null
    ) {
        val finalTag = tag ?: getCallerTag()
        if (throwable != null) {
            Timber.tag(finalTag).w(throwable, message)
        } else {
            Timber.tag(finalTag).w(message)
        }
    }

    fun e(
        message: String,
        tag: String? = null,
        throwable: Throwable? = null
    ) {
        val finalTag = tag ?: getCallerTag()
        if (throwable != null) {
            Timber.tag(finalTag).e(throwable, message)
        } else {
            Timber.tag(finalTag).e(message)
        }
    }
}
