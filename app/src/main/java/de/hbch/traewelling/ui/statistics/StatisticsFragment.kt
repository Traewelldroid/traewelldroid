package de.hbch.traewelling.ui.statistics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import de.hbch.traewelling.databinding.FragmentStatisticsBinding
import java.util.*
import androidx.core.util.Pair
import de.hbch.traewelling.R


class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = this@StatisticsFragment.viewModel
            statisticsFragment = this@StatisticsFragment
            lifecycleOwner = this@StatisticsFragment.viewLifecycleOwner
        }

        return binding.root
    }

    fun selectDateRange() {
        val from = viewModel.fromDate.value ?: Date()
        val until = viewModel.untilDate.value ?: Date()

        val picker = MaterialDatePicker
            .Builder
            .dateRangePicker()
            .setSelection(
                Pair(from.time, until.time)
            )
            .setTitleText(R.string.title_select_statistics_date_range)
            .build()

        picker.addOnPositiveButtonClickListener { dateRange ->
            viewModel.fromDate.postValue(Date(dateRange.first))
            viewModel.untilDate.postValue(Date(dateRange.second))
        }

        picker.show(childFragmentManager, "DateRangePicker")
    }
}