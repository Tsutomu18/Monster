package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.Monster
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.ui.components.MonsterCanvasAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MonsterSortOrder
import com.example.ui.viewmodel.MonsterViewModel

/**
 * Composable interface displaying the player's captured Monster collection backed by
 * [MonsterViewModel] (Room local database + Firebase Firestore synchronization),
 * with interactive search, element & IV-rank filtering, sorting, and cloud sync controls.
 */
@Composable
fun MonsterCollectionScreen(
    monsterViewModel: MonsterViewModel,
    modifier: Modifier = Modifier,
    onMonsterDeleted: ((String) -> Unit)? = null
) {
    val monsters by monsterViewModel.filteredMonsters.collectAsStateWithLifecycle()
    val allMonsters by monsterViewModel.allMonsters.collectAsStateWithLifecycle()
    val filterState by monsterViewModel.filterState.collectAsStateWithLifecycle()
    val syncStatus by monsterViewModel.syncStatus.collectAsStateWithLifecycle()

    val elementOptions = listOf("TERRA", "PYRO", "AQUA", "VOLT", "CRYO", "AETHER", "UMBRA", "LUX")
    val ivRankOptions = listOf("S+", "S", "A", "B", "C")

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, AetherCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header & Room <-> Firestore Sync Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (syncStatus.isCloudAvailable) Icons.Default.CloudSync else Icons.Default.Storage,
                        contentDescription = "Datenbank-Status",
                        tint = if (syncStatus.isCloudAvailable) AetherCyan else BadgeGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MONSTER-SAMMLUNG (${monsters.size}/${allMonsters.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = syncStatus.statusMessageDe,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (syncStatus.isCloudAvailable) ElementTerra else BadgeGold
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { monsterViewModel.refreshAndSyncMonsters() },
                    enabled = !syncStatus.isSyncing,
                    modifier = Modifier.testTag("monster_collection_sync_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ElevatedTacticalCard,
                        contentColor = AetherCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Synchronisieren",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (syncStatus.isSyncing) "Sync..." else "Room ⇄ Cloud")
                }
            }

            // 2. Search Input
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { monsterViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monster_search_input"),
                singleLine = true,
                placeholder = { Text("Monster nach Name, Element oder Gen-Trait filtern...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suche",
                        tint = AetherCyan
                    )
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { monsterViewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Suche löschen"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Element Filter Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Element Filter",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Element:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = filterState.selectedElement == null,
                            onClick = { monsterViewModel.selectElementFilter(null) },
                            label = { Text("Alle") },
                            modifier = Modifier.testTag("filter_element_all")
                        )
                    }
                    items(elementOptions) { element ->
                        FilterChip(
                            selected = filterState.selectedElement == element,
                            onClick = { monsterViewModel.selectElementFilter(element) },
                            label = { Text(element) },
                            modifier = Modifier.testTag("filter_element_$element")
                        )
                    }
                }
            }

            // 4. IV-Rank & Sort Order Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.selectedIvRank == null,
                            onClick = { monsterViewModel.selectIvRankFilter(null) },
                            label = { Text("Alle Ränge") },
                            modifier = Modifier.testTag("filter_iv_all")
                        )
                    }
                    items(ivRankOptions) { rank ->
                        FilterChip(
                            selected = filterState.selectedIvRank == rank,
                            onClick = { monsterViewModel.selectIvRankFilter(rank) },
                            label = { Text("Gen $rank") },
                            modifier = Modifier.testTag("filter_iv_$rank")
                        )
                    }
                    items(MonsterSortOrder.entries) { order ->
                        AssistChip(
                            onClick = { monsterViewModel.selectSortOrder(order) },
                            label = {
                                Text(
                                    text = order.labelDe,
                                    color = if (filterState.sortOrder == order) AetherCyan else TextSecondary
                                )
                            },
                            modifier = Modifier.testTag("sort_order_${order.name}")
                        )
                    }
                }
            }

            // 5. Filtered Monster Cards
            if (monsters.isEmpty()) {
                Surface(
                    color = ElevatedTacticalCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Keine Monster entsprechen den aktuellen Filterkriterien.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { monsterViewModel.clearFilters() },
                            modifier = Modifier.testTag("reset_monster_filters_button")
                        ) {
                            Text("Filter zurücksetzen", color = AetherCyan)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monsters.forEach { monster ->
                        MonsterCollectionItemCard(
                            monster = monster,
                            canDelete = allMonsters.size > 1,
                            onQuickBoost = {
                                val boosted = monster.copy(
                                    currentLevel = (monster.currentLevel + 1L).coerceAtMost(100L),
                                    attack = monster.attack + 4L,
                                    defense = monster.defense + 3L,
                                    speed = monster.speed + 4L,
                                    evolutionProgressPercent = (monster.evolutionProgressPercent + 15L).coerceAtMost(100L)
                                )
                                monsterViewModel.saveMonster(boosted)
                            },
                            onDelete = {
                                monsterViewModel.deleteMonster(monster.id)
                                onMonsterDeleted?.invoke(monster.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonsterCollectionItemCard(
    monster: Monster,
    canDelete: Boolean,
    onQuickBoost: () -> Unit,
    onDelete: () -> Unit
) {
    val species = AetheronWorldCatalog.getSpecies(monster.speciesId.toInt().coerceIn(1, 240))
    Surface(
        color = ElevatedTacticalCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
            .testTag("collection_monster_card_${monster.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonsterCanvasAvatar(
                    species = species,
                    ivRank = monster.geneticIvRank,
                    size = 54.dp,
                    showBadge = species.isLegendary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${monster.name} • Lv.${monster.currentLevel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = BadgeGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Gen ${monster.geneticIvRank} • G${monster.generation}",
                                style = MaterialTheme.typography.labelMedium,
                                color = BadgeGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Element: ${monster.primaryElement} • Trait: ${monster.geneticTraitName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherCyan
                    )

                    Text(
                        text = "ATK ${monster.attack} (+${monster.trainedAttack}) • DEF ${monster.defense} (+${monster.trainedDefense}) • SPD ${monster.speed} (+${monster.trainedSpeed}) • HP ${monster.maxHp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Evolution progress bar & Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Evolutions-Fortschritt (Stufe ${monster.evolutionStage}): ${monster.evolutionProgressPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElementTerra
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (monster.evolutionProgressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElementTerra,
                        trackColor = Color.Black.copy(alpha = 0.35f)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onQuickBoost,
                    modifier = Modifier.testTag("boost_monster_${monster.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Monster Level & Stats trainieren",
                        tint = ElementTerra
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_monster_${monster.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Monster entlassen",
                            tint = ElementPyro
                        )
                    }
                }
            }
        }
    }
}
