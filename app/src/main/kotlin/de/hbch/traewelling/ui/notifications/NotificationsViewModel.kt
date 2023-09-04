package de.hbch.traewelling.ui.notifications

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.notifications.Notification
import de.hbch.traewelling.api.models.notifications.NotificationPage
import de.hbch.traewelling.logging.Logger
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
                        Logger.captureException(t)
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
                    Logger.captureException(t)
                }
            })
    }

    fun markAsRead(id: String, successfulCallback: (Notification) -> Unit) {
        TraewellingApi
            .notificationService
            .markAsRead(id)
            .enqueue(object: Callback<Data<Notification>> {
                override fun onResponse(
                    call: Call<Data<Notification>>,
                    response: Response<Data<Notification>>
                ) {
                    if (response.isSuccessful) {
                        val notification = response.body()
                        if (notification != null) {
                            successfulCallback(notification.data)
                        }
                    }
                }

                override fun onFailure(call: Call<Data<Notification>>, t: Throwable) {
                    Logger.captureException(t)
                }
            })
    }

    fun markAsUnread(id: String, successfulCallback: (Notification) -> Unit) {
        TraewellingApi
            .notificationService
            .markAsUnread(id)
            .enqueue(object: Callback<Data<Notification>> {
                override fun onResponse(
                    call: Call<Data<Notification>>,
                    response: Response<Data<Notification>>
                ) {
                    if (response.isSuccessful) {
                        val notification = response.body()
                        if (notification != null) {
                            successfulCallback(notification.data)
                        }
                    }
                }

                override fun onFailure(call: Call<Data<Notification>>, t: Throwable) {
                    Logger.captureException(t)
                }
            })
    }

    fun markAllAsRead(successfulCallback: () -> Unit) {
        TraewellingApi
            .notificationService
            .markAllAsRead()
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        successfulCallback()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Logger.captureException(t)
                }
            })
    }
}
