package com.example.task2mc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.task2mc.R
import com.example.task2mc.data.GeoPoint
import com.example.task2mc.data.RoutePhoto
import com.example.task2mc.utils.FusedPuckLocationProvider
import com.example.task2mc.utils.loadMarkerBitmap
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.Marker
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions

private fun GeoPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

/**
 * Mapa de Mapbox con: punto azul del usuario (puck), linea del recorrido,
 * marcadores de las fotos y los botones de play/stop y borrar.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun RouteMap(
    routePoints: List<GeoPoint>,
    photos: List<RoutePhoto>,
    currentLocation: GeoPoint?,
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado de la camara del mapa (posicion, zoom). Arranca en Bogota hasta tener el GPS
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(16.0)
            center(Point.fromLngLat(-74.0721, 4.7110))
            pitch(0.0)
            bearing(0.0)
        }
    }

    // El punto azul se alimenta con la misma ubicacion de Google (Fused) que usa el recorrido
    val puckProvider = remember { FusedPuckLocationProvider() }
    LaunchedEffect(currentLocation) {
        currentLocation?.let { puckProvider.update(it.toPoint()) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            composeMapInitOptions = ComposeMapInitOptions(
                pixelRatio = LocalDensity.current.density,
                textureView = true
            ),
            mapViewportState = viewportState,
            scaleBar = {}
        ) {
            // Activa el punto de ubicacion del usuario y hace que la camara lo siga.
            // Se vuelve a ejecutar al dar play/stop para re-centrar el mapa en el usuario.
            MapEffect(isTracking) { mapView ->
                mapView.location.setLocationProvider(puckProvider)
                mapView.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                    puckBearingEnabled = false
                    locationPuck = createDefault2DPuck(withBearing = false)
                }
                viewportState.transitionToFollowPuckState(
                    FollowPuckViewportStateOptions.Builder().zoom(17.0).build()
                )
            }

            // Linea del recorrido: necesita al menos 2 puntos
            if (routePoints.size >= 2) {
                PolylineAnnotation(points = routePoints.map { it.toPoint() }) {
                    lineColor = Color(0xFF0B3D91)
                    lineWidth = 5.0
                }
            }

            // Un marcador por cada foto que tenga ubicacion
            photos.forEach { photo ->
                val location = photo.location
                if (location != null) {
                    key(photo.name) {
                        PhotoMarker(photo = photo, point = location.toPoint())
                    }
                }
            }
        }

        RouteControls(
            isTracking = isTracking,
            pointCount = routePoints.size,
            hasRoute = routePoints.isNotEmpty() || photos.isNotEmpty(),
            onToggleTracking = onToggleTracking,
            onClear = onClear,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )
    }
}

/** Marcador con la miniatura de la foto; mientras carga se muestra un marcador simple. */
@OptIn(MapboxExperimental::class)
@Composable
private fun PhotoMarker(photo: RoutePhoto, point: Point) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, photo.uri) {
        value = loadMarkerBitmap(context, photo.uri)
    }
    val thumbnail = bitmap
    if (thumbnail == null) {
        Marker(point = point, text = photo.name)
    } else {
        val icon = rememberIconImage(
            key = photo.uri,
            painter = BitmapPainter(thumbnail.asImageBitmap())
        )
        PointAnnotation(point = point) {
            iconImage = icon
            textField = photo.name
            textSize = 11.0
            textOffset = listOf(0.0, 2.6)
            textColor = Color.Black
            textHaloColor = Color.White
            textHaloWidth = 1.5
        }
    }
}

@Composable
private fun RouteControls(
    isTracking: Boolean,
    pointCount: Int,
    hasRoute: Boolean,
    onToggleTracking: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasRoute) {
            FilledTonalIconButton(
                onClick = onClear,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = stringResource(R.string.cd_clear_route)
                )
            }
        }

        BadgedBox(
            badge = {
                if (pointCount > 0) {
                    Badge { Text(pointCount.toString()) }
                }
            }
        ) {
            FilledTonalIconButton(
                onClick = onToggleTracking,
                shape = RoundedCornerShape(16.dp),
                colors = if (isTracking) {
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    IconButtonDefaults.filledTonalIconButtonColors()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (isTracking) R.string.cd_stop_route else R.string.cd_start_route
                    )
                )
            }
        }
    }
}
