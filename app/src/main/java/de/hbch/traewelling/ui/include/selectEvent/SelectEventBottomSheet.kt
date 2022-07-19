package de.hbch.traewelling.ui.include.selectEvent

import StandardListItemAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.EventListItemAdapter
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.databinding.BottomSheetSelectBusinessTypeBinding
import de.hbch.traewelling.databinding.BottomSheetSelectEventBinding

class SelectEventBottomSheet(private val events: List<Event>, private val callback: (Event?) -> Unit) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "SelectEventBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetSelectEventBinding.inflate(inflater, container, false)

        binding.bottomSheet = this
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEvents.adapter = EventListItemAdapter(
            events
        ) { event ->
            callback(event)
            dismiss()
        }

        return binding.root
    }

    fun removeEvent() {
        callback(null)
        dismiss()
    }
}