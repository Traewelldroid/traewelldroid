package de.hbch.traewelling.ui.statistics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import de.hbch.traewelling.databinding.FragmentStatisticsBinding
import de.hbch.traewelling.theme.MainTheme


class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: FragmentStatisticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@StatisticsFragment.viewLifecycleOwner
            statisticsContent.setContent {
                MainTheme {
                    Statistics(
                        modifier = Modifier.padding(16.dp),
                        statisticsViewModel = viewModel
                    )
                }
            }
        }

        return binding.root
    }
}
