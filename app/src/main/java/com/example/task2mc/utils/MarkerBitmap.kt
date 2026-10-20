package com.example.task2mc.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.createBitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap

/**
 * Carga la foto (uri) con Coil y la convierte en una miniatura circular con borde
 * blanco, lista para usarse como icono de un marcador del mapa.
 */
suspend fun loadMarkerBitmap(context: Context, uri: String, sizePx: Int = 128): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(uri)
        .size(Size(sizePx, sizePx))
        .allowHardware(false)
        .build()
    val result = SingletonImageLoader.get(context).execute(request)
    val source = (result as? SuccessResult)?.image?.toBitmap() ?: return null
    return circularThumbnail(source, sizePx)
}

private fun circularThumbnail(source: Bitmap, sizePx: Int): Bitmap {
    val border = sizePx * 0.06f
    val output = createBitmap(sizePx, sizePx)
    val canvas = Canvas(output)

    // Recorte central cuadrado de la foto original
    val side = minOf(source.width, source.height)
    val left = (source.width - side) / 2
    val top = (source.height - side) / 2
    val square = Bitmap.createBitmap(source, left, top, side, side)
    val scaled = Bitmap.createScaledBitmap(square, sizePx, sizePx, true)

    val clip = Path().apply {
        addCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - border, Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(clip)
    canvas.drawBitmap(scaled, 0f, 0f, null)
    canvas.restore()

    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = border
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - border / 2f, ring)
    return output
}
