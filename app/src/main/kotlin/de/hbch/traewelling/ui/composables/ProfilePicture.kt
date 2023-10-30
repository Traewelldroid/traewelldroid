package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.user.User

@Composable
fun ProfilePicture(
    user: User,
    modifier: Modifier = Modifier
) {
    ProfilePicture(
        name = user.name,
        url = user.avatarUrl,
        modifier = modifier
    )
}

@Composable
fun ProfilePicture(
    name: String,
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = name,
        modifier = modifier
            .clip(CircleShape),
        placeholder = painterResource(id = R.drawable.ic_new_user),
    )
}
