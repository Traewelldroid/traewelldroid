package de.hbch.traewelling.ui.checkIn

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentCheckInBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.checkInSuccessful.CheckInSuccessfulBottomSheet
import kotlinx.coroutines.*


class CheckInFragment : Fragment() {

    private lateinit var binding: FragmentCheckInBinding
    private val args: CheckInFragmentArgs by navArgs()
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckInBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            layoutCheckIn.transitionName = args.transitionName
            destination = args.destination
            viewModel = checkInViewModel
            btnSendToot.visibility =
                when (loggedInUserViewModel.loggedInUser.value?.mastodonUrl != null) {
                    true -> VISIBLE
                    false -> GONE
                }
            btnSendTweet.visibility =
                when (loggedInUserViewModel.loggedInUser.value?.twitterUrl != null) {
                    true -> VISIBLE
                    false -> GONE
                }
            toggleGroupSocialMedia.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_send_toot -> checkInViewModel.toot.value = isChecked
                    R.id.btn_send_tweet -> checkInViewModel.tweet.value = isChecked
                }
            }
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            val color = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.windowBackground, color, true)
            if (color.type >= TypedValue.TYPE_FIRST_COLOR_INT && color.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                setAllContainerColors(color.data)
            }
        }

        checkInViewModel.checkInResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                val checkInSuccessfulBottomSheet = CheckInSuccessfulBottomSheet(response)
                checkInSuccessfulBottomSheet.show(parentFragmentManager, CheckInSuccessfulBottomSheet.TAG)
                GlobalScope.launch(Dispatchers.Main) {
                    findNavController().navigate(CheckInFragmentDirections.actionCheckInFragmentToDashboardFragment())
                    delay(3000)
                    checkInSuccessfulBottomSheet.dismiss()
                }
            }
        }
        return binding.root
    }
}