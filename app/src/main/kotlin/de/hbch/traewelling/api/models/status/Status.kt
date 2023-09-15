package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import java.time.ZonedDateTime

data class Status(
    val id: Int,
    val body: String?,
    val createdAt: ZonedDateTime,
    val profilePicture: String?,
    @SerializedName("user") val userId: Int,
    val username: String,
    val visibility: StatusVisibility,
    val business: StatusBusiness,
    var likes: Int?,
    var liked: Boolean?,
    @SerializedName("train") val journey: Journey,
    val event: Event?
)

enum class StatusVisibility() {
    @SerializedName("0")
    PUBLIC {
        override fun getIcon() = R.drawable.ic_public
        override fun getTitle() = R.string.visibility_public
    },
    @SerializedName("1")
    UNLISTED {
        override fun getIcon() = R.drawable.ic_lock_open
        override fun getTitle() = R.string.visibility_unlisted
    },
    @SerializedName("2")
    FOLLOWERS {
        override fun getIcon() = R.drawable.ic_people
        override fun getTitle() = R.string.visibility_followers
    },
    @SerializedName("3")
    PRIVATE {
        override fun getIcon() = R.drawable.ic_lock
        override fun getTitle() = R.string.visibility_private
    },
    @SerializedName("4")
    ONLY_AUTHENTICATED {
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
