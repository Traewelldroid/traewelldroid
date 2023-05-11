package de.hbch.traewelling.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentSettingsBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.main.MainActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.settingsView.setContent {
            MainTheme {
                Settings(
                    loggedInUserViewModel = loggedInUserViewModel,
                    emojiPackItemAdapter = (this@SettingsFragment.requireActivity() as MainActivity).emojiPackItemAdapter,
                    traewellingLogoutAction = {
                        this@SettingsFragment.logout()
                    }
                )
            }
        }
        return binding.root
    }

    private fun logout() {
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