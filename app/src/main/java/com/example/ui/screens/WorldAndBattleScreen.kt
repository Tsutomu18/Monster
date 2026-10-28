package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.*
import com.example.ui.components.InteractiveWorldMapCanvas
import com.example.ui.components.MapCameraMode
import com.example.ui.components.MonsterCanvasAvatar
import com.example.ui.components.TrainerDirection
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronGameState
import com.example.ui.viewmodel.AetheronViewModel
import com.example.ui.viewmodel.BattleState

@Composable
fun WorldAndBattleScreen(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel,
    onOpenSystemMenu: (initialSection: Int) -> Unit = {}
) {
    val currentBiome = AetheronWorldCatalog.biomes.find { it.id == gameState.currentBiomeId }
        ?: AetheronWorldCatalog.biomes.first()
    val continentNpcs = AetheronWorldCatalog.worldNpcs.filter { it.continentId == gameState.currentContinentId }

    var cameraMode by remember { mutableStateOf(MapCameraMode.POKEMON_25D) }
    var trainerDirection by remember { mutableStateOf(TrainerDirection.DOWN) }
    val leadMonster = gameState.monsters.firstOrNull()
    val companionSpecies = remember(leadMonster?.speciesId) {
        leadMonster?.let { m -> AetheronWorldCatalog.allSpecies.find { it.id == m.speciesId } }
            ?: AetheronWorldCatalog.allSpecies.firstOrNull()
    }

    fun moveTrainer(dx: Int, dy: Int) {
        trainerDirection = when {
            dx < 0 -> TrainerDirection.LEFT
            dx > 0 -> TrainerDirection.RIGHT
            dy < 0 -> TrainerDirection.UP
            dy > 0 -> TrainerDirection.DOWN
            else -> trainerDirection
        }
        viewModel.movePlayerOnMap(dx, dy)
    }

    val activeNpc = continentNpcs.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Minimal Location & Weather Context Strip above the 2.5D Map
            Surface(
                color = DeepTacticalSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${currentBiome.primaryElement.iconCode} ${currentBiome.nameDe}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "(Lv.${currentBiome.recommendedLevel})",
                                style = MaterialTheme.typography.labelMedium,
                                color = AetherCyan
                            )
                        }
                        Text(
                            text = "☀️ ${gameState.currentWeather.labelDe} • ${gameState.currentTimeOfDay.labelDe}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BadgeGold,
                            maxLines = 1
                        )
                    }

                    // Compact 2.5D / Iso Camera Switch
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapCameraMode.entries.forEach { mode ->
                            val isSelected = cameraMode == mode
                            val shortLabel = if (mode == MapCameraMode.POKEMON_25D) "2.5D" else "Iso"
                            Text(
                                text = shortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MidnightSlate else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AetherCyan else ElevatedTacticalCard)
                                    .clickable { cameraMode = mode }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("camera_mode_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 2. Large, Unobstructed 2.5D Pokémon-Style Overworld Map
            InteractiveWorldMapCanvas(
                biome = currentBiome,
                playerX = gameState.playerX,
                playerY = gameState.playerY,
                weather = gameState.currentWeather,
                timeOfDay = gameState.currentTimeOfDay,
                npcs = continentNpcs,
                onTileTapped = { dx, dy -> moveTrainer(dx, dy) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                companionSpecies = companionSpecies,
                trainerDirection = trainerDirection,
                cameraMode = cameraMode
            )

            // 3. Clean Handheld Control Deck: D-Pad + Wild Encounter + SYSTEM Button
            Surface(
                color = DeepTacticalSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Row of Control Deck: D-Pad on Left + Action Buttons on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedIconButton(
                                onClick = { moveTrainer(-1, 0) },
                                modifier = Modifier.size(48.dp).testTag("dpad_left")
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Links", tint = AetherCyan)
                            }
                            OutlinedIconButton(
                                onClick = { moveTrainer(0, -1) },
                                modifier = Modifier.size(48.dp).testTag("dpad_up")
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Oben", tint = AetherCyan)
                            }
                            OutlinedIconButton(
                                onClick = { moveTrainer(0, 1) },
                                modifier = Modifier.size(48.dp).testTag("dpad_down")
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Unten", tint = AetherCyan)
                            }
                            OutlinedIconButton(
                                onClick = { moveTrainer(1, 0) },
                                modifier = Modifier.size(48.dp).testTag("dpad_right")
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Rechts", tint = AetherCyan)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.triggerWildEncounter() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("wild_encounter_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CatchingPokemon, contentDescription = "Spüren", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Spüren", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onOpenSystemMenu(0) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("world_system_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AetherCyan,
                                    contentColor = MidnightSlate
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Apps, contentDescription = "System Menü", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("System", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Subtle Single-Line NPC Hint Bar
                    if (activeNpc != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ElevatedTacticalCard, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "💬 ${activeNpc.nameDe}: ${activeNpc.dialogueByTime[gameState.currentTimeOfDay] ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Clean, Decluttered Tactical Turn-Based Battle Overlay
        AnimatedVisibility(visible = gameState.activeBattle != null) {
            gameState.activeBattle?.let { battle ->
                TacticalBattleTheaterModal(
                    gameState = gameState,
                    battle = battle,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Content for the "Welt & Reise" and "Arena-Orden" sections inside the central System Menu.
 */
@Composable
fun SystemWorldAndBadgesSection(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel,
    showBadgesSection: Boolean,
    onCloseSystemMenu: () -> Unit
) {
    val currentBiome = AetheronWorldCatalog.biomes.find { it.id == gameState.currentBiomeId }
        ?: AetheronWorldCatalog.biomes.first()
    val continentBiomes = AetheronWorldCatalog.biomes.filter { it.continentId == gameState.currentContinentId }
    val continentBadges = AetheronWorldCatalog.gymBadges.filter { it.continentId == gameState.currentContinentId }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (!showBadgesSection) {
            // WELT & REISE SECTION
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedTacticalCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "KONTINENT WÄHLEN",
                            style = MaterialTheme.typography.labelLarge,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                1 to "1. Sylvaria",
                                2 to "2. Pyroclast",
                                3 to "3. Aetheris"
                            ).forEach { (cid, label) ->
                                val selected = gameState.currentContinentId == cid
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val firstBiome = AetheronWorldCatalog.biomes.first { it.continentId == cid }
                                        viewModel.selectContinentAndBiome(cid, firstBiome.id)
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("continent_chip_$cid"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AetherCyan.copy(alpha = 0.22f),
                                        selectedLabelColor = AetherCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "BIOME AUF KONTINENT ${gameState.currentContinentId}",
                            style = MaterialTheme.typography.labelLarge,
                            color = BadgeGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(continentBiomes) { biome ->
                                val isCurrent = biome.id == currentBiome.id
                                AssistChip(
                                    onClick = {
                                        viewModel.selectContinentAndBiome(biome.continentId, biome.id)
                                        onCloseSystemMenu()
                                    },
                                    label = {
                                        Text("${biome.primaryElement.iconCode} ${biome.nameDe} (Lv.${biome.recommendedLevel})")
                                    },
                                    modifier = Modifier.testTag("biome_chip_${biome.id}"),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isCurrent) biome.primaryElement.color.copy(alpha = 0.25f) else DeepTacticalSurface,
                                        labelColor = if (isCurrent) biome.primaryElement.color else TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedTacticalCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WETTER & TAGESZEIT-SIMULATION",
                                style = MaterialTheme.typography.labelLarge,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gameState.currentWeather.labelDe} • ${gameState.currentTimeOfDay.labelDe} (${gameState.currentTimeOfDay.hoursRange})",
                                style = MaterialTheme.typography.titleMedium,
                                color = BadgeGold
                            )
                            Text(
                                text = gameState.currentWeather.descriptionDe,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        FilledTonalButton(
                            onClick = { viewModel.cycleWeatherAndTime() },
                            modifier = Modifier.testTag("cycle_weather_time_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherCyan.copy(alpha = 0.18f),
                                contentColor = AetherCyan
                            )
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = "Zyklus wechseln", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zyklus")
                        }
                    }
                }
            }
        } else {
            // ARENA-ORDEN SECTION
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElevatedTacticalCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ARENA-ORDEN (KONTINENT ${gameState.currentContinentId})",
                                style = MaterialTheme.typography.labelLarge,
                                color = BadgeGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${gameState.earnedBadges.size}/24 Gesamt",
                                style = MaterialTheme.typography.labelMedium,
                                color = AetherCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Quick continent switcher inside Orden view too
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1 to "Sylvaria", 2 to "Pyroclast", 3 to "Aetheris").forEach { (cid, name) ->
                                FilterChip(
                                    selected = gameState.currentContinentId == cid,
                                    onClick = {
                                        val firstBiome = AetheronWorldCatalog.biomes.first { it.continentId == cid }
                                        viewModel.selectContinentAndBiome(cid, firstBiome.id)
                                    },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        continentBadges.chunked(2).forEach { rowBadges ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowBadges.forEach { badge ->
                                    val won = badge.badgeIndex in gameState.earnedBadges
                                    Surface(
                                        color = if (won) BadgeGold.copy(alpha = 0.18f) else DeepTacticalSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (won) BadgeGold else TacticalBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                onCloseSystemMenu()
                                                viewModel.startGymLeaderBattle(badge)
                                            }
                                            .testTag("gym_badge_${badge.badgeIndex}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "#${badge.badgeIndex} ${badge.badgeNameDe}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (won) BadgeGold else TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${badge.leaderNameDe} • Lv.${badge.recommendedLevel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                            Text(text = if (won) "🏆" else badge.element.iconCode)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalBattleTheaterModal(
    gameState: AetheronGameState,
    battle: BattleState,
    viewModel: AetheronViewModel
) {
    val playerMon = gameState.monsters.find { it.instanceId == battle.playerMonsterId }
        ?: gameState.monsters.first()
    val playerSpecies = AetheronWorldCatalog.getSpecies(playerMon.speciesId)
    val enemyMon = battle.enemyMonster
    val enemySpecies = AetheronWorldCatalog.getSpecies(enemyMon.speciesId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightSlate.copy(alpha = 0.92f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AetherCyan.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Slim Battle Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = battle.opponentTitleDe,
                            style = MaterialTheme.typography.titleMedium,
                            color = BadgeGold,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "• R${battle.turnNumber} (${gameState.currentWeather.labelDe})",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherCyan,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = { viewModel.closeBattleOverlay() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_battle_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kampf verlassen", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clean Arena Combatants Stage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedTacticalCard, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player Side
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MonsterCanvasAvatar(
                            species = playerSpecies,
                            ivRank = playerMon.ivRank,
                            size = 68.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${playerMon.nickname} Lv.${playerMon.level}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        val pMaxHp = playerMon.maxHp(playerSpecies).coerceAtLeast(1)
                        LinearProgressIndicator(
                            progress = { (playerMon.currentHp.toFloat() / pMaxHp).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(vertical = 3.dp)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = ElementTerra,
                            trackColor = MidnightSlate
                        )
                        Text(
                            text = "${playerMon.currentHp}/$pMaxHp KP • ${playerMon.currentStamina} AP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrimsonAccent,
                            fontWeight = FontWeight.Black
                        )
                        if (battle.lastEffectText.isNotEmpty()) {
                            Text(
                                text = battle.lastEffectText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(battle.lastEffectColorHex)
                            )
                        }
                    }

                    // Enemy Side
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MonsterCanvasAvatar(
                            species = enemySpecies,
                            ivRank = enemyMon.ivRank,
                            size = 68.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${enemyMon.nickname} Lv.${enemyMon.level}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        LinearProgressIndicator(
                            progress = { (enemyMon.currentHp.toFloat() / battle.enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(vertical = 3.dp)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = ElementPyro,
                            trackColor = MidnightSlate
                        )
                        Text(
                            text = "${enemyMon.currentHp}/${battle.enemyMaxHp} KP (${enemySpecies.primaryElement.labelDe})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact 2-Line Battle Ticker
                Surface(
                    color = MidnightSlate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        battle.battleLog.takeLast(2).forEach { line ->
                            Text(
                                text = "• $line",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (battle.isFinished) {
                    Button(
                        onClick = { viewModel.closeBattleOverlay() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("finish_battle_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = MidnightSlate)
                    ) {
                        Text("Weiter zur Weltkarte", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Clean 2x2 Tactical Skills Grid
                    playerSpecies.skills.chunked(2).forEach { rowSkills ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSkills.forEach { sk ->
                                val eff = sk.element.effectivenessAgainst(enemySpecies.primaryElement)
                                val effBadge = when {
                                    eff > 1.2f -> "▲1.6x"
                                    eff < 0.85f -> "▼0.65x"
                                    else -> "1.0x"
                                }
                                OutlinedButton(
                                    onClick = { viewModel.executeCombatTurn(sk) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("skill_btn_${sk.id}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${sk.element.iconCode} ${sk.nameDe}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = sk.element.color,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Pwr ${sk.power} • $effBadge",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Compact Capture Orbs & Healing Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!battle.isGymBattle && !battle.isArenaPvp) {
                            val basicOrbs = gameState.inventory["orb_aether_basic"] ?: 0
                            val masterOrbs = gameState.inventory["orb_aether_master"] ?: 0
                            FilledTonalButton(
                                onClick = { viewModel.attemptCaptureInBattle(useMasterOrb = false) },
                                enabled = basicOrbs > 0,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("capture_basic_button")
                            ) {
                                Text("Sphäre ($basicOrbs)", style = MaterialTheme.typography.labelMedium)
                            }
                            FilledTonalButton(
                                onClick = { viewModel.attemptCaptureInBattle(useMasterOrb = true) },
                                enabled = masterOrbs > 0,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("capture_master_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BadgeGold.copy(alpha = 0.22f),
                                    contentColor = BadgeGold
                                )
                            ) {
                                Text("Hyper ($masterOrbs)", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        val potions = gameState.inventory["pot_vital_elixir"] ?: 0
                        FilledTonalButton(
                            onClick = { viewModel.usePotionInBattle() },
                            enabled = potions > 0,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("use_potion_button")
                        ) {
                            Text("Heilen ($potions)", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
