package com.octopus.edu.core.data.auth.authAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.octopus.edu.core.common.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

interface FirebaseAuthAdapter {
    suspend fun signInWithCredentials(token: String)

    fun isUserLoggedIn(): Flow<Boolean>

    fun signOut()
}

class FirebaseAuthAdapterImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthAdapter {
    override suspend fun signInWithCredentials(token: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        firebaseAuth.signInWithCredential(firebaseCredential).await()
    }

    override fun isUserLoggedIn(): Flow<Boolean> =
        callbackFlow {
            val initialSent = trySend(firebaseAuth.currentUser != null)
            if (!initialSent.isSuccess) {
                Logger.w(message = "Initial emission dropped")
            }
            val authListener =
                FirebaseAuth.AuthStateListener {
                    val sent = trySend(it.currentUser != null)
                    if (!sent.isSuccess) {
                        Logger.w(message = "Auth state emission dropped")
                    }
                }
            firebaseAuth.addAuthStateListener(authListener)

            awaitClose {
                firebaseAuth.removeAuthStateListener(authListener)
            }
        }.distinctUntilChanged().conflate()

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
