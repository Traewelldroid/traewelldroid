package de.hbch.traewelling.ui.include.homelandStation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.databinding.BottomSheetHomelandStationBinding

class HomelandStationBottomSheet(val stationName: String): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetHomelandStationBinding.inflate(inflater, container, false)

        binding.stationName = stationName

        return binding.root
    }
}