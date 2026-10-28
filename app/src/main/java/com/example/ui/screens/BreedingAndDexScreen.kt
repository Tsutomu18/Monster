package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.ui.components.MonsterCanvasAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronGameState
import com.example.ui.viewmodel.AetheronViewModel
import com.example.ui.viewmodel.MonsterViewModel

@Composable
fun BreedingAndDexScreen(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel,
    monsterViewModel: MonsterViewModel? = null
) {
    LaunchedEffect(gameState.monsters, monsterViewModel) {
        if (monsterViewModel != null && gameState.monsters.isNotEmpty()) {
            monsterViewModel.seedStarterMonstersIfEmpty(gameState.monsters)
        }
    }

    var parentAId by remember(gameState.monsters) {
        mutableStateOf(gameState.monsters.getOrNull(0)?.instanceId ?: "")
    }
    var parentBId by remember(gameState.monsters) {
        mutableStateOf(gameState.monsters.getOrNull(1)?.instanceId ?: gameState.monsters.firstOrNull()?.instanceId ?: "")
    }
    var selectedDexContinent by remember { mutableIntStateOf(1) }

    val continentSpecies = remember(selectedDexContinent) {
        AetheronWorldCatalog.allSpecies.filter { it.continentId == selectedDexContinent }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 0. Monster Collection (Room + Firestore Sync & Filter UI)
        if (monsterViewModel != null) {
            item {
                MonsterCollectionScreen(
                    monsterViewModel = monsterViewModel,
                    onMonsterDeleted = { deletedId ->
                        viewModel.releaseMonsterFromCollection(deletedId)
                    }
                )
            }
        }

        // 1. Complex Genetics & Legendary Breeding Lab
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BadgeGold.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = "Zucht-Labor", tint = BadgeGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GENETIK- & LEGENDEN-ZUCHTLABOR",
                            style = MaterialTheme.typography.titleLarge,
                            color = BadgeGold
                        )
                    }
                    Text(
                        text = "Paare zwei Monster, um vererbbare Eigenschaften (Traits), höhere Gen-Ränge (bis S+) und trainierte Bonus-Stats weiterzugeben. Selbst gefangene Legendäre Monster können durch Zucht auf Generation 2+ verstärkt werden!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Elternteil A wählen:",
                        style = MaterialTheme.typography.labelLarge,
                        color = AetherCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(gameState.monsters) { mon ->
                            val sp = AetheronWorldCatalog.getSpecies(mon.speciesId)
                            FilterChip(
                                selected = mon.instanceId == parentAId,
                                onClick = { parentAId = mon.instanceId },
                                label = {
                                    Text("${if (sp.isLegendary) "⚡" else ""}${mon.nickname} (${mon.ivRank} • Gen ${mon.generation})")
                                },
                                modifier = Modifier.testTag("breed_parentA_${mon.instanceId}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Elternteil B wählen:",
                        style = MaterialTheme.typography.labelLarge,
                        color = AetherCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(gameState.monsters) { mon ->
                            val sp = AetheronWorldCatalog.getSpecies(mon.speciesId)
                            FilterChip(
                                selected = mon.instanceId == parentBId,
                                onClick = { parentBId = mon.instanceId },
                                label = {
                                    Text("${if (sp.isLegendary) "⚡" else ""}${mon.nickname} (${mon.ivRank} • Gen ${mon.generation})")
                                },
                                modifier = Modifier.testTag("breed_parentB_${mon.instanceId}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val parentA = gameState.monsters.find { it.instanceId == parentAId }
                    val parentB = gameState.monsters.find { it.instanceId == parentBId }
                    if (parentA != null && parentB != null) {
                        val spA = AetheronWorldCatalog.getSpecies(parentA.speciesId)
                        val spB = AetheronWorldCatalog.getSpecies(parentB.speciesId)
                        val traitsPreview = (parentA.inheritedTraits + parentB.inheritedTraits)
                            .distinctBy { it.id }
                            .joinToString(", ") { it.nameDe }

                        Surface(
                            color = ElevatedTacticalCard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Zucht-Prognose: ${parentA.nickname} × ${parentB.nickname}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vererbbare Gen-Merkmale: $traitsPreview",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ElementTerra
                                )
                                if (spA.isLegendary || spB.isLegendary) {
                                    Text(
                                        text = "⚡ LEGENDÄRE VERSTÄRKUNG AKTIV: Der Nachkomme erbt die Legendäre Spezies auf Generation ${maxOf(parentA.generation, parentB.generation) + 1} (+8% Grundwerte pro Generation)!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BadgeGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.breedMonsters(parentA.instanceId, parentB.instanceId) },
                            enabled = parentA.instanceId != parentB.instanceId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("breed_confirm_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BadgeGold,
                                contentColor = MidnightSlate
                            )
                        ) {
                            Text(
                                text = if (parentA.instanceId == parentB.instanceId) "Wähle 2 verschiedene Eltern" else "Nachkommen züchten & Gen-Linie verstärken",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. 240-Monster Continent Encyclopedia (80 per Continent)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = "Aether-Dex", tint = AetherCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AETHER-DEX (${gameState.capturedSpeciesIds.size}/240 SPEZIES)",
                                style = MaterialTheme.typography.titleLarge,
                                color = AetherCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1 to "Sylvaria (#1-80)",
                            2 to "Pyroclast (#81-160)",
                            3 to "Aetheris (#161-240)"
                        ).forEach { (cid, label) ->
                            FilterChip(
                                selected = selectedDexContinent == cid,
                                onClick = { selectedDexContinent = cid },
                                label = { Text(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dex_continent_$cid")
                            )
                        }
                    }
                }
            }
        }

        items(continentSpecies, key = { it.id }) { sp ->
            val captured = sp.id in gameState.capturedSpeciesIds
            Surface(
                color = if (sp.isLegendary) BadgeGold.copy(alpha = 0.12f) else DeepTacticalSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (sp.isLegendary) BadgeGold else if (captured) AetherCyan.copy(alpha = 0.5f) else TacticalBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonsterCanvasAvatar(
                        species = sp,
                        ivRank = if (sp.isLegendary) "S+" else "S",
                        size = 58.dp,
                        showBadge = sp.isLegendary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "#${sp.id} ${sp.nameDe}" + (if (sp.isLegendary) " [LEGENDÄR]" else ""),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (sp.isLegendary) BadgeGold else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (captured) "✓ Gefangen" else "Unentdeckt",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (captured) ElementTerra else TextSecondary
                            )
                        }
                        Text(
                            text = "${sp.primaryElement.iconCode} ${sp.primaryElement.labelDe} • Stufe ${sp.stage} • Gen-Merkmal: ${sp.signatureTrait.nameDe}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AetherCyan
                        )
                        Text(
                            text = "Wetter-Affinität: ${sp.preferredWeather?.labelDe ?: "Alle"} | Aktiv: ${sp.preferredTime?.labelDe ?: "Immer"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
