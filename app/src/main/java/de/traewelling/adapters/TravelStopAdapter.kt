package de.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.traewelling.R
import de.traewelling.api.models.trip.HafasTrainTripStation
import de.traewelling.databinding.TravelStopListItemBinding
import de.traewelling.models.TravelStop

class TravelStopAdapter(val stops: List<HafasTrainTripStation>, val onClick: (View, HafasTrainTripStation) -> Unit)
    : RecyclerView.Adapter<TravelStopAdapter.TravelStopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelStopViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TravelStopListItemBinding.inflate(inflater, parent, false)
        return TravelStopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TravelStopViewHolder, position: Int) {
        holder.bind(stops[position], position == (itemCount - 1))
        holder.itemView.setOnClickListener {
            onClick(it, stops[position])
        }
        holder.itemView.transitionName = stops[position].name
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