package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetaverseParticipant
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.AccentYellowBright
import com.example.ui.theme.AccentYellowMuted
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive 3D Avatar & Shirt Preview Card for Menu and Shop.
 * Supports horizontal touch-drag rotation to inspect 3D extruded torso, shirts, sleeves, and hats.
 */
@Composable
fun Avatar3DPreviewCard(
    username: String,
    avatarColorHex: String,
    avatarOutfit: String,
    avatarHat: String,
    avatarAura: String,
    isDeveloper: Boolean = false,
    isCryaAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    var yawAngle by remember { mutableFloatStateOf(25f) }
    val infiniteTransition = rememberInfiniteTransition(label = "preview_3d_anim")
    val animTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick"
    )

    val previewParticipant = remember(username, avatarColorHex, avatarOutfit, avatarHat, avatarAura, isDeveloper, isCryaAdmin, yawAngle) {
        MetaverseParticipant(
            id = "preview_me",
            username = username,
            isMe = true,
            isDeveloper = isDeveloper,
            isCryaAdmin = isCryaAdmin,
            x = 0f,
            y = 0f,
            facingAngle = yawAngle,
            isWalking = false,
            avatarColorHex = avatarColorHex,
            avatarHat = avatarHat,
            avatarOutfit = avatarOutfit,
            avatarAura = avatarAura
        )
    }

    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow.copy(alpha = 0.7f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive 3D Viewport Box
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberBlack)
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            yawAngle = (yawAngle + dragAmount * 0.8f) % 360f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height * 0.78f
                    // Draw 3D yellow/black studded pedestal under avatar
                    drawIsometric3DBox(
                        x = cx - 34f,
                        y = cy - 6f,
                        w = 68f,
                        h = 10f,
                        depthX = 12f,
                        depthY = -7f,
                        frontColor = Color(0xFF1E1E1E),
                        topColor = Color(0xFF2B2B2B),
                        sideColor = Color(0xFF121212),
                        borderColor = AccentYellow.copy(alpha = 0.6f)
                    )
                    // Draw scaled 3D Avatar
                    drawIsometric3DAvatar(
                        p = previewParticipant,
                        sx = cx,
                        sy = cy - 8f,
                        animTick = animTick,
                        scale = 1.45f,
                        showNameplate = false
                    )
                }

                Text(
                    text = "3D • DRAG TO ROTATE",
                    color = AccentYellow.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Equipped 3D Loadout Summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "3D AVATAR RIG",
                    color = AccentYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = username,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isDeveloper) {
                        DevYellowHammerBadge(sizeDp = 20f)
                    }
                }

                LoadoutBadgeRow(label = "3D SHIRT", value = avatarOutfit)
                LoadoutBadgeRow(label = "3D HAT", value = avatarHat)
                LoadoutBadgeRow(label = "EFFECT", value = avatarAura)
            }
        }
    }
}

/**
 * Custom minimalistic Black & Yellow Hammer Badge displayed next to Developer names.
 */
@Composable
fun DevYellowHammerBadge(
    modifier: Modifier = Modifier,
    sizeDp: Float = 18f
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape((sizeDp * 0.28f).dp))
            .background(CyberBlack)
            .border(1.2.dp, AccentYellow, RoundedCornerShape((sizeDp * 0.28f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCustomYellowHammer(
                cx = size.width * 0.5f,
                cy = size.height * 0.5f,
                r = size.minDimension * 0.36f
            )
        }
    }
}

fun DrawScope.drawCustomYellowHammer(cx: Float, cy: Float, r: Float) {
    // Diagonal wooden/steel handle in vibrant yellow
    val handlePath = Path().apply {
        moveTo(cx - r * 0.15f, cy - r * 0.1f)
        lineTo(cx + r * 0.15f, cy + r * 0.15f)
        lineTo(cx - r * 0.55f, cy + r * 0.85f)
        lineTo(cx - r * 0.82f, cy + r * 0.58f)
        close()
    }
    drawPath(handlePath, AccentYellow)

    // Hammer head block (angled top-right / top-left)
    val headPath = Path().apply {
        moveTo(cx - r * 0.45f, cy - r * 0.72f)
        lineTo(cx + r * 0.78f, cy + r * 0.05f)
        lineTo(cx + r * 0.48f, cy + r * 0.48f)
        lineTo(cx - r * 0.75f, cy - r * 0.28f)
        close()
    }
    drawPath(headPath, AccentYellowBright)
    drawPath(headPath, CyberBlack, style = Stroke(width = 1.2f))
}

@Composable
private fun LoadoutBadgeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceHigh)
            .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = AccentYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Draws an isometric 3D extruded cuboid (Front Face + Top Face + Side 3D Extruded Face)
 */
fun DrawScope.drawIsometric3DBox(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    depthX: Float,
    depthY: Float,
    frontColor: Color,
    topColor: Color,
    sideColor: Color,
    borderColor: Color = CyberBlack
) {
    // 1. Top Face (parallelogram extruded by depthX, depthY)
    val topPath = Path().apply {
        moveTo(x, y)
        lineTo(x + depthX, y + depthY)
        lineTo(x + w + depthX, y + depthY)
        lineTo(x + w, y)
        close()
    }
    drawPath(topPath, topColor)
    drawPath(topPath, borderColor, style = Stroke(1.1f))

    // 2. Side Face (Right or Left depending on depthX sign)
    val sidePath = Path().apply {
        if (depthX >= 0f) {
            moveTo(x + w, y)
            lineTo(x + w + depthX, y + depthY)
            lineTo(x + w + depthX, y + h + depthY)
            lineTo(x + w, y + h)
        } else {
            moveTo(x, y)
            lineTo(x + depthX, y + depthY)
            lineTo(x + depthX, y + h + depthY)
            lineTo(x, y + h)
        }
        close()
    }
    drawPath(sidePath, sideColor)
    drawPath(sidePath, borderColor, style = Stroke(1.1f))

    // 3. Front Face
    drawRect(frontColor, Offset(x, y), Size(w, h))
    drawRect(borderColor, Offset(x, y), Size(w, h), style = Stroke(1.2f))
}

/**
 * Renders a full 3D Volumetric Blocky Avatar with 3D Shirts, Sleeves, Hats, and Auras.
 */
fun DrawScope.drawIsometric3DAvatar(
    p: MetaverseParticipant,
    sx: Float,
    sy: Float,
    animTick: Float,
    scale: Float = 1.0f,
    showNameplate: Boolean = true
) {
    val rawSkin = try {
        Color(android.graphics.Color.parseColor(p.avatarColorHex))
    } catch (_: Exception) {
        AccentYellow
    }

    val skinFront = rawSkin
    val skinTop = lightenColor(rawSkin, 0.18f)
    val skinSide = darkenColor(rawSkin, 0.25f)

    // Determine 3D perspective extrusion based on facingAngle
    val rad = Math.toRadians(p.facingAngle.toDouble())
    val dirSign = if (cos(rad) >= -0.2) 1f else -1f
    val dx3d = 5.5f * scale * dirSign
    val dy3d = -3.8f * scale

    // Walking & Emote kinematics
    var emoteYOffset = 0f
    var leftLegSwing = 0f
    var rightLegSwing = 0f
    var armWave = 0f

    if (p.isWalking) {
        val walkCycle = sin(animTick * 8f) * 6f * scale
        leftLegSwing = walkCycle
        rightLegSwing = -walkCycle
    }

    when (p.currentEmote?.lowercase()) {
        "dance" -> {
            emoteYOffset = sin(animTick * 6f) * 5f * scale
            armWave = cos(animTick * 6f) * 9f * scale
        }
        "wave" -> {
            armWave = sin(animTick * 8f) * 12f * scale
        }
        "cheer" -> {
            emoteYOffset = -kotlin.math.abs(sin(animTick * 6f)) * 14f * scale
            armWave = 14f * scale
        }
        "laugh" -> {
            emoteYOffset = sin(animTick * 12f) * 3f * scale
        }
        "backflip" -> {
            emoteYOffset = -kotlin.math.abs(sin(animTick * 4f)) * 20f * scale
        }
        "sit" -> {
            emoteYOffset = 11f * scale
        }
    }

    val baseY = sy + emoteYOffset

    // 1. Ground Shadow
    drawOval(
        CyberBlack.copy(alpha = 0.65f),
        Offset(sx - 20f * scale, sy - 6f * scale),
        Size(40f * scale, 13f * scale)
    )

    // 2. 3D Aura Effects
    when (p.avatarAura) {
        "Starlight Sparkles", "Silver Sparkles", "Gold Sparkles" -> {
            for (s in 0 until 4) {
                val sparkX = sx + cos(animTick * 2.2f + s * 1.57f) * 26f * scale
                val sparkY = baseY - 26f * scale + sin(animTick * 2.2f + s * 1.57f) * 18f * scale
                drawCircle(AccentYellow, 3.2f * scale, Offset(sparkX, sparkY))
            }
        }
        "Cyan Plasma Flames", "Noir Smoke Flames", "Black & Yellow Aura" -> {
            drawOval(
                AccentYellow.copy(alpha = 0.28f),
                Offset(sx - 26f * scale, baseY - 48f * scale),
                Size(52f * scale, 50f * scale),
                style = Stroke(2.2f * scale)
            )
        }
        "Rainbow Trail", "Monochrome Trail", "Yellow Volt Trail" -> {
            for (r in 0 until 3) {
                val c = if (r % 2 == 0) AccentYellow else Color.White
                drawCircle(
                    c.copy(alpha = 0.45f),
                    (7f - r * 2f) * scale,
                    Offset(sx - 11f * (r + 1) * scale, baseY - 6f * scale)
                )
            }
        }
    }

    // Resolve 3D Shirt / Outfit Palette & Design
    val shirtSpec = resolve3DShirtSpec(p.avatarOutfit, skinFront)

    // 3. 3D Blocky Legs (Pants)
    val legW = 8.5f * scale
    val legH = 16f * scale
    if (p.currentEmote?.lowercase() != "sit") {
        // Left Leg 3D Box
        drawIsometric3DBox(
            x = sx - 9.5f * scale,
            y = baseY - legH + leftLegSwing,
            w = legW,
            h = legH,
            depthX = dx3d * 0.75f,
            depthY = dy3d * 0.75f,
            frontColor = shirtSpec.pantsColor,
            topColor = lightenColor(shirtSpec.pantsColor, 0.15f),
            sideColor = darkenColor(shirtSpec.pantsColor, 0.25f)
        )
        // Shoe trim
        drawRect(
            AccentYellow,
            Offset(sx - 9.5f * scale, baseY - 3f * scale + leftLegSwing),
            Size(legW, 2.5f * scale)
        )

        // Right Leg 3D Box
        drawIsometric3DBox(
            x = sx + 1f * scale,
            y = baseY - legH + rightLegSwing,
            w = legW,
            h = legH,
            depthX = dx3d * 0.75f,
            depthY = dy3d * 0.75f,
            frontColor = shirtSpec.pantsColor,
            topColor = lightenColor(shirtSpec.pantsColor, 0.15f),
            sideColor = darkenColor(shirtSpec.pantsColor, 0.25f)
        )
        drawRect(
            AccentYellow,
            Offset(sx + 1f * scale, baseY - 3f * scale + rightLegSwing),
            Size(legW, 2.5f * scale)
        )
    } else {
        drawIsometric3DBox(
            x = sx - 9f * scale,
            y = baseY - 8f * scale,
            w = 18f * scale,
            h = 8f * scale,
            depthX = dx3d,
            depthY = dy3d,
            frontColor = shirtSpec.pantsColor,
            topColor = lightenColor(shirtSpec.pantsColor, 0.15f),
            sideColor = darkenColor(shirtSpec.pantsColor, 0.25f)
        )
    }

    // 4. Back Arm (Left Arm in 3D space)
    val torsoW = 21f * scale
    val torsoH = 21f * scale
    val torsoY = baseY - 37f * scale
    val armW = 7f * scale
    val armH = 19f * scale

    draw3DArmWithSleeve(
        x = sx - torsoW / 2f - armW - 1f * scale,
        y = torsoY + rightLegSwing * 0.35f,
        w = armW,
        h = armH,
        dx3d = dx3d * 0.7f,
        dy3d = dy3d * 0.7f,
        sleeveColor = shirtSpec.sleeveColor,
        skinFront = skinFront,
        skinSide = skinSide,
        shortSleeve = shirtSpec.isShortSleeve,
        scale = scale
    )

    // 5. 3D Extruded Torso & Detailed 3D Shirt Graphic
    drawIsometric3DBox(
        x = sx - torsoW / 2f,
        y = torsoY,
        w = torsoW,
        h = torsoH,
        depthX = dx3d,
        depthY = dy3d,
        frontColor = shirtSpec.torsoColor,
        topColor = lightenColor(shirtSpec.torsoColor, 0.2f),
        sideColor = darkenColor(shirtSpec.torsoColor, 0.3f),
        borderColor = AccentYellow.copy(alpha = 0.85f)
    )

    // Render specific 3D Shirt / Jacket details on the torso front face
    draw3DShirtDetails(
        outfitName = p.avatarOutfit,
        tx = sx - torsoW / 2f,
        ty = torsoY,
        tw = torsoW,
        th = torsoH,
        scale = scale
    )

    // 6. Front Arm (Right Arm in 3D space)
    val rightArmY = if (armWave != 0f) torsoY - 8f * scale - armWave else torsoY + leftLegSwing * 0.35f
    draw3DArmWithSleeve(
        x = sx + torsoW / 2f + 1f * scale,
        y = rightArmY,
        w = armW,
        h = armH,
        dx3d = dx3d * 0.7f,
        dy3d = dy3d * 0.7f,
        sleeveColor = shirtSpec.sleeveColor,
        skinFront = skinFront,
        skinSide = skinSide,
        shortSleeve = shirtSpec.isShortSleeve,
        scale = scale
    )

    // 7. 3D Extruded Blocky Head + 3D Stud
    val headSize = 16.5f * scale
    val headY = torsoY - headSize - 1.5f * scale
    drawIsometric3DBox(
        x = sx - headSize / 2f,
        y = headY,
        w = headSize,
        h = headSize,
        depthX = dx3d * 0.85f,
        depthY = dy3d * 0.85f,
        frontColor = skinFront,
        topColor = skinTop,
        sideColor = skinSide,
        borderColor = CyberBlack
    )

    // 3D Top Stud on Head
    drawOval(
        skinTop,
        Offset(sx - 4f * scale + dx3d * 0.35f, headY - 3.2f * scale + dy3d * 0.35f),
        Size(8f * scale, 3.5f * scale)
    )
    drawOval(
        CyberBlack,
        Offset(sx - 4f * scale + dx3d * 0.35f, headY - 3.2f * scale + dy3d * 0.35f),
        Size(8f * scale, 3.5f * scale),
        style = Stroke(1f * scale)
    )

    // Classic Face (Eyes & Smile)
    val eyeSize = 2.2f * scale
    drawRect(CyberBlack, Offset(sx - 4.2f * scale, headY + 5.5f * scale), Size(eyeSize, eyeSize))
    drawRect(CyberBlack, Offset(sx + 2.0f * scale, headY + 5.5f * scale), Size(eyeSize, eyeSize))
    drawLine(
        CyberBlack,
        Offset(sx - 3.6f * scale, headY + 11.5f * scale),
        Offset(sx + 3.6f * scale, headY + 11.5f * scale),
        strokeWidth = 1.6f * scale
    )

    // 8. 3D Hats & Headgear
    draw3DHat(
        hatName = p.avatarHat,
        sx = sx,
        headY = headY,
        headSize = headSize,
        dx3d = dx3d,
        dy3d = dy3d,
        scale = scale
    )

    // 9. Voice Chat Audio Rings in Yellow
    if (p.isSpeaking && p.voiceLevel > 0.05f) {
        val ringPulse = (animTick * 3f) % 1f
        val ringRadius = (24f + ringPulse * 16f) * scale
        drawCircle(
            AccentYellow.copy(alpha = (1f - ringPulse) * 0.85f),
            ringRadius,
            Offset(sx, headY + 6f * scale),
            style = Stroke(2.2f * scale)
        )
    }

    if (!showNameplate) return

    // 10. Minimalist Black & Yellow Billboard Nameplate & Speech Bubble
    val nameplateY = headY - (if (p.avatarHat != "None") 24f * scale else 15f * scale)
    val badgeString = buildString {
        append(p.username)
        if (p.isSpeaking) append(" [MIC]")
    }

    val paint = android.graphics.Paint().apply {
        color = if (p.isMe) android.graphics.Color.rgb(255, 214, 0) else android.graphics.Color.WHITE
        textSize = 20f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.LEFT
    }
    val textBounds = android.graphics.Rect()
    paint.getTextBounds(badgeString, 0, badgeString.length, textBounds)
    val hammerSpace = if (p.isDeveloper) 22f else 0f
    val pillW = textBounds.width() + 20f + hammerSpace
    val pillH = textBounds.height() + 12f
    val pillLeft = sx - pillW / 2f
    val pillTop = nameplateY - pillH + 5f

    drawRect(
        CyberBlack.copy(alpha = 0.92f),
        Offset(pillLeft, pillTop),
        Size(pillW, pillH)
    )
    drawRect(
        if (p.isMe || p.isDeveloper || p.isCryaAdmin) AccentYellow else CyberBorder,
        Offset(pillLeft, pillTop),
        Size(pillW, pillH),
        style = Stroke(1.5f)
    )

    val textStartX = pillLeft + 10f
    drawContext.canvas.nativeCanvas.drawText(badgeString, textStartX, nameplateY, paint)

    if (p.isDeveloper) {
        val badgeCx = textStartX + textBounds.width() + 12f
        val badgeCy = pillTop + pillH * 0.5f
        drawRect(
            CyberBlack,
            Offset(badgeCx - 7.5f, badgeCy - 7.5f),
            Size(15f, 15f)
        )
        drawRect(
            AccentYellow,
            Offset(badgeCx - 7.5f, badgeCy - 7.5f),
            Size(15f, 15f),
            style = Stroke(1.2f)
        )
        drawCustomYellowHammer(cx = badgeCx, cy = badgeCy, r = 5.5f)
    }

    drawContext.canvas.nativeCanvas.apply {
        if (p.recentSpeech != null) {
            val bubbleText = p.recentSpeech!!
            val bubblePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 20f
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val bBounds = android.graphics.Rect()
            bubblePaint.getTextBounds(bubbleText, 0, bubbleText.length, bBounds)
            val bw = (bBounds.width() + 24f).coerceAtLeast(60f)
            val bh = bBounds.height() + 16f
            val by = nameplateY - pillH - 12f

            drawRect(
                AccentYellow,
                Offset(sx - bw / 2f, by - bh),
                Size(bw, bh)
            )
            drawRect(
                CyberBlack,
                Offset(sx - bw / 2f, by - bh),
                Size(bw, bh),
                style = Stroke(2f)
            )
            drawText(bubbleText, sx, by - 8f, bubblePaint)
        }
    }
}

private data class Shirt3DSpec(
    val torsoColor: Color,
    val sleeveColor: Color,
    val pantsColor: Color,
    val isShortSleeve: Boolean
)

private fun resolve3DShirtSpec(outfitName: String, skinColor: Color): Shirt3DSpec {
    return when (outfitName) {
        "Black & Yellow Cyber Shirt" -> Shirt3DSpec(
            torsoColor = Color(0xFF141414),
            sleeveColor = AccentYellow,
            pantsColor = Color(0xFF1A1A1A),
            isShortSleeve = false
        )
        "Classic 'R' Stud Shirt", "Classic 2009 T-Shirt" -> Shirt3DSpec(
            torsoColor = Color(0xFF1F1F1F),
            sleeveColor = skinColor,
            pantsColor = Color(0xFF262626),
            isShortSleeve = true
        )
        "Iron Cafe Barista Apron" -> Shirt3DSpec(
            torsoColor = Color(0xFF1A1A1A),
            sleeveColor = Color(0xFF2C2C2C),
            pantsColor = Color(0xFF121212),
            isShortSleeve = true
        )
        "Hazard Stripe 3D Hoodie" -> Shirt3DSpec(
            torsoColor = AccentYellow,
            sleeveColor = Color(0xFF181818),
            pantsColor = Color(0xFF141414),
            isShortSleeve = false
        )
        "Varsity Letterman Jacket", "2009 Striped Jacket" -> Shirt3DSpec(
            torsoColor = Color(0xFF121212),
            sleeveColor = AccentYellowBright,
            pantsColor = Color(0xFF1E1E1E),
            isShortSleeve = false
        )
        "VIP Gold & Noir Tuxedo", "VIP Black Tie" -> Shirt3DSpec(
            torsoColor = Color(0xFF0D0D0D),
            sleeveColor = Color(0xFF0D0D0D),
            pantsColor = Color(0xFF0A0A0A),
            isShortSleeve = false
        )
        "Builder Vest & Suspenders" -> Shirt3DSpec(
            torsoColor = AccentYellow,
            sleeveColor = Color(0xFF202020),
            pantsColor = Color(0xFF1A1A1A),
            isShortSleeve = true
        )
        "Skater Layered Graphic Tee" -> Shirt3DSpec(
            torsoColor = Color(0xFF151515),
            sleeveColor = AccentYellow,
            pantsColor = Color(0xFF222222),
            isShortSleeve = false
        )
        else -> Shirt3DSpec(
            torsoColor = Color(0xFF141414),
            sleeveColor = AccentYellow,
            pantsColor = Color(0xFF1A1A1A),
            isShortSleeve = false
        )
    }
}

private fun DrawScope.draw3DArmWithSleeve(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    dx3d: Float,
    dy3d: Float,
    sleeveColor: Color,
    skinFront: Color,
    skinSide: Color,
    shortSleeve: Boolean,
    scale: Float
) {
    if (shortSleeve) {
        val sleeveH = h * 0.48f
        // Upper short sleeve 3D box
        drawIsometric3DBox(
            x = x,
            y = y,
            w = w,
            h = sleeveH,
            depthX = dx3d,
            depthY = dy3d,
            frontColor = sleeveColor,
            topColor = lightenColor(sleeveColor, 0.18f),
            sideColor = darkenColor(sleeveColor, 0.25f)
        )
        // Lower forearm skin 3D box
        drawIsometric3DBox(
            x = x,
            y = y + sleeveH,
            w = w,
            h = h - sleeveH,
            depthX = dx3d,
            depthY = dy3d,
            frontColor = skinFront,
            topColor = skinFront,
            sideColor = skinSide
        )
    } else {
        val sleeveH = h * 0.76f
        drawIsometric3DBox(
            x = x,
            y = y,
            w = w,
            h = sleeveH,
            depthX = dx3d,
            depthY = dy3d,
            frontColor = sleeveColor,
            topColor = lightenColor(sleeveColor, 0.18f),
            sideColor = darkenColor(sleeveColor, 0.25f)
        )
        // Hand block
        drawIsometric3DBox(
            x = x,
            y = y + sleeveH,
            w = w,
            h = h - sleeveH,
            depthX = dx3d,
            depthY = dy3d,
            frontColor = skinFront,
            topColor = skinFront,
            sideColor = skinSide
        )
    }
}

private fun DrawScope.draw3DShirtDetails(
    outfitName: String,
    tx: Float,
    ty: Float,
    tw: Float,
    th: Float,
    scale: Float
) {
    val cx = tx + tw / 2f
    when (outfitName) {
        "Black & Yellow Cyber Shirt" -> {
            // Yellow collar + bold geometric chest badge
            drawRect(AccentYellow, Offset(tx + 3f * scale, ty), Size(tw - 6f * scale, 2.5f * scale))
            drawRect(AccentYellow, Offset(cx - 5f * scale, ty + 5f * scale), Size(10f * scale, 10f * scale))
            drawRect(CyberBlack, Offset(cx - 3f * scale, ty + 7f * scale), Size(6f * scale, 6f * scale))
        }
        "Classic 'R' Stud Shirt", "Classic 2009 T-Shirt" -> {
            drawRect(AccentYellow, Offset(cx - 5.5f * scale, ty + 4.5f * scale), Size(11f * scale, 11f * scale))
            drawContext.canvas.nativeCanvas.apply {
                val rPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 10f * scale
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("R", cx, ty + 13f * scale, rPaint)
            }
        }
        "Iron Cafe Barista Apron" -> {
            // Golden yellow apron front & pocket
            drawRect(AccentYellow, Offset(tx + 3f * scale, ty + 4f * scale), Size(tw - 6f * scale, th - 4f * scale))
            drawRect(CyberBlack, Offset(cx - 4f * scale, ty + 11f * scale), Size(8f * scale, 5f * scale))
        }
        "Hazard Stripe 3D Hoodie" -> {
            // Diagonal black hazard stripes across yellow torso
            drawRect(CyberBlack, Offset(tx + 2f * scale, ty + 4f * scale), Size(tw - 4f * scale, 3.5f * scale))
            drawRect(CyberBlack, Offset(tx + 2f * scale, ty + 11f * scale), Size(tw - 4f * scale, 3.5f * scale))
        }
        "Varsity Letterman Jacket", "2009 Striped Jacket" -> {
            // Center button placket & 'C' chest letter
            drawLine(AccentYellow, Offset(cx, ty), Offset(cx, ty + th), strokeWidth = 1.8f * scale)
            drawRect(AccentYellow, Offset(tx + 2.5f * scale, ty + 4f * scale), Size(5f * scale, 6f * scale))
            drawRect(AccentYellow, Offset(tx, ty + th - 2.5f * scale), Size(tw, 2.5f * scale))
        }
        "VIP Gold & Noir Tuxedo", "VIP Black Tie" -> {
            // White/Yellow V-shirt + gold tie
            val lapelPath = Path().apply {
                moveTo(cx - 4f * scale, ty)
                lineTo(cx + 4f * scale, ty)
                lineTo(cx, ty + 10f * scale)
                close()
            }
            drawPath(lapelPath, Color.White)
            drawRect(AccentYellow, Offset(cx - 1.2f * scale, ty + 2f * scale), Size(2.4f * scale, 11f * scale))
        }
        "Builder Vest & Suspenders" -> {
            // Black center zip & reflective grey/white stripes
            drawRect(CyberBlack, Offset(cx - 2f * scale, ty), Size(4f * scale, th))
            drawRect(Color.White, Offset(tx + 1f * scale, ty + 13f * scale), Size(tw - 2f * scale, 2.5f * scale))
        }
        else -> {
            // Lightning / graphic emblem
            drawRect(AccentYellow, Offset(cx - 4.5f * scale, ty + 5f * scale), Size(9f * scale, 9f * scale))
        }
    }
}

private fun DrawScope.draw3DHat(
    hatName: String,
    sx: Float,
    headY: Float,
    headSize: Float,
    dx3d: Float,
    dy3d: Float,
    scale: Float
) {
    when (hatName) {
        "Crown", "Crown of CRYA", "Dominos Crown" -> {
            drawIsometric3DBox(
                x = sx - 9.5f * scale,
                y = headY - 7.5f * scale,
                w = 19f * scale,
                h = 7.5f * scale,
                depthX = dx3d * 0.8f,
                depthY = dy3d * 0.8f,
                frontColor = AccentYellow,
                topColor = AccentYellowBright,
                sideColor = AccentYellowMuted
            )
            // Crown gems
            drawRect(CyberBlack, Offset(sx - 6f * scale, headY - 5f * scale), Size(3f * scale, 3f * scale))
            drawRect(CyberBlack, Offset(sx + 3f * scale, headY - 5f * scale), Size(3f * scale, 3f * scale))
        }
        "Builder Hardhat", "Classic Builder Hardhat" -> {
            drawIsometric3DBox(
                x = sx - 10.5f * scale,
                y = headY - 7f * scale,
                w = 21f * scale,
                h = 7f * scale,
                depthX = dx3d * 0.85f,
                depthY = dy3d * 0.85f,
                frontColor = AccentYellow,
                topColor = AccentYellowBright,
                sideColor = AccentYellowMuted
            )
            drawRect(CyberBlack, Offset(sx - 13f * scale, headY - 1.5f * scale), Size(26f * scale, 2.5f * scale))
        }
        "Classic Top Hat", "Top Hat" -> {
            drawIsometric3DBox(
                x = sx - 7.5f * scale,
                y = headY - 14f * scale,
                w = 15f * scale,
                h = 13f * scale,
                depthX = dx3d * 0.75f,
                depthY = dy3d * 0.75f,
                frontColor = Color(0xFF141414),
                topColor = Color(0xFF292929),
                sideColor = CyberBlack,
                borderColor = AccentYellow
            )
            // Yellow band
            drawRect(AccentYellow, Offset(sx - 7.5f * scale, headY - 4.5f * scale), Size(15f * scale, 3f * scale))
            // Brim
            drawRect(CyberBlack, Offset(sx - 12f * scale, headY - 1.5f * scale), Size(24f * scale, 2.5f * scale))
        }
        "Clockwork Headphones", "RGB Cyber Headset" -> {
            drawRect(AccentYellow, Offset(sx - 10f * scale, headY - 3f * scale), Size(20f * scale, 2.5f * scale))
            drawRect(AccentYellow, Offset(sx - 12f * scale, headY + 2f * scale), Size(4f * scale, 9f * scale))
            drawRect(AccentYellow, Offset(sx + 8f * scale, headY + 2f * scale), Size(4f * scale, 9f * scale))
        }
        "Cyber Hazard Visor" -> {
            drawRect(AccentYellow, Offset(sx - 9.5f * scale, headY + 3.5f * scale), Size(19f * scale, 5f * scale))
            drawRect(CyberBlack, Offset(sx - 9.5f * scale, headY + 3.5f * scale), Size(19f * scale, 5f * scale), style = Stroke(1.2f * scale))
        }
        "Golden Halo", "Silver Halo" -> {
            drawOval(
                AccentYellow,
                Offset(sx - 11f * scale, headY - 12f * scale),
                Size(22f * scale, 6f * scale),
                style = Stroke(2.4f * scale)
            )
        }
    }
}

private fun lightenColor(color: Color, fraction: Float): Color {
    return Color(
        red = (color.red + (1f - color.red) * fraction).coerceIn(0f, 1f),
        green = (color.green + (1f - color.green) * fraction).coerceIn(0f, 1f),
        blue = (color.blue + (1f - color.blue) * fraction).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

private fun darkenColor(color: Color, fraction: Float): Color {
    return Color(
        red = (color.red * (1f - fraction)).coerceIn(0f, 1f),
        green = (color.green * (1f - fraction)).coerceIn(0f, 1f),
        blue = (color.blue * (1f - fraction)).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}
