package de.hbch.traewelling.ui.checkIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.status.UpdateStatusRequest
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditCheckInFragment : AbstractCheckInFragment() {
    private val args: EditCheckInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val response = super.onCreateView(inflater, container, savedInstanceState)
        binding.apply {

            checkInCard.setContent {
                MainTheme {
                    CheckIn(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        checkInViewModel = checkInViewModel,
                        checkInAction = {
                            submit()
                        },
                        isEditMode = true,
                        eventViewModel = eventViewModel,
                        changeDestinationAction = {
                            onChangeDestination()
                        }
                    )
                }
            }

            checkInViewModel.lineName = args.line
            checkInViewModel.message.postValue(args.body)
            checkInViewModel.statusVisibility.postValue(enumValues<StatusVisibility>()[args.visibility])
            checkInViewModel.statusBusiness.postValue(enumValues<StatusBusiness>()[args.business])
            checkInViewModel.destination = args.destination
        }

        return response
    }


    override fun onChangeDestination() {
        findNavController().navigate(
            EditCheckInFragmentDirections.actionEditStatusFragmentToUpdateDestinationFragment(
                args.transitionName,
                args.departureTime,
                args.destination,
                args.statusId,
                args.body,
                args.visibility,
                args.business,
                args.tripId,
                args.line,
                args.startStationId
            )
        )
    }

    override fun submit() {
        val model = checkInViewModel
        val callback = object : Callback<Data<Status>> {
            override fun onResponse(call: Call<Data<Status>>, response: Response<Data<Status>>) {
                val body = response.body()
                if (body != null) {
                    findNavController().navigate(
                        EditCheckInFragmentDirections.actionEditStatusFragmentToStatusDetailFragment(
                            body.data.id,
                            body.data.userId
                        )
                    )
                    checkInViewModel.reset()
                }
            }

            override fun onFailure(call: Call<Data<Status>>, t: Throwable) {
                val alertBottomSheet = AlertBottomSheet(
                    AlertType.ERROR,
                    requireContext().getString(R.string.check_in_failure),
                    3000
                )
                alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
            }
        }
        if (!args.changeDestination) {
            TraewellingApi.checkInService.updateCheckIn(
                args.statusId, UpdateStatusRequest(
                    model.message.value,
                    model.statusBusiness.value ?: error("Invalid data"),
                    model.statusVisibility.value ?: error("Invalid data")
                )
            ).enqueue(callback)
        } else {
            checkInViewModel.startStationId = args.startStationId
            checkInViewModel.departureTime = args.departureTime
            TraewellingApi.checkInService.updateCheckIn(
                args.statusId, UpdateStatusRequest(
                    model.message.value,
                    model.statusBusiness.value ?: error("Invalid data"),
                    model.statusVisibility.value ?: error("Invalid data"),
                    args.destinationId,
                    args.departureTime
                )
            ).enqueue(callback)
        }
    }
}