package de.hbch.traewelling.ui.checkIn

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.status.UpdateStatusRequest
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.checkInSuccessful.CheckInSuccessfulBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

class EditCheckInFragment : AbstractCheckInFragment() {
    private val args: EditCheckInFragmentArgs by navArgs()
    private lateinit var menuProvider: MenuProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val response = super.onCreateView(inflater, container, savedInstanceState)
        binding.apply {
            editMode = true
            layoutCheckIn.transitionName = args.transitionName
            destination = args.destination
            viewModel!!.message.postValue(args.body)
            viewModel!!.lineName = args.line
            viewModel!!.statusVisibility.postValue(enumValues<StatusVisibility>()[args.visibility])
            viewModel!!.statusBusiness.postValue(enumValues<StatusBusiness>()[args.business])
        }

        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.status_edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_change_destination -> {
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
                        true
                    }
                    else -> false
                }
            }
        }


        requireActivity().addMenuProvider(menuProvider)

        return response
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    override fun submit() {
        val model = binding.viewModel!!
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
        if (!args.replace) {
            TraewellingApi.checkInService.updateCheckIn(
                args.statusId, UpdateStatusRequest(
                    model.message.value,
                    model.statusBusiness.value ?: error("Invalid data"),
                    model.statusVisibility.value ?: error("Invalid data")
                )
            ).enqueue(callback)
        } else {
            if ((Date().time - args.departureTime.time) > TimeUnit.MINUTES.toMillis(20)) {
                val listener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> replace()
                        DialogInterface.BUTTON_NEGATIVE -> {
                            findNavController()
                                .navigate(
                                    EditCheckInFragmentDirections.actionEditStatusFragmentToDashboardFragment()
                                )
                        }
                        else -> error("Unexpected type: $which")
                    }
                }
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.confirm_check_in_replacement)
                    .setPositiveButton(R.string.ok, listener)
                    .setNegativeButton(R.string.abort, listener)
                    .show()

            } else {
                replace()
            }
        }
    }

    private fun replace() {
        checkInViewModel.startStationId = args.startStationId
        checkInViewModel.destinationStationId = args.destinationId
        checkInViewModel.departureTime = args.departureTime
        TraewellingApi.checkInService.deleteStatus(args.statusId)
            .enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    checkInViewModel.checkIn({ response ->
                        if (response != null) {
                            val checkInSuccessfulBottomSheet =
                                CheckInSuccessfulBottomSheet(response)
                            checkInSuccessfulBottomSheet.show(
                                parentFragmentManager,
                                CheckInSuccessfulBottomSheet.TAG
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                findNavController().navigate(
                                    EditCheckInFragmentDirections.actionEditStatusFragmentToStatusDetailFragment(
                                        response.status.id,
                                        response.status.userId
                                    )
                                )
                                delay(3000)
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

                override fun onFailure(call: Call<Any>, t: Throwable) {
                }
            })
    }
}