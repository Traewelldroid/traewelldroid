package de.traewelling.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.models.CheckIn

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        binding.testCheckIn.checkIn = CheckIn(
            "Memmingen",
            "München Hbf",
            "RE 72",
            "120 km",
            "1h 35min",
            "16:08",
            "Buchloe",
            "17:43",
            "gertrud",
            "16:04",
            true
        )

        return binding.root
    }
}