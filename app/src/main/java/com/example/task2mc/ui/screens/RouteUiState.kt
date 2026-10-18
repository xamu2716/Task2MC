package com.example.task2mc.ui.screens

import com.example.task2mc.data.GeoPoint
import com.example.task2mc.data.RoutePhoto

data class RouteUiState(
    val isTracking: Boolean = false,
    val routePoints: List<GeoPoint> = emptyList(),
    val photos: List<RoutePhoto> = emptyList(),
    val currentLocation: GeoPoint? = null,
    val useFrontCamera: Boolean = false
)