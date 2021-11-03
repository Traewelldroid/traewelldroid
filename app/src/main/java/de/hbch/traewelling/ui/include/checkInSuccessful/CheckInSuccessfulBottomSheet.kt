package de.hbch.traewelling.ui.include.checkInSuccessful

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.adapters.StringArrayAdapter
import de.hbch.traewelling.api.models.status.CheckInResponse
import de.hbch.traewelling.databinding.BottomSheetCheckinSuccessfulBinding

class CheckInSuccessfulBottomSheet(private val checkInResponse: CheckInResponse) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "CheckInSuccessfulBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetCheckinSuccessfulBinding.inflate(inflater, container, false)
        binding.apply {
            checkIn = checkInResponse
            coTravellersLayout.visibility = when (checkInResponse.coTravellers.isNotEmpty()) {
                true -> {
                    recyclerViewCoTravellers.layoutManager = LinearLayoutManager(requireContext())
                    recyclerViewCoTravellers.adapter = StringArrayAdapter(
                        checkInResponse.coTravellers.map {
                        it.username
                    })
                    View.VISIBLE
                }
                false -> View.GONE
            }
        }
        return binding.root
    }
}