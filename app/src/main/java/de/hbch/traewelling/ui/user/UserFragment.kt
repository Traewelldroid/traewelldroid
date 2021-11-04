package de.hbch.traewelling.ui.user

import StandardListItemAdapter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private lateinit var menuItems: List<MenuItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserFragment.loggedInUserViewModel
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuItems = listOf(
            MenuItem(R.string.logout, R.drawable.ic_logout) {
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
        )
    }

    override fun onCreateOptionsMenu(optionsMenu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(optionsMenu, inflater)
        menuItems.forEachIndexed { index, item ->
            optionsMenu
                .add(
                    0,
                    Menu.FIRST + index,
                    Menu.NONE,
                    item.title
                )
                .setIcon(item.drawable)
                .setShowAsActionFlags(android.view.MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return try {
            val menuItem = menuItems[item.itemId - Menu.FIRST]
            menuItem.action()
            true
        } catch (_: Exception) {
            super.onOptionsItemSelected(item)
        }
    }
}

class MenuItem(
    val title: Int,
    val drawable: Int,
    val action: () -> Unit
)