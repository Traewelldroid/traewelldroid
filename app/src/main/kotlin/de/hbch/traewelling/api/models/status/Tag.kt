package de.hbch.traewelling.api.models.status

import de.hbch.traewelling.R

enum class Tag {
    TRAVEL_CLASS {
        override val icon = R.drawable.ic_travel_class
        override val title = R.string.tag_travel_class_title
        override val example = R.string.tag_travel_class_example
        override val key = "trwl:travel_class"
    },
    TICKET {
        override val icon = R.drawable.ic_ticket
        override val title = R.string.tag_ticket_title
        override val example = R.string.tag_ticket_example
        override val key = "trwl:ticket"
    },
    COACH {
        override val icon = R.drawable.ic_coach
        override val title = R.string.tag_coach_title
        override val example = R.string.tag_coach_example
        override val key = "trwl:wagon"
    },
    SEAT {
        override val icon = R.drawable.ic_seat
        override val title = R.string.tag_seat_title
        override val example = R.string.tag_seat_example
        override val key = "trwl:seat"
    };

    abstract val icon: Int
    abstract val title: Int
    abstract val example: Int
    abstract val key: String
    open val value: String = ""
}
