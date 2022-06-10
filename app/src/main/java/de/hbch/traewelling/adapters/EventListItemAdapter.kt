package de.hbch.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.databinding.EventListItemBinding
import de.hbch.traewelling.databinding.TravelStopListItemBinding
import de.hbch.traewelling.models.TravelStop

class EventListItemAdapter(val events: List<Event>, val onClick: (Event) -> Unit)
    : RecyclerView.Adapter<EventListItemAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EventListItemBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
        holder.itemView.setOnClickListener {
            onClick(event)
        }
    }

    override fun getItemCount() = events.size

    class EventViewHolder(val binding: EventListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.event = event
        }
    }
}