package de.traewelling.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }

    fun login() {
        val jwt = viewModel.login()
        if (jwt != null) {
            val secureStorage = SecureStorage(this)
            secureStorage.storeObject(SharedValues.SS_JWT, jwt)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}