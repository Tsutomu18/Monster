package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.ArenaRoster
import com.example.data.remote.TradeOffer
import com.example.data.remote.UiState
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.DynamicQuest
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronGameState
import com.example.ui.viewmodel.AetheronViewModel

@Composable
fun QuestsCraftingAndOnlineScreen(
    gameState: AetheronGameState,
    openTradesState: UiState<List<TradeOffer>>,
    arenaRostersState: UiState<List<ArenaRoster>>,
    viewModel: AetheronViewModel
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Dynamic Quests & Factions, 1: Crafting & Skills, 2: Online Trade & Arena

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "Quests & Fraktionen",
                1 to "Crafting & Skills",
                2 to "Online-Tausch & PvP"
            ).forEach { (idx, title) ->
                FilterChip(
                    selected = subTab == idx,
                    onClick = { subTab = idx },
                    label = { Text(title, fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_subtab_$idx")
                )
            }
        }

        when (subTab) {
            0 -> DynamicQuestsAndFactionsSection(gameState, viewModel)
            1 -> CraftingAndSkillTreeSection(gameState, viewModel)
            else -> OnlineTradeAndArenaSection(gameState, openTradesState, arenaRostersState, viewModel)
        }
    }
}

@Composable
private fun DynamicQuestsAndFactionsSection(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel
) {
    val currentBiome = AetheronWorldCatalog.biomes.find { it.id == gameState.currentBiomeId }
        ?: AetheronWorldCatalog.biomes.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Dynamic Quest Generator Context Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, AetherCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DYNAMISCHER QUEST-GENERATOR",
                                style = MaterialTheme.typography.titleLarge,
                                color = AetherCyan
                            )
                            Text(
                                text = "Faktoren: Standort (${currentBiome.nameDe}) • Stufe ${gameState.playerLevel} • Erfüllt: ${gameState.completedQuestsCount} • Wetter: ${gameState.currentWeather.labelDe}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        FilledTonalButton(
                            onClick = { viewModel.refreshDynamicQuests() },
                            modifier = Modifier.testTag("refresh_dynamic_quests_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Neu generieren", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Neu generieren")
                        }
                    }
                }
            }
        }

        // 2. Active Dynamic Quests List
        items(gameState.activeQuests, key = { it.id }) { quest ->
            DynamicQuestCard(
                quest = quest,
                onAdvanceProgress = { viewModel.simulateQuestStepProgress(quest.id) },
                onClaimReward = { viewModel.claimQuestReward(quest.id) }
            )
        }

        // 3. Faction Politics & Reputation
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
                        text = "FRAKTIONEN & POLITISCHES SYSTEM",
                        style = MaterialTheme.typography.titleLarge,
                        color = BadgeGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AetheronWorldCatalog.factions.forEach { faction ->
                        val rep = gameState.factionStandings[faction.id] ?: 100
                        Surface(
                            color = ElevatedTacticalCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${faction.nameDe} (Kontinent ${faction.homeContinentId})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ruf: $rep" + (if (rep >= 250) " [VERBÜNDET]" else ""),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (rep >= 250) ElementTerra else BadgeGold
                                    )
                                }
                                Text(
                                    text = faction.ideologyDe,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Bonus: ${faction.perkDescriptionDe}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicQuestCard(
    quest: DynamicQuest,
    onAdvanceProgress: () -> Unit,
    onClaimReward: () -> Unit
) {
    val rewardItemName = AetheronWorldCatalog.items.find { it.id == quest.rewardItemId }?.nameDe ?: quest.rewardItemId
    val starsText = "★".repeat(quest.difficultyStars) + "☆".repeat((5 - quest.difficultyStars).coerceAtLeast(0))

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (quest.isCompleted) ElementTerra else TacticalBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${quest.category.icon} ${quest.category.labelDe} • ${quest.factionNameDe}",
                    style = MaterialTheme.typography.labelLarge,
                    color = AetherCyan
                )
                Text(
                    text = "Schwierigkeit: $starsText",
                    style = MaterialTheme.typography.labelLarge,
                    color = BadgeGold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = quest.titleDe,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = quest.descriptionDe,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (quest.currentProgress.toFloat() / quest.requiredCount.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (quest.isCompleted) ElementTerra else AetherCyan,
                trackColor = MidnightSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fortschritt: ${quest.currentProgress}/${quest.requiredCount}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Belohnung: +${quest.rewardGold} Gold • +${quest.rewardXp} EP • +${quest.rewardFactionRep} Ruf • ${quest.rewardItemCount}x $rewardItemName",
                        style = MaterialTheme.typography.labelMedium,
                        color = BadgeGold
                    )
                }

                if (quest.isCompleted) {
                    Button(
                        onClick = onClaimReward,
                        modifier = Modifier.testTag("claim_quest_${quest.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElementTerra,
                            contentColor = MidnightSlate
                        )
                    ) {
                        Text("Belohnung einlösen", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onAdvanceProgress,
                        modifier = Modifier.testTag("advance_quest_${quest.id}")
                    ) {
                        Text("Mission ausführen")
                    }
                }
            }
        }
    }
}

@Composable
private fun CraftingAndSkillTreeSection(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Player Skill Tree
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
                        Text(
                            text = "CHARAKTER-SKILL-SYSTEM",
                            style = MaterialTheme.typography.titleLarge,
                            color = AetherCyan
                        )
                        Text(
                            text = "Verfügbare Skill-Punkte: ${gameState.skillPoints}",
                            style = MaterialTheme.typography.labelLarge,
                            color = BadgeGold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    gameState.playerSkills.forEach { node ->
                        Surface(
                            color = ElevatedTacticalCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${node.nameDe} (Rang ${node.currentRank}/${node.maxRank})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = node.descriptionDe,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Button(
                                    onClick = { viewModel.upgradePlayerSkill(node.id) },
                                    enabled = gameState.skillPoints > 0 && node.currentRank < node.maxRank,
                                    modifier = Modifier.testTag("upgrade_skill_${node.id}")
                                ) {
                                    Text("+1 Rang")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Crafting Forge (Spheres, Potions, Evolution Artifacts & Gear)
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
                        text = "AETHER-SCHMIEDE & CRAFTING (${gameState.gold} GOLD)",
                        style = MaterialTheme.typography.titleLarge,
                        color = BadgeGold
                    )
                    Text(
                        text = "Stelle Fang-Sphären, Heiltränke, Trainings-Ausrüstung und seltene Evolutions-Artefakte aus Biom-Ressourcen her.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AetheronWorldCatalog.recipes.forEach { rec ->
                        val resultItem = AetheronWorldCatalog.items.find { it.id == rec.resultItemId }
                        val ownedResult = gameState.inventory[rec.resultItemId] ?: 0
                        val canCraft = gameState.gold >= rec.goldCost && rec.ingredients.all { (ingId, req) ->
                            (gameState.inventory[ingId] ?: 0) >= req
                        }
                        val ingredientsDesc = rec.ingredients.entries.joinToString(" + ") { (ingId, req) ->
                            val ingName = AetheronWorldCatalog.items.find { it.id == ingId }?.nameDe ?: ingId
                            val have = gameState.inventory[ingId] ?: 0
                            "$ingName ($have/$req)"
                        }

                        Surface(
                            color = ElevatedTacticalCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${rec.resultCount}x ${resultItem?.nameDe ?: rec.resultItemId} (Besitz: $ownedResult)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = resultItem?.descriptionDe ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Benötigt: $ingredientsDesc • ${rec.goldCost} Gold",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AetherCyan
                                    )
                                }
                                Button(
                                    onClick = { viewModel.craftRecipe(rec) },
                                    enabled = canCraft,
                                    modifier = Modifier.testTag("craft_btn_${rec.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BadgeGold,
                                        contentColor = MidnightSlate
                                    )
                                ) {
                                    Text("Herstellen", fontWeight = FontWeight.Bold)
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
private fun OnlineTradeAndArenaSection(
    gameState: AetheronGameState,
    openTradesState: UiState<List<TradeOffer>>,
    arenaRostersState: UiState<List<ArenaRoster>>,
    viewModel: AetheronViewModel
) {
    val activeMon = gameState.monsters.find { it.instanceId == gameState.selectedMonsterId }
        ?: gameState.monsters.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Cloud Save & Online PvP Arena Registration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AetherCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CROSS-PLAY CLOUD & ONLINE-ARENA",
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan
                    )
                    Text(
                        text = "Synchronisiere deinen Fortschritt in der Cloud oder registriere dein Top-3-Team in der globalen PvP-Arena.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncToCloud(silent = false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cloud_sync_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = MidnightSlate)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud-Save", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.publishSquadToOnlineArena() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("publish_arena_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BadgeGold, contentColor = MidnightSlate)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Arena-Team melden", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Online Arena Challengers
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
                        text = "ONLINE-PVP-ARENA HERAUSFORDERER (SIEGE: ${gameState.arenaWins})",
                        style = MaterialTheme.typography.titleLarge,
                        color = BadgeGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when (arenaRostersState) {
                        is UiState.Loading -> CircularProgressIndicator(color = AetherCyan)
                        is UiState.Error -> Text(arenaRostersState.message, color = ElementPyro)
                        is UiState.Success -> {
                            val rosters = arenaRostersState.data
                            if (rosters.isEmpty()) {
                                Text(
                                    text = "Noch keine Herausforderer registriert. Melde dein Team oben als Erster an!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            } else {
                                rosters.forEach { challenger ->
                                    Surface(
                                        color = ElevatedTacticalCard,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${challenger.trainerName} • Kampfkraft ${challenger.combatPower}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = challenger.teamSummary,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary
                                                )
                                            }
                                            Button(
                                                onClick = { viewModel.startOnlineArenaDuel(challenger) },
                                                modifier = Modifier.testTag("duel_arena_${challenger.id}"),
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent, contentColor = TextPrimary)
                                            ) {
                                                Text("Duellieren")
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

        // 3. Global Monster Trade Network
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GLOBALER SPIELER-MONSTER-TAUSCH",
                                style = MaterialTheme.typography.titleLarge,
                                color = AetherCyan
                            )
                            Text(
                                text = "Biete ${activeMon.nickname} (Lv.${activeMon.level}, Gen ${activeMon.ivRank}) im Tausch gegen seltene Kontinent-Spezies an.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                val targetSpId = ((activeMon.speciesId + 80) % 240).coerceAtLeast(1)
                                viewModel.postMonsterTradeOffer(activeMon.instanceId, targetSpId)
                            },
                            modifier = Modifier.testTag("post_trade_offer_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anbieten")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (openTradesState) {
                        is UiState.Loading -> CircularProgressIndicator(color = AetherCyan)
                        is UiState.Error -> Text(openTradesState.message, color = ElementPyro)
                        is UiState.Success -> {
                            val trades = openTradesState.data
                            if (trades.isEmpty()) {
                                Text(
                                    text = "Derzeit keine offenen Tauschangebote. Erstelle oben das erste Angebot!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            } else {
                                trades.forEach { offer ->
                                    Surface(
                                        color = ElevatedTacticalCard,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${offer.offeredMonsterName} Lv.${offer.offeredLevel} (${offer.offeredIvRank}) ⇄ ${offer.requestedSpeciesName}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Trainer: ${offer.ownerName} • Gen-Merkmal: ${offer.offeredTrait} • ${offer.offeredTrainedStats}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = ElementTerra
                                                )
                                            }
                                            Button(
                                                onClick = { viewModel.acceptTradeOffer(offer) },
                                                modifier = Modifier.testTag("accept_trade_${offer.id}")
                                            ) {
                                                Text("Tauschen")
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
}
