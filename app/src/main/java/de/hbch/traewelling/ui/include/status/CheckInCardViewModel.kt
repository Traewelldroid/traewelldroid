package de.hbch.traewelling.ui.include.status

import android.util.Log
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInCardViewModel : ViewModel() {
    fun createFavorite(statusId: Int, successCallback: () -> Unit) {
        TraewellingApi.checkInService.createFavorite(statusId)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        successCallback()
                    }
                    else
                        Log.e("StatusCardViewModel", response.toString())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("StatusCardViewModel", t.stackTraceToString())
                }
            })
    }

    fun deleteFavorite(statusId: Int, successCallback: () -> Unit) {
        TraewellingApi.checkInService.deleteFavorite(statusId)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        successCallback()
                    }
                    else
                        Log.e("StatusCardViewModel", response.toString())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("StatusCardViewModel", t.stackTraceToString())
                }
            })
    }
}