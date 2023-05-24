package de.hbch.traewelling.ui.login

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.main.MainActivity
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserDenyList
import net.openid.appauth.browser.VersionedBrowserMatcher
import java.security.MessageDigest
import java.security.SecureRandom

class LoginActivity : ComponentActivity() {

    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
    private lateinit var authorizationService : AuthorizationService
    private lateinit var authIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAuth()

        setContent {
            MainTheme {
                LoginScreen(
                    loginAction = { initiateOAuthPKCELogin() },
                    informationAction = { showInfoActivity() }
                )
            }
        }
    }

    private fun initAuth() {
        val authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(SharedValues.URL_AUTHORIZATION),
            Uri.parse(SharedValues.URL_TOKEN_EXCHANGE)
        )

        val appAuthConfiguration = AppAuthConfiguration.Builder()
            .setBrowserMatcher(
                BrowserDenyList(
                    VersionedBrowserMatcher.FIREFOX_BROWSER,
                    VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB
                )
            )
            .build()

        authorizationService = AuthorizationService(
            application,
            appAuthConfiguration
        )
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        val builder = AuthorizationRequest.Builder(
            authServiceConfig,
            BuildConfig.OAUTH_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(BuildConfig.OAUTH_REDIRECT_URL)
        )
        builder
            .setCodeVerifier(
                codeVerifier,
                codeChallenge,
                "S256"
            )
            .setScopes(SharedValues.AUTH_SCOPES)
            .setPrompt("consent")

        val request = builder.build()
        authIntent = authorizationService.getAuthorizationRequestIntent(request)

        authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result ->
                    run {
                        if (result.resultCode == Activity.RESULT_OK) {
                            handleAuthorizationResponse(result.data!!)
                        }
                    }
        }
    }
    private fun initiateOAuthPKCELogin() {
        if (appCanHandleLinks()) {
            authorizationLauncher.launch(authIntent)
        } else {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.request_url_verification))
            alertDialog.setMessage(getString(R.string.request_url_login_verification))
            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.yes)
            ) { _, _ ->
                val settingsDestination =
                    if (VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                    else
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                startActivity(
                    Intent(
                        settingsDestination,
                        Uri.parse("package:${packageName}")
                    )
                )
            }
            alertDialog.show()
        }
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)

        if (authorizationResponse != null) {
            val tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest()
            authorizationService.performTokenRequest(tokenExchangeRequest) { response, _ ->
                if (response?.accessToken != null) {
                    val secureStorage = SecureStorage(this)
                    secureStorage.storeObject(SharedValues.SS_JWT, response.accessToken!!)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showInfoActivity() {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    private fun appCanHandleLinks(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BuildConfig.OAUTH_REDIRECT_URL)
        val resolveInfo =
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                packageManager.queryIntentActivities(intent, ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
            else
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo.all {
            it.activityInfo.packageName == BuildConfig.APPLICATION_ID
        }
    }
}