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
import com.example.ui.components.MonsterCanvasAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronGameState
import com.example.ui.viewmodel.AetheronViewModel
import com.example.ui.viewmodel.BattleState

@Composable
fun WorldAndBattleScreen(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel
) {
    val currentBiome = AetheronWorldCatalog.biomes.find { it.id == gameState.currentBiomeId }
        ?: AetheronWorldCatalog.biomes.first()
    val continentBiomes = AetheronWorldCatalog.biomes.filter { it.continentId == gameState.currentContinentId }
    val continentBadges = AetheronWorldCatalog.gymBadges.filter { it.continentId == gameState.currentContinentId }
    val continentNpcs = AetheronWorldCatalog.worldNpcs.filter { it.continentId == gameState.currentContinentId }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // 1. Continent Selector (3 Continents, 80 Monsters & 8 Badges each)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "3 GROSSE KONTINENTE & WETTER-SIMULATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = AetherCyan
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
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Biome Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(continentBiomes) { biome ->
                                val isCurrent = biome.id == currentBiome.id
                                AssistChip(
                                    onClick = { viewModel.selectContinentAndBiome(biome.continentId, biome.id) },
                                    label = {
                                        Text("${biome.primaryElement.iconCode} ${biome.nameDe} (Lv.${biome.recommendedLevel})")
                                    },
                                    modifier = Modifier.testTag("biome_chip_${biome.id}"),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isCurrent) biome.primaryElement.color.copy(alpha = 0.25f) else ElevatedTacticalCard,
                                        labelColor = if (isCurrent) biome.primaryElement.color else TextPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Weather & Day-Night Cycle Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ElevatedTacticalCard, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wetter: ${gameState.currentWeather.labelDe} | ${gameState.currentTimeOfDay.labelDe} (${gameState.currentTimeOfDay.hoursRange})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BadgeGold
                                )
                                Text(
                                    text = gameState.currentWeather.descriptionDe,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
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
            }

            // 2. Interactive 2D Tile Map + Touch D-Pad Controls
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentBiome.nameDe.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Koordinaten (${gameState.playerX}, ${gameState.playerY}) • Tippe auf die Karte oder nutze das Steuerkreuz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { viewModel.triggerWildEncounter() },
                                modifier = Modifier.testTag("wild_encounter_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.CatchingPokemon, contentDescription = "Spüren", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Monster aufspüren")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        InteractiveWorldMapCanvas(
                            biome = currentBiome,
                            playerX = gameState.playerX,
                            playerY = gameState.playerY,
                            weather = gameState.currentWeather,
                            timeOfDay = gameState.currentTimeOfDay,
                            npcs = continentNpcs,
                            onTileTapped = { dx, dy -> viewModel.movePlayerOnMap(dx, dy) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Touch-Optimized D-Pad & NPC Schedule Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedIconButton(
                                    onClick = { viewModel.movePlayerOnMap(-1, 0) },
                                    modifier = Modifier.size(48.dp).testTag("dpad_left")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Links", tint = AetherCyan)
                                }
                                OutlinedIconButton(
                                    onClick = { viewModel.movePlayerOnMap(0, -1) },
                                    modifier = Modifier.size(48.dp).testTag("dpad_up")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Oben", tint = AetherCyan)
                                }
                                OutlinedIconButton(
                                    onClick = { viewModel.movePlayerOnMap(0, 1) },
                                    modifier = Modifier.size(48.dp).testTag("dpad_down")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Unten", tint = AetherCyan)
                                }
                                OutlinedIconButton(
                                    onClick = { viewModel.movePlayerOnMap(1, 0) },
                                    modifier = Modifier.size(48.dp).testTag("dpad_right")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Rechts", tint = AetherCyan)
                                }
                            }

                            val activeNpc = continentNpcs.firstOrNull()
                            if (activeNpc != null) {
                                Surface(
                                    color = ElevatedTacticalCard,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "NPC-Tagesablauf: ${activeNpc.nameDe}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = BadgeGold
                                        )
                                        Text(
                                            text = activeNpc.dialogueByTime[gameState.currentTimeOfDay] ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. 24 Gym Badges (8 on current Continent)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ARENA-ORDEN AUF KONTINENT ${gameState.currentContinentId} (${gameState.earnedBadges.size}/24 GESAMT)",
                            style = MaterialTheme.typography.labelLarge,
                            color = BadgeGold
                        )
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
                                        color = if (won) BadgeGold.copy(alpha = 0.16f) else ElevatedTacticalCard,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (won) BadgeGold else TacticalBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.startGymLeaderBattle(badge) }
                                            .testTag("gym_badge_${badge.badgeIndex}")
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "#${badge.badgeIndex} ${badge.badgeNameDe}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (won) BadgeGold else TextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(text = if (won) "🏆" else badge.element.iconCode)
                                            }
                                            Text(
                                                text = "${badge.leaderNameDe} • Lv.${badge.recommendedLevel}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Tactical Turn-Based Battle Overlay
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
            .background(MidnightSlate.copy(alpha = 0.96f))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, AetherCyan.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = battle.opponentTitleDe,
                            style = MaterialTheme.typography.titleLarge,
                            color = BadgeGold
                        )
                        Text(
                            text = "Runde ${battle.turnNumber} • Wetter: ${gameState.currentWeather.labelDe} (${gameState.currentTimeOfDay.labelDe})",
                            style = MaterialTheme.typography.labelMedium,
                            color = AetherCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.closeBattleOverlay() },
                        modifier = Modifier.testTag("close_battle_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kampf verlassen", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Arena Combatants Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedTacticalCard, RoundedCornerShape(16.dp))
                        .padding(12.dp),
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
                            size = 82.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${playerMon.nickname} Lv.${playerMon.level}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        val pMaxHp = playerMon.maxHp(playerSpecies).coerceAtLeast(1)
                        LinearProgressIndicator(
                            progress = { (playerMon.currentHp.toFloat() / pMaxHp).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = ElementTerra,
                            trackColor = MidnightSlate
                        )
                        Text(
                            text = "KP: ${playerMon.currentHp}/$pMaxHp | AP: ${playerMon.currentStamina}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.headlineLarge,
                            color = CrimsonAccent
                        )
                        if (battle.lastEffectText.isNotEmpty()) {
                            Text(
                                text = battle.lastEffectText,
                                style = MaterialTheme.typography.labelLarge,
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
                            size = 82.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${enemyMon.nickname} Lv.${enemyMon.level}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { (enemyMon.currentHp.toFloat() / battle.enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = ElementPyro,
                            trackColor = MidnightSlate
                        )
                        Text(
                            text = "KP: ${enemyMon.currentHp}/${battle.enemyMaxHp} (${enemySpecies.primaryElement.labelDe})",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Battle Log
                Surface(
                    color = MidnightSlate,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp, max = 110.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        battle.battleLog.takeLast(4).forEach { line ->
                            Text(
                                text = "• $line",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (battle.isFinished) {
                    Button(
                        onClick = { viewModel.closeBattleOverlay() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("finish_battle_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = MidnightSlate)
                    ) {
                        Text("Weiter zur Weltkarte", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // 4 Tactical Elemental Skills
                    Text(
                        text = "TAKTISCHE FÄHIGKEITEN (WETTER & ELEMENTE BEACHTEN)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "${sk.element.iconCode} ${sk.nameDe}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = sk.element.color
                                        )
                                        Text(
                                            text = "Pwr ${sk.power} • ${sk.staminaCost} AP • $effBadge",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Capture Orbs & Healing Row
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("capture_basic_button")
                            ) {
                                Text("Sphäre ($basicOrbs)")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.attemptCaptureInBattle(useMasterOrb = true) },
                                enabled = masterOrbs > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("capture_master_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BadgeGold.copy(alpha = 0.22f),
                                    contentColor = BadgeGold
                                )
                            ) {
                                Text("Hyper ($masterOrbs)")
                            }
                        }
                        val potions = gameState.inventory["pot_vital_elixir"] ?: 0
                        FilledTonalButton(
                            onClick = { viewModel.usePotionInBattle() },
                            enabled = potions > 0,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("use_potion_button")
                        ) {
                            Text("Heilen ($potions)")
                        }
                    }
                }
            }
        }
    }
}
