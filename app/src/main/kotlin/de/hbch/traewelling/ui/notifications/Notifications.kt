package de.hbch.traewelling.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.notifications.Notification
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.util.OnBottomReached
import de.hbch.traewelling.util.getLocalDateTimeString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Notifications(
    notificationsViewModel: NotificationsViewModel,
    navHostController: NavHostController,
    unreadNotificationsChanged: () -> Unit = { }
) {
    var isLoading by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    val notifications = remember { mutableStateListOf<Notification>() }
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { currentPage = 1 }
    )

    LaunchedEffect(currentPage) {
        if (currentPage == 1) {
            notifications.clear()
        }
        isLoading = true
        notificationsViewModel.getNotifications(currentPage) {
            notifications.addAll(it.data)
            isLoading = false
        }
    }

    listState.OnBottomReached {
        currentPage++
    }


    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(notifications) {
                Notification(
                    notification = it,
                    notificationsViewModel = notificationsViewModel,
                    unreadNotificationsChanged = unreadNotificationsChanged,
                    onClick = {
                        it.type.getOnClick(it).invoke(navHostController)
                    }
                )
            }
            item {
                Box { }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isLoading,
            state = pullRefreshState
        )
    }
}

@Composable
private fun Notification(
    notification: Notification,
    notificationsViewModel: NotificationsViewModel,
    modifier: Modifier = Modifier,
    unreadNotificationsChanged: () -> Unit = { },
    onClick: () -> Unit = { }
) {
    var isRead by remember { mutableStateOf(notification.readAt != null) }
    val markAsReadCallback: (Notification) -> Unit = {
        isRead = it.readAt != null
        notification.readAt = it.readAt
        unreadNotificationsChanged()
    }
    val onRead: () -> Unit = {
        if (isRead) {
            notificationsViewModel.markAsUnread(notification.id, markAsReadCallback)
        } else {
            notificationsViewModel.markAsRead(notification.id, markAsReadCallback)
        }
    }
    val onNotificationClick = {
        if (!isRead) {
            notificationsViewModel.markAsRead(notification.id, markAsReadCallback)
        }
        onClick()
    }

    if (isRead) {
        ReadNotification(
            notification = notification,
            modifier = modifier,
            onRead = onRead,
            onClick = onNotificationClick
        )
    } else {
        UnreadNotification(
            notification = notification,
            modifier = modifier,
            onRead = onRead,
            onClick = onNotificationClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnreadNotification(
    notification: Notification,
    modifier: Modifier = Modifier,
    onRead: () -> Unit = { },
    onClick: () -> Unit = { }
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(8.dp)
        ) {
            NotificationBody(
                notification = notification,
                isRead = false,
                onRead = onRead
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadNotification(
    notification: Notification,
    modifier: Modifier = Modifier,
    onRead: () -> Unit = { },
    onClick: () -> Unit = { }
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(8.dp)
        ) {
            NotificationBody(
                notification = notification,
                isRead = true,
                onRead = onRead
            )
        }
    }
}

@Composable
private fun NotificationBody(
    notification: Notification,
    isRead: Boolean,
    modifier: Modifier = Modifier,
    onRead: () -> Unit = { }
) {
    val context = LocalContext.current
    val headline = notification.type.getHeadline(context, notification)
    val body = notification.type.getBody(context, notification)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Headline
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            val markReadIcon =
                if (isRead) {
                    R.drawable.ic_mark_as_unread
                } else {
                    R.drawable.ic_mark_as_read
                }
            IconButton(onClick = onRead) {
                Icon(
                    painter = painterResource(id = markReadIcon),
                    contentDescription = null
                )
            }
        }
        // Body
        if (body != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = body,
                style = AppTypography.labelMedium
            )
        }
        // Footer
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            text = getLocalDateTimeString(date = notification.createdAt),
            style = AppTypography.labelMedium
        )
    }
}
