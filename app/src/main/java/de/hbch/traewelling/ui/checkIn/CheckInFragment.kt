package de.hbch.traewelling.ui.checkIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.PointReason
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.checkInSuccessful.CheckInSuccessfulBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CheckInFragment : AbstractCheckInFragment() {

    private val args: CheckInFragmentArgs by navArgs()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val response = super.onCreateView(inflater, container, savedInstanceState)
        binding.apply {
            layoutCheckIn.transitionName = args.transitionName
            destination = args.destination
            binding.viewModel!!.statusVisibility.postValue(loggedInUserViewModel.defaultStatusVisibility)
        }
        return response
    }

    override fun submit() {
        checkInViewModel.checkIn({ response ->
            if (response != null) {
                val checkInSuccessfulBottomSheet = CheckInSuccessfulBottomSheet(response)
                checkInSuccessfulBottomSheet.show(
                    parentFragmentManager,
                    CheckInSuccessfulBottomSheet.TAG
                )
                CoroutineScope(Dispatchers.Main).launch {
                    findNavController().navigate(CheckInFragmentDirections.actionCheckInFragmentToDashboardFragment())
                    val dismissTime =
                        if (response.points.calculation.reason == PointReason.IN_TIME) 3000L else 7000L
                    delay(dismissTime)
                    checkInSuccessfulBottomSheet.dismiss()
                }
                checkInViewModel.reset()
            }
        }, { statusCode ->
            val alertBottomSheet = AlertBottomSheet(
                AlertType.ERROR,
                requireContext().getString(
                    when (statusCode) {
                        409 -> R.string.check_in_conflict
                        else -> R.string.check_in_failure
                    }
                ),
                3000
            )
            alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
        })
    }
}