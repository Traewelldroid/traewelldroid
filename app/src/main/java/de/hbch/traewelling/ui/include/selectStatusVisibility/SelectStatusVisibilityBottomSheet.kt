package de.hbch.traewelling.ui.include.selectStatusVisibility

import StandardListItemAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hbch.traewelling.R
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
                bdg.imageId = when (visibility) {
                    StatusVisibility.PUBLIC -> R.drawable.ic_public
                    StatusVisibility.UNLISTED -> R.drawable.ic_lock_open
                    StatusVisibility.FOLLOWERS -> R.drawable.ic_people
                    StatusVisibility.PRIVATE -> R.drawable.ic_lock
                }
                bdg.title = getString(when (visibility) {
                    StatusVisibility.PUBLIC -> R.string.visibility_public
                    StatusVisibility.UNLISTED -> R.string.visibility_unlisted
                    StatusVisibility.FOLLOWERS -> R.string.visibility_followers
                    StatusVisibility.PRIVATE -> R.string.visibility_private
                })
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