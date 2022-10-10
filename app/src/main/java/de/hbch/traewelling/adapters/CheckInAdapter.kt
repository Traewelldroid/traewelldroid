package de.hbch.traewelling.adapters

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.ui.include.status.CardCheckInOverview
import de.hbch.traewelling.ui.include.status.StatusCardViewModel
import de.hbch.traewelling.util.StationNameClickListener
import java.util.*

class CheckInAdapter(
    val checkIns: MutableList<Status>,
    private val loggedInUserId: LiveData<Int>,
    private val onStationNameClickedListener: StationNameClickListener
    )
    : RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder {
        val checkInCard = CardCheckInOverview(
            parent.context,
            null,
            this
        )
        checkInCard.binding.lifecycleOwner = parent.findViewTreeLifecycleOwner()
        checkInCard.setOnStationNameClickedListener(onStationNameClickedListener)
        return CheckInViewHolder(
            checkInCard,
            loggedInUserId
        )
    }

    override fun getItemCount(): Int {
        return checkIns.size
    }

    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {
        holder.bind(checkIns[position])
    }

    fun clearAndAddCheckIns(statuses: List<Status>) {
        notifyItemRangeRemoved(0, itemCount)
        checkIns.clear()
        checkIns.addAll(statuses)
        notifyItemRangeInserted(0, itemCount)
    }

    fun concatCheckIns(statuses: List<Status>) {
        val itemCountBeforeAdding = itemCount
        checkIns.addAll(statuses)
        notifyItemRangeInserted(itemCountBeforeAdding, statuses.size)
    }

    class CheckInViewHolder(
        private val checkInCard: CardCheckInOverview,
        private val loggedInUserId: LiveData<Int>
    ) : RecyclerView.ViewHolder(checkInCard.binding.root) {
        fun bind(checkIn: Status) {
            val binding = checkInCard.binding
            binding.checkInCard = checkInCard
            binding.checkIn = checkIn
            binding.viewModel = StatusCardViewModel(
                checkIn
            )
            loggedInUserId.observe(binding.lifecycleOwner!!) {
                binding.viewModel!!.isOwnStatus.postValue(it == checkIn.userId)
            }
            if (checkIn.body.isNullOrBlank()) {
                binding.checkInBody.visibility = View.GONE
                binding.iconCheckInBody.visibility = View.GONE
            }
            if (checkIn.journey.destination.arrivalSave.before(Date()))
                binding.nextStation.visibility = View.GONE
            binding.executePendingBindings()
        }
    }
}