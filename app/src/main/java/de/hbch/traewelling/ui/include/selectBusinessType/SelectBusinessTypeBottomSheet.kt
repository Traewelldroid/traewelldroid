package de.hbch.traewelling.ui.include.selectBusinessType

import StandardListItemAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.databinding.BottomSheetSelectBusinessTypeBinding

class SelectBusinessTypeBottomSheet(private val callback: (StatusBusiness) -> Unit): BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SelectBusinessTypeBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetSelectBusinessTypeBinding.inflate(inflater, container, false)

        binding.recyclerViewStatusBusinesses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewStatusBusinesses.adapter = StandardListItemAdapter(
            StatusBusiness.values().asList(), { business, binding ->
                binding.imageId = when (business) {
                    StatusBusiness.PRIVATE -> R.drawable.ic_person
                    StatusBusiness.BUSINESS -> R.drawable.ic_business
                    StatusBusiness.COMMUTE -> R.drawable.ic_commute
                }
                binding.title = getString(when(business) {
                    StatusBusiness.PRIVATE -> R.string.business_private
                    StatusBusiness.BUSINESS -> R.string.business
                    StatusBusiness.COMMUTE -> R.string.business_commute
                })
            }, { business ->
                callback(business)
                dismiss()
            }
        )

        return binding.root
    }
}