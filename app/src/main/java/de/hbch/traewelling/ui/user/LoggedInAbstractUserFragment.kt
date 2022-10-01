package de.hbch.traewelling.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.info.InfoActivity


class LoggedInAbstractUserFragment : AbstractUserFragment() {
    override val viewModel: LoggedInUserViewModel by activityViewModels()
    private lateinit var menuItems: List<MenuItem>
    private lateinit var menuProvider: MenuProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        menuItems = listOf(
            MenuItem(R.string.settings, R.drawable.ic_settings) {
                findNavController()
                    .navigate(LoggedInAbstractUserFragmentDirections.actionUserFragmentToSettingsFragment())
            },
            MenuItem(R.string.information, R.drawable.ic_settings) {
                startActivity(Intent(requireContext(), InfoActivity::class.java))
            }
        )

        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuItems.forEachIndexed { index, item ->
                    menu
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

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return try {
                    val item = menuItems[menuItem.itemId - Menu.FIRST]
                    item.action()
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        requireActivity().addMenuProvider(menuProvider)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }
}

class MenuItem(
    val title: Int,
    val drawable: Int,
    val action: () -> Unit
)
