package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MonsterCanvasAvatar(
    species: MonsterSpecies,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    ivRank: String = "A",
    showBadge: Boolean = true
) {
    val primaryColor = species.primaryElement.color
    val secondaryColor = species.secondaryElement?.color ?: primaryColor.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.32f),
                        ElevatedTacticalCard,
                        MidnightSlate
                    )
                )
            )
            .border(
                width = if (species.isLegendary) 2.dp else 1.dp,
                color = if (species.isLegendary) BadgeGold else primaryColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val baseRadius = (minOf(w, h) / 2f) * (0.52f + species.stage * 0.08f)

            // Aura ring for Stage 3 or Legendary
            if (species.stage >= 3 || species.isLegendary) {
                drawCircle(
                    color = if (species.isLegendary) BadgeGold.copy(alpha = 0.45f) else secondaryColor.copy(alpha = 0.35f),
                    radius = baseRadius * 1.25f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Wings / Crests based on species ID & stage
            val wingSpread = baseRadius * (0.9f + (species.id % 4) * 0.12f)
            val leftWing = Path().apply {
                moveTo(cx - baseRadius * 0.3f, cy)
                lineTo(cx - wingSpread * 1.25f, cy - baseRadius * 0.85f)
                lineTo(cx - wingSpread * 0.9f, cy + baseRadius * 0.45f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(cx + baseRadius * 0.3f, cy)
                lineTo(cx + wingSpread * 1.25f, cy - baseRadius * 0.85f)
                lineTo(cx + wingSpread * 0.9f, cy + baseRadius * 0.45f)
                close()
            }
            drawPath(leftWing, color = secondaryColor.copy(alpha = 0.85f))
            drawPath(rightWing, color = secondaryColor.copy(alpha = 0.85f))

            // Core Monster Body Silhouette
            val bodyShape = species.id % 3
            when (bodyShape) {
                0 -> {
                    drawCircle(
                        brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                        radius = baseRadius * 0.78f,
                        center = Offset(cx, cy + baseRadius * 0.06f)
                    )
                }
                1 -> {
                    val diamond = Path().apply {
                        moveTo(cx, cy - baseRadius * 0.9f)
                        lineTo(cx + baseRadius * 0.82f, cy)
                        lineTo(cx, cy + baseRadius * 0.88f)
                        lineTo(cx - baseRadius * 0.82f, cy)
                        close()
                    }
                    drawPath(diamond, brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                }
                else -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                        topLeft = Offset(cx - baseRadius * 0.72f, cy - baseRadius * 0.68f),
                        size = Size(baseRadius * 1.44f, baseRadius * 1.44f),
                        cornerRadius = CornerRadius(baseRadius * 0.38f, baseRadius * 0.38f)
                    )
                }
            }

            // Crown / Horns
            val hornHeight = baseRadius * (0.4f + species.stage * 0.14f)
            val crest = Path().apply {
                moveTo(cx - baseRadius * 0.35f, cy - baseRadius * 0.5f)
                lineTo(cx, cy - baseRadius * 0.6f - hornHeight)
                lineTo(cx + baseRadius * 0.35f, cy - baseRadius * 0.5f)
                close()
            }
            drawPath(crest, color = if (species.isLegendary) BadgeGold else primaryColor)

            // Glowing Eyes
            val eyeOffsetX = baseRadius * 0.28f
            val eyeOffsetY = cy - baseRadius * 0.08f
            drawCircle(
                color = MidnightSlate,
                radius = baseRadius * 0.16f,
                center = Offset(cx - eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = MidnightSlate,
                radius = baseRadius * 0.16f,
                center = Offset(cx + eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = Color.White,
                radius = baseRadius * 0.09f,
                center = Offset(cx - eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = Color.White,
                radius = baseRadius * 0.09f,
                center = Offset(cx + eyeOffsetX, eyeOffsetY)
            )
        }

        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .background(MidnightSlate.copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = ivRank,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (ivRank.startsWith("S")) BadgeGold else AetherCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InteractiveWorldMapCanvas(
    biome: BiomeZone,
    playerX: Int,
    playerY: Int,
    weather: WeatherType,
    timeOfDay: TimeOfDay,
    npcs: List<WorldNpc>,
    onTileTapped: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = 8
    val baseTileColor = Color(biome.mapColorHex)
    val timeOverlayColor = when (timeOfDay) {
        TimeOfDay.DAWN -> Color(0x22FFAB91)
        TimeOfDay.DAY -> Color.Transparent
        TimeOfDay.DUSK -> Color(0x33FF6E40)
        TimeOfDay.NIGHT -> Color(0x66050814)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, TacticalBorder, RoundedCornerShape(18.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(playerX, playerY) {
                    detectTapGestures { offset ->
                        val cellW = size.width / gridSize.toFloat()
                        val cellH = size.height / gridSize.toFloat()
                        val targetX = (offset.x / cellW).toInt().coerceIn(0, gridSize - 1)
                        val targetY = (offset.y / cellH).toInt().coerceIn(0, gridSize - 1)
                        val dx = (targetX - playerX).coerceIn(-1, 1)
                        val dy = (targetY - playerY).coerceIn(-1, 1)
                        if (dx != 0 || dy != 0) {
                            onTileTapped(dx, dy)
                        } else {
                            onTileTapped(0, 0)
                        }
                    }
                }
        ) {
            val cellW = size.width / gridSize.toFloat()
            val cellH = size.height / gridSize.toFloat()

            // Draw 8x8 Biome Tiles (Coromon/Pokemon top-down terrain)
            for (gx in 0 until gridSize) {
                for (gy in 0 until gridSize) {
                    val isTallGrass = (gx + gy * 3) % 3 == 0
                    val isAetherShrine = (gx == 6 && gy == 1) || (gx == 1 && gy == 6)
                    val tileColor = when {
                        isAetherShrine -> AetherCyanDark.copy(alpha = 0.65f)
                        isTallGrass -> baseTileColor.copy(alpha = 0.95f)
                        else -> baseTileColor.copy(alpha = 0.65f)
                    }
                    drawRect(
                        color = tileColor,
                        topLeft = Offset(gx * cellW, gy * cellH),
                        size = Size(cellW - 2f, cellH - 2f)
                    )
                    if (isTallGrass) {
                        // Draw tall grass / crystal tufts where wild monsters spawn
                        val cx = (gx + 0.5f) * cellW
                        val cy = (gy + 0.5f) * cellH
                        drawCircle(
                            color = biome.primaryElement.color.copy(alpha = 0.35f),
                            radius = minOf(cellW, cellH) * 0.25f,
                            center = Offset(cx, cy)
                        )
                    }
                    if (isAetherShrine) {
                        val cx = (gx + 0.5f) * cellW
                        val cy = (gy + 0.5f) * cellH
                        drawCircle(
                            color = BadgeGold,
                            radius = minOf(cellW, cellH) * 0.28f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            // Draw NPCs at their scheduled positions based on TimeOfDay
            npcs.forEach { npc ->
                val (nx, ny) = when (timeOfDay) {
                    TimeOfDay.DAWN -> npc.dawnLocation
                    TimeOfDay.DAY -> npc.dayLocation
                    TimeOfDay.DUSK -> npc.duskLocation
                    TimeOfDay.NIGHT -> npc.nightLocation
                }
                val ncx = (nx.coerceIn(0, 7) + 0.5f) * cellW
                val ncy = (ny.coerceIn(0, 7) + 0.5f) * cellH
                drawCircle(
                    color = BadgeGold,
                    radius = minOf(cellW, cellH) * 0.32f,
                    center = Offset(ncx, ncy)
                )
                drawCircle(
                    color = MidnightSlate,
                    radius = minOf(cellW, cellH) * 0.14f,
                    center = Offset(ncx, ncy)
                )
            }

            // Draw Player Trainer Avatar
            val px = (playerX + 0.5f) * cellW
            val py = (playerY + 0.5f) * cellH
            drawCircle(
                color = Color.White,
                radius = minOf(cellW, cellH) * 0.38f,
                center = Offset(px, py)
            )
            drawCircle(
                color = AetherCyan,
                radius = minOf(cellW, cellH) * 0.28f,
                center = Offset(px, py)
            )

            // Day / Night Lighting Tint
            drawRect(color = timeOverlayColor, size = size)

            // Dynamic Weather Particle Effects
            for (i in 0 until 18) {
                val wx = ((i * 73 + playerX * 29) % 100) / 100f * size.width
                val wy = ((i * 47 + playerY * 31) % 100) / 100f * size.height
                when (weather) {
                    WeatherType.TROPICAL_RAIN, WeatherType.THUNDER_TEMPEST -> {
                        drawLine(
                            color = if (weather == WeatherType.THUNDER_TEMPEST && i % 4 == 0) ElementVolt else ElementAqua.copy(alpha = 0.7f),
                            start = Offset(wx, wy),
                            end = Offset(wx - 8f, wy + 18f),
                            strokeWidth = 2.5f
                        )
                    }
                    WeatherType.SOLAR_FLARE -> {
                        drawCircle(
                            color = ElementLux.copy(alpha = 0.35f),
                            radius = (6f + (i % 4) * 3f),
                            center = Offset(wx, wy)
                        )
                    }
                    WeatherType.OBSIDIAN_SANDSTORM -> {
                        drawLine(
                            color = ElementDraco.copy(alpha = 0.55f),
                            start = Offset(wx, wy),
                            end = Offset(wx + 22f, wy + 4f),
                            strokeWidth = 3f
                        )
                    }
                    WeatherType.AETHER_MIST -> {
                        val angle = i * 0.5f
                        drawCircle(
                            color = AetherCyan.copy(alpha = 0.45f),
                            radius = 7f + (sin(angle) * 3f),
                            center = Offset(wx + cos(angle) * 6f, wy)
                        )
                    }
                }
            }
        }
    }
}
