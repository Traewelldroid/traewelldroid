package de.hbch.traewelling.ui.user

import android.os.Bundle
import android.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.UserViewModel

abstract class AbstractUserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    protected abstract val viewModel: UserViewModel
    private var page = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AbstractUserFragment.viewModel
        }
        binding.recyclerViewCheckIn.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCheckIn.adapter = CheckInAdapter(
            mutableListOf(),
            viewModel.userId
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

        viewModel.profilePictureSrc.observe(viewLifecycleOwner) { src ->
            binding.imageProfile.load(src) {
                crossfade(true)
                placeholder(R.drawable.ic_new_user)
                transformations(CircleCropTransformation())
            }
        }

        loadCheckIns()

        return binding.root
    }

    protected fun loadCheckIns() {
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
    }
}
