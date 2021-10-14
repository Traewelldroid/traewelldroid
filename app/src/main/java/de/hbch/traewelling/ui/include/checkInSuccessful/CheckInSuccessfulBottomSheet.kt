package de.hbch.traewelling.ui.include.checkInSuccessful

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.api.models.status.CheckInResponse
import de.hbch.traewelling.databinding.BottomSheetCheckinSuccessfulBinding

class CheckInSuccessfulBottomSheet(val checkInResponse: CheckInResponse) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "CheckInSuccessfulBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetCheckinSuccessfulBinding.inflate(inflater, container, false)
        binding.checkIn = checkInResponse
        return binding.root
    }
}