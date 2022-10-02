package de.hbch.traewelling.ui.checkIn

import android.os.Bundle
import android.view.*
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                    R.id.menu_also_check_in -> {
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
        TraewellingApi.checkInService.updateCheckIn(
            args.statusId, UpdateStatusRequest(
                model.message.value,
                model.statusBusiness.value ?: error("Invalid data"),
                model.statusVisibility.value ?: error("Invalid data")
            )
        ).enqueue(object : Callback<Data<Status>> {
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
        })
    }
}