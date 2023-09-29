package de.hbch.traewelling.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import de.hbch.traewelling.shared.SharedValues
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

private val _mapnik: OnlineTileSourceBase = XYTileSource(
    "Mapnik",
    0,
    19,
    256,
    ".png",
    arrayOf("https://tile.openstreetmap.org/"),
    "© OpenStreetMap contributors, Style: CC-BY-SA 2.0, OpenRailwayMap",
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_NO_BULK
                or TileSourcePolicy.FLAG_NO_PREVENTIVE
                or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
    )
)

private fun getTileSource(layer: OpenRailwayMapLayer): XYTileSource {
    return XYTileSource(
        "OpenRailwayMap",
        0,
        19,
        256,
        ".png",
        arrayOf("https://tiles.openrailwaymap.org/${layer.key}/"),
        "© OpenStreetMap contributors, Style: CC-BY-SA 2.0, OpenRailwayMap"
    )
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            clipToOutline = true
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                else -> { }
            }
        }
    }

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onInit: (MapView) -> Unit = { },
    onLoad: (MapView) -> Unit = { }
) {
    val mapViewState = rememberMapViewWithLifecycle()

    AndroidView(
        factory = {
            onInit(mapViewState)
            mapViewState
        },
        modifier = modifier,
    ) { mapView ->
        Configuration.getInstance().userAgentValue = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
        onLoad(mapView)
    }
}

@Composable
fun OpenRailwayMapView(
    modifier: Modifier = Modifier,
    onInit: (MapView) -> Unit = { },
    onLoad: (MapView) -> Unit = { }
) {
    val context = LocalContext.current

    MapView(
        modifier = modifier,
        onLoad = {
            onLoad(it)
        },
        onInit = { mapView ->
            val secureStorage = SecureStorage(context)
            val selectedOrmLayer =
                secureStorage.getObject(SharedValues.SS_ORM_LAYER, OpenRailwayMapLayer::class.java)
                    ?: OpenRailwayMapLayer.STANDARD
            val tileSource = getTileSource(layer = selectedOrmLayer)
            mapView.setTileSource(_mapnik)
            mapView.setMultiTouchControls(true)

            val tileProvider = MapTileProviderBasic(context)
            tileProvider.tileSource = tileSource
            val tilesOverlay = TilesOverlay(tileProvider, context)
            mapView.overlays.add(tilesOverlay)

            val copyrightOverlay = CopyrightOverlay(context).apply {
                setAlignRight(true)
                setTextSize(8)
            }
            mapView.overlays.add(copyrightOverlay)

            onInit(mapView)
        }
    )
}

fun getPolyLinesFromFeatureCollection(featureCollection: FeatureCollection?, color: Int): List<Polyline> {
    val polyLines: MutableList<Polyline> = mutableListOf()

    featureCollection?.features?.forEach { feature ->
        val polyline = Polyline()
        feature.geometry?.coordinates?.forEach { coordinate ->
            polyline.addPoint(
                GeoPoint(
                    coordinate[1],
                    coordinate[0]
                )
            )
        }
        polyLines.add(polyline)

        polyline.outlinePaint.color = color
    }

    return polyLines
}

fun getBoundingBoxFromPolyLines(polyLines: List<Polyline>): BoundingBox {
    return BoundingBox.fromGeoPointsSafe(polyLines.map { it.actualPoints }.flatten())
}

enum class OpenRailwayMapLayer {
    STANDARD {
        override val key = "standard"
        override val title = R.string.standard_layer
        override val description = R.string.standard_description
    },
    SIGNALS {
        override val key = "signals"
        override val title = R.string.signal_layer
        override val description = R.string.signal_description
    },
    MAXSPEED {
        override val key = "maxspeed"
        override val title = R.string.maxspeed_layer
        override val description = R.string.maxspeed_description
    };

    abstract val key: String
    abstract val title: Int
    abstract val description: Int
}
