package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VoidSurfaceHigh
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    radiusDp: Float = 60f,
    thumbRadiusDp: Float = 24f,
    onMove: (dx: Float, dy: Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val maxDistance = radiusDp * 1.5f

    Box(
        modifier = modifier
            .size((radiusDp * 2).dp)
            .clip(CircleShape)
            .background(VoidSurfaceHigh.copy(alpha = 0.55f))
            .border(2.dp, NeonCyan.copy(alpha = 0.35f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        val dist = hypot(newX, newY)

                        if (dist <= maxDistance) {
                            offsetX = newX
                            offsetY = newY
                        } else {
                            val angle = atan2(newY.toDouble(), newX.toDouble())
                            offsetX = (cos(angle) * maxDistance).toFloat()
                            offsetY = (sin(angle) * maxDistance).toFloat()
                        }

                        // Normalize between -1f and 1f
                        val normalizedX = (offsetX / maxDistance).coerceIn(-1f, 1f)
                        val normalizedY = (offsetY / maxDistance).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Center crosshair
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )

        // Draggable thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size((thumbRadiusDp * 2).dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.85f))
                .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        )
    }
}
