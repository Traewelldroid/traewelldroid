package de.hbch.traewelling.ui.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.databinding.ActivityLoginBinding
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.main.MainActivity
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.AnyBrowserMatcher
import java.security.MessageDigest
import java.security.SecureRandom

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
    private lateinit var authorizationService : AuthorizationService
    private lateinit var authIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.apply {
            loginActivity = this@LoginActivity
            lifecycleOwner = this@LoginActivity
        }

        initAuth()

        setContentView(binding.root)
    }

    private fun initAuth() {
        val authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(SharedValues.URL_AUTHORIZATION),
            Uri.parse(SharedValues.URL_TOKEN_EXCHANGE)
        )

        val appAuthConfiguration = AppAuthConfiguration.Builder()
            .setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
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
    fun initiateOAuthPKCELogin() {
        authorizationLauncher.launch(authIntent)
    }

    fun handleAuthorizationResponse(intent: Intent) {
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

    fun showInfoActivity() {
        startActivity(Intent(this, InfoActivity::class.java))
    }
}