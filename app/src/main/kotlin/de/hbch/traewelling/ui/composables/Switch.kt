package de.hbch.traewelling.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme

@Composable
fun SwitchWithIconAndText(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @DrawableRes drawableId: Int,
    @StringRes stringId: Int,
    enabled: Boolean = true
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
            },
            enabled = enabled
        )
    }
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
