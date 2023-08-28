package de.hbch.traewelling.ui.composables

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme

@Composable
fun SwitchWithIconAndText(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @DrawableRes drawableId: Int,
    @StringRes stringId: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = drawableId),
                contentDescription = null
            )
            Text(
                text = stringResource(id = stringId),
                style = AppTypography.labelLarge
            )
        }
        Switch(
            modifier = Modifier.padding(start = 16.dp),
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnableNotificationsSwitch(
    modifier: Modifier = Modifier
) {
    var enableNotifications by remember { mutableStateOf(false) }
    var onCheckedChange: (Boolean) -> Unit = { enableNotifications = it }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState =
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        var pressedSwitch by remember { mutableStateOf(false) }
        val permissionGranted by remember { derivedStateOf { notificationPermissionState.status == PermissionStatus.Granted } }

        LaunchedEffect(permissionGranted) {
            if (permissionGranted && pressedSwitch) {
                enableNotifications = true
            }
        }

        onCheckedChange = {
            pressedSwitch = true
            if (!permissionGranted) {
                notificationPermissionState.launchPermissionRequest()
            } else {
                enableNotifications = it
            }
        }
    }

    SwitchWithIconAndText(
        modifier = modifier,
        checked = enableNotifications,
        onCheckedChange = onCheckedChange,
        drawableId = R.drawable.ic_notification,
        stringId = R.string.enable_push_notifications
    )
}

@Preview(showBackground = true)
@Composable
private fun SwitchWithIconAndButtonPreview() {
    var checked by remember { mutableStateOf(false) }

    MainTheme {
        SwitchWithIconAndText(
            checked = checked,
            onCheckedChange = {
                checked = it
            },
            drawableId = R.drawable.ic_mastodon,
            stringId = R.string.send_toot
        )
    }
}
