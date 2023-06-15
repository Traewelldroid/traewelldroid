package de.hbch.traewelling.ui.statusDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.ui.composables.OpenRailwayMapView
import de.hbch.traewelling.ui.composables.getPolylinesFromFeatureCollection
import org.osmdroid.util.GeoPoint

@Composable
fun StatusDetail(
    modifier: Modifier = Modifier,
    statusDetailViewModel: StatusDetailViewModel
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusDetailMap(
            modifier = Modifier.fillMaxHeight(0.4f),
            statusDetailViewModel = statusDetailViewModel
        )
    }
}

@Composable
private fun StatusDetailMap(
    modifier: Modifier = Modifier,
    statusDetailViewModel: StatusDetailViewModel
) {
    val color = LocalColorScheme.current.primary.toArgb()
    ElevatedCard(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        OpenRailwayMapView(
            modifier = modifier.fillMaxWidth(),
            onLoad = {
                it.controller.setZoom(17.5)
                it.controller.setCenter(GeoPoint(47.9, 10.5))

                statusDetailViewModel.getPolylineForStatus(1117999, { collection ->
                    val polylines = getPolylinesFromFeatureCollection(collection, color)
                    it.overlays.addAll(polylines)
                    it.zoomToBoundingBox(polylines[0].bounds.increaseByScale(1.1f), false)
                }, { })
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
    StatusDetail(
        statusDetailViewModel = statusDetailViewModel
    )
}
