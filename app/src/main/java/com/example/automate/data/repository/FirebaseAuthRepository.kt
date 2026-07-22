package com.example.automate.data.repository

import com.example.automate.domain.model.UserProfile
import com.example.automate.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun register(profile: UserProfile, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(profile.email, password)
                .await()
            val user = authResult.user
                ?: throw IllegalStateException("Registration succeeded but no user was returned.")

            firestore.collection("users").document(user.uid)
                .set(
                    mapOf(
                        "fullName" to profile.fullName,
                        "age" to profile.age,
                        "email" to profile.email,
                        "hasLicense" to profile.hasLicense,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            user.sendEmailVerification().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun isCurrentUserEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return try {
            user.reload().await()
            user.isEmailVerified
        } catch (e: Exception) {
            user.isEmailVerified
        }
    }

    override suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user to verify.")
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun mapExceptionToThrowable(e: Exception): Throwable {
        val message = when (e) {
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email address format."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password login is not enabled."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                    "ERROR_WEAK_PASSWORD" -> "The password is too weak."
                    else -> e.localizedMessage ?: "An unknown authentication error occurred."
                }
            }
            else -> e.localizedMessage ?: "An unexpected error occurred."
        }
        return Exception(message)
    }
}
