package de.hbch.traewelling.api.models.status

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import java.time.ZonedDateTime

data class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String?,
    @SerializedName("type") val type: String,
    @SerializedName("createdAt") val createdAt: ZonedDateTime,
    @SerializedName("profilePicture") val profilePicture: String?,
    @SerializedName("user") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("preventIndex") val preventIndex: Boolean,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("business") val business: StatusBusiness,
    @SerializedName("likes") var likes: Int?,
    @SerializedName("liked") var liked: Boolean?,
    @SerializedName("train") val journey: Journey,
    @SerializedName("event") val event: Event?,
    @SerializedName("socialText") val socialText: String?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(gson.toJson(this))
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toStatusDto(): de.hbch.traewelling.api.dtos.Status {
        return de.hbch.traewelling.api.dtos.Status(
            id,
            journey.origin.name,
            journey.origin.id,
            journey.origin.departurePlanned,
            journey.origin.departureReal,
            journey.departureManual,
            journey.destination.name,
            journey.destination.id,
            journey.destination.arrivalPlanned,
            journey.destination.arrivalReal,
            journey.arrivalManual,
            journey.category,
            journey.hafasTripId,
            journey.line,
            journey.journeyNumber,
            journey.distance,
            journey.duration,
            business,
            body ?: "",
            liked,
            likes,
            userId,
            username,
            createdAt,
            visibility,
            event?.name
        )
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

enum class StatusVisibility() {
    @SerializedName("0")
    PUBLIC() {
        override fun getIcon() = R.drawable.ic_public
        override fun getTitle() = R.string.visibility_public
    },
    @SerializedName("1")
    UNLISTED() {
        override fun getIcon() = R.drawable.ic_lock_open
        override fun getTitle() = R.string.visibility_unlisted
    },
    @SerializedName("2")
    FOLLOWERS() {
        override fun getIcon() = R.drawable.ic_people
        override fun getTitle() = R.string.visibility_followers
    },
    @SerializedName("3")
    PRIVATE() {
        override fun getIcon() = R.drawable.ic_lock
        override fun getTitle() = R.string.visibility_private
    },
    @SerializedName("4")
    ONLY_AUTHENTICATED() {
        override fun getIcon() = R.drawable.ic_authorized
        override fun getTitle() = R.string.visibility_only_authenticated
    };

    abstract fun getIcon(): Int
    abstract fun getTitle(): Int
}

enum class StatusBusiness(val business: Int) {
    @SerializedName("0")
    PRIVATE(0) {
        override fun getIcon() = R.drawable.ic_person
        override fun getTitle() = R.string.business_private
    },
    @SerializedName("1")
    BUSINESS(1) {
        override fun getIcon() = R.drawable.ic_business
        override fun getTitle() = R.string.business
    },
    @SerializedName("2")
    COMMUTE(2) {
        override fun getIcon() = R.drawable.ic_commute
        override fun getTitle() = R.string.business_commute
    };

    abstract fun getIcon(): Int
    abstract fun getTitle(): Int
}
