package de.hbch.traewelling.ui.activeCheckins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import de.hbch.traewelling.databinding.FragmentActiveCheckinsBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.status.CheckInListFragment

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.checkInList.getFragment<CheckInListFragment>().checkInListViewModel =
            viewModel
        super.onViewCreated(view, savedInstanceState)
    }
}