package de.hbch.traewelling.ui.include.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.databinding.BottomSheetAlertBinding

class AlertBottomSheet(
    private val alertType: String,
    private val alertText: String
    ): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetAlertBinding.inflate(inflater, container, false)

        binding.apply {
            alertText = this@AlertBottomSheet.alertText
            alertType = this@AlertBottomSheet.alertType
        }

        return binding.root
    }
}