package com.rostry.prototype.ui.auth

import android.content.Context
import android.content.Intent
import com.rostry.prototype.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        // WEB_CLIENT_ID placeholder: Replace R.string.default_web_client_id
        // with the OAuth 2.0 web client ID from Google Cloud Console.
        // The google-services plugin auto-generates this string resource
        // from the web client ID in google-services.json.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = Tasks.await(auth.signInWithCredential(credential))
                val firebaseUser = authResult.user
                    ?: return@withContext Result.failure(Exception("Firebase user is null"))
                Result.success(firebaseUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun signOut() {
        googleSignInClient.signOut()
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
