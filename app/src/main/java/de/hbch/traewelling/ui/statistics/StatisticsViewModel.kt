package de.hbch.traewelling.ui.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.events.UnauthorizedEvent
import io.sentry.Sentry
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class StatisticsViewModel : ViewModel() {

    val dateRange = MutableLiveData<Pair<Date, Date>>()
    val statistics = MutableLiveData<PersonalStatistics?>()

    init {
        dateRange.postValue(initDateRange())
    }

    private fun initDateRange(): Pair<Date, Date> {
        val startCalendar = GregorianCalendar()
        startCalendar.time = Date()
        startCalendar.set(Calendar.DATE, 1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)

        val endCalendar = GregorianCalendar()
        endCalendar.time = Date()
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)

        return Pair(startCalendar.time, endCalendar.time)
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
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }

                override fun onFailure(call: Call<Data<PersonalStatistics>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}