package de.traewelling.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.jcloquell.androidsecurestorage.SecureStorage
import de.traewelling.R
import de.traewelling.databinding.ActivityLoginBinding
import de.traewelling.shared.SharedValues
import de.traewelling.ui.main.MainActivity

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
        viewModel.loginSuccessful.observe(this) { success ->
            if (success != null) {
                if (success) {
                    val secureStorage = SecureStorage(this)
                    secureStorage.storeObject(SharedValues.SS_JWT, viewModel.jwt.value!!)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    setError(true)
                }
            }
        }
    }

    fun login() {
        setError(false)
        viewModel.login()
    }

    private fun setError(error: Boolean) {
        val errorText = when(error) {
            true -> "Bitte überprüfe deine Eingaben"
            false -> ""
        }
        binding.textInputPassword.error = errorText
        binding.textInputUsername.error = errorText
    }
}