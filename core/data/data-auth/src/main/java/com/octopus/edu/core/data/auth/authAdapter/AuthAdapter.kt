package com.octopus.edu.core.data.auth.authAdapter

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.octopus.edu.core.common.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

interface AuthAdapter {
    /**
 * Retrieves the current user's Firebase ID token.
 *
 * @param forceRefresh If `true`, forces fetching a fresh token from the server; if `false`, allows using a cached token.
 * @return The ID token string if available, or `null` if no user is signed in or the token could not be obtained.
 */
suspend fun getIdToken(forceRefresh: Boolean): String?

    /**
 * Signs in using the provided OAuth token.
 *
 * @param token The OAuth token (ID or access token) obtained from the identity provider.
 * @return An AuthResult containing the signed-in user's authentication information and metadata.
 */
suspend fun signInWithCredentials(token: String): AuthResult

    fun isUserLoggedIn(): Flow<Boolean>

    /**
 * Signs out the current user from Firebase Authentication.
 *
 * Clears the client's authentication state and notifies any registered authentication state listeners.
 */
fun signOut()
}

class FirebaseAuthAdapter(
    private val firebaseAuth: FirebaseAuth
) : AuthAdapter {
    /**
     * Retrieves the current Firebase user's ID token.
     *
     * @param forceRefresh If `true`, forces fetching a fresh token instead of using a cached one.
     * @return The ID token string if a user is signed in and retrieval succeeds, `null` if there is no user or if token retrieval fails.
     */
    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        val user = firebaseAuth.currentUser
        return try {
            user?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            Logger.e(message = "Failed to get ID token", throwable = e)
            null
        }
    }

    override suspend fun signInWithCredentials(token: String): AuthResult {
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        return firebaseAuth.signInWithCredential(firebaseCredential).await()
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