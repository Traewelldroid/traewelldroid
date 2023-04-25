package de.hbch.traewelling.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme

@Composable
fun OutlinedButtonWithIconAndText(
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { }
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        RowWithIconAndText(stringId = stringId, drawableId = drawableId)
    }
}

@Composable
fun ButtonWithIconAndText(
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { }
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        RowWithIconAndText(stringId = stringId, drawableId = drawableId)
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    val stringId = R.string.locate
    val drawableId = R.drawable.ic_locate

    MainTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            ButtonWithIconAndText(stringId = stringId, drawableId = drawableId, modifier = Modifier.weight(1f))
            OutlinedButtonWithIconAndText(stringId = stringId, drawableId = drawableId, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RowWithIconAndText(
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = drawableId),
                contentDescription = stringResource(id = stringId),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = stringId),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
