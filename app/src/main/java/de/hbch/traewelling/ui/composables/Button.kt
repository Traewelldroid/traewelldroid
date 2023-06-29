package de.hbch.traewelling.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OutlinedButtonWithIconAndText(
    modifier: Modifier = Modifier,
    text: String = "",
    @DrawableRes drawableId: Int? = null,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    onClick: () -> Unit = { },
    isLoading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        enabled = !isLoading
    ) {
        RowWithIconAndText(
            text = text,
            drawableId = drawableId,
            isLoading = isLoading
        )
    }
}

@Composable
fun OutlinedButtonWithIconAndText(
    modifier: Modifier = Modifier,
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int? = null,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    onClick: () -> Unit = { },
    isLoading: Boolean = false,
) {
    OutlinedButtonWithIconAndText(
        modifier = modifier,
        text = stringResource(id = stringId),
        drawableId = drawableId,
        colors = colors,
        onClick = onClick,
        isLoading = isLoading
    )
}

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    text: String = "",
    @DrawableRes drawableId: Int? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit = { },
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        enabled = !isLoading
    ) {
        RowWithIconAndText(
            text = text,
            drawableId = drawableId,
            isLoading = isLoading,
            progressColor = Color.White
        )
    }
}

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    @StringRes stringId: Int,
    @DrawableRes drawableId: Int? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit = { },
    isLoading: Boolean = false
) {
    ButtonWithIconAndText(
        modifier = modifier,
        text = stringResource(id = stringId),
        drawableId = drawableId,
        colors = colors,
        onClick = onClick,
        isLoading = isLoading
    )
}

@Composable
private fun RowWithIconAndText(
    text: String,
    @DrawableRes drawableId: Int? = null,
    progressColor: Color = ProgressIndicatorDefaults.circularColor,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconModifier = Modifier.padding(end = 8.dp)
            if (drawableId != null && !isLoading) {
                Icon(
                    painter = painterResource(id = drawableId),
                    contentDescription = text,
                    modifier = iconModifier
                )
            } else if (isLoading) {
                CircularProgressIndicator(
                    modifier = iconModifier.size(24.dp),
                    color = progressColor,
                    strokeWidth = 3.dp
                )
            }
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    val stringId = R.string.locate
    val drawableId = R.drawable.ic_locate

    MainTheme {
        Column {
            val rowModifier = Modifier.padding(8.dp)
            var isLoading by remember { mutableStateOf(false) }

            // Filled / outlined buttons with(-out) icons in a row
            Row(modifier = rowModifier) {
                ButtonWithIconAndText(
                    stringId = stringId,
                    drawableId = drawableId,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButtonWithIconAndText(
                    stringId = stringId,
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading
                )
            }

            // Button without modifier
            Row(modifier = rowModifier) {
                ButtonWithIconAndText(
                    stringId = R.string.login_oauth,
                    drawableId = R.drawable.ic_popup
                )
            }

            // Button with modifier for full width
            Row(modifier = rowModifier) {
                ButtonWithIconAndText(
                    stringId = R.string.login_oauth,
                    drawableId = R.drawable.ic_popup,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Button with icon for loading state
            Row(modifier = rowModifier) {
                ButtonWithIconAndText(
                    stringId = R.string.login_oauth,
                    drawableId = R.drawable.ic_popup,
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(5000)
                            isLoading = false
                        }
                    }
                )
            }
        }
    }
}
