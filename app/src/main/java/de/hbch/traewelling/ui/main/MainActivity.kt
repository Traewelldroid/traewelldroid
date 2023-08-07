package de.hbch.traewelling.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.jcloquell.androidsecurestorage.SecureStorage
import de.c1710.filemojicompat_ui.views.picker.EmojiPackItemAdapter
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.events.UnauthorizedEvent
import de.hbch.traewelling.navigation.BOTTOM_NAVIGATION
import de.hbch.traewelling.navigation.ComposeMenuItem
import de.hbch.traewelling.navigation.Dashboard
import de.hbch.traewelling.navigation.Notifications
import de.hbch.traewelling.navigation.PersonalProfile
import de.hbch.traewelling.navigation.SCREENS
import de.hbch.traewelling.navigation.TraewelldroidNavHost
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.FeatureFlags
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.notifications.NotificationsViewModel
import de.hbch.traewelling.util.popBackStackAndNavigate
import de.hbch.traewelling.util.publishStationShortcuts
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.polling.PollingModes
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.time.Duration
import java.time.LocalDateTime

class MainActivity : ComponentActivity()
{
    private val loggedInUserViewModel: LoggedInUserViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by viewModels()
    private var newIntentReceived: ((Intent?) -> Unit)? = null
    private lateinit var secureStorage: SecureStorage
    lateinit var emojiPackItemAdapter: EmojiPackItemAdapter

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        initUnleash()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedEvent(@Suppress("UNUSED_PARAMETER") unauthorizedEvent: UnauthorizedEvent) {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
        secureStorage.removeObject(SharedValues.SS_JWT)
        TraewellingApi.jwt = ""
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorage = SecureStorage(this)
        emojiPackItemAdapter = EmojiPackItemAdapter.get(this)
        TraewellingApi.jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java)!!

        loggedInUserViewModel.getLoggedInUser()
        loggedInUserViewModel.getLastVisitedStations {
            publishStationShortcuts(this, it)
        }
        eventViewModel.activeEvents()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            newIntentReceived = {
                navController.handleDeepLink(it)
            }

            TraewelldroidApp(
                navController = navController,
                loggedInUserViewModel = loggedInUserViewModel,
                eventViewModel = eventViewModel,
                checkInViewModel = checkInViewModel
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntentReceived?.invoke(intent)
    }

    private fun initUnleash() {
        val url = BuildConfig.UNLEASH_URL
        val key = BuildConfig.UNLEASH_KEY
        val flags = FeatureFlags.getInstance()
        if (url.isNotBlank() && key.isNotBlank()) {
            val config = UnleashConfig.newBuilder()
                .proxyUrl(url)
                .clientKey(key)
                .pollingMode(PollingModes.autoPoll(5 * 60) {
                    flags.flagsUpdated()
                })
                .httpClientConnectionTimeout(1000)
                .httpClientReadTimeout(1000)
                .build()
            flags.init(UnleashClient(config))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraewelldroidApp(
    navController: NavHostController,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    checkInViewModel: CheckInViewModel
) {
    MainTheme {
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentScreen = SCREENS.find { it.route == currentDestination?.route } ?: Dashboard
        val loggedInUser by loggedInUserViewModel.loggedInUser.observeAsState()

        val appBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)

        val menuItems = remember { mutableStateListOf<ComposeMenuItem>() }
        val menuItemsChanged: (List<ComposeMenuItem>) -> Unit = {
            menuItems.clear()
            menuItems.addAll(it)
        }

        var fabVisible by remember { mutableStateOf(false) }
        var fabIcon by remember { mutableStateOf<Int?>(null) }
        var fabLabel by remember { mutableStateOf<Int?>(null) }
        var fabListener by remember { mutableStateOf({ }) }
        var unreadNotificationCount by remember { mutableStateOf(0) }
        val notificationsViewModel: NotificationsViewModel = viewModel()
        val onNotificationCountChanged: () -> Unit = {
            notificationsViewModel.getUnreadNotificationCount {
                unreadNotificationCount = it
            }
        }
        var lastNotificationRequest by remember { mutableStateOf<LocalDateTime>(LocalDateTime.MIN) }

        navController.addOnDestinationChangedListener { _, destination, args ->
            val lastRequest = lastNotificationRequest
            val duration = Duration.between(lastRequest, LocalDateTime.now())
            if (duration.toMinutes() > 0) {
                onNotificationCountChanged()
                lastNotificationRequest = LocalDateTime.now()
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (currentScreen == Dashboard) {
                            Icon(
                                modifier = Modifier.size(64.dp),
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                tint = LocalColorScheme.current.primary
                            )
                        } else {
                            Text(
                                text = stringResource(id = currentScreen.label)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    actions = {
                        if (menuItems.isNotEmpty()) {
                            if (menuItems.size <= 2) {
                                menuItems.forEach { item ->
                                    IconButton(onClick = item.onClick) {
                                        Icon(
                                            painter = painterResource(id = item.icon),
                                            contentDescription = stringResource(id = item.label)
                                        )
                                    }
                                }
                            } else {
                                var menuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_more),
                                            contentDescription = null
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        menuItems.forEach { menuItem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(id = menuItem.label)
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(id = menuItem.icon),
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = menuItem.onClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = navController.previousBackStackEntry == null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    NavigationBar {
                        BOTTOM_NAVIGATION.forEach { destination ->
                            NavigationBarItem(
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (destination == Notifications && unreadNotificationCount > 0) {
                                                Badge {
                                                    Text(
                                                        text = unreadNotificationCount.toString()
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        val user = loggedInUser
                                        if (
                                            destination == PersonalProfile &&
                                            user != null
                                        ) {
                                            AsyncImage(
                                                model = user.avatarUrl,
                                                contentDescription = user.name,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape),
                                                placeholder = painterResource(id = destination.icon)
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(id = destination.icon),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = destination.label),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = currentScreen == destination,
                                onClick = {
                                    navController.popBackStackAndNavigate(destination.route)
                                    appBarState.contentOffset = 0f
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (fabVisible) {
                    val icon = fabIcon
                    val label = fabLabel
                    if (icon != null && label != null) {
                        ExtendedFloatingActionButton(onClick = fabListener) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = stringResource(id = label)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            TraewelldroidNavHost(
                navController = navController,
                loggedInUserViewModel = loggedInUserViewModel,
                eventViewModel = eventViewModel,
                checkInViewModel = checkInViewModel,
                notificationsViewModel = notificationsViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                onMenuChange = menuItemsChanged,
                onFloatingActionButtonChange = { icon, label, listener ->
                    fabIcon = icon
                    fabLabel = label
                    fabListener = listener
                    fabVisible = true
                },
                onResetFloatingActionButton = {
                    fabVisible = false
                    fabIcon = null
                    fabLabel = null
                    fabListener = { }
                },
                onNotificationCountChange = onNotificationCountChanged
            )
        }
    }
}
