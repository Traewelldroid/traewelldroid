package de.hbch.traewelling.ui.include.status

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusCardViewModel(
    val status: Status
) : ViewModel() {

    private val _liked = MutableLiveData(status.liked)
    val liked: LiveData<Boolean?> get() = _liked

    private val _likes = MutableLiveData(status.likes)
    val likes: LiveData<Int?> get() = _likes

    val isOwnStatus = MutableLiveData(false)

    fun handleFavoriteClick() {
        if (liked.value!!)
            deleteFavorite()
        else
            createFavorite()
    }

    fun deleteStatus(
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.checkInService.deleteStatus(status.id)
            .enqueue(object: Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful)
                        successCallback()
                    else {
                        Sentry.captureMessage(response.errorBody().toString())
                        failureCallback()
                    }
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Sentry.captureException(t)
                    failureCallback()
                }
            })
    }

    private fun createFavorite() {
        TraewellingApi.checkInService.createFavorite(status.id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        _liked.postValue(true)
                        _likes.postValue((_likes.value ?: 0) + 1)
                        status.liked = true
                        status.likes = status.likes?.inc()
                    }
                    else
                        Log.e("StatusCardViewModel", response.toString())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("StatusCardViewModel", t.stackTraceToString())
                }
            })
    }

    private fun deleteFavorite() {
        TraewellingApi.checkInService.deleteFavorite(status.id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        _liked.postValue(false)
                        _likes.postValue((_likes.value ?: 0) - 1)
                        status.liked = false
                        status.likes = status.likes?.dec()
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