package de.hbch.traewelling.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.api.models.notifications.Notification

@Composable
fun Notifications() {
    val notificationsViewModel: NotificationsViewModel = viewModel()
    var currentPage by remember { mutableStateOf(1) }
    val notifications = remember { mutableStateListOf<Notification>() }

    LaunchedEffect(currentPage) {
        notificationsViewModel.getNotifications(currentPage) {
            notifications.addAll(it.data)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notifications) {
            NotificationBody(notification = it)
        }
    }
}

@Composable
private fun NotificationBody(
    notification: Notification,
    modifier: Modifier = Modifier
) {
    val headline = notification.type.getHeadline(notification = notification)
    val body = notification.type.getBody(notification = notification)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Headline
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = notification.type.icon),
                contentDescription = null
            )
            Text(
                modifier = Modifier.weight(1f),
                text = headline,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (notification.readAt == null) {
                // unread
            } else {
                // read
            }
        }
        // Body
        if (body != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = body
            )
        }
        // Footer
    }
}
