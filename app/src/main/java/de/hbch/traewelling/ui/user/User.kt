package de.hbch.traewelling.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.UserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText

@Composable
fun UserCard(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    loggedInUserViewModel: LoggedInUserViewModel
) {
    val stateUser by userViewModel.user.observeAsState()
    val stateLoggedInUser by loggedInUserViewModel.user.observeAsState()

    stateUser?.let { displayedUser ->
        stateLoggedInUser?.let { loggedInUser ->
            UserCardContent(
                user = displayedUser,
                loggedInUser = loggedInUser,
                modifier = modifier,
                followAction = { userViewModel.handleFollowButton() },
                muteAction = { userViewModel.handleMuteButton() }
            )
        }
    }
}

@Composable
fun UserCard(
    modifier: Modifier = Modifier,
    user: User?,
    loggedInUser: User?
) {
    user?.let {
        loggedInUser?.let {
            UserCardContent(
                user = user,
                loggedInUser = loggedInUser,
                modifier = modifier,
                followAction = { }
            )
        }
    }
}

@Composable
private fun UserCardContent(
    user: User,
    loggedInUser: User,
    modifier: Modifier = Modifier,
    followAction: () -> Unit = { },
    muteAction: () -> Unit = { }
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.outlinedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.name,
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.ic_new_user),
            )
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = AppTypography.titleLarge,
                        text = user.name
                    )
                    if (user.privateProfile) {
                        Icon(
                            painterResource(id = R.drawable.ic_lock),
                            contentDescription = stringResource(id = R.string.private_profile),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Text(
                    style = AppTypography.titleMedium,
                    text = user.username
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_navigation),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.format_distance_kilometers, user.distance / 1000)
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = stringResource(id = R.string.display_points, user.points)
                    )
                    Icon(
                        painterResource(id = R.drawable.ic_score),
                        contentDescription = null
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_travel_time),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = getDurationString(user.duration)
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = stringResource(id = R.string.display_average_speed, user.averageSpeed / 1000.0)
                    )
                    Icon(
                        painterResource(id = R.drawable.ic_speed),
                        contentDescription = null
                    )
                }
            }
            if (loggedInUser.id != user.id) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                    FollowButton(user = user, onClick = followAction, modifier = buttonModifier)
                    MuteButton(user = user, onClick = muteAction, modifier = buttonModifier)
                }
            }
        }
    }
}

@Composable
private fun FollowButton(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
    * Handle four states:
    * - Following
    * - Not following, public profile
    * - Not following, private profile
    * - Follow request pending
    */
    val iconDrawable =
        if (user.following) {
            R.drawable.ic_person_remove
        } else if (user.followRequestPending) {
            R.drawable.ic_hourglass
        } else {
            R.drawable.ic_add_person
        }

    val buttonText =
        if (user.following) {
            R.string.unfollow
        } else if (user.followRequestPending) {
            R.string.request_follow_pending
        } else if (user.privateProfile) {
            R.string.request_follow
        } else {
            R.string.follow
        }

    ButtonWithIconAndText(
        stringId = buttonText,
        drawableId = iconDrawable,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun MuteButton(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconDrawable =
        if (user.muted)
            R.drawable.ic_unmute
        else
            R.drawable.ic_mute

    val buttonText =
        if (user.muted)
            R.string.unmute
        else
            R.string.mute

    ButtonWithIconAndText(
        stringId = buttonText,
        drawableId = iconDrawable,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun getDurationString(
    duration: Int
): String {
    val minutes = duration % 60
    var hours = duration / 60
    val days = hours / 24
    hours -= days * 24

    return if (days > 0) {
        stringResource(id = R.string.display_travel_time_days_hours_minutes, days, hours, minutes)
    } else {
        if (hours == 0) {
            stringResource(R.string.display_travel_time_minutes, minutes)
        } else {
            stringResource(id = R.string.display_travel_time_hours_minutes, hours, minutes)
        }
    }
}

@Preview
@Composable
private fun UserCardPreview() {
    val user1 = User(
        0,
        "Hildegard Test",
        "hildetest",
        "urlurl",
        1234567890,
        10241024,
        12345678.9,
        4711,
        null,
        null,
        false,
        null,
        null,
        false,
        false,
        false,
        null
    )
    val user2 = User(
        1,
        "Sebastian Sepp",
        "sebblsepp",
        "urlurl",
        56789,
        4568,
        3456.9856,
        42,
        null,
        null,
        true,
        null,
        null,
        false,
        false,
        false,
        null
    )

    MainTheme {
        Column {
            UserCardContent(user = user1, loggedInUser = user1)
            UserCardContent(user = user2, loggedInUser = user1, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
