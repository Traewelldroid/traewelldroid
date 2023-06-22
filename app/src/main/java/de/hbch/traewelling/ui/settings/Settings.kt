package de.hbch.traewelling.ui.settings

import androidx.annotation.StringRes
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.jcloquell.androidsecurestorage.SecureStorage
import de.c1710.filemojicompat_ui.views.picker.EmojiPackItemAdapter
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Settings(
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    emojiPackItemAdapter: EmojiPackItemAdapter? = null,
    traewellingLogoutAction: () -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val cardModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

        CheckInProviderSettings(
            modifier = cardModifier,
            loggedInUserViewModel = loggedInUserViewModel,
            traewellingLogoutAction = traewellingLogoutAction
        )
        HashtagSettings(
            modifier = cardModifier
        )
        EmojiSettings(
            modifier = cardModifier,
            emojiPackItemAdapter = emojiPackItemAdapter
        )
    }
}

@Composable
private fun CheckInProviderSettings(
    modifier: Modifier = Modifier,
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    traewellingLogoutAction: () -> Unit = { }
) {
    SettingsCard(
        modifier = modifier,
        title = R.string.settings_check_in_providers,
        description = R.string.settings_check_in_providers_description,
        expandable = true
    ) {
        TraewellingProviderSettings(
            modifier = Modifier.fillMaxWidth(),
            loggedInUserViewModel = loggedInUserViewModel,
            logoutAction = traewellingLogoutAction
        )
    }
}

@Composable
private fun TraewellingProviderSettings(
    modifier: Modifier = Modifier,
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    logoutAction: () -> Unit = { }
) {
    if (loggedInUserViewModel != null) {
        var secureStorage: SecureStorage?
        var jwt by remember { mutableStateOf("") }
        val username by loggedInUserViewModel.username.observeAsState("")

        if (!LocalView.current.isInEditMode) {
            secureStorage = SecureStorage(LocalContext.current)
            jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java) ?: ""
        }
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Träwelling",
                style = AppTypography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(id = R.string.signed_in_as, username)
            )
            Text(
                text = stringResource(id = R.string.jwt_expiration, getJwtExpiration(jwt = jwt))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWithIconAndText(
                    modifier = Modifier.padding(top = 8.dp),
                    stringId = R.string.logout,
                    onClick = logoutAction
                )
            }
        }
    }
}

@Composable
private fun getJwtExpiration(jwt: String): String {
    var expiresAt = Date()
    if (jwt != "") {
        try {
            expiresAt = JWT(jwt).expiresAt ?: Date()
        }
        catch(_: Exception) { }
    }
    var dateTimeString = ""
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    dateTimeString = df.format(expiresAt)

    return dateTimeString
}

@Composable
private fun HashtagSettings(
    modifier: Modifier = Modifier
) {
    var hashtagText by remember { mutableStateOf("") }
    var secureStorage: SecureStorage? = null
    var saveHashtagAction: () -> Unit = { }
    val defaultColor = LocalColorScheme.current.primary
    val buttonColor = remember { Animatable(defaultColor) }
    val coroutineScope = rememberCoroutineScope()

    if (!LocalView.current.isInEditMode) {
        secureStorage = SecureStorage(LocalContext.current)
        hashtagText = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java) ?: ""
        saveHashtagAction = {
            secureStorage.storeObject(SharedValues.SS_HASHTAG, hashtagText)
            coroutineScope.launch {
                buttonColor.animateTo(Color.hsl(150f, 1f, 0.25f), animationSpec = tween(500))
            }
        }
    }

    SettingsCard(
        modifier = modifier,
        title = R.string.hashtag,
        description = R.string.default_hashtag_text,
        expandable = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f),
                value = hashtagText,
                singleLine = true,
                onValueChange = {
                    hashtagText = it
                    coroutineScope.launch {
                        buttonColor.animateTo(defaultColor, animationSpec = tween(500))
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hashtag),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.hashtag)
                    )
                }
            )
            FilledIconButton(
                onClick = saveHashtagAction,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = buttonColor.value,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_in),
                    contentDescription = stringResource(id = R.string.store_hashtag)
                )
            }
        }
    }
}

@Composable
private fun EmojiSettings(
    modifier: Modifier = Modifier,
    emojiPackItemAdapter: EmojiPackItemAdapter? = null
) {
    SettingsCard(
        modifier = modifier,
        title = R.string.emoji,
        description = R.string.emoji_text,
        expandable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (emojiPackItemAdapter != null) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        val recyclerView = RecyclerView(context, null)

                        recyclerView.layoutManager = LinearLayoutManager(context)
                        recyclerView.adapter = emojiPackItemAdapter
                        recyclerView.isNestedScrollingEnabled = false

                        recyclerView
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    @StringRes title: Int,
    @StringRes description: Int,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier,
        onClick = {
            if (expandable)
                expanded = !expanded
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = title),
                        style = AppTypography.headlineSmall
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = stringResource(id = description),
                        style = AppTypography.labelSmall
                    )
                }
                if (expandable) {
                    IconToggleButton(
                        checked = expanded,
                        onCheckedChange = { expanded = !expanded }
                    ) {
                        val icon = if (expanded)
                            R.drawable.ic_expand_less
                        else
                            R.drawable.ic_expand_more

                        Icon(
                            painterResource(id = icon),
                            contentDescription = null
                        )
                    }
                }
            }
            AnimatedVisibility((expandable && expanded) || !expandable) {
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalColorScheme.current.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SettingsCardPreview() {
    MainTheme {
        val title = R.string.hashtag
        val description = R.string.default_hashtag_text
        val content = @Composable {
            OutlinedTextField(
                value = "Test",
                onValueChange = { },
                label = {
                    Text(
                        text = stringResource(id = title)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column {
            SettingsCard(
                modifier = Modifier.fillMaxWidth(),
                expandable = true,
                title = title,
                description = description,
                content = content
            )

            SettingsCard(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                expandable = false,
                title = title,
                description = description,
                content = content
            )
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    MainTheme {
        Settings()
    }
}
