package de.hbch.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.HafasTrip
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.ConnectionListItemBinding
import de.hbch.traewelling.databinding.FragmentSearchConnectionBinding
import java.util.*
import java.util.concurrent.TimeUnit

class ConnectionAdapter(
    private val connections: MutableList<HafasTrip>,
    val searchBinding: FragmentSearchConnectionBinding? = null,
    val onItemClick: (View, HafasTrip) -> Unit
) : RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder>() {

    private val connectionsFiltered: MutableList<HafasTrip> = mutableListOf()
    private var filter: ProductType? = null

    init {
        connectionsFiltered.addAll(connections)
    }

    fun addNewConnections(newConnections: List<HafasTrip>) {
        connections.clear()
        connections.addAll(newConnections)
        applyFilter(filter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ConnectionListItemBinding.inflate(inflater, parent, false)
        return ConnectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        val connection = connectionsFiltered[position]
        holder.bind(connection)
        if (connection.isCancelled)
            holder.itemView.setOnClickListener { }
        else
            holder.itemView.setOnClickListener {
                onItemClick(it, connection)
            }
    }

    override fun getItemCount() = connectionsFiltered.size

    inner class ConnectionViewHolder(val binding: ConnectionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: HafasTrip) {

            val difference = TimeUnit.MILLISECONDS.toMinutes(
                (trip.departure ?: Date()).time - (trip.plannedDeparture ?: Date()).time
            )

            binding.textViewDepartureTime.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    when {
                        difference <= 0 -> R.color.train_on_time
                        difference in 0..5 -> R.color.warning
                        else -> R.color.train_delayed
                    }
                )
            )

            binding.layoutConnectionListItem.transitionName = trip.tripId
            binding.connection = trip
            if (!trip.station?.name.isNullOrBlank() && searchBinding?.stationId != null && trip.station?.id != searchBinding.stationId) {
                binding.textViewOrigin.visibility = View.VISIBLE
            }
            binding.executePendingBindings()
        }
    }

    fun applyFilter(products: ProductType?) {
        filter = products
        notifyItemRangeRemoved(0, connectionsFiltered.size)
        connectionsFiltered.clear()
        if (filter == null) {
            connectionsFiltered.addAll(connections)
        } else {
            connectionsFiltered.addAll(connections.filter { trip ->
                if (trip.line != null) {
                    if (filter == ProductType.LONG_DISTANCE)
                        trip.line.product == ProductType.NATIONAL ||
                                trip.line.product == ProductType.NATIONAL_EXPRESS
                    else if (filter == ProductType.REGIONAL)
                        trip.line.product == ProductType.REGIONAL ||
                                trip.line.product == ProductType.REGIONAL_EXPRESS
                    else
                        trip.line.product == filter
                } else
                    false
            })
        }
        notifyItemRangeInserted(0, connectionsFiltered.size)
    }
}