package com.example.task2mc.utils

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider

/**
 * Proveedor de ubicacion para el punto azul (puck) de Mapbox.
 * En vez de que Mapbox consulte su propio GPS, le pasamos la ubicacion de Google (Fused),
 * la misma que usamos para el recorrido y para las fotos.
 */
class FusedPuckLocationProvider : LocationProvider {

    private var consumer: LocationConsumer? = null
    private var last: Point? = null

    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        consumer = locationConsumer
        last?.let { locationConsumer.onLocationUpdated(it) }
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        if (consumer === locationConsumer) consumer = null
    }

    fun update(point: Point) {
        last = point
        consumer?.onLocationUpdated(point)
    }
}
