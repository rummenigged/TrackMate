package com.octopus.edu.core.common

import timber.log.Timber
import java.util.regex.Pattern

object Logger {
    private val ANONYMOUS_CLASS_PATTERN = Pattern.compile("\\$\\d+$")

    private fun getCallerTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size > 3) {
            val callerElement = stackTrace[3]
            var tag = callerElement.className.substringAfterLast('.')
            ANONYMOUS_CLASS_PATTERN.matcher(tag).replaceAll("")
            tag = tag.substringBefore('$')
            return tag.ifEmpty { "Logger" }
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
