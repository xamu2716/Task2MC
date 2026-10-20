package com.example.task2mc.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "LocationTracker"

/**
 * Envuelve FusedLocationProviderClient y expone las ubicaciones como un Flow.
 * Al dejar de recolectar el Flow, la suscripcion al GPS se cancela sola.
 */
class LocationTracker(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // el permiso se valida antes, en RouteScreen
    fun locationUpdates(): Flow<GeoPoint> = callbackFlow {
        Log.d(TAG, "Subscribing to location updates")

        // 1) Ultima ubicacion conocida (puede ser null en un dispositivo recien encendido)
        client.lastLocation
            .addOnSuccessListener { location ->
                Log.d(TAG, "lastLocation = $location")
                location?.let { trySend(GeoPoint(it.latitude, it.longitude)) }
            }
            .addOnFailureListener { Log.e(TAG, "lastLocation failed", it) }

        // 2) Una lectura puntual, para no esperar al primer ciclo de actualizaciones
        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                Log.d(TAG, "currentLocation = $location")
                location?.let { trySend(GeoPoint(it.latitude, it.longitude)) }
            }
            .addOnFailureListener { Log.e(TAG, "currentLocation failed", it) }

        // 3) Actualizaciones continuas
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach {
                    Log.d(TAG, "update = ${it.latitude}, ${it.longitude} (acc ${it.accuracy} m)")
                    trySend(GeoPoint(it.latitude, it.longitude))
                }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { Log.e(TAG, "requestLocationUpdates failed", it) }

        awaitClose {
            Log.d(TAG, "Unsubscribing from location updates")
            tokenSource.cancel()
            client.removeLocationUpdates(callback)
        }
    }
}
