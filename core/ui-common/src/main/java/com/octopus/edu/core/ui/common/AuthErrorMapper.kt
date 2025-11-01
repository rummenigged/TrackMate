package com.octopus.edu.core.ui.common

object AuthErrorMapper {
    fun toUserMessage(rawMessage: String?): String =
        if (rawMessage.isNullOrBlank()) {
            "Something went wrong. Please try again."
        } else {
            "Couldn't sign in. Please try again."
        }
}
