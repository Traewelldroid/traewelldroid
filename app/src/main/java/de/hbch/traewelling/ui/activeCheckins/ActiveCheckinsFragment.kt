package de.hbch.traewelling.ui.activeCheckins

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentActiveCheckinsBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel

class ActiveCheckinsFragment : Fragment() {

    private lateinit var binding: FragmentActiveCheckinsBinding
    private val viewModel: ActiveCheckinsViewModel by viewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveCheckinsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val recyclerView = binding.recyclerViewActiveCheckIns
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CheckInAdapter(
            mutableListOf(),
            loggedInUserViewModel.userId
        ) { _, _ -> }

        getActiveCheckins()

        binding.swipeRefreshCheckins.setOnRefreshListener {
            getActiveCheckins()
        }

        return binding.root
    }

    fun getActiveCheckins() {
        binding.swipeRefreshCheckins.isRefreshing = true
        viewModel.getActiveCheckins(
            { statuses ->
                val checkInAdapter = binding.recyclerViewActiveCheckIns.adapter as CheckInAdapter
                checkInAdapter.clearAndAddCheckIns(statuses)
                binding.swipeRefreshCheckins.isRefreshing = false
            },
            {
                binding.swipeRefreshCheckins.isRefreshing = false
            }
        )
    }
}