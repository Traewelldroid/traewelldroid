package de.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.traewelling.R
import de.traewelling.api.models.trip.HafasTrip
import de.traewelling.databinding.ConnectionListItemBinding
import de.traewelling.models.Connection

class ConnectionAdapter(val connections: List<HafasTrip>, val onItemClick: (View, HafasTrip) -> Unit) : RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ConnectionListItemBinding.inflate(inflater, parent, false)
        return ConnectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        holder.bind(connections[position])
        holder.itemView.setOnClickListener {
            onItemClick(it, connections[position])
        }
    }

    override fun getItemCount() = connections.size

    class ConnectionViewHolder(val binding: ConnectionListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: HafasTrip) {

            binding.textViewDepartureTime.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    when(trip.departure > trip.plannedDeparture) {
                        true -> R.color.train_delayed
                        false -> R.color.train_on_time
                    }
                )
            )

            binding.layoutConnectionListItem.transitionName = "${trip.tripId}"
            binding.connection = trip
            binding.executePendingBindings()
        }
    }
}