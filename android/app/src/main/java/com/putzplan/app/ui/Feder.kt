package com.putzplan.app.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.federPress(
    pressScale: Float = 0.97f,
    akzent: Color? = null,
    form: Shape = MaterialTheme.shapes.medium,
    ruheElevation: Dp = 6.dp,
    gedruecktElevation: Dp = 2.dp
): Modifier = composed {
    val density = LocalDensity.current
    val maxOffset = with(density) { 3.dp.toPx() }

    var zielScale by remember { mutableStateOf(1f) }
    var zielX by remember { mutableStateOf(0f) }
    var zielY by remember { mutableStateOf(0f) }
    var zielElevation by remember { mutableStateOf(ruheElevation) }

    val feder = spring<Float>(dampingRatio = 0.72f, stiffness = 350f)
    val scale by animateFloatAsState(zielScale, feder, label = "federScale")
    val offX by animateFloatAsState(zielX, feder, label = "federX")
    val offY by animateFloatAsState(zielY, feder, label = "federY")
    val elevation by animateDpAsState(
        zielElevation,
        spring(dampingRatio = 0.72f, stiffness = 350f),
        label = "federElev"
    )

    val schatten = akzent?.copy(alpha = 0.30f) ?: Color.Black.copy(alpha = 0.22f)

    this
        .shadow(
            elevation = elevation,
            shape = form,
            clip = false,
            ambientColor = schatten,
            spotColor = schatten
        )
        .pointerInput(Unit) {
            detectTapGestures { pos ->
                val cx = size.width / 2f
                val cy = size.height / 2f
                zielX = (pos.x - cx).coerceIn(-maxOffset, maxOffset)
                zielY = (pos.y - cy).coerceIn(-maxOffset, maxOffset)
                zielScale = pressScale
                zielElevation = gedruecktElevation
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    waitForUpOrCancellation()
                }
                zielScale = 1f
                zielX = 0f
                zielY = 0f
                zielElevation = ruheElevation
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offX
            translationY = offY
        }
}
