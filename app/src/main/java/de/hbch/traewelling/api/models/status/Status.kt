package de.hbch.traewelling.api.models.status

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.event.Event
import java.util.*

data class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String?,
    @SerializedName("type") val type: String,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("user") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("preventIndex") val preventIndex: Boolean,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("business") val business: StatusBusiness,
    @SerializedName("likes") var likes: Int,
    @SerializedName("liked") var liked: Boolean,
    @SerializedName("train") val journey: Journey,
    @SerializedName("event") val event: Event?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(gson.toJson(this))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Status> {
        private val gson = Gson()
        override fun createFromParcel(parcel: Parcel): Status {
            return gson.fromJson(parcel.readString(), Status::class.java)
        }

        override fun newArray(size: Int): Array<Status?> {
            return arrayOfNulls(size)
        }
    }
}

enum class StatusVisibility(val visibility: Int) {
    @SerializedName("0")
    PUBLIC(0),
    @SerializedName("1")
    UNLISTED(1),
    @SerializedName("2")
    FOLLOWERS(2),
    @SerializedName("3")
    PRIVATE(3),
    @SerializedName("4")
    ONLY_AUTHENTICATED(4)
}

enum class StatusBusiness(val business: Int) {
    @SerializedName("0")
    PRIVATE(0),
    @SerializedName("1")
    BUSINESS(1),
    @SerializedName("2")
    COMMUTE(2)
}

enum class PointReason(val pointReason: Int) {
    @SerializedName("0")
    IN_TIME(0),
    @SerializedName("1")
    GOOD_ENOUGH(1),
    @SerializedName("2")
    NOT_SUFFICIENT(2),
    @SerializedName("3")
    FORCED(3)
}
