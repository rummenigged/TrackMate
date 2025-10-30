package com.octopus.edu.core.ui.common.compositionLocals

import androidx.compose.runtime.staticCompositionLocalOf
import com.octopus.edu.core.common.credentialService.ICredentialService

val LocalCredentialManager =
    staticCompositionLocalOf<ICredentialService> {
        error("CompositionLocal ICredentialService not present")
    }
