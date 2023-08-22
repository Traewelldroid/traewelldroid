package de.hbch.traewelling.ui.tag

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Tag
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TagViewModel : ViewModel() {
    private fun getApiCallback(
        successfulCallback: (Tag) -> Unit,
        failureCallback: () -> Unit
    ): Callback<Data<Tag>> = object: Callback<Data<Tag>> {
        override fun onResponse(call: Call<Data<Tag>>, response: Response<Data<Tag>>) {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    successfulCallback(body.data)
                    return
                }
            }
            failureCallback()
        }

        override fun onFailure(call: Call<Data<Tag>>, t: Throwable) {
            failureCallback()
            Sentry.captureException(t)
        }
    }

    fun getTagsForStatus(
        statusId: Int,
        successfulCallback: (List<Tag>) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .getTagsForStatusById(statusId)
            .enqueue(object: Callback<Data<List<Tag>>> {
                override fun onResponse(
                    call: Call<Data<List<Tag>>>,
                    response: Response<Data<List<Tag>>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            successfulCallback(data)
                            return
                        }
                    }
                    failureCallback()
                }

                override fun onFailure(call: Call<Data<List<Tag>>>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }

    fun createTag(
        statusId: Int,
        tag: Tag,
        successfulCallback: (Tag) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .createTagForStatus(statusId, tag)
            .enqueue(getApiCallback(successfulCallback, failureCallback))
    }

    fun updateTag(
        statusId: Int,
        tag: Tag,
        successfulCallback: (Tag) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .updateTagForStatus(statusId, tag.safeKey.key, tag)
            .enqueue(getApiCallback(successfulCallback, failureCallback))
    }

    fun deleteTag(
        statusId: Int,
        tag: Tag,
        successfulCallback: () -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .deleteTagForStatus(statusId, tag.safeKey.key)
            .enqueue(object: Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        successfulCallback()
                    } else {
                        failureCallback()
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}
