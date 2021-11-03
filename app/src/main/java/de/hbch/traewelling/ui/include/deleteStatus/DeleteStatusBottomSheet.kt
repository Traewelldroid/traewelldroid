package de.hbch.traewelling.ui.include.deleteStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.databinding.BottomSheetDeleteStatusBinding

class DeleteStatusBottomSheet(
    private val onSubmitCallback: (DeleteStatusBottomSheet) -> Unit
): BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetDeleteStatusBinding.inflate(inflater, container, false)

        binding.bottomSheet = this

        return binding.root
    }

    fun deleteStatus() {
        onSubmitCallback(this)
    }
}