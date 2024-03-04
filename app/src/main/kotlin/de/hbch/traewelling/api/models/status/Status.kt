package de.hbch.traewelling.api.models.status

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.util.extractUsernames
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
    @SerializedName("isLikable") val likeable: Boolean?,
    @SerializedName("train") val journey: Journey,
    val event: Event?,
    val client: ApiClient?,
    @SerializedName("bodyMentions") val mentions: List<UserMention>
) {
    fun getStatusText(): String {
        var statusBody = body ?: ""

        if (username == "ErikUden") {
            statusBody += "\n🍑"
        }

        return statusBody
    }

    fun getStatusBody(mentionColor: Color): AnnotatedString {
        val statusBody = getStatusText()

        val usernames = statusBody.extractUsernames()
        val builder = AnnotatedString.Builder(statusBody)

        val usernameStyle = SpanStyle(fontWeight = FontWeight.ExtraBold, color = mentionColor)
        usernames.forEach { match ->
            val username = match.groupValues.getOrElse(1) { "" }
            if (mentions.any { it.user.username == username }) {
                builder.addStyle(usernameStyle, match.range.first, match.range.last + 1)
                builder.addStringAnnotation(
                    "userMention",
                    username,
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        return builder.toAnnotatedString()
    }

    val isTraewelldroidCheckIn get() = client?.id == 43
}

enum class StatusVisibility() {
    @SerializedName("0")
    PUBLIC {
        override val icon = R.drawable.ic_public
        override val title = R.string.visibility_public
    },
    @SerializedName("1")
    UNLISTED {
        override val icon = R.drawable.ic_lock_open
        override val title = R.string.visibility_unlisted
    },
    @SerializedName("2")
    FOLLOWERS {
        override val icon = R.drawable.ic_people
        override val title = R.string.visibility_followers
    },
    @SerializedName("3")
    PRIVATE {
        override val icon = R.drawable.ic_lock
        override val title = R.string.visibility_private
    },
    @SerializedName("4")
    ONLY_AUTHENTICATED {
        override val icon = R.drawable.ic_authorized
        override val title = R.string.visibility_only_authenticated
    };

    abstract val icon: Int
    abstract val title: Int
}

enum class StatusBusiness(val business: Int) {
    @SerializedName("0")
    PRIVATE(0) {
        override val icon = R.drawable.ic_person
        override val title = R.string.business_private
    },
    @SerializedName("1")
    BUSINESS(1) {
        override val icon = R.drawable.ic_business
        override val title = R.string.business
    },
    @SerializedName("2")
    COMMUTE(2) {
        override val icon = R.drawable.ic_commute
        override val title = R.string.business_commute
    };

    abstract val icon: Int
    abstract val title: Int
}
