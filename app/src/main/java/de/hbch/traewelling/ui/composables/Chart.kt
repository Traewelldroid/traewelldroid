package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme

@OptIn(ExperimentalTextApi::class)
@Composable
fun ColumnChart(
    modifier: Modifier = Modifier,
    input: Pair<List<Pair<String, Int>>, @Composable (Int) -> String>
) {
    val textMeasurer = rememberTextMeasurer()
    val data = input.first
    val formatter = input.second
    val maxValue = data.maxByOrNull { it.second }?.second ?: 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { (label, value) ->
            val percentage = value.toFloat() / maxValue.toFloat()
            val color = LocalColorScheme.current.secondaryContainer
            val labelText = "$label (${formatter(value)})"
            val textColor = LocalColorScheme.current.onSecondaryContainer

            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .align(Alignment.BottomStart)
                        .fillMaxHeight(percentage)
                        .fillMaxWidth()
                        .background(color)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .drawBehind {
                            drawContext.canvas.apply {
                                rotate(-90f)
                                val measuredText =
                                    textMeasurer.measure(
                                        AnnotatedString(labelText),
                                        style = AppTypography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                drawText(
                                    measuredText,
                                    topLeft = Offset((size.height * -1) + 20, 5f),
                                    color = textColor
                                )

                            }
                        }
                )
            }
        }
    }
}
