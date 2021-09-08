package de.traewelling.adapters

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import de.traewelling.api.models.status.Status
import de.traewelling.databinding.CardCheckinOverviewBinding
import de.traewelling.models.CheckIn

class CheckInAdapter(val checkIns: List<Status>) : RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardCheckinOverviewBinding.inflate(inflater, parent, false)
        return CheckInViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return checkIns.size
    }

    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {
        holder.bind(checkIns[position])
    }

    class CheckInViewHolder(val binding: CardCheckinOverviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(checkIn: Status) {
            binding.checkIn = checkIn
            if (checkIn.body == null || checkIn.body == "")
                binding.nextStation.textSize = 0F
            binding.executePendingBindings()
        }
    }
}