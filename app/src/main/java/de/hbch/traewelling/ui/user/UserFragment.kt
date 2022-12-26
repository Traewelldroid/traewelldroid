package de.hbch.traewelling.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.shared.UserViewModel

class UserFragment : AbstractUserFragment() {

    override val viewModel: UserViewModel by activityViewModels()
    private val args: UserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.loadUser(args.userName) {
            loadCheckIns()
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
    }
}
