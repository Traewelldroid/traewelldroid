package de.hbch.traewelling.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentSettingsBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.login.LoginActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var secureStorage: SecureStorage
    val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    var jwt: MutableLiveData<String> = MutableLiveData("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorage = SecureStorage(requireContext())
        jwt.postValue(secureStorage.getObject(SharedValues.SS_JWT, String::class.java) ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            fragment = this@SettingsFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storedHashtag = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java)
        binding.editTextHashtag.setText(storedHashtag ?: "")
    }

    fun storeHashtag() {
        secureStorage.storeObject(SharedValues.SS_HASHTAG, binding.editTextHashtag.text.toString())
    }

    fun renewLogin() {
        settingsViewModel.renewLogin(
            { token ->
                jwt.postValue(token.jwt)
                secureStorage.storeObject(SharedValues.SS_JWT, token.jwt)
                val bottomSheet = AlertBottomSheet(AlertType.SUCCESS, getString(R.string.renew_login_success), 3000)
                bottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
            },
            {
                val bottomSheet = AlertBottomSheet(AlertType.ERROR, getString(R.string.renew_login_failed), 3000)
                bottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
            }
        )
    }

    fun logout() {
        loggedInUserViewModel.logout( {
            val secureStorage = SecureStorage(requireContext())
            secureStorage.removeObject(SharedValues.SS_JWT)
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }, {
            val bottomSheet = AlertBottomSheet(
                AlertType.ERROR,
                getString(R.string.error_logout)
            )
            bottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
        })
    }
}