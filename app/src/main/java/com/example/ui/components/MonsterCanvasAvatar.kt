package com.example.ui.components

import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlin.math.abs
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

enum class MapCameraMode(val labelDe: String) {
    POKEMON_25D("2.5D Pokémon-Kamera"),
    ISOMETRIC_25D("2.5D Diorama-Isometrie")
}

enum class TrainerDirection {
    DOWN, UP, LEFT, RIGHT
}

private enum class RenderItemType {
    PROP_TREE,
    BUILDING_SHRINE,
    NPC,
    COMPANION,
    TRAINER
}

private data class DepthRenderItem(
    val gridX: Int,
    val gridY: Int,
    val sortKey: Float,
    val type: RenderItemType,
    val npc: WorldNpc? = null
)

@Composable
fun InteractiveWorldMapCanvas(
    biome: BiomeZone,
    playerX: Int,
    playerY: Int,
    weather: WeatherType,
    timeOfDay: TimeOfDay,
    npcs: List<WorldNpc>,
    onTileTapped: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    companionSpecies: MonsterSpecies? = null,
    trainerDirection: TrainerDirection = TrainerDirection.DOWN,
    cameraMode: MapCameraMode = MapCameraMode.POKEMON_25D
) {
    val gridSize = 8
    val baseTileColor = Color(biome.mapColorHex)
    val accentElementColor = biome.primaryElement.color

    // Subtle continuous animation for 2.5D world life (grass sway, water ripples, bobbing, weather)
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "map25d_anim")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis = 3600, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Determine companion monster tile (1 step behind trainer based on facing direction)
    val (companionX, companionY) = remember(playerX, playerY, trainerDirection) {
        val rawCx = when (trainerDirection) {
            TrainerDirection.LEFT -> playerX + 1
            TrainerDirection.RIGHT -> playerX - 1
            else -> playerX
        }
        val rawCy = when (trainerDirection) {
            TrainerDirection.UP -> playerY + 1
            TrainerDirection.DOWN -> playerY - 1
            else -> playerY
        }
        val cx = rawCx.coerceIn(0, gridSize - 1)
        val cy = rawCy.coerceIn(0, gridSize - 1)
        if (cx == playerX && cy == playerY) {
            val fallbackX = (playerX - 1).coerceAtLeast(0)
            val fallbackY = if (fallbackX == playerX) (playerY - 1).coerceAtLeast(0) else playerY
            fallbackX to fallbackY
        } else {
            cx to cy
        }
    }

    val skyColors = when (timeOfDay) {
        TimeOfDay.DAWN -> listOf(Color(0xFF2B1B42), Color(0xFF8C4A67), Color(0xFFFFAB91))
        TimeOfDay.DAY -> listOf(Color(0xFF0D3B66), Color(0xFF1E6091), Color(0xFF76C893))
        TimeOfDay.DUSK -> listOf(Color(0xFF1D1135), Color(0xFF7C2D52), Color(0xFFFF6E40))
        TimeOfDay.NIGHT -> listOf(Color(0xFF040711), Color(0xFF0B132B), Color(0xFF1C2541))
    }

    val timeOverlayColor = when (timeOfDay) {
        TimeOfDay.DAWN -> Color(0x1AFFAB91)
        TimeOfDay.DAY -> Color.Transparent
        TimeOfDay.DUSK -> Color(0x28FF6E40)
        TimeOfDay.NIGHT -> Color(0x50040714)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, TacticalBorder, RoundedCornerShape(18.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(playerX, playerY, cameraMode) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val horizonY = h * 0.20f
                        val mapBottomY = h * 0.90f
                        val mapHeight = mapBottomY - horizonY

                        val (targetX, targetY) = if (cameraMode == MapCameraMode.POKEMON_25D) {
                            val normY = ((offset.y - horizonY) / mapHeight).coerceIn(0f, 0.999f)
                            // Inverse perspective curve: gy = (normY ^ (1/1.18)) * gridSize
                            val gy = (Math.pow(normY.toDouble(), 1.0 / 1.18) * gridSize).toInt().coerceIn(0, gridSize - 1)
                            val rowProgress = (gy + 0.5f) / gridSize.toFloat()
                            val rowWidth = w * (0.58f + 0.36f * rowProgress)
                            val rowLeft = (w - rowWidth) / 2f
                            val gx = (((offset.x - rowLeft) / rowWidth) * gridSize).toInt().coerceIn(0, gridSize - 1)
                            gx to gy
                        } else {
                            // Isometric 2.5D hit mapping
                            val centerX = w * 0.5f
                            val topOriginY = h * 0.22f
                            val tileHalfW = (w * 0.44f) / gridSize
                            val tileHalfH = (h * 0.32f) / gridSize
                            val relX = offset.x - centerX
                            val relY = offset.y - topOriginY
                            val gxFloat = (relX / tileHalfW + relY / tileHalfH) * 0.5f
                            val gyFloat = (relY / tileHalfH - relX / tileHalfW) * 0.5f
                            gxFloat.toInt().coerceIn(0, gridSize - 1) to gyFloat.toInt().coerceIn(0, gridSize - 1)
                        }

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
            val w = size.width
            val h = size.height
            val horizonY = h * 0.21f
            val mapBottomY = h * 0.89f
            val mapHeight = mapBottomY - horizonY

            // 1. Draw 2.5D Skybox & Horizon Backdrop
            drawRect(
                brush = Brush.verticalGradient(
                    colors = skyColors,
                    startY = 0f,
                    endY = horizonY * 1.35f
                ),
                size = Size(w, horizonY * 1.35f)
            )
            // Deep ground void below the 2.5D diorama
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(MidnightSlate, Color(0xFF04060C)),
                    startY = horizonY,
                    endY = h
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            // Celestial Sun / Moon in the 2.5D Skybox
            val sunX = when (timeOfDay) {
                TimeOfDay.DAWN -> w * 0.22f
                TimeOfDay.DAY -> w * 0.52f
                TimeOfDay.DUSK -> w * 0.78f
                TimeOfDay.NIGHT -> w * 0.82f
            }
            val sunY = horizonY * 0.48f
            val celestialColor = when (timeOfDay) {
                TimeOfDay.DAWN -> Color(0xFFFFCC80)
                TimeOfDay.DAY -> Color(0xFFFFF59D)
                TimeOfDay.DUSK -> Color(0xFFFF8A65)
                TimeOfDay.NIGHT -> AetherCyan
            }
            drawCircle(
                color = celestialColor.copy(alpha = 0.22f),
                radius = 28f + sin(animPhase) * 3f,
                center = Offset(sunX, sunY)
            )
            drawCircle(
                color = celestialColor,
                radius = 13f,
                center = Offset(sunX, sunY)
            )

            // Distant 2.5D Continent Horizon Silhouette (Parallax Mountain / Spire Range)
            val mountainPath = Path().apply {
                moveTo(0f, horizonY + 8f)
                val peaks = 9
                for (i in 0..peaks) {
                    val px = (i.toFloat() / peaks) * w
                    val peakOffset = if (i % 2 == 0) {
                        horizonY - (14f + (i * 13 % 18))
                    } else {
                        horizonY - 4f
                    }
                    lineTo(px, peakOffset)
                }
                lineTo(w, horizonY + 8f)
                close()
            }
            drawPath(
                path = mountainPath,
                color = baseTileColor.copy(alpha = 0.42f)
            )

            // Helper functions to project (gx, gy) grid coordinates into 2.5D screen space
            fun projectCorner(gx: Float, gy: Float, elevationPx: Float = 0f): Offset {
                return if (cameraMode == MapCameraMode.POKEMON_25D) {
                    val t = (gy / gridSize.toFloat()).coerceIn(0f, 1.2f)
                    val perspT = Math.pow(t.toDouble(), 1.18).toFloat()
                    val rowY = horizonY + perspT * mapHeight
                    val rowWidth = w * (0.56f + 0.38f * perspT)
                    val rowLeft = (w - rowWidth) * 0.5f
                    val x = rowLeft + (gx / gridSize.toFloat()) * rowWidth
                    Offset(x, rowY - elevationPx * (0.65f + 0.55f * perspT))
                } else {
                    val centerX = w * 0.5f
                    val topOriginY = h * 0.20f
                    val tileHalfW = (w * 0.43f) / gridSize
                    val tileHalfH = (h * 0.33f) / gridSize
                    val x = centerX + (gx - gy) * tileHalfW
                    val y = topOriginY + (gx + gy) * tileHalfH - elevationPx
                    Offset(x, y)
                }
            }

            fun tileScale(gy: Int): Float {
                return if (cameraMode == MapCameraMode.POKEMON_25D) {
                    val t = ((gy + 0.5f) / gridSize.toFloat()).coerceIn(0f, 1f)
                    0.68f + 0.52f * t
                } else {
                    0.88f
                }
            }

            // Tile classification helpers for Pokémon-style routes, rivers, ledges, shrines, and tall grass
            fun isWaterChannel(gx: Int, gy: Int): Boolean = (gx == 3 && gy in 2..5 && gy != 4)
            fun isBridgeTile(gx: Int, gy: Int): Boolean = (gx == 3 && gy == 4)
            fun isCobbleRoad(gx: Int, gy: Int): Boolean = (gy == 4 || gx == 2) && !isWaterChannel(gx, gy)
            fun isAetherShrine(gx: Int, gy: Int): Boolean = (gx == 6 && gy == 1) || (gx == 1 && gy == 6)
            fun isElevatedPlateau(gx: Int, gy: Int): Boolean =
                (gy == 0 || (gx == 7 && gy <= 3) || isAetherShrine(gx, gy)) && !isCobbleRoad(gx, gy)
            fun isTallGrass(gx: Int, gy: Int): Boolean =
                !isWaterChannel(gx, gy) && !isBridgeTile(gx, gy) && !isCobbleRoad(gx, gy) && !isAetherShrine(gx, gy) &&
                    ((gx + gy * 3) % 3 == 0 || (gx * 2 + gy) % 5 == 0)

            fun tileElevation(gx: Int, gy: Int): Float = when {
                isAetherShrine(gx, gy) -> 12f
                isElevatedPlateau(gx, gy) -> 9f
                isBridgeTile(gx, gy) -> 5f
                isWaterChannel(gx, gy) -> -4f
                else -> 0f
            }

            // 2. Render 8x8 2.5D Extruded Terrain Tiles Back-to-Front (gy = 0..7)
            for (gy in 0 until gridSize) {
                for (gx in 0 until gridSize) {
                    val elev = tileElevation(gx, gy)
                    val topLeft = projectCorner(gx.toFloat(), gy.toFloat(), elev)
                    val topRight = projectCorner(gx + 1f, gy.toFloat(), elev)
                    val bottomRight = projectCorner(gx + 1f, gy + 1f, elev)
                    val bottomLeft = projectCorner(gx.toFloat(), gy + 1f, elev)

                    val baseSlabBottomLeft = projectCorner(gx.toFloat(), gy + 1f, -10f)
                    val baseSlabBottomRight = projectCorner(gx + 1f, gy + 1f, -10f)
                    val baseSlabTopRight = projectCorner(gx + 1f, gy.toFloat(), -10f)

                    val isShrine = isAetherShrine(gx, gy)
                    val isWater = isWaterChannel(gx, gy)
                    val isBridge = isBridgeTile(gx, gy)
                    val isRoad = isCobbleRoad(gx, gy)
                    val isGrass = isTallGrass(gx, gy)
                    val isPlateau = isElevatedPlateau(gx, gy)

                    val topColor = when {
                        isShrine -> AetherCyanDark
                        isWater -> ElementAqua.copy(alpha = 0.82f)
                        isBridge -> Color(0xFF8D6E63)
                        isRoad -> Color(0xFF5C677D)
                        isPlateau -> baseTileColor.copy(alpha = 0.98f)
                        isGrass -> baseTileColor.copy(alpha = 0.90f)
                        else -> baseTileColor.copy(alpha = 0.74f)
                    }

                    // Draw Front Extruded 3D Cliff Wall (visible on elevated tiles, front row, or isometric edges)
                    val showFrontFace = gy == gridSize - 1 || elev > tileElevation(gx, (gy + 1).coerceAtMost(gridSize - 1)) || cameraMode == MapCameraMode.ISOMETRIC_25D
                    if (showFrontFace) {
                        val frontWall = Path().apply {
                            moveTo(bottomLeft.x, bottomLeft.y)
                            lineTo(bottomRight.x, bottomRight.y)
                            lineTo(baseSlabBottomRight.x, baseSlabBottomRight.y)
                            lineTo(baseSlabBottomLeft.x, baseSlabBottomLeft.y)
                            close()
                        }
                        val cliffColor = when {
                            isShrine -> AetherCyanDark.copy(alpha = 0.55f)
                            isRoad || isBridge -> Color(0xFF3E4656)
                            else -> baseTileColor.copy(
                                red = (baseTileColor.red * 0.55f),
                                green = (baseTileColor.green * 0.55f),
                                blue = (baseTileColor.blue * 0.62f)
                            )
                        }
                        drawPath(frontWall, color = cliffColor)
                        // Horizontal rock strata line on cliff face
                        drawLine(
                            color = MidnightSlate.copy(alpha = 0.45f),
                            start = Offset(
                                (bottomLeft.x + baseSlabBottomLeft.x) * 0.5f,
                                (bottomLeft.y + baseSlabBottomLeft.y) * 0.5f
                            ),
                            end = Offset(
                                (bottomRight.x + baseSlabBottomRight.x) * 0.5f,
                                (bottomRight.y + baseSlabBottomRight.y) * 0.5f
                            ),
                            strokeWidth = 1.5f
                        )
                    }

                    // Draw Right Side 3D Bevel Wall for Isometric or Elevated Tiles
                    if (cameraMode == MapCameraMode.ISOMETRIC_25D || gx == gridSize - 1 || elev > tileElevation((gx + 1).coerceAtMost(gridSize - 1), gy)) {
                        val rightWall = Path().apply {
                            moveTo(topRight.x, topRight.y)
                            lineTo(bottomRight.x, bottomRight.y)
                            lineTo(baseSlabBottomRight.x, baseSlabBottomRight.y)
                            lineTo(baseSlabTopRight.x, baseSlabTopRight.y)
                            close()
                        }
                        drawPath(
                            path = rightWall,
                            color = topColor.copy(
                                red = topColor.red * 0.42f,
                                green = topColor.green * 0.42f,
                                blue = topColor.blue * 0.48f
                            )
                        )
                    }

                    // Draw Top 2.5D Surface Quad
                    val topQuad = Path().apply {
                        moveTo(topLeft.x, topLeft.y)
                        lineTo(topRight.x, topRight.y)
                        lineTo(bottomRight.x, bottomRight.y)
                        lineTo(bottomLeft.x, bottomLeft.y)
                        close()
                    }
                    drawPath(topQuad, color = topColor)
                    drawPath(
                        path = topQuad,
                        color = MidnightSlate.copy(alpha = 0.28f),
                        style = Stroke(width = 1.2f)
                    )

                    val centerPt = projectCorner(gx + 0.5f, gy + 0.5f, elev)
                    val scale = tileScale(gy)

                    // Surface Details: Water Ripples, Bridge Planks, Cobblestones, or 2.5D Tall Grass Blades
                    if (isWater) {
                        val rippleOffset = sin(animPhase + gx + gy) * 4f * scale
                        drawLine(
                            color = Color.White.copy(alpha = 0.55f),
                            start = Offset(centerPt.x - 10f * scale + rippleOffset, centerPt.y - 2f * scale),
                            end = Offset(centerPt.x + 8f * scale + rippleOffset, centerPt.y - 2f * scale),
                            strokeWidth = 2f * scale
                        )
                        drawLine(
                            color = AetherCyan.copy(alpha = 0.65f),
                            start = Offset(centerPt.x - 6f * scale - rippleOffset, centerPt.y + 4f * scale),
                            end = Offset(centerPt.x + 10f * scale - rippleOffset, centerPt.y + 4f * scale),
                            strokeWidth = 1.6f * scale
                        )
                    } else if (isBridge) {
                        // Wooden planks + side rails
                        for (p in -2..2) {
                            val py = centerPt.y + p * 3.2f * scale
                            drawLine(
                                color = Color(0xFF4E342E),
                                start = Offset(centerPt.x - 12f * scale, py),
                                end = Offset(centerPt.x + 12f * scale, py),
                                strokeWidth = 1.5f * scale
                            )
                        }
                    } else if (isRoad) {
                        // Subtle cobblestone pebbles
                        drawCircle(
                            color = Color.White.copy(alpha = 0.16f),
                            radius = 3.5f * scale,
                            center = Offset(centerPt.x - 5f * scale, centerPt.y - 2f * scale)
                        )
                        drawCircle(
                            color = MidnightSlate.copy(alpha = 0.25f),
                            radius = 4f * scale,
                            center = Offset(centerPt.x + 5f * scale, centerPt.y + 2f * scale)
                        )
                    } else if (isGrass) {
                        // Pokémon-style 2.5D vertical swaying tall grass tufts sticking up from the tile
                        val sway = sin(animPhase + gx * 0.7f + gy * 0.5f) * 3.2f * scale
                        for (tuft in -1..1) {
                            val bx = centerPt.x + tuft * 7.5f * scale
                            val by = centerPt.y + (tuft % 2) * 2.5f * scale
                            val bladeHeight = (10f + (tuft + 1) * 2f) * scale
                            val bladePath = Path().apply {
                                moveTo(bx - 2.6f * scale, by)
                                lineTo(bx + sway, by - bladeHeight)
                                lineTo(bx + 2.6f * scale, by)
                                close()
                            }
                            drawPath(
                                path = bladePath,
                                color = accentElementColor.copy(alpha = 0.78f)
                            )
                        }
                    }

                    // Highlight active Player Target Tile with glowing 2.5D selection frame
                    if (gx == playerX && gy == playerY) {
                        drawPath(
                            path = topQuad,
                            color = AetherCyan.copy(alpha = 0.35f)
                        )
                        drawPath(
                            path = topQuad,
                            color = Color.White,
                            style = Stroke(width = 2.2f)
                        )
                    }
                }
            }

            // 3. Collect all 2.5D Standing Objects (Trees/Pillars, Shrines/Gyms, NPCs, Companion, Trainer)
            //    and sort by depth (Painter's Algorithm) so southern objects occlude northern ones!
            val renderItems = mutableListOf<DepthRenderItem>()

            // Biome decorative 3D trees / crystal spires only on the 4 outer corners so the 2.5D map stays open & clear
            val treeCoords = listOf(0 to 0, 7 to 0, 0 to 7, 7 to 6)
            treeCoords.forEach { (tx, ty) ->
                if ((tx != playerX || ty != playerY) && (tx != companionX || ty != companionY)) {
                    val sortVal = if (cameraMode == MapCameraMode.POKEMON_25D) {
                        ty + tx * 0.01f
                    } else {
                        (tx + ty).toFloat()
                    }
                    renderItems.add(DepthRenderItem(tx, ty, sortVal, RenderItemType.PROP_TREE))
                }
            }

            // 2.5D Pokémon-Style Aether Shrines / Mini-Gym Buildings at (6,1) and (1,6)
            listOf(6 to 1, 1 to 6).forEach { (sx, sy) ->
                val sortVal = if (cameraMode == MapCameraMode.POKEMON_25D) {
                    sy + sx * 0.01f - 0.1f
                } else {
                    (sx + sy).toFloat() - 0.1f
                }
                renderItems.add(DepthRenderItem(sx, sy, sortVal, RenderItemType.BUILDING_SHRINE))
            }

            // Scheduled NPCs
            npcs.forEach { npc ->
                val (nxRaw, nyRaw) = when (timeOfDay) {
                    TimeOfDay.DAWN -> npc.dawnLocation
                    TimeOfDay.DAY -> npc.dayLocation
                    TimeOfDay.DUSK -> npc.duskLocation
                    TimeOfDay.NIGHT -> npc.nightLocation
                }
                val nx = nxRaw.coerceIn(0, gridSize - 1)
                val ny = nyRaw.coerceIn(0, gridSize - 1)
                val sortVal = if (cameraMode == MapCameraMode.POKEMON_25D) {
                    ny + nx * 0.01f + 0.15f
                } else {
                    (nx + ny).toFloat() + 0.15f
                }
                renderItems.add(DepthRenderItem(nx, ny, sortVal, RenderItemType.NPC, npc = npc))
            }

            // Following Companion Monster Billboard
            if (companionSpecies != null) {
                val sortVal = if (cameraMode == MapCameraMode.POKEMON_25D) {
                    companionY + companionX * 0.01f + 0.22f
                } else {
                    (companionX + companionY).toFloat() + 0.22f
                }
                renderItems.add(DepthRenderItem(companionX, companionY, sortVal, RenderItemType.COMPANION))
            }

            // Player Trainer Billboard
            val trainerSort = if (cameraMode == MapCameraMode.POKEMON_25D) {
                playerY + playerX * 0.01f + 0.28f
            } else {
                (playerX + playerY).toFloat() + 0.28f
            }
            renderItems.add(DepthRenderItem(playerX, playerY, trainerSort, RenderItemType.TRAINER))

            renderItems.sortBy { it.sortKey }

            // Shadow angle based on TimeOfDay
            val shadowSkewX = when (timeOfDay) {
                TimeOfDay.DAWN -> 8f
                TimeOfDay.DAY -> 2f
                TimeOfDay.DUSK -> -8f
                TimeOfDay.NIGHT -> 4f
            }

            // 4. Draw Sorted 2.5D Standing Objects
            renderItems.forEach { item ->
                val elev = tileElevation(item.gridX, item.gridY)
                val anchor = projectCorner(item.gridX + 0.5f, item.gridY + 0.55f, elev)
                val scale = tileScale(item.gridY)

                when (item.type) {
                    RenderItemType.PROP_TREE -> {
                        // Angled ground shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.35f),
                            topLeft = Offset(anchor.x - 12f * scale + shadowSkewX * 0.4f, anchor.y - 5f * scale),
                            size = Size(24f * scale, 10f * scale)
                        )
                        // Tree Trunk / Obsidian Pillar
                        drawRoundRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(anchor.x - 3.5f * scale, anchor.y - 18f * scale),
                            size = Size(7f * scale, 18f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        // 3-Tiered Pokémon Pine / Crystal Canopy
                        for (tier in 0..2) {
                            val tierY = anchor.y - (13f + tier * 9f) * scale
                            val tierW = (16f - tier * 3.2f) * scale
                            val tierH = 14f * scale
                            val canopy = Path().apply {
                                moveTo(anchor.x, tierY - tierH)
                                lineTo(anchor.x + tierW, tierY)
                                lineTo(anchor.x - tierW, tierY)
                                close()
                            }
                            drawPath(
                                path = canopy,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        accentElementColor,
                                        baseTileColor.copy(alpha = 0.95f)
                                    ),
                                    startY = tierY - tierH,
                                    endY = tierY
                                )
                            )
                        }
                    }

                    RenderItemType.BUILDING_SHRINE -> {
                        // 2.5D Pokémon-Gym / Aether Shrine structure with pitched roof & glowing portal
                        val bw = 26f * scale
                        val bh = 22f * scale
                        // Ground shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.42f),
                            topLeft = Offset(anchor.x - bw * 0.65f, anchor.y - 6f * scale),
                            size = Size(bw * 1.3f, 12f * scale)
                        )
                        // Building Front Wall
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(anchor.x - bw * 0.5f, anchor.y - bh),
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(4f * scale, 4f * scale)
                        )
                        // Glowing Gold Pillars
                        drawRect(
                            color = BadgeGold,
                            topLeft = Offset(anchor.x - bw * 0.45f, anchor.y - bh),
                            size = Size(3.5f * scale, bh)
                        )
                        drawRect(
                            color = BadgeGold,
                            topLeft = Offset(anchor.x + bw * 0.45f - 3.5f * scale, anchor.y - bh),
                            size = Size(3.5f * scale, bh)
                        )
                        // Glowing Aether Portal Doorway
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(AetherCyan, AetherCyanDark)),
                            topLeft = Offset(anchor.x - 5.5f * scale, anchor.y - 12f * scale),
                            size = Size(11f * scale, 12f * scale),
                            cornerRadius = CornerRadius(5f * scale, 5f * scale)
                        )
                        // Pitched 2.5D Roof
                        val roofPath = Path().apply {
                            moveTo(anchor.x - bw * 0.65f, anchor.y - bh + 2f * scale)
                            lineTo(anchor.x, anchor.y - bh - 14f * scale)
                            lineTo(anchor.x + bw * 0.65f, anchor.y - bh + 2f * scale)
                            close()
                        }
                        drawPath(
                            path = roofPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(BadgeGold, CrimsonAccent),
                                startY = anchor.y - bh - 14f * scale,
                                endY = anchor.y - bh + 2f * scale
                            )
                        )
                    }

                    RenderItemType.NPC -> {
                        val bob = sin(animPhase + item.gridX) * 1.8f * scale
                        // Shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.4f),
                            topLeft = Offset(anchor.x - 9f * scale, anchor.y - 4f * scale),
                            size = Size(18f * scale, 8f * scale)
                        )
                        // NPC Cloak / Body (2.5D Chibi Standing Figure)
                        drawRoundRect(
                            color = BadgeGold,
                            topLeft = Offset(anchor.x - 7f * scale, anchor.y - 18f * scale + bob),
                            size = Size(14f * scale, 15f * scale),
                            cornerRadius = CornerRadius(5f * scale, 5f * scale)
                        )
                        // NPC Head
                        drawCircle(
                            color = Color(0xFFFFE0B2),
                            radius = 6.5f * scale,
                            center = Offset(anchor.x, anchor.y - 23f * scale + bob)
                        )
                        // NPC Hood / Hat
                        drawArc(
                            color = Color(0xFFB45309),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(anchor.x - 7f * scale, anchor.y - 30f * scale + bob),
                            size = Size(14f * scale, 13f * scale)
                        )
                        // Floating Quest "!" Indicator Bubble above NPC
                        drawCircle(
                            color = MidnightSlate.copy(alpha = 0.9f),
                            radius = 5.5f * scale,
                            center = Offset(anchor.x + 8f * scale, anchor.y - 33f * scale + bob)
                        )
                        drawCircle(
                            color = BadgeGold,
                            radius = 2.2f * scale,
                            center = Offset(anchor.x + 8f * scale, anchor.y - 33f * scale + bob)
                        )
                    }

                    RenderItemType.COMPANION -> {
                        if (companionSpecies != null) {
                            val bob = sin(animPhase * 2f) * 2.5f * scale
                            val cPrimary = companionSpecies.primaryElement.color
                            val cSecondary = companionSpecies.secondaryElement?.color ?: BadgeGold
                            // Companion Shadow
                            drawOval(
                                color = Color.Black.copy(alpha = 0.38f),
                                topLeft = Offset(anchor.x - 9f * scale, anchor.y - 4f * scale),
                                size = Size(18f * scale, 7f * scale)
                            )
                            // Aura ring under companion
                            drawOval(
                                color = cPrimary.copy(alpha = 0.45f),
                                topLeft = Offset(anchor.x - 11f * scale, anchor.y - 5f * scale),
                                size = Size(22f * scale, 9f * scale),
                                style = Stroke(width = 1.6f * scale)
                            )
                            // Ears / Wings
                            val leftEar = Path().apply {
                                moveTo(anchor.x - 4f * scale, anchor.y - 14f * scale + bob)
                                lineTo(anchor.x - 12f * scale, anchor.y - 24f * scale + bob)
                                lineTo(anchor.x - 8f * scale, anchor.y - 10f * scale + bob)
                                close()
                            }
                            val rightEar = Path().apply {
                                moveTo(anchor.x + 4f * scale, anchor.y - 14f * scale + bob)
                                lineTo(anchor.x + 12f * scale, anchor.y - 24f * scale + bob)
                                lineTo(anchor.x + 8f * scale, anchor.y - 10f * scale + bob)
                                close()
                            }
                            drawPath(leftEar, color = cSecondary)
                            drawPath(rightEar, color = cSecondary)
                            // Companion Body
                            drawCircle(
                                brush = Brush.verticalGradient(listOf(cPrimary, cSecondary)),
                                radius = 8.5f * scale,
                                center = Offset(anchor.x, anchor.y - 12f * scale + bob)
                            )
                            // Glowing Eyes
                            drawCircle(
                                color = Color.White,
                                radius = 1.8f * scale,
                                center = Offset(anchor.x - 3f * scale, anchor.y - 13f * scale + bob)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.8f * scale,
                                center = Offset(anchor.x + 3f * scale, anchor.y - 13f * scale + bob)
                            )
                        }
                    }

                    RenderItemType.TRAINER -> {
                        val walkBob = abs(sin(animPhase * 2f)) * 2.5f * scale
                        // Ground drop shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.50f),
                            topLeft = Offset(anchor.x - 11f * scale, anchor.y - 5f * scale),
                            size = Size(22f * scale, 9f * scale)
                        )
                        // Boots / Legs
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(anchor.x - 5.5f * scale, anchor.y - 8f * scale - walkBob * 0.4f),
                            size = Size(4.5f * scale, 8f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(anchor.x + 1f * scale, anchor.y - 8f * scale - walkBob * 0.4f),
                            size = Size(4.5f * scale, 8f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        // Backpack (visible especially when facing UP/LEFT/RIGHT)
                        val packOffsetX = when (trainerDirection) {
                            TrainerDirection.LEFT -> 5f * scale
                            TrainerDirection.RIGHT -> -5f * scale
                            else -> 0f
                        }
                        drawRoundRect(
                            color = BadgeGold,
                            topLeft = Offset(anchor.x - 7f * scale + packOffsetX, anchor.y - 21f * scale - walkBob),
                            size = Size(14f * scale, 11f * scale),
                            cornerRadius = CornerRadius(4f * scale, 4f * scale)
                        )
                        // Trainer Tactical Jacket Torso
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(AetherCyan, AetherCyanDark)),
                            topLeft = Offset(anchor.x - 7.5f * scale, anchor.y - 22f * scale - walkBob),
                            size = Size(15f * scale, 15f * scale),
                            cornerRadius = CornerRadius(5f * scale, 5f * scale)
                        )
                        // White Collar / Stripe
                        drawLine(
                            color = Color.White,
                            start = Offset(anchor.x, anchor.y - 21f * scale - walkBob),
                            end = Offset(anchor.x, anchor.y - 9f * scale - walkBob),
                            strokeWidth = 2f * scale
                        )
                        // Trainer Head
                        drawCircle(
                            color = Color(0xFFFFDFC4),
                            radius = 7.5f * scale,
                            center = Offset(anchor.x, anchor.y - 28f * scale - walkBob)
                        )
                        // Eyes (shown when not facing UP)
                        if (trainerDirection != TrainerDirection.UP) {
                            val eyeShiftX = when (trainerDirection) {
                                TrainerDirection.LEFT -> -2.2f * scale
                                TrainerDirection.RIGHT -> 2.2f * scale
                                else -> 0f
                            }
                            drawCircle(
                                color = MidnightSlate,
                                radius = 1.5f * scale,
                                center = Offset(anchor.x - 2.4f * scale + eyeShiftX, anchor.y - 27.5f * scale - walkBob)
                            )
                            drawCircle(
                                color = MidnightSlate,
                                radius = 1.5f * scale,
                                center = Offset(anchor.x + 2.4f * scale + eyeShiftX, anchor.y - 27.5f * scale - walkBob)
                            )
                        }
                        // Iconic Pokémon-Style Trainer Cap & Brim
                        drawArc(
                            color = CrimsonAccent,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(anchor.x - 8.2f * scale, anchor.y - 36.5f * scale - walkBob),
                            size = Size(16.4f * scale, 14f * scale)
                        )
                        // White Poké/Aether crest on cap front
                        if (trainerDirection != TrainerDirection.UP) {
                            drawCircle(
                                color = Color.White,
                                radius = 2.4f * scale,
                                center = Offset(anchor.x, anchor.y - 32.5f * scale - walkBob)
                            )
                        }
                        // Cap Visor Brim pointing in facing direction
                        val brimOffsetX = when (trainerDirection) {
                            TrainerDirection.LEFT -> -5f * scale
                            TrainerDirection.RIGHT -> 5f * scale
                            else -> 0f
                        }
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(anchor.x - 6f * scale + brimOffsetX, anchor.y - 30.5f * scale - walkBob),
                            size = Size(12f * scale, 2.8f * scale),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }

            // 5. Day / Night Atmospheric Lighting Tint
            drawRect(color = timeOverlayColor, size = size)

            // 6. Subtle Dynamic 2.5D Weather Particle Overlay (reduced count for visual clarity)
            for (i in 0 until 10) {
                val drift = (animPhase / 6.2831855f) * 100f
                val wx = ((i * 73 + playerX * 19 + drift.toInt()) % 100) / 100f * w
                val wy = ((i * 47 + playerY * 23 + (drift * 1.4f).toInt()) % 100) / 100f * h
                when (weather) {
                    WeatherType.TROPICAL_RAIN, WeatherType.THUNDER_TEMPEST -> {
                        drawLine(
                            color = if (weather == WeatherType.THUNDER_TEMPEST && i % 4 == 0) ElementVolt else ElementAqua.copy(alpha = 0.60f),
                            start = Offset(wx, wy),
                            end = Offset(wx - 7f, wy + 16f),
                            strokeWidth = 2f
                        )
                    }
                    WeatherType.SOLAR_FLARE -> {
                        drawCircle(
                            color = ElementLux.copy(alpha = 0.25f),
                            radius = (6f + (i % 3) * 3f),
                            center = Offset(wx, wy)
                        )
                    }
                    WeatherType.OBSIDIAN_SANDSTORM -> {
                        drawLine(
                            color = ElementDraco.copy(alpha = 0.45f),
                            start = Offset(wx, wy),
                            end = Offset(wx + 18f, wy + 3f),
                            strokeWidth = 2.4f
                        )
                    }
                    WeatherType.AETHER_MIST -> {
                        val angle = animPhase + i * 0.5f
                        drawCircle(
                            color = AetherCyan.copy(alpha = 0.38f),
                            radius = 5.5f + (sin(angle) * 2f),
                            center = Offset(wx + cos(angle) * 5f, wy)
                        )
                    }
                }
            }
        }

        // Minimal, unobtrusive Coordinate & Companion Pill in Top-Left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(MidnightSlate.copy(alpha = 0.68f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Pos (${playerX},${playerY})",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (companionSpecies != null) {
                Text(
                    text = "• ${companionSpecies.nameDe}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadgeGold
                )
            }
        }
    }
}

