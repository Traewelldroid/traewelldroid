package de.hbch.traewelling.ui.notifications

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.notifications.NotificationPage
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsViewModel : ViewModel() {
    fun getUnreadNotificationCount(successfulCallback: (Int) -> Unit) {
        TraewellingApi
            .notificationService
            .getUnreadNotificationsCount()
            .enqueue(
                object: Callback<Data<Int>> {
                    override fun onResponse(call: Call<Data<Int>>, response: Response<Data<Int>>) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            if (data != null) {
                                successfulCallback(data.data)
                            }
                        }
                    }

                    override fun onFailure(call: Call<Data<Int>>, t: Throwable) {
                        Sentry.captureException(t)
                    }
                }
            )
    }

    fun getNotifications(page: Int, successfulCallback: (NotificationPage) -> Unit) {
        TraewellingApi
            .notificationService
            .getNotifications(page)
            .enqueue(object: Callback<NotificationPage> {
                override fun onResponse(
                    call: Call<NotificationPage>,
                    response: Response<NotificationPage>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) {
                            successfulCallback(data)
                        }
                    }
                }
                override fun onFailure(call: Call<NotificationPage>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}