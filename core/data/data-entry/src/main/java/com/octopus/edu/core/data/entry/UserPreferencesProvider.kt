package com.octopus.edu.core.data.entry

import com.google.firebase.auth.FirebaseAuth

interface UserPreferencesProvider {
    val userId: String
}

class UserPreferencesProviderImpl(
    private val firebaseAuth: FirebaseAuth
) : UserPreferencesProvider {
    override val userId: String
        get() = firebaseAuth.currentUser?.uid ?: throw RuntimeException("User not authenticated")
}
