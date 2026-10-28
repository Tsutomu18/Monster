package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Shield
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
import com.example.domain.engine.TrainingExerciseType
import com.example.domain.model.*
import com.example.ui.components.MonsterCanvasAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronGameState
import com.example.ui.viewmodel.AetheronViewModel
import kotlinx.coroutines.delay

@Composable
fun DojoTrainingAndEvolutionScreen(
    gameState: AetheronGameState,
    viewModel: AetheronViewModel
) {
    val selectedMonster = gameState.monsters.find { it.instanceId == gameState.selectedMonsterId }
        ?: gameState.monsters.first()
    val species = AetheronWorldCatalog.getSpecies(selectedMonster.speciesId)

    var activeExercise by remember { mutableStateOf(TrainingExerciseType.POWER_FORGE) }

    // Interactive Mini-Game 1 State (Power Forge: Pendulum Timing)
    var forgeGauge by remember { mutableFloatStateOf(0.5f) }
    var forgeDirection by remember { mutableFloatStateOf(1f) }
    var forgeActive by remember { mutableStateOf(false) }
    var forgeHits by remember { mutableIntStateOf(0) }
    var forgeScoreAcc by remember { mutableIntStateOf(0) }

    LaunchedEffect(forgeActive) {
        while (forgeActive) {
            delay(28L)
            var next = forgeGauge + forgeDirection * 0.045f
            if (next >= 1f) {
                next = 1f
                forgeDirection = -1f
            } else if (next <= 0f) {
                next = 0f
                forgeDirection = 1f
            }
            forgeGauge = next
        }
    }

    // Interactive Mini-Game 2 State (Aegis Reflex: 4-Sector Shield Parry)
    var aegisTargetSector by remember { mutableIntStateOf(0) } // 0: Nord, 1: Ost, 2: Süd, 3: West
    var aegisBlocksDone by remember { mutableIntStateOf(0) }
    var aegisPerfectBlocks by remember { mutableIntStateOf(0) }

    // Interactive Mini-Game 3 State (Aether Sprint: Rune Sequence 1..6)
    var sprintNextRune by remember { mutableIntStateOf(1) }
    var sprintRuneOrder by remember { mutableStateOf((1..6).shuffled()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 1. Team Monster Selector
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
                        text = "AKTIVES MONSTER FÜR TRAINING & EVOLUTION WÄHLEN",
                        style = MaterialTheme.typography.labelLarge,
                        color = AetherCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(gameState.monsters) { mon ->
                            val sp = AetheronWorldCatalog.getSpecies(mon.speciesId)
                            val isSelected = mon.instanceId == selectedMonster.instanceId
                            Surface(
                                color = if (isSelected) AetherCyan.copy(alpha = 0.18f) else ElevatedTacticalCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(155.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AetherCyan else TacticalBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { viewModel.selectActiveMonster(mon.instanceId) }
                                    .testTag("select_monster_${mon.instanceId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MonsterCanvasAvatar(species = sp, ivRank = mon.ivRank, size = 50.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = mon.nickname,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Lv.${mon.level} • Gen ${mon.generation}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = BadgeGold
                                        )
                                        Text(
                                            text = "TP: +${mon.totalTrainedPoints()}",
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

        // 2. Detailed Stat Breakdown & Trained Effort Values
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MonsterCanvasAvatar(species = species, ivRank = selectedMonster.ivRank, size = 68.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${selectedMonster.nickname} (#${species.id})",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${species.primaryElement.iconCode} ${species.primaryElement.labelDe}" +
                                        (species.secondaryElement?.let { " / ${it.iconCode} ${it.labelDe}" } ?: "") +
                                        " • Kampfkraft ${selectedMonster.combatPower(species)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BadgeGold
                                )
                                Text(
                                    text = "Dominanter Trainings-Fokus: ${selectedMonster.dominantTrainedStat().labelDe}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Individual Stat Bars (Base + Trained TP)
                    StatTrainingBarRow(
                        label = "Angriff (ATK)",
                        totalValue = selectedMonster.effectiveAtk(species),
                        trainedBonus = selectedMonster.trainedAtk,
                        barColor = ElementPyro
                    )
                    StatTrainingBarRow(
                        label = "Verteidigung (DEF)",
                        totalValue = selectedMonster.effectiveDef(species),
                        trainedBonus = selectedMonster.trainedDef,
                        barColor = ElementFerro
                    )
                    StatTrainingBarRow(
                        label = "Geschwindigkeit (SPD)",
                        totalValue = selectedMonster.effectiveSpd(species),
                        trainedBonus = selectedMonster.trainedSpd,
                        barColor = ElementVolt
                    )
                    StatTrainingBarRow(
                        label = "Elementarkraft (ELM)",
                        totalValue = selectedMonster.effectiveElem(species),
                        trainedBonus = selectedMonster.trainedElem,
                        barColor = AetherCyan
                    )
                    StatTrainingBarRow(
                        label = "Vitalität (HP)",
                        totalValue = selectedMonster.maxHp(species),
                        trainedBonus = selectedMonster.trainedHp,
                        barColor = ElementTerra
                    )
                }
            }
        }

        // 3. Interactive Dojo Mini-Games & Exercises
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, AetherCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Dojo", tint = AetherCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INTERAKTIVES DOJO-TRAINING (MINI-SPIELE)",
                            style = MaterialTheme.typography.titleLarge,
                            color = AetherCyan
                        )
                    }
                    Text(
                        text = "Trainiere gezielt einzelne Attribute durch Geschicklichkeits-Übungen. Gute Leistungen bringen S-Rang-Multiplikatoren, Bonus-Items und steuern den Evolutionspfad!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exercise Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrainingExerciseType.entries.forEach { ex ->
                            val selected = activeExercise == ex
                            FilterChip(
                                selected = selected,
                                onClick = { activeExercise = ex },
                                label = {
                                    Text(
                                        text = "${ex.primaryStat.shortLabel}/${ex.secondaryStat.shortLabel}",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("exercise_tab_${ex.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = ElevatedTacticalCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = activeExercise.titleDe,
                                style = MaterialTheme.typography.titleMedium,
                                color = BadgeGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeExercise.instructionsDe,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            when (activeExercise) {
                                TrainingExerciseType.POWER_FORGE -> {
                                    // Mini-Game 1: Power Forge Timing Gauge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MidnightSlate)
                                            .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
                                    ) {
                                        // Golden Target Zone (75%..95%)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(0.20f)
                                                .align(Alignment.CenterEnd)
                                                .offset(x = (-12).dp)
                                                .background(BadgeGold.copy(alpha = 0.38f))
                                        )
                                        // Moving Indicator
                                        LinearProgressIndicator(
                                            progress = { forgeGauge },
                                            modifier = Modifier.fillMaxSize(),
                                            color = if (forgeGauge in 0.72f..0.96f) BadgeGold else AetherCyan,
                                            trackColor = Color.Transparent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!forgeActive) {
                                                    forgeActive = true
                                                    forgeHits = 0
                                                    forgeScoreAcc = 0
                                                } else {
                                                    val hitScore = when {
                                                        forgeGauge in 0.75f..0.95f -> 98
                                                        forgeGauge in 0.60f..0.99f -> 82
                                                        else -> 65
                                                    }
                                                    forgeScoreAcc += hitScore
                                                    forgeHits += 1
                                                    if (forgeHits >= 3) {
                                                        forgeActive = false
                                                        viewModel.completeTrainingMiniGame(
                                                            monsterId = selectedMonster.instanceId,
                                                            exercise = TrainingExerciseType.POWER_FORGE,
                                                            scorePercent = forgeScoreAcc / 3
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("minigame_forge_action_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (forgeActive) CrimsonAccent else AetherCyan,
                                                contentColor = if (forgeActive) Color.White else MidnightSlate
                                            )
                                        ) {
                                            Icon(Icons.Default.Bolt, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (!forgeActive) "Kraft-Übung starten" else "SCHLAG! (${forgeHits + 1}/3)",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                forgeActive = false
                                                viewModel.completeTrainingMiniGame(
                                                    monsterId = selectedMonster.instanceId,
                                                    exercise = TrainingExerciseType.POWER_FORGE,
                                                    scorePercent = 95
                                                )
                                            },
                                            modifier = Modifier.testTag("quick_s_rank_forge_button")
                                        ) {
                                            Text("Meister-Schlag (S-Rang)")
                                        }
                                    }
                                }

                                TrainingExerciseType.AEGIS_REFLEX -> {
                                    // Mini-Game 2: 4-Direction Shield Parry
                                    val directions = listOf("⬆️ Nord-Schild", "➡️ Ost-Schild", "⬇️ Süd-Schild", "⬅️ West-Schild")
                                    Text(
                                        text = "Anfliegende Salve auf: ${directions[aegisTargetSector]} (${aegisBlocksDone}/4 Paraden)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AetherCyan
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        directions.forEachIndexed { idx, label ->
                                            val isTarget = idx == aegisTargetSector
                                            Button(
                                                onClick = {
                                                    if (idx == aegisTargetSector) aegisPerfectBlocks += 1
                                                    aegisBlocksDone += 1
                                                    aegisTargetSector = (aegisTargetSector + 1 + idx) % 4
                                                    if (aegisBlocksDone >= 4) {
                                                        val score = 60 + aegisPerfectBlocks * 10
                                                        aegisBlocksDone = 0
                                                        aegisPerfectBlocks = 0
                                                        viewModel.completeTrainingMiniGame(
                                                            monsterId = selectedMonster.instanceId,
                                                            exercise = TrainingExerciseType.AEGIS_REFLEX,
                                                            scorePercent = score
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("aegis_sector_$idx"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isTarget) BadgeGold else DeepTacticalSurface,
                                                    contentColor = if (isTarget) MidnightSlate else TextPrimary
                                                )
                                            ) {
                                                Icon(Icons.Default.Shield, contentDescription = label, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                TrainingExerciseType.AETHER_SPRINT -> {
                                    // Mini-Game 3: Rune Sequence 1..6
                                    Text(
                                        text = "Tippe auf Rune #$sprintNextRune von 6",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ElementVolt
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        sprintRuneOrder.forEach { runeNum ->
                                            val done = runeNum < sprintNextRune
                                            OutlinedButton(
                                                onClick = {
                                                    if (runeNum == sprintNextRune) {
                                                        if (sprintNextRune >= 6) {
                                                            sprintNextRune = 1
                                                            sprintRuneOrder = (1..6).shuffled()
                                                            viewModel.completeTrainingMiniGame(
                                                                monsterId = selectedMonster.instanceId,
                                                                exercise = TrainingExerciseType.AETHER_SPRINT,
                                                                scorePercent = 96
                                                            )
                                                        } else {
                                                            sprintNextRune += 1
                                                        }
                                                    } else {
                                                        // Complete with B/A rank if tapped out of order
                                                        sprintNextRune = 1
                                                        viewModel.completeTrainingMiniGame(
                                                            monsterId = selectedMonster.instanceId,
                                                            exercise = TrainingExerciseType.AETHER_SPRINT,
                                                            scorePercent = 78
                                                        )
                                                    }
                                                },
                                                enabled = !done,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("sprint_rune_$runeNum")
                                            ) {
                                                Text("$runeNum", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Last Training Result Banner
                            gameState.lastTrainingResult?.let { res ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MidnightSlate,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Letztes Ergebnis: ${res.gradeRank}-Rang (${res.scorePercent}%) → +${res.primaryStatGained} ${res.exercise.primaryStat.shortLabel}, +${res.secondaryStatGained} ${res.exercise.secondaryStat.shortLabel}, +${res.xpGained} EP" +
                                                (res.bonusItemNameDe?.let { " | Bonus: +1x $it!" } ?: ""),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = ElementTerra
                                        )
                                        if (res.unlockedEvolutionPreview != null) {
                                            Text(
                                                text = res.unlockedEvolutionPreview,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BadgeGold
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

        // 4. Evolution & Rare Artifact Awakening Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTacticalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Evolution", tint = BadgeGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EVOLUTIONSPFADE & ARTEFAKT-ERWECKUNG",
                            style = MaterialTheme.typography.titleLarge,
                            color = BadgeGold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (species.evolutions.isEmpty()) {
                        Text(
                            text = "${selectedMonster.nickname} hat bereits die höchste Entwicklungsstufe (Stufe ${species.stage}) erreicht! Du kannst es im Zucht-Labor weiter verstärken.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        species.evolutions.forEach { branch ->
                            val targetSp = AetheronWorldCatalog.getSpecies(branch.targetSpeciesId)
                            val canEvolve = if (branch.requiredArtifactId != null) {
                                (gameState.inventory[branch.requiredArtifactId] ?: 0) > 0
                            } else {
                                val statVal = when (branch.requiredDominantStat) {
                                    StatType.ATK -> selectedMonster.trainedAtk
                                    StatType.DEF -> selectedMonster.trainedDef
                                    StatType.SPD -> selectedMonster.trainedSpd
                                    StatType.ELEM -> selectedMonster.trainedElem
                                    StatType.HP -> selectedMonster.trainedHp
                                    null -> selectedMonster.totalTrainedPoints()
                                }
                                selectedMonster.level >= branch.minLevel && statVal >= branch.minTrainedStatValue
                            }

                            Surface(
                                color = ElevatedTacticalCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MonsterCanvasAvatar(species = targetSp, ivRank = selectedMonster.ivRank, size = 56.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "→ ${branch.targetSpeciesName} (#${branch.targetSpeciesId})",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = branch.conditionDescriptionDe,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (canEvolve) ElementTerra else TextSecondary
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.evolveMonster(selectedMonster.instanceId, branch) },
                                        enabled = canEvolve,
                                        modifier = Modifier.testTag("evolve_btn_${branch.targetSpeciesId}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BadgeGold,
                                            contentColor = MidnightSlate
                                        )
                                    ) {
                                        Text("Entwickeln", fontWeight = FontWeight.Bold)
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
private fun StatTrainingBarRow(
    label: String,
    totalValue: Int,
    trainedBonus: Int,
    barColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                text = "$totalValue (Dojo-Bonus: +$trainedBonus)",
                style = MaterialTheme.typography.labelLarge,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (totalValue / 320f).coerceIn(0.08f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MidnightSlate
        )
    }
}
