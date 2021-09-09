package de.traewelling.ui.include.status

import android.util.Log
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.status.Status
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusCardViewModel : ViewModel() {

    fun handleFavoriteClick(status: Status) {
        if (status.liked)
            deleteFavorite(status)
        else
            createFavorite(status)
    }

    private fun createFavorite(status: Status) {
        TraewellingApi.checkInService.createFavorite(status.id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful)
                        status.liked = true
                    else
                        Log.e("StatusCardViewModel", response.toString())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("StatusCardViewModel", t.stackTraceToString())
                }
            })
    }

    private fun deleteFavorite(status: Status) {
        TraewellingApi.checkInService.deleteFavorite(status.id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful)
                        status.liked = false
                    else
                        Log.e("StatusCardViewModel", response.toString())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("StatusCardViewModel", t.stackTraceToString())
                }
            })
    }
}