package de.hbch.traewelling.ui.user

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.CircleCropTransformation
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentUserBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.UserViewModel
import de.hbch.traewelling.ui.include.status.CheckInListFragment

abstract class AbstractUserFragment : Fragment() {

    protected lateinit var binding: FragmentUserBinding
    protected abstract val viewModel: UserViewModel
    protected val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
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

        viewModel.profilePictureSrc.observe(viewLifecycleOwner) { src ->
            binding.imageProfile.load(src) {
                crossfade(true)
                placeholder(R.drawable.ic_new_user)
                transformations(CircleCropTransformation())
            }
        }

        page = 1 // Reset page
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.checkInList.getFragment<CheckInListFragment>().checkInListViewModel =
            loggedInUserViewModel
        super.onViewCreated(view, savedInstanceState)
    }

}
