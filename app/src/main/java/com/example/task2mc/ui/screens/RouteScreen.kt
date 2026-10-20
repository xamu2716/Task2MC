package com.example.task2mc.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.task2mc.R
import com.example.task2mc.data.LocationTracker
import com.example.task2mc.ui.components.CameraPane
import com.example.task2mc.ui.components.PermissionDeniedMessage
import com.example.task2mc.ui.components.PhotoStrip
import com.example.task2mc.ui.components.RouteMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteScreen(
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Permisos: se piden todos juntos al iniciar
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    val cameraGranted = permissionsState.permissions.any {
        it.permission == Manifest.permission.CAMERA && it.status.isGranted
    }
    val locationGranted = permissionsState.permissions.any {
        (it.permission == Manifest.permission.ACCESS_FINE_LOCATION ||
            it.permission == Manifest.permission.ACCESS_COARSE_LOCATION) &&
            it.status.isGranted
    }

    // El GPS solo se escucha mientras la pantalla esta visible y hay permiso de ubicacion
    val tracker = remember { LocationTracker(context.applicationContext) }
    LaunchedEffect(locationGranted, lifecycleOwner) {
        if (locationGranted) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tracker.locationUpdates().collect { viewModel.onLocation(it) }
            }
        }
    }

    val cameraRegion: @Composable (Modifier) -> Unit = { regionModifier ->
        Column(
            modifier = regionModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RegionCard(modifier = Modifier.weight(1f)) {
                if (cameraGranted) {
                    CameraPane(
                        useFrontCamera = state.useFrontCamera,
                        onToggleCamera = viewModel::onToggleCamera,
                        onPhotoSaved = viewModel::onPhotoSaved
                    )
                } else {
                    PermissionDeniedMessage(
                        message = stringResource(R.string.permission_camera_denied)
                    )
                }
            }
            // La tira de fotos solo aparece cuando hay fotos y la camara esta habilitada
            AnimatedVisibility(visible = cameraGranted && state.photos.isNotEmpty()) {
                PhotoStrip(photos = state.photos)
            }
        }
    }

    val mapRegion: @Composable (Modifier) -> Unit = { regionModifier ->
        RegionCard(modifier = regionModifier) {
            if (locationGranted) {
                RouteMap(
                    routePoints = state.routePoints,
                    photos = state.photos,
                    currentLocation = state.currentLocation,
                    isTracking = state.isTracking,
                    onToggleTracking = viewModel::onToggleTracking,
                    onClear = viewModel::onClearRoute
                )
            } else {
                PermissionDeniedMessage(
                    message = stringResource(R.string.permission_location_denied)
                )
            }
        }
    }

    // Vertical: camara arriba y mapa abajo. Horizontal (tablet): lado a lado.
    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(8.dp)) {
        if (maxWidth > maxHeight) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cameraRegion(Modifier.weight(1f).fillMaxSize())
                mapRegion(Modifier.weight(1f).fillMaxSize())
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cameraRegion(Modifier.weight(1f).fillMaxWidth())
                mapRegion(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RegionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    OutlinedCard(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
