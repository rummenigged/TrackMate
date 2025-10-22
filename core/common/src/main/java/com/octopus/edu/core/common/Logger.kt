package com.octopus.edu.core.common

import timber.log.Timber
import java.util.regex.Pattern

object Logger {
    /**
     * Derives a default log tag from the calling class.
     *
     * Strips numeric anonymous-class suffixes (e.g. `$1`) and nested-class markers, and falls back to `"Logger"` when a caller tag cannot be determined.
     *
     * @return The caller's simple class name with anonymous/nested suffixes removed, or `"Logger"` if no suitable caller tag is available.
     */
    private fun getCallerTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size > 3) {
            val callerElement = stackTrace[3]
            var tag = callerElement.className.substringAfterLast('.')
            val anonymousClassPattern = Pattern.compile("\\$\\d+$")
            val matcher = anonymousClassPattern.matcher(tag)
            if (matcher.find()) {
                tag = matcher.replaceAll("")
            }
            tag = tag.substringBefore('$')
            return tag.ifEmpty { "Logger" }
        }
        return "Logger"
    }

    /**
     * Logs a debug message using Timber with an auto-derived caller tag when none is provided.
     *
     * @param message The message to log.
     * @param tag Optional explicit tag; when null, a tag is derived from the caller's class name.
     */
    fun d(
        message: String,
        tag: String? = null,
    ) {
        val finalTag = tag ?: getCallerTag()
        Timber.tag(finalTag).d(message)
    }

    /**
     * Logs a warning message using the provided tag or a tag derived from the caller when none is provided.
     *
     * @param message The warning message to log.
     * @param tag Optional tag to associate with the log; if null, a caller-derived tag will be used.
     * @param throwable Optional throwable whose stack trace will be logged alongside the message.
     */
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

    /**
     * Logs an error message using Timber, deriving a tag from the caller when none is provided.
     *
     * @param message The error message to log.
     * @param tag Optional explicit log tag; if `null`, a tag is derived from the call site.
     * @param throwable Optional throwable whose stack trace will be logged alongside the message.
     */
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