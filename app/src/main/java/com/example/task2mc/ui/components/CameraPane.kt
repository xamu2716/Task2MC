package com.example.task2mc.ui.components

import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.task2mc.R
import com.example.task2mc.utils.takePhoto
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "CameraPane"

/**
 * Vista previa de la camara (CameraX) con boton de obturador y boton para
 * cambiar entre camara frontal y trasera.
 */
@Composable
fun CameraPane(
    useFrontCamera: Boolean,
    onToggleCamera: () -> Unit,
    onPhotoSaved: (name: String, uri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // CameraX nos entrega una "superficie" donde dibujar la imagen; la guardamos como estado
    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsStateWithLifecycle()
    val imageCapture = remember { ImageCapture.Builder().build() }

    val selector = if (useFrontCamera) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }

    // Se vuelve a enlazar la camara cada vez que cambia el selector (frontal/trasera)
    LaunchedEffect(selector) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request -> surfaceRequests.value = request }
        }
        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            awaitCancellation()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Camera not available for selector", e)
        } finally {
            provider.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
                implementationMode = ImplementationMode.EMBEDDED,
                contentScale = ContentScale.Crop
            )
        }

        FilledTonalIconButton(
            onClick = onToggleCamera,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Cameraswitch,
                contentDescription = stringResource(R.string.cd_switch_camera)
            )
        }

        FilledTonalIconButton(
            onClick = { takePhoto(context, imageCapture, onPhotoSaved) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = stringResource(R.string.cd_take_photo)
            )
        }
    }
}
