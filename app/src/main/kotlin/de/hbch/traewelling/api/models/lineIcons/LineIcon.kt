package de.hbch.traewelling.api.models.lineIcons

import androidx.compose.ui.graphics.Color
import de.hbch.traewelling.util.colorFromHex

data class LineIcon(
    val operatorName: String,
    val displayedName: String,
    val operatorCode: String?,
    val lineId: String,
    val backgroundColor: String,
    val textColor: String,
    val shape: LineIconShape
) {
    fun getBackgroundColor(): Color = colorFromHex(backgroundColor)
    fun getTextColor(): Color = colorFromHex(textColor)
}

enum class LineIconShape {
    rectangle,
    pill,
    trapezoid,
    hexagon,
    rectangle_rounded_corner
}
