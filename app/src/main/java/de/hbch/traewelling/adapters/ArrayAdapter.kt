package de.hbch.traewelling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class StringArrayAdapter(private val strings: List<String>) : RecyclerView.Adapter<StringArrayAdapter.StringArrayViewHolder>() {

    class StringArrayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringArrayViewHolder {
        return StringArrayViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: StringArrayViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(android.R.id.text1).text = strings[position]
    }

    override fun getItemCount(): Int = strings.size
}