package de.hbch.traewelling.ui.composables

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import org.unifiedpush.android.connector.UnifiedPush.getDistributor
import org.unifiedpush.android.connector.UnifiedPush.getDistributors
import org.unifiedpush.android.connector.UnifiedPush.registerApp
import org.unifiedpush.android.connector.UnifiedPush.saveDistributor
import org.unifiedpush.android.connector.UnifiedPush.unregisterApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnablePushNotificationsCard(
    onNotificationsEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unifiedPushDistributors = getDistributors(context)
    var upDistributorSelectionVisible by remember { mutableStateOf(false) }
    var selectedDistributor by remember { mutableStateOf(getDistributor(context)) }

    if (upDistributorSelectionVisible) {
        Dialog(
            onDismissRequest = {
                upDistributorSelectionVisible = false
                saveDistributor(context, unifiedPushDistributors[0])
            }
        ) {
            Box(
                modifier = Modifier.padding(8.dp)
            ) {
                UnifiedPushDistributorSelection(
                    selectedDistributor = selectedDistributor,
                    distributors = unifiedPushDistributors,
                    distributorSelected = {
                        saveDistributor(context, it)
                        registerApp(context)
                        upDistributorSelectionVisible = false
                        selectedDistributor = it
                    }
                )
            }
        }
    }

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            EnableNotificationsSwitch(
                onNotificationsEnabledChange = {
                    onNotificationsEnabledChange(it)
                    if (unifiedPushDistributors.isNotEmpty()) {
                        if (it) {
                            if (unifiedPushDistributors.size == 1) {
                                val distributor = unifiedPushDistributors[0]
                                saveDistributor(context, distributor)
                                registerApp(context)
                                selectedDistributor = distributor
                            } else {
                                upDistributorSelectionVisible = true
                            }
                        } else {
                            unregisterApp(context)
                            selectedDistributor = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = unifiedPushDistributors.isNotEmpty()
            )
            if (unifiedPushDistributors.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_up_distributor_found),
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            if (selectedDistributor.isNotBlank() && selectedDistributor != context.packageName) {
                Text(
                    text = stringResource(id = R.string.selected_up_distributor, selectedDistributor)
                )
            }
        }
    }
}

@Composable
fun NotificationsAvailableHint(
    loggedInUserViewModel: LoggedInUserViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = { }
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = stringResource(id = R.string.push_hint_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = AppTypography.headlineSmall
                )
                Text(
                    text = stringResource(id = R.string.push_hint_text),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = AppTypography.labelMedium
                )
                ButtonWithIconAndText(
                    stringId = R.string.logout,
                    drawableId = R.drawable.ic_logout,
                    onClick = {
                        loggedInUserViewModel.logoutWithRestart(context)
                    }
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnableNotificationsSwitch(
    onNotificationsEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var enableNotifications by remember { mutableStateOf(false) }
    var onCheckedChange: (Boolean) -> Unit = { enableNotifications = it }

    LaunchedEffect(enableNotifications) {
        onNotificationsEnabledChange(enableNotifications)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState =
            rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionResult = {
                    enableNotifications = it
                }
            )
        val permissionGranted by remember {
            derivedStateOf { notificationPermissionState.status == PermissionStatus.Granted }
        }

        onCheckedChange = {
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
        stringId = R.string.enable_push_notifications,
        enabled = enabled
    )
}

@Composable
private fun UnifiedPushDistributorSelection(
    selectedDistributor: String,
    distributors: List<String>,
    distributorSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var selection by remember { mutableStateOf(selectedDistributor) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.select_up_distributor),
            style = AppTypography.titleLarge
        )
        Column(
            modifier = Modifier.selectableGroup()
        ) {
            distributors.forEach {
                val selected: () -> Unit = {
                    selection = it
                    distributorSelected(it)
                }
                val distributorName =
                    if (it == context.packageName)
                        stringResource(id = R.string.embedded_up_distributor)
                    else
                        context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it, 0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = selected),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selection == it,
                        onClick = selected
                    )
                    Text(
                        text = distributorName.toString(),
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun NotificationsAvailableHintPreview() {
    MainTheme {
        NotificationsAvailableHint(
            loggedInUserViewModel = LoggedInUserViewModel(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
