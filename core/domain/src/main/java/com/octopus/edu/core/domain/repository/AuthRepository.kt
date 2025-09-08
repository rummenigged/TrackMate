package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isUserLoggedIn: Flow<Boolean>

    suspend fun signIn(token: String): ResultOperation<Unit>

    suspend fun signOut(): ResultOperation<Unit>
}
