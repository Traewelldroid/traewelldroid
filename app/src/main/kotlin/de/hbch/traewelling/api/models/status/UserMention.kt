package de.hbch.traewelling.api.models.status

import de.hbch.traewelling.api.models.user.User

data class UserMention(
    val user: User,
    val position: Int,
    val length: Int
)
