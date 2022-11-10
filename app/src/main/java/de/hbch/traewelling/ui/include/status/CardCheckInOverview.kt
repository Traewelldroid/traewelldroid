package de.hbch.traewelling.ui.include.status

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.CardCheckinOverviewBinding
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
import de.hbch.traewelling.util.StationNameClickListener
import java.util.Date

class CardCheckInOverview(
    context: Context?,
    attrs: AttributeSet?,
    private val adapter: CheckInAdapter
) :
    MaterialCardView(context, attrs, 0) {

    val binding: CardCheckinOverviewBinding =
        CardCheckinOverviewBinding
            .inflate(LayoutInflater.from(context), this, false)

    private val fragmentManager get() = (context as FragmentActivity).supportFragmentManager
    private val navController get() = fragmentManager.primaryNavigationFragment?.findNavController()

    private var onStationNameClickedListener: StationNameClickListener = { _, _ -> }

    fun handleDeleteClicked() {
        val bottomSheet = DeleteStatusBottomSheet { bottomSheet ->
            bottomSheet.dismiss()
            binding.viewModel?.deleteStatus({
                adapter.notifyItemRemoved(
                    adapter.checkIns.indexOf(binding.checkIn)
                )
                adapter.checkIns.remove(binding.checkIn)
                val alertBottomSheet = AlertBottomSheet(
                    AlertType.SUCCESS,
                    context.resources.getString(R.string.status_delete_success),
                    3000
                )
                alertBottomSheet.show(fragmentManager, AlertBottomSheet.TAG)
            }, {
                val alertBottomSheet = AlertBottomSheet(
                    AlertType.ERROR,
                    context.resources.getString(R.string.status_delete_failure),
                    3000
                )
                alertBottomSheet.show(fragmentManager, AlertBottomSheet.TAG)
            })
        }
        if (context is FragmentActivity) {
            bottomSheet.show(fragmentManager, DeleteStatusBottomSheet.TAG)
        }
    }

    fun handleUserSelected() {
        navController?.navigate(
            R.id.userProfileFragment,
            bundleOf("userName" to binding.checkIn?.username)
        )
    }

    fun handleCheckInSelected() {
        navController?.navigate(
            R.id.statusDetailFragment,
            bundleOf(Pair("statusId", binding.checkIn?.id), Pair("userId", binding.checkIn?.userId))
        )
    }

    fun handleEditClicked() {
        navController?.navigate(
            R.id.editStatusFragment,
            bundleOf(
                "transitionName" to binding.checkIn?.journey?.origin?.name,
                "destination" to binding.checkIn?.journey?.destination?.name,
                "body" to binding.checkIn?.body,
                "departureTime" to binding.checkIn?.journey?.origin?.departurePlanned,
                "business" to binding.checkIn?.business?.ordinal,
                "visibility" to binding.checkIn?.visibility?.ordinal,
                "line" to binding.checkIn?.journey?.line,
                "statusId" to binding.checkIn?.id,
                "tripId" to binding.checkIn?.journey?.hafasTripId,
                "startStationId" to binding.checkIn?.journey?.origin?.id,
            )
        )
    }

    fun onStationNameClicked(stationName: String) =
        onStationNameClicked(stationName, null)

    fun onStationNameClicked(stationName: String, date: Date?) {
        onStationNameClickedListener(stationName, date)
    }

    fun setOnStationNameClickedListener(listener: StationNameClickListener) {
        onStationNameClickedListener = listener
    }
}