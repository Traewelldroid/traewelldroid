package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class Tag(
    val key: TagType?,
    val value: String,
    val visibility: StatusVisibility
) {
    val safeKey get() = key ?: TagType.UNKNOWN
}

@Suppress("unused")
enum class TagType {
    @SerializedName("trwl:travel_class")
    TRAVEL_CLASS {
        override val icon = R.drawable.ic_travel_class
        override val title = R.string.tag_travel_class_title
        override val key = "trwl:travel_class"
        override val example = R.string.tag_travel_class_example
    },
    @SerializedName("trwl:ticket")
    TICKET {
        override val icon = R.drawable.ic_ticket
        override val title = R.string.tag_ticket_title
        override val key = "trwl:ticket"
        override val example = R.string.tag_ticket_example
    },
    @SerializedName("trwl:wagon")
    COACH {
        override val icon = R.drawable.ic_coach
        override val title = R.string.tag_coach_title
        override val key = "trwl:wagon"
        override val example = R.string.tag_coach_example
    },
    @SerializedName("trwl:seat")
    SEAT {
        override val icon = R.drawable.ic_seat
        override val title = R.string.tag_seat_title
        override val key = "trwl:seat"
        override val example = R.string.tag_seat_example
    },
    @SerializedName("trwl:locomotive_class")
    LOCOMOTIVE_CLASS {
        override val icon = R.drawable.ic_train
        override val title = R.string.tag_locomotive_class_title
        override val key = "trwl:locomotive_class"
        override val example = R.string.tag_locomotive_class_example
    },
    @SerializedName("trwl:vehicle_number")
    VEHICLE_NUMBER {
        override val icon = R.drawable.ic_vehicle_number
        override val title = R.string.tag_vehicle_number_title
        override val key = "trwl:vehicle_number"
        override val example = R.string.tag_vehicle_number_example
    },
    @SerializedName("trwl:wagon_class")
    WAGON_CLASS {
        override val icon = R.drawable.ic_wagon_class
        override val title = R.string.tag_wagon_class_title
        override val key = "trwl:wagon_class"
        override val example = R.string.tag_wagon_class_example
    },
    @SerializedName("trwl:role")
    STAFF_ROLE {
        override val icon = R.drawable.ic_business
        override val title = R.string.tag_staff_role_title
        override val key = "trwl:role"
        override val example = R.string.tag_staff_role_example
    },
    @SerializedName("trwl:passenger_rights")
    PASSENGER_RIGHTS {
        override val icon = R.drawable.ic_passenger_claim
        override val title = R.string.tag_passenger_rights_title
        override val key = "trwl:passenger_rights"
        override val example = R.string.tag_passenger_rights_example
    },
    UNKNOWN {
        override val icon = R.drawable.ic_unknown
        override val title = R.string.unknown
        override val key = "unknown"
        override val example = R.string.unknown
        override val selectable = false
    };

    abstract val icon: Int
    abstract val title: Int
    abstract val key: String
    abstract val example: Int
    open val selectable: Boolean = true
}
