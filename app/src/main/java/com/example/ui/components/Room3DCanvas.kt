package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.MetaverseParticipant
import com.example.data.model.RoomObject3D
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.AccentYellowBright
import com.example.ui.theme.AccentYellowMuted
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHigh
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private sealed class RenderItem(val depth: Float) {
    class Obj(val obj: RoomObject3D, depth: Float) : RenderItem(depth)
    class Avatar(val p: MetaverseParticipant, depth: Float) : RenderItem(depth)
}

@Composable
fun Room3DCanvas(
    modifier: Modifier = Modifier,
    theme: String,
    participants: List<MetaverseParticipant>,
    roomObjects: List<RoomObject3D>,
    onTapFloor: (worldX: Float, worldY: Float) -> Unit,
    onSelectParticipant: (MetaverseParticipant) -> Unit,
    onTapObject: (RoomObject3D) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "room_anim")
    val animTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_tick"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(participants, roomObjects) {
                detectTapGestures { tapOffset ->
                    val cx = size.width / 2f
                    val cy = size.height / 2f - 40f
                    val tileW = 48f
                    val tileH = 26f

                    val tappedParticipant = participants.findLast { p ->
                        val (sx, sy) = worldToScreen(p.x, p.y, cx, cy, tileW, tileH)
                        hypot(tapOffset.x - sx, tapOffset.y - (sy - 40f)) < 42f
                    }
                    if (tappedParticipant != null) {
                        onSelectParticipant(tappedParticipant)
                        return@detectTapGestures
                    }

                    val tappedObj = roomObjects.findLast { obj ->
                        val (sx, sy) = worldToScreen(obj.x, obj.y, cx, cy, tileW, tileH)
                        hypot(tapOffset.x - sx, tapOffset.y - (sy - 20f)) < 38f
                    }
                    if (tappedObj != null) {
                        onTapObject(tappedObj)
                        return@detectTapGestures
                    }

                    val (wx, wy) = screenToWorld(tapOffset.x, tapOffset.y, cx, cy, tileW, tileH)
                    onTapFloor(wx, wy)
                }
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f - 40f
        val tileW = 48f
        val tileH = 26f

        // 1. Draw Isometric 3D Studded Baseplate in Black & Yellow
        drawIsometric3DFloor(theme, cx, cy, tileW, tileH)

        // 2. Depth-sort 3D Objects + 3D Avatars
        val renderItems = mutableListOf<RenderItem>()
        for (obj in roomObjects) {
            val (_, sy) = worldToScreen(obj.x, obj.y, cx, cy, tileW, tileH)
            renderItems.add(RenderItem.Obj(obj, sy))
        }
        for (p in participants) {
            val (_, sy) = worldToScreen(p.x, p.y, cx, cy, tileW, tileH)
            renderItems.add(RenderItem.Avatar(p, sy))
        }

        renderItems.sortBy { it.depth }

        // 3. Render 3D Objects and 3D Avatars
        for (item in renderItems) {
            when (item) {
                is RenderItem.Obj -> {
                    val (sx, sy) = worldToScreen(item.obj.x, item.obj.y, cx, cy, tileW, tileH)
                    drawRoomObject3D(item.obj, sx, sy, animTick)
                }
                is RenderItem.Avatar -> {
                    val (sx, sy) = worldToScreen(item.p.x, item.p.y, cx, cy, tileW, tileH)
                    drawIsometric3DAvatar(item.p, sx, sy, animTick)
                }
            }
        }
    }
}

private fun worldToScreen(
    wx: Float,
    wy: Float,
    cx: Float,
    cy: Float,
    tileW: Float,
    tileH: Float
): Pair<Float, Float> {
    val sx = cx + (wx - wy) * (tileW * 0.5f)
    val sy = cy + (wx + wy) * (tileH * 0.5f)
    return Pair(sx, sy)
}

private fun screenToWorld(
    sx: Float,
    sy: Float,
    cx: Float,
    cy: Float,
    tileW: Float,
    tileH: Float
): Pair<Float, Float> {
    val dx = (sx - cx) / (tileW * 0.5f)
    val dy = (sy - cy) / (tileH * 0.5f)
    val wx = (dx + dy) * 0.5f
    val wy = (dy - dx) * 0.5f
    return Pair(wx, wy)
}

private fun DrawScope.drawIsometric3DFloor(
    theme: String,
    cx: Float,
    cy: Float,
    tileW: Float,
    tileH: Float
) {
    val gridSize = 8
    val isIronCafe = theme.contains("Iron", ignoreCase = true) || theme.contains("Cafe", ignoreCase = true)

    for (x in -gridSize until gridSize) {
        for (y in -gridSize until gridSize) {
            val (p1x, p1y) = worldToScreen(x.toFloat(), y.toFloat(), cx, cy, tileW, tileH)
            val (p2x, p2y) = worldToScreen((x + 1).toFloat(), y.toFloat(), cx, cy, tileW, tileH)
            val (p3x, p3y) = worldToScreen((x + 1).toFloat(), (y + 1).toFloat(), cx, cy, tileW, tileH)
            val (p4x, p4y) = worldToScreen(x.toFloat(), (y + 1).toFloat(), cx, cy, tileW, tileH)

            val path = Path().apply {
                moveTo(p1x, p1y)
                lineTo(p2x, p2y)
                lineTo(p3x, p3y)
                lineTo(p4x, p4y)
                close()
            }

            val isEven = (x + y) % 2 == 0
            val isDanceFloor = isIronCafe && x in -3..2 && y in -3..2
            val tileColor = when {
                isDanceFloor && isEven -> AccentYellow.copy(alpha = 0.22f)
                isDanceFloor -> Color(0xFF141414)
                isEven -> CyberSurface
                else -> CyberSurfaceHigh
            }

            drawPath(path, tileColor)
            drawPath(path, CyberBorder, style = Stroke(width = 1f))

            // 3D Stud on each tile
            val tileCenterX = (p1x + p3x) * 0.5f
            val tileCenterY = (p1y + p3y) * 0.5f

            drawOval(
                CyberBlack.copy(alpha = 0.65f),
                Offset(tileCenterX - 5f, tileCenterY - 1.8f),
                Size(10f, 5f)
            )
            drawOval(
                if (isDanceFloor && isEven) AccentYellow.copy(alpha = 0.7f) else Color(0xFF2D2D2D),
                Offset(tileCenterX - 4.5f, tileCenterY - 2.8f),
                Size(9f, 4f)
            )
        }
    }

    // Yellow 3D Baseplate Perimeter Border
    val (c1x, c1y) = worldToScreen(-gridSize.toFloat(), -gridSize.toFloat(), cx, cy, tileW, tileH)
    val (c2x, c2y) = worldToScreen(gridSize.toFloat(), -gridSize.toFloat(), cx, cy, tileW, tileH)
    val (c3x, c3y) = worldToScreen(gridSize.toFloat(), gridSize.toFloat(), cx, cy, tileW, tileH)
    val (c4x, c4y) = worldToScreen(-gridSize.toFloat(), gridSize.toFloat(), cx, cy, tileW, tileH)

    val borderPath = Path().apply {
        moveTo(c1x, c1y)
        lineTo(c2x, c2y)
        lineTo(c3x, c3y)
        lineTo(c4x, c4y)
        close()
    }
    drawPath(borderPath, AccentYellow, style = Stroke(width = 2.5f))

    // Center Spawn Pad
    val (centerX, centerY) = worldToScreen(0f, 0f, cx, cy, tileW, tileH)
    drawOval(
        AccentYellow.copy(alpha = 0.35f),
        Offset(centerX - 34f, centerY - 17f),
        Size(68f, 34f),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawRoomObject3D(
    obj: RoomObject3D,
    sx: Float,
    sy: Float,
    animTick: Float
) {
    when (obj.type) {
        "DJ_BOOTH" -> {
            drawIsometric3DBox(
                x = sx - 28f,
                y = sy - 34f,
                w = 56f,
                h = 30f,
                depthX = 10f,
                depthY = -6f,
                frontColor = CyberSurfaceHigh,
                topColor = Color(0xFF2C2C2C),
                sideColor = CyberBlack,
                borderColor = AccentYellow
            )
            val pulse = 9f + sin(animTick * 6f) * 2f
            drawCircle(AccentYellow, pulse, Offset(sx - 14f, sy - 19f), style = Stroke(2f))
            drawCircle(AccentYellow, pulse, Offset(sx + 14f, sy - 19f), style = Stroke(2f))
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(255, 214, 0)
                    textSize = 16f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("JUKEBOX", sx, sy - 44f, paint)
            }
        }
        "DISCO_BALL" -> {
            val ballY = sy - 105f
            drawLine(AccentYellow, Offset(sx, ballY - 30f), Offset(sx, ballY), strokeWidth = 2f)
            drawCircle(AccentYellow, 16f, Offset(sx, ballY))
            drawCircle(CyberBlack, 16f, Offset(sx, ballY), style = Stroke(2f))
            for (i in 0 until 4) {
                val angle = animTick + i * (PI.toFloat() / 2f)
                val bx = sx + cos(angle) * 48f
                val by = sy + sin(angle) * 20f
                drawOval(AccentYellow.copy(alpha = 0.25f), Offset(bx - 12f, by - 6f), Size(24f, 12f))
            }
        }
        "NEON_SIGN" -> {
            val hoverY = sy - 74f + sin(animTick * 2f) * 3f
            drawIsometric3DBox(
                x = sx - 64f,
                y = hoverY,
                w = 128f,
                h = 28f,
                depthX = 6f,
                depthY = -4f,
                frontColor = CyberBlack,
                topColor = CyberSurfaceHigh,
                sideColor = CyberSurface,
                borderColor = AccentYellowBright
            )
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(255, 214, 0)
                    textSize = 19f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("CRYAMANIA 3D", sx, hoverY + 20f, paint)
            }
        }
        "LOUNGE_SOFA" -> {
            drawIsometric3DBox(
                x = sx - 26f,
                y = sy - 24f,
                w = 52f,
                h = 20f,
                depthX = 8f,
                depthY = -5f,
                frontColor = CyberSurfaceHigh,
                topColor = AccentYellowMuted,
                sideColor = CyberBlack,
                borderColor = AccentYellow
            )
        }
        "ARCADE_CABINET" -> {
            drawIsometric3DBox(
                x = sx - 16f,
                y = sy - 46f,
                w = 32f,
                h = 42f,
                depthX = 8f,
                depthY = -5f,
                frontColor = CyberSurface,
                topColor = CyberSurfaceHigh,
                sideColor = CyberBlack,
                borderColor = AccentYellow
            )
            drawRect(AccentYellow, Offset(sx - 11f, sy - 38f), Size(22f, 15f))
        }
        else -> {
            drawCircle(AccentYellow, 10f, Offset(sx, sy - 15f))
        }
    }
}
