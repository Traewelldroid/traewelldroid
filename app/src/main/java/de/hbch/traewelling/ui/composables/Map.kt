package de.hbch.traewelling.ui.composables

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

private val MAPNIK: OnlineTileSourceBase = XYTileSource(
    "Mapnik",
    0,
    19,
    256,
    ".png",
    arrayOf("https://a.tile.openstreetmap.org/","https://b.tile.openstreetmap.org/","https://c.tile.openstreetmap.org/"),
    "Mapnik",
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_NO_BULK
                or TileSourcePolicy.FLAG_NO_PREVENTIVE
                or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
    )
)

private val OPENRAILWAYMAP = XYTileSource(
    "OpenRailwayMap",
    0,
    19,
    256,
    ".png",
    arrayOf("https://tiles.openrailwaymap.org/standard/"),
    "Copyright oder so"
)

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            clipToOutline = true
        }
    }

    // Makes MapView follow the lifecycle of this composable
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
            Log.d("MapEvent", event.name)
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onDetach()
                    mapView.onPause()
                }
                else -> {
                }
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
            Log.d("MapEvents", "init!")
            onInit(mapViewState)
            mapViewState
        },
        modifier = modifier,
    ) { mapView ->
        Log.d("MapEvents", "update!")
        Configuration.getInstance().userAgentValue = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
        onLoad(mapView)
    }
}

@Composable
fun OpenRailwayMapView(
    modifier: Modifier = Modifier,
    onLoad: (map: MapView) -> Unit = { }
) {
    val context = LocalContext.current

    MapView(
        modifier = modifier,
        onLoad = { mapView ->
        },
        onInit = { mapView ->
            mapView.setTileSource(MAPNIK)
            mapView.setMultiTouchControls(true)

            val tileProvider = MapTileProviderBasic(context)
            tileProvider.tileSource = OPENRAILWAYMAP
            val tilesOverlay = TilesOverlay(tileProvider, context).apply {

            }
            mapView.overlays.add(tilesOverlay)

            val copyrightOverlay = CopyrightOverlay(context).apply {
                setAlignRight(true)
                setTextSize(8)
            }
            mapView.overlays.add(copyrightOverlay)

            onLoad(mapView)
        }
    )
}

fun getPolylinesFromFeatureCollection(featureCollection: FeatureCollection, color: Int): List<Polyline> {
    val polylines: MutableList<Polyline> = mutableListOf()

    featureCollection.features?.forEach { feature ->
        val polyline = Polyline()
        feature.geometry?.coordinates?.forEach { coordinate ->
            polyline.addPoint(
                GeoPoint(
                    coordinate[1],
                    coordinate[0]
                )
            )
        }
        polylines.add(polyline)

        polyline.outlinePaint.color = color
    }

    return polylines
}
