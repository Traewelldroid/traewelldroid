package de.hbch.traewelling.ui.statistics

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import de.hbch.traewelling.databinding.FragmentStatisticsBinding
import java.util.*
import de.hbch.traewelling.R
import de.hbch.traewelling.util.TravelTimeValueFormatter
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.util.CheckInCountValueFormatter


class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: FragmentStatisticsBinding
    private val chart: BarChart get() = binding.chart
    private val dataSetColors = mutableListOf<Int>()
    private var statistics: PersonalStatistics? = null

    init {
        dataSetColors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
        dataSetColors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
        dataSetColors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        binding.apply {
            viewModel = this@StatisticsFragment.viewModel
            statisticsFragment = this@StatisticsFragment
            lifecycleOwner = this@StatisticsFragment.viewLifecycleOwner
        }

        initChart(chart)

        viewModel.dateRange.observe(viewLifecycleOwner) {
            requestAndDisplayStatistics()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.chipGroupStatisticSelectionData.setOnCheckedStateChangeListener { _, _ ->
            displayStatistics()
        }
        binding.chipGroupStatisticSelectionType.setOnCheckedStateChangeListener { _, _ ->
            displayStatistics()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    fun selectDateRange() {
        val range = viewModel.dateRange.value ?: Pair(Date(), Date())

        val picker = MaterialDatePicker
            .Builder
            .dateRangePicker()
            .setSelection(
                androidx.core.util.Pair(range.first.time, range.second.time)
            )
            .setTitleText(R.string.title_select_statistics_date_range)
            .build()

        picker.addOnPositiveButtonClickListener { dateRange ->
            val startCalendar =  GregorianCalendar()
            startCalendar.time = Date(dateRange.first)
            val endCalendar = GregorianCalendar()
            endCalendar.time = Date(dateRange.second)

            startCalendar.set(Calendar.HOUR_OF_DAY, 0)
            startCalendar.set(Calendar.MINUTE, 0)
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)

            viewModel
                .dateRange
                .postValue(
                    Pair(
                        startCalendar.time,
                        endCalendar.time
                    )
                )
        }

        picker.show(childFragmentManager, "DateRangePicker")
    }

    private fun initChart(chart: BarChart) {
        chart.axisLeft.setDrawGridLines(false)
        chart.legend.isWordWrapEnabled = true
        chart.description.isEnabled = false
        chart.animateY(500)
        chart.setDrawGridBackground(false)
        chart.xAxis.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setScaleEnabled(false)

        if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            chart.legend.textColor = Color.WHITE
            chart.xAxis.textColor = Color.WHITE
            chart.axisLeft.textColor = Color.WHITE
            chart.axisRight.textColor = Color.WHITE
        }
    }

    private fun requestAndDisplayStatistics() {
        viewModel.getPersonalStatisticsForSelectedTimeRange(
            { stats ->
                statistics = stats
                displayStatistics()
            },
            {}
        )
    }

    private fun displayStatistics() {
        val currentStats = statistics
        if (currentStats == null) {
            requestAndDisplayStatistics()
            return
        }

        val entrySet = mutableListOf<IBarDataSet>()
        val travelTimeValueFormatter = TravelTimeValueFormatter(resources)
        val checkInCountValueFormatter = CheckInCountValueFormatter()

        // TODO refactor with inheritance
        if (binding.chipStatisticsOperators.isChecked) {
            if (binding.chipStatisticsCheckIns.isChecked) {
                currentStats.operators.forEachIndexed { index, operatorStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), operatorStats.checkInCount.toFloat())), operatorStats.operatorName ?: getString(R.string.other_operators))
                    dataSet.valueFormatter = checkInCountValueFormatter
                    entrySet.add(dataSet)
                }
            } else if (binding.chipStatisticsTravelTime.isChecked) {
                currentStats.operators.forEachIndexed { index, operatorStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), operatorStats.duration.toFloat())), operatorStats.operatorName ?: getString(R.string.other_operators))
                    dataSet.valueFormatter = travelTimeValueFormatter
                    entrySet.add(dataSet)
                }
            }
        } else if (binding.chipStatisticsTravelTypes.isChecked) {
            if (binding.chipStatisticsCheckIns.isChecked) {
                currentStats.categories.forEachIndexed { index, categoryStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), categoryStats.checkInCount.toFloat())), ProductType.toString(resources, categoryStats.productType))
                    dataSet.valueFormatter = checkInCountValueFormatter
                    entrySet.add(dataSet)
                }
            } else if (binding.chipStatisticsTravelTime.isChecked) {
                currentStats.categories.forEachIndexed { index, categoryStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), categoryStats.duration.toFloat())), ProductType.toString(resources, categoryStats.productType))
                    dataSet.valueFormatter = travelTimeValueFormatter
                    entrySet.add(dataSet)
                }
            }
        } else if (binding.chipStatisticsBusinesses.isChecked) {
            if (binding.chipStatisticsCheckIns.isChecked) {
                currentStats.purposes.forEachIndexed { index, purposeStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), purposeStats.checkInCount.toFloat())), StatusBusiness.toString(resources, purposeStats.businessType))
                    dataSet.valueFormatter = checkInCountValueFormatter
                    entrySet.add(dataSet)
                }
            } else if (binding.chipStatisticsTravelTime.isChecked) {
                currentStats.purposes.forEachIndexed { index, purposeStats ->
                    val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), purposeStats.duration.toFloat())), StatusBusiness.toString(resources, purposeStats.businessType))
                    dataSet.valueFormatter = travelTimeValueFormatter
                    entrySet.add(dataSet)
                }
            }
        }

        entrySet.forEachIndexed { index, dataSet ->
            dataSet as BarDataSet
            dataSet.color = dataSetColors[index % dataSetColors.size]
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                dataSet.valueTextColor = Color.WHITE
        }

        val chartData = BarData(entrySet)
        chart.data = chartData
        chart.animateY(500)
        chart.invalidate()
        chart.notifyDataSetChanged()

        chart.setDrawGridBackground(false)
    }
}