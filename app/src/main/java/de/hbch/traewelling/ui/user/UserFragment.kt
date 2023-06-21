package de.hbch.traewelling.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.UserViewModel
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.ui.info.InfoActivity

class UserFragment : Fragment() {

    private val args: UserFragmentArgs by navArgs()
    private lateinit var binding: FragmentUserBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val userStatusViewModel: UserStatusViewModel by viewModels()
    private val checkInCardViewModel: CheckInCardViewModel by viewModels()
    private lateinit var menuItems: List<MenuItem>
    private lateinit var menuProvider: MenuProvider
    private var currentPage = 1

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // enable menu
        enableMenu()

        val username = args.username ?: loggedInUserViewModel.loggedInUser.value?.username
        userStatusViewModel.loadUser(username)

        binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.userContent.setContent {
            MainTheme {
                val refreshing by userStatusViewModel.isRefreshing.observeAsState(false)
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = {
                        currentPage = 1
                        userStatusViewModel.loadUser(username)
                    })
                val listState = rememberLazyListState()

                listState.onBottomReached {
                    if (userStatusViewModel.checkIns.size > 0) {
                        userStatusViewModel.loadStatusesForUser(page = ++currentPage)
                    }
                }

                Box(
                    modifier = Modifier.pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = listState
                    ) {
                        item {
                            UserCard(
                                userViewModel = userStatusViewModel,
                                loggedInUserViewModel = loggedInUserViewModel
                            )
                        }
                        items(
                            items = userStatusViewModel.checkIns
                        ) { status ->
                            CheckInCard(
                                checkInCardViewModel = checkInCardViewModel,
                                status = status.toStatusDto(),
                                loggedInUserViewModel = loggedInUserViewModel,
                                stationSelected = { stationName, date ->
                                    findNavController()
                                        .navigate(
                                            UserFragmentDirections.actionUserFragmentToSearchConnectionFragment(
                                                stationName,
                                                date
                                            )
                                        )
                                },
                                statusSelected = { statusId, userId ->
                                    findNavController()
                                        .navigate(
                                            UserFragmentDirections
                                                .actionUserFragmentToStatusDetailFragment(
                                                    statusId,
                                                    userId
                                                )
                                        )
                                }
                            )
                        }
                    }
                    PullRefreshIndicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        refreshing = refreshing,
                        state = pullRefreshState
                    )
                }
            }
        }
        /*binding.recyclerViewCheckIn.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCheckIn.adapter = CheckInAdapter(
            mutableListOf(),
            loggedInUserViewModel.userId
        ) { _, _ -> }
        binding.nestedScrollViewUser.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val vw = v.getChildAt(v.childCount - 1)
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

        page = 1 // Reset page*/

        return binding.root
    }

    private fun enableMenu() {
        menuItems = listOf(
            MenuItem(R.string.settings, R.drawable.ic_settings) {
                findNavController()
                    .navigate(UserFragmentDirections.actionUserFragmentToSettingsFragment())
            },
            MenuItem(R.string.information, R.drawable.ic_privacy) {
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

        if (args.username == null) {
            requireActivity().addMenuProvider(menuProvider)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (args.username == null) {
            requireActivity().removeMenuProvider(menuProvider)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    protected fun loadCheckIns(username: String, page: Int = 1) {
        userStatusViewModel.loadStatusesForUser(username, page)
    }

    protected fun loadUser(username: String) {
        userStatusViewModel.loadUser(username)
    }

    /*protected fun loadCheckIns() {
        binding.swipeRefreshDashboardCheckIns.isRefreshing = true
        viewModel.getPersonalCheckIns(
            page,
            { statusPage ->
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
                val adapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                if (page == 1) {
                    adapter.clearAndAddCheckIns(statusPage.data)
                } else {
                    adapter.concatCheckIns(statusPage.data)
                }
            },
            {
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
            }
        )
    }*/









    /*override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadCheckIns(args.userName)
        loadUser(args.userName)
        viewModel.loadUser(args.userName) {
            binding.isOwnProfile =
                (loggedInUserViewModel.user.value?.id ?: -1) == (it.data.id)
        }
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val adapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
        adapter.setOnStationNameClickedListener { stationName, date -> findNavController()
            .navigate(
                UserFragmentDirections.actionUserProfileFragmentToSearchConnectionFragment(
                    stationName,
                    date
                )
            ) }

        return view
    }*/
}

class MenuItem(
    val title: Int,
    val drawable: Int,
    val action: () -> Unit
)
