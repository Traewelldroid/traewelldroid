package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.api.models.lineIcons.LineIconShape
import de.hbch.traewelling.shared.LineIcons
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LineIconStyle

@Composable
fun LineIcon(
    lineName: String,
    modifier: Modifier = Modifier,
    operatorCode: String? = null,
    lineId: String? = null,
    defaultTextStyle: TextStyle = AppTypography.bodyMedium
) {
    val opCode = operatorCode?.replace("nahreisezug", "") ?: ""

    val lineIcon = LineIcons.getInstance().icons.firstOrNull {
        it.lineId == lineId
                && it.operatorCode == opCode
    }

    val shape: Shape = when (lineIcon?.shape) {
        LineIconShape.pill -> RoundedCornerShape(percent = 50)
        LineIconShape.rectangle_rounded_corner -> RoundedCornerShape(percent = 20)
        else -> RectangleShape
    }
    val borderColor: Color = lineIcon?.getBorderColor() ?: Color.Transparent

    val displayedName = lineIcon?.displayedName ?: lineName

    if (lineIcon != null) {
        Box(
            modifier = modifier
                .widthIn(48.dp, 144.dp)
                .background(
                    color = lineIcon.getBackgroundColor(),
                    shape = shape
                )
                .border(2.dp, borderColor, shape)
                .padding(2.dp)
        ) {
            Text(
                text = displayedName,
                modifier = Modifier.align(Alignment.Center),
                color = lineIcon.getTextColor(),
                style = LineIconStyle,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text(
            text = displayedName,
            modifier = modifier,
            style = defaultTextStyle
        )
    }
}
