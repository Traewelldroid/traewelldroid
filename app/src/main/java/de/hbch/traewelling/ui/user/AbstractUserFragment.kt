package de.hbch.traewelling.ui.user

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.UserViewModel
import de.hbch.traewelling.theme.MainTheme

abstract class AbstractUserFragment : Fragment() {

    protected lateinit var binding: FragmentUserBinding
    protected abstract val viewModel: UserViewModel
    protected val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    //private var page = 1
    protected val userStatusViewModel: UserStatusViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AbstractUserFragment.viewModel
            userContent.setContent {
                MainTheme {
                    val refreshing by userStatusViewModel.isRefreshing.observeAsState(false)
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = {

                        })
                    Box(
                        modifier = Modifier.pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn {
                            item {
                                UserCard(
                                    userViewModel = this@AbstractUserFragment.viewModel,
                                    loggedInUserViewModel = loggedInUserViewModel
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
        }
        binding.recyclerViewCheckIn.layoutManager = LinearLayoutManager(requireContext())
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

        page = 1 // Reset page

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCheckIns()
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
}
