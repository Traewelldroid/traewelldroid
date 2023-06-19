package de.hbch.traewelling.ui.statusDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.OpenRailwayMapView
import de.hbch.traewelling.ui.composables.getPolylinesFromFeatureCollection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

@Composable
fun StatusDetail(
    modifier: Modifier = Modifier,
    statusId: Int,
    statusDetailViewModel: StatusDetailViewModel
) {
    var mapExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val mapModifier = modifier
            .fillMaxHeight(if (mapExpanded) 1.0f else 0.5f)
            .animateContentSize()
        Box(
            modifier = modifier
        ) {
            StatusDetailMap(
                modifier = mapModifier.align(Alignment.TopCenter),
                statusId = statusId,
                statusDetailViewModel = statusDetailViewModel
            )
            IconToggleButton(
                modifier = Modifier.align(Alignment.TopEnd),
                checked = mapExpanded,
                onCheckedChange = {
                    mapExpanded = it
                },
                colors = IconButtonDefaults.filledIconToggleButtonColors()
            ) {
                AnimatedContent(mapExpanded) {
                    val iconSource =
                        if (it) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen
                    Icon(
                        painter = painterResource(id = iconSource),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDetailMap(
    modifier: Modifier = Modifier,
    statusId: Int,
    statusDetailViewModel: StatusDetailViewModel
) {
    val color = LocalColorScheme.current.primary.toArgb()
    var polylines: List<Polyline> = listOf()
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        OpenRailwayMapView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            onLoad = {
                if (polylines.isEmpty()) {
                    statusDetailViewModel.getPolylineForStatus(statusId, { collection ->
                        polylines = getPolylinesFromFeatureCollection(collection, color)
                        it.overlays.addAll(polylines)
                        it.zoomToBoundingBox(polylines[0].bounds.increaseByScale(1.1f), false)
                    }, { })
                }
            }
        )
    }
}

@Composable
private fun StatusDetails(
    modifier: Modifier = Modifier
) {

}

@Preview
@Composable
private fun StatusDetailPreview() {
    val statusDetailViewModel = StatusDetailViewModel()
    MainTheme {
        StatusDetail(
            modifier = Modifier.fillMaxWidth(),
            statusDetailViewModel = statusDetailViewModel,
            statusId = 1117900
        )
    }
}
