package com.example.task2mc.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.task2mc.data.RoutePhoto

/** Lista horizontal con las miniaturas de las fotos tomadas en el recorrido. */
@Composable
fun PhotoStrip(
    photos: List<RoutePhoto>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Cada foto nueva se agrega al final: desplazamos la lista para mostrarla
    LaunchedEffect(photos.size) {
        if (photos.isNotEmpty()) listState.animateScrollToItem(photos.lastIndex)
    }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos, key = { it.name }) { photo ->
                AsyncImage(
                    model = photo.uri,
                    contentDescription = photo.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
