package com.example.task2mc.ui.screens

import androidx.lifecycle.ViewModel
import com.example.task2mc.data.GeoPoint
import com.example.task2mc.data.RoutePhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RouteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    // Llega cada ubicacion nueva del GPS
    fun onLocation(point: GeoPoint) {
        _uiState.update { current ->
            current.copy(
                currentLocation = point,
                routePoints = if (current.isTracking) current.routePoints + point
                else current.routePoints
            )
        }
    }

    // Boton play/stop
    fun onToggleTracking() {
        _uiState.update { current ->
            if (current.isTracking) {
                current.copy(isTracking = false)
            } else {
                current.copy(
                    isTracking = true,
                    routePoints = current.routePoints + listOfNotNull(current.currentLocation)
                )
            }
        }
    }

    // La camara guardo la foto correctamente
    fun onPhotoSaved(name: String, uri: String) {
        _uiState.update { current ->
            current.copy(
                photos = current.photos + RoutePhoto(
                    name = name,
                    uri = uri,
                    location = current.currentLocation
                )
            )
        }
    }

    // TODO tuyo: boton borrar recorrido
    // Boton borrar recorrido
    fun onClearRoute() {
        _uiState.update { current ->
            current.copy(
                routePoints = emptyList(),
                photos = emptyList()
            )
        }
    }
    

    // Boton cambiar camara frontal/trasera
    fun onToggleCamera() {
        _uiState.update { current ->
            current.copy(useFrontCamera = !current.useFrontCamera)
        }
    }
}