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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
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
                                },
                                handleEditClicked = { statusValue ->
                                    findNavController().navigate(
                                        R.id.editStatusFragment,
                                        bundleOf(
                                            "transitionName" to statusValue.origin,
                                            "destination" to statusValue.destination,
                                            "body" to statusValue.message,
                                            "departureTime" to statusValue.departurePlanned,
                                            "business" to statusValue.business.ordinal,
                                            "visibility" to statusValue.visibility.ordinal,
                                            "line" to statusValue.line,
                                            "statusId" to statusValue.statusId,
                                            "tripId" to statusValue.hafasTripId,
                                            "startStationId" to statusValue.originId,
                                            "category" to statusValue.productType
                                        )
                                    )
                                },
                                handleDeleteClicked = { statusValue ->
                                    val bottomSheet = DeleteStatusBottomSheet { bottomSheet ->
                                    bottomSheet.dismiss()
                                    checkInCardViewModel.deleteStatus(statusValue.statusId, {
                                        userStatusViewModel.checkIns.removeIf { it.id == statusValue.statusId }
                                        val alertBottomSheet = AlertBottomSheet(
                                            AlertType.SUCCESS,
                                            requireContext().resources.getString(R.string.status_delete_success),
                                            3000
                                        )
                                        alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
                                    }, {
                                        val alertBottomSheet = AlertBottomSheet(
                                            AlertType.ERROR,
                                            requireContext().resources.getString(R.string.status_delete_failure),
                                            3000
                                        )
                                        alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
                                    })
                                }
                                    if (context is FragmentActivity) {
                                        bottomSheet.show(parentFragmentManager, DeleteStatusBottomSheet.TAG)
                                    }
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
}

class MenuItem(
    val title: Int,
    val drawable: Int,
    val action: () -> Unit
)
