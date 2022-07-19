package de.hbch.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.databinding.TravelStopListItemBinding
import de.hbch.traewelling.models.TravelStop

class TravelStopAdapter(val stops: List<HafasTrainTripStation>, val onClick: (View, HafasTrainTripStation) -> Unit)
    : RecyclerView.Adapter<TravelStopAdapter.TravelStopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelStopViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TravelStopListItemBinding.inflate(inflater, parent, false)
        return TravelStopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TravelStopViewHolder, position: Int) {
        val stop = stops[position]
        holder.bind(stop, position == (itemCount - 1))
        holder.itemView.transitionName = stop.name

        if (!stop.isCancelled)
            holder.itemView.setOnClickListener {
                onClick(it, stop)
            }
    }

    override fun getItemCount() = stops.size

    class TravelStopViewHolder(val binding: TravelStopListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(travelStop: HafasTrainTripStation, isLast: Boolean) {
            if (isLast)
                binding.perlschnurConnectionBottom.visibility = INVISIBLE
            binding.travelStop = travelStop
        }
    }
}