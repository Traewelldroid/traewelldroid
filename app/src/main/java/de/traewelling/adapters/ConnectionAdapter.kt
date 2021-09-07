package de.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.traewelling.databinding.ConnectionListItemBinding
import de.traewelling.models.Connection

class ConnectionAdapter(val connections: List<Connection>, val onItemClick: (View, Connection) -> Unit) : RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder>() {

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
        fun bind(connection: Connection) {
            binding.layoutConnectionListItem.transitionName = "${connection.line}${connection.destination}${connection.departureTime}"
            binding.connection = connection
            binding.executePendingBindings()
        }
    }
}