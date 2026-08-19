package com.example.automate.data.repository

import com.example.automate.domain.model.UserProfile
import com.example.automate.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
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

    override suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user.")
            val snapshot = firestore.collection("users").document(user.uid).get().await()
            val profile = UserProfile(
                fullName = snapshot.getString("fullName") ?: "",
                age = (snapshot.getLong("age") ?: 0L).toInt(),
                email = snapshot.getString("email") ?: user.email ?: "",
                hasLicense = snapshot.getBoolean("hasLicense") ?: false,
                photoBase64 = snapshot.getString("photoBase64"),
                licensePhotoBase64 = snapshot.getString("licensePhotoBase64")
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun updateCurrentUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user.")
            val updates = mutableMapOf<String, Any>(
                "fullName" to profile.fullName,
                "age" to profile.age,
                "hasLicense" to profile.hasLicense
            )
            if (profile.photoBase64 != null) {
                updates["photoBase64"] = profile.photoBase64
            }
            if (profile.licensePhotoBase64 != null) {
                updates["licensePhotoBase64"] = profile.licensePhotoBase64
            }
            firestore.collection("users").document(user.uid)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user.")
            val email = user.email
                ?: throw IllegalStateException("This account has no email on file.")
            user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun changeEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user.")
            val email = user.email
                ?: throw IllegalStateException("This account has no email on file.")
            user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
            user.verifyBeforeUpdateEmail(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToThrowable(e))
        }
    }

    override suspend fun deleteAccount(currentPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in user.")
            val email = user.email
                ?: throw IllegalStateException("This account has no email on file.")
            user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()

            val userDoc = firestore.collection("users").document(user.uid)
            val vehicles = userDoc.collection("vehicles").get().await()
            for (vehicleDoc in vehicles.documents) {
                vehicleDoc.reference.delete().await()
            }
            userDoc.delete().await()

            user.delete().await()
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
