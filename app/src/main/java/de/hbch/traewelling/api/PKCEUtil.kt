package de.hbch.traewelling.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.api.models.auth.TokenResponse
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.main.MainActivity
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

private val verifiers = mutableMapOf<String, String>()

private fun redirectUri() = BuildConfig.OAUTH_CALLBACK_URL.buildUpon()
    .appendPath("auth")
    .appendPath("login")
    .build()
    .toString()

object PKCEUtil {
    private val secureRandom = SecureRandom()

    fun handleCallback(activity: Activity, uri: Uri, secureStorage: SecureStorage) {
        val code = uri.getQueryParameter("code") ?: error("Missing authorization code")
        val verifier =
            uri.getQueryParameter("state")?.let { verifiers.remove(it) } ?: error("Invalid state")
        TraewellingApi.authService.requestToken(
            BuildConfig.OAUTH_CLIENT_ID.toString(),
            redirectUri(),
            "authorization_code",
            verifier,
            code
        ).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                val accessToken = response.body()?.accessToken ?: return onFailure(
                    call,
                    IllegalArgumentException("Missing token")
                )
                secureStorage.storeObject(SharedValues.SS_JWT, accessToken)
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.EMPTY,
                        activity,
                        MainActivity::class.java
                    )
                )
                activity.finish()
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Sentry.captureException(t)
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.EMPTY,
                        activity,
                        LoginActivity::class.java
                    ).apply {
                        putExtra(SharedValues.EXTRA_LOGIN_FAILED, true)
                    }
                )
                activity.finish()
            }
        })
    }

    private fun nextChallenge(): ChallengeContext {
        val verifier = generateCodeVerifier()
        val state = generateCodeVerifier()
        verifiers[state] = verifier
        return ChallengeContext(state, generateCodeChallenge(verifier))
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun generateCodeVerifier(): String {
        val codeVerifier = ByteArray(32)
        secureRandom.nextBytes(codeVerifier)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier)
    }

    fun Context.openOAuthAuthorizationPage() {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()

        val (state, challenge) = nextChallenge()
        val url = Uri.parse("https://traewelling.de").buildUpon()
            .appendPath("oauth").appendEncodedPath("authorize")
            .appendQueryParameter("client_id", "28")
            .appendQueryParameter("redirect_uri", redirectUri())
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("response_type", "code")
            .build()

        intent.launchUrl(this, url)
    }
}

private data class ChallengeContext(val state: String, val challenge: String)