package de.hbch.traewelling.ui.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.api.PCKEUtil.openOAuthAuthorizationPage
import de.hbch.traewelling.databinding.ActivityLoginBinding
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.main.MainActivity
import retrofit2.http.Url

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginActivityViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.apply {
            viewModel = this@LoginActivity.viewModel
            loginActivity = this@LoginActivity
            lifecycleOwner = this@LoginActivity
        }
        setContentView(binding.root)

        setError(false)

        if (intent.getBooleanExtra(SharedValues.EXTRA_LOGIN_FAILED, false)) {
            val alertBottomSheet = AlertBottomSheet(
                AlertType.ERROR,
                getString(R.string.login_failed),
                5000
            )
            alertBottomSheet.show(supportFragmentManager, AlertBottomSheet.TAG)
        }
    }

    fun initiateOAuthLogin() = openOAuthAuthorizationPage()

    fun login() {
        setError(false)
        viewModel.login(
            binding.editTextLogin.text.toString(),
            binding.editTextPassword.text.toString(),
            { jwt ->
                if (jwt == null) {
                    setError(true)
                } else {
                    val secureStorage = SecureStorage(this)
                    secureStorage.storeObject(SharedValues.SS_JWT, jwt)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            },
            {
                setError(true)
            }
        )
    }

    fun showInfoActivity() {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    private fun setError(error: Boolean) {
        val errorText = when(error) {
            true -> "Bitte überprüfe deine Eingaben"
            false -> ""
        }
        binding.textInputPassword.error = errorText
        binding.textInputLogin.error = errorText
    }
}