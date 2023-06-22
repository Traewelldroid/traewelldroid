package de.hbch.traewelling.ui.activeCheckins

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentActiveCheckinsBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel

class ActiveCheckinsFragment : Fragment() {

    private lateinit var binding: FragmentActiveCheckinsBinding
    private val viewModel: ActiveCheckinsViewModel by viewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val checkInCardViewModel: CheckInCardViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActiveCheckinsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.activeCheckinsContent.setContent {
            MainTheme {
                val refreshing by viewModel.isRefreshing.observeAsState(false)
                val checkIns = remember { viewModel.checkIns }
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = {
                        viewModel.getActiveCheckins()
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = true
                    ) {
                        items(
                            items = checkIns
                        ) { status ->
                            CheckInCard(
                                checkInCardViewModel = checkInCardViewModel,
                                status = status.toStatusDto(),
                                loggedInUserViewModel = loggedInUserViewModel,
                                userSelected = { username ->
                                    findNavController()
                                        .navigate(
                                            ActiveCheckinsFragmentDirections
                                                .actionActiveCheckinsFragmentToUserProfileFragment(username)
                                        )
                                },
                                statusSelected = { statusId, userId ->
                                    findNavController()
                                        .navigate(
                                            ActiveCheckinsFragmentDirections
                                                .actionActiveCheckinsFragmentToStatusDetailFragment(
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
                                            viewModel.checkIns.removeIf { it.id == statusValue.statusId }
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
}