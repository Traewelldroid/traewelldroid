package de.hbch.traewelling.util

import android.util.Log
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.getStopovers
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun List<Status>.fetchStopOvers(callback: (List<Status>) -> Unit) {
    val statusIds = map { it.journey.tripId }
    TraewellingApi.checkInService.getStopovers(*statusIds.toIntArray())
        .enqueue(object : Callback<Data<Map<Int, List<HafasTrainTripStation>>>> {
            override fun onResponse(
                call: Call<Data<Map<Int, List<HafasTrainTripStation>>>>,
                response: Response<Data<Map<Int, List<HafasTrainTripStation>>>>
            ) {
                val stopoversById = response.body()!!.data
                val statusesWithStopovers = map {
                    val journeyWithStopovers =
                        it.journey.copy(stopovers = stopoversById[it.journey.tripId])

                    it.copy(journey = journeyWithStopovers)
                }

                callback(statusesWithStopovers)
            }

            override fun onFailure(
                call: Call<Data<Map<Int, List<HafasTrainTripStation>>>>,
                t: Throwable
            ) {
                Log.w("TRLW", "Could not load stopovers", t)
                Sentry.captureException(t)
                callback(this@fetchStopOvers)
            }
        })
}
