package de.traewelling.ui.checkIn

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.traewelling.R
import de.traewelling.databinding.FragmentCheckInBinding


class CheckInFragment : Fragment() {

    private lateinit var binding: FragmentCheckInBinding
    private val args: CheckInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckInBinding.inflate(inflater, container, false)
        binding.layoutCheckIn.transitionName = args.transitionName
        binding.line = "RE 75"
        binding.destination = "Kempten(Allgäu)Hbf"
        binding.checkInFragment = this
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.design_default_color_surface, requireContext().theme))
        }
        return binding.root
    }

    fun checkIn() {
        Toast.makeText(requireContext(), binding.editTextStatusMessage.text.toString(), Toast.LENGTH_SHORT).show()
        findNavController().navigate(CheckInFragmentDirections.actionCheckInFragmentToDashboardFragment())
    }
}