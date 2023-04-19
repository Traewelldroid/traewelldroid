package de.hbch.traewelling.ui.info

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    showOss: () -> Unit,
    showProjectRepo: () -> Unit,
    showLegalInfo: () -> Unit,
    backPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(id = R.string.information))
                },
                navigationIcon = {
                    IconButton(onClick = backPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.jwt_expiration)
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight()
            ) {
                val verticalPadding = Modifier.padding(vertical = 8.dp)
                OutlinedButtonWithIconAndText(
                    stringId = R.string.open_source_licenses,
                    drawableId = R.drawable.ic_library,
                    onClick = showOss,
                    modifier = verticalPadding
                )
                OutlinedButtonWithIconAndText(
                    stringId = R.string.view_on_github,
                    drawableId = R.drawable.ic_code,
                    onClick = showProjectRepo,
                    modifier = verticalPadding
                )
                OutlinedButtonWithIconAndText(
                    stringId = R.string.legal,
                    drawableId = R.drawable.ic_privacy,
                    modifier = verticalPadding,
                    onClick = showLegalInfo
                )
                Box(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.version_with_code,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun OutlinedButtonWithIconAndText(
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { }
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = drawableId),
                contentDescription = stringResource(id = stringId),
            )
            Text(
                text = stringResource(id = stringId),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun InfoScreenPreview() {
    MainTheme {
        InfoScreen(
            showOss = { },
            showProjectRepo = { },
            showLegalInfo = { },
            backPressed = { }
        )
    }
}

@Preview
@Composable
private fun OutlinedButtonWithIconAndTextPreview() {
    MainTheme {
        OutlinedButtonWithIconAndText(
            stringId = R.string.app_name,
            drawableId = R.drawable.ic_train
        )
    }
}
