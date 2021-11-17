package de.hbch.traewelling.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.login.LoginActivity
import java.lang.Exception


class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private lateinit var menuItems: List<MenuItem>
    private var page = 1

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
        binding.recyclerViewCheckIn.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCheckIn.adapter = CheckInAdapter(mutableListOf(), loggedInUserViewModel.userId) {}
        binding.nestedScrollViewUser.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val vw = v?.getChildAt(v.childCount - 1)
            val diff = (vw?.bottom?.minus((v.height + v.scrollY)))
            if (diff!! == 0) {
                if (!binding.swipeRefreshDashboardCheckIns.isRefreshing) {
                    page++
                    loadCheckIns()
                }
            }
        })

        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            page = 1
            loadCheckIns()
        }

        loggedInUserViewModel.profilePictureSrc.observe(viewLifecycleOwner) { src ->
            binding.imageProfile.load(src) {
                crossfade(true)
                placeholder(R.drawable.ic_new_user)
                transformations(CircleCropTransformation())
            }
        }

        loadCheckIns()

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

    fun loadCheckIns() {
        binding.swipeRefreshDashboardCheckIns.isRefreshing = true
        loggedInUserViewModel.getPersonalCheckIns(
            page,
            { statusPage ->
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
                val adapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                if (page == 1) {
                    adapter.notifyItemRangeRemoved(0, adapter.checkIns.size)
                    adapter.checkIns.clear()
                    adapter.checkIns.addAll(statusPage.data)
                    adapter.notifyItemRangeInserted(0, adapter.checkIns.size)
                } else {
                    val currentCount = adapter.checkIns.size
                    adapter.checkIns.addAll(statusPage.data)
                    adapter.notifyItemRangeInserted(currentCount, adapter.checkIns.size)
                }
            },
            {
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
            }
        )
    }
}

class MenuItem(
    val title: Int,
    val drawable: Int,
    val action: () -> Unit
)