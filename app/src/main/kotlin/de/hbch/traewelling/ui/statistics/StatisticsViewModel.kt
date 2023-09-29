package de.hbch.traewelling.ui.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.statistics.DailyStatistics
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.events.UnauthorizedEvent
import de.hbch.traewelling.logging.Logger
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class StatisticsViewModel : ViewModel() {

    val dateRange = MutableLiveData<Pair<LocalDate, LocalDate>>()
    val statistics = MutableLiveData<PersonalStatistics?>()

    init {
        dateRange.postValue(initDateRange())
    }

    private fun initDateRange(): Pair<LocalDate, LocalDate> {
        val startDate = LocalDate.now().withDayOfMonth(1)
        val endDate = LocalDate.now()

        return Pair(startDate, endDate)
    }

    fun getPersonalStatisticsForSelectedTimeRange() {
        val range = dateRange.value ?: initDateRange()
        dateRange.postValue(range)

        val from = range.first
        val until = range.second

        TraewellingApi
            .statisticsService
            .getPersonalStatistics(from, until)
            .enqueue(object: Callback<Data<PersonalStatistics>> {
                override fun onResponse(
                    call: Call<Data<PersonalStatistics>>,
                    response: Response<Data<PersonalStatistics>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            statistics.postValue(data)
                            return
                        }
                    }
                    if (response.code() == 403) {
                        EventBus.getDefault().post(UnauthorizedEvent())
                    }
                }

                override fun onFailure(call: Call<Data<PersonalStatistics>>, t: Throwable) {
                    Logger.captureException(t)
                }
            })
    }

    fun getDailyStatistics(date: String, onSuccess: (DailyStatistics) -> Unit, onError: () -> Unit) {
        TraewellingApi
            .statisticsService
            .getDailyStatistics(date)
            .enqueue(object: Callback<Data<DailyStatistics>> {
                override fun onResponse(
                    call: Call<Data<DailyStatistics>>,
                    response: Response<Data<DailyStatistics>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            onSuccess(data)
                            return
                        }
                    }
                    onError()
                }

                override fun onFailure(call: Call<Data<DailyStatistics>>, t: Throwable) {
                    onError()
                    Logger.captureException(t)
                }
            })
    }
}
