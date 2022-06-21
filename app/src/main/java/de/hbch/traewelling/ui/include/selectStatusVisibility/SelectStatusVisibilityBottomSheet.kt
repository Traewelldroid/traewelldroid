package de.hbch.traewelling.ui.include.selectStatusVisibility

import StandardListItemAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.getStatusVisibilityImageResource
import de.hbch.traewelling.adapters.getStatusVisibilityTextResource
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.databinding.BottomSheetSelectStatusVisibilityBinding

class SelectStatusVisibilityBottomSheet(
    private val callback: (StatusVisibility) -> Unit
): BottomSheetDialogFragment()  {

    companion object {
        const val TAG = "SelectStatusVisibilityBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetSelectStatusVisibilityBinding.inflate(inflater, container, false)

        binding.recyclerViewStatusVisibilities.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewStatusVisibilities.adapter = StandardListItemAdapter(
            StatusVisibility.values().asList(),
            { visibility, bdg ->
                bdg.imageId = getStatusVisibilityImageResource(visibility)
                bdg.title = getString(getStatusVisibilityTextResource(visibility))
            }, { visibility ->
                onSelectVisibility(visibility)
            }
        )

        return binding.root
    }

    private fun onSelectVisibility(visibility: StatusVisibility) {
        callback(visibility)
        dismiss()
    }
}