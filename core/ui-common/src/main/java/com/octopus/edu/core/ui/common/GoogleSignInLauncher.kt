package com.octopus.edu.core.ui.common

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.octopus.edu.core.common.credentialService.ICredentialService
import com.octopus.edu.core.common.credentialService.SignInInitiationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GoogleSignInLauncher internal constructor(
    private val activity: Activity?,
    private val credentialService: ICredentialService,
    private val coroutineScope: CoroutineScope,
    private val onSignInResult: (SignInInitiationResult) -> Unit
) {
    fun launch() {
        if (activity == null) {
            onSignInResult(SignInInitiationResult.Error("Activity cannot be null"))
            return
        }

        coroutineScope.launch {
            val result = credentialService.initiateGoogleSignIn(activity)
            onSignInResult(result)
        }
    }
}

@Composable
fun rememberGoogleSignInLauncher(
    credentialService: ICredentialService,
    onSignInResult: (SignInInitiationResult) -> Unit
): GoogleSignInLauncher {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val latestOnSignInResult by rememberUpdatedState(onSignInResult)

    return remember(activity, credentialService) {
        GoogleSignInLauncher(activity, credentialService, scope) { result ->
            latestOnSignInResult(result)
        }
    }
}
