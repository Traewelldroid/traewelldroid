package de.traewelling.ui.checkIn

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.traewelling.R
import de.traewelling.databinding.FragmentCheckInBinding
import de.traewelling.shared.CheckInViewModel


class CheckInFragment : Fragment() {

    private lateinit var binding: FragmentCheckInBinding
    private val args: CheckInFragmentArgs by navArgs()
    private val checkInViewModel: CheckInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckInBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            layoutCheckIn.transitionName = args.transitionName
            destination = args.destination
            viewModel = checkInViewModel
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            val color = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.windowBackground, color, true)
            if (color.type >= TypedValue.TYPE_FIRST_COLOR_INT && color.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                setAllContainerColors(color.data)
            }
        }

        checkInViewModel.checkInResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Toast.makeText(requireContext(), "Check-In successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(CheckInFragmentDirections.actionCheckInFragmentToDashboardFragment())
            }
        }
        return binding.root
    }
}