package de.traewelling.ui.selectDestination

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.traewelling.databinding.FragmentSelectDestinationBinding

class SelectDestinationFragment : Fragment() {

    private lateinit var binding: FragmentSelectDestinationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }
}