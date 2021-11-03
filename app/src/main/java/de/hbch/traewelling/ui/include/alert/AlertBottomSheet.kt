package de.hbch.traewelling.ui.include.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.databinding.BottomSheetAlertBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlertBottomSheet(
    private val alertType: AlertType,
    private val alertText: String,
    private val dismissTimeInMillis: Long = 0L
    ): BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AlertBottomSheet"
    }

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

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        if (dismissTimeInMillis > 0) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(dismissTimeInMillis)
                dismiss()
            }
        }
    }
}

enum class AlertType {
    SUCCESS,
    ERROR
}