package com.example.automate.domain.repository

import com.example.automate.domain.model.UserProfile

interface AuthRepository {
    suspend fun register(profile: UserProfile, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun isCurrentUserEmailVerified(): Boolean
    suspend fun resendVerificationEmail(): Result<Unit>
    suspend fun getCurrentUserProfile(): Result<UserProfile>
    suspend fun updateCurrentUserProfile(profile: UserProfile): Result<Unit>
    fun logout()
    fun isUserLoggedIn(): Boolean
}
