package com.example.ui.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.ArenaRoster
import com.example.data.remote.PlayerCloudProfile
import com.example.data.remote.TradeOffer
import com.example.data.remote.UiState
import com.example.data.repository.AetheronRepository
import com.example.data.repository.AetheronSyncRepository
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.engine.DynamicQuestAndTrainingEngine
import com.example.domain.engine.TrainingExerciseType
import com.example.domain.engine.TrainingSessionResult
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

data class BattleState(
    val isGymBattle: Boolean = false,
    val isArenaPvp: Boolean = false,
    val opponentTitleDe: String,
    val gymBadgeToWin: GymLeaderBadge? = null,
    val playerMonsterId: String,
    val enemyMonster: MonsterInstance,
    val enemyMaxHp: Int,
    val turnNumber: Int = 1,
    val battleLog: List<String> = emptyList(),
    val lastEffectText: String = "",
    val lastEffectColorHex: Long = 0xFF00E5FF,
    val isFinished: Boolean = false,
    val playerWon: Boolean = false,
    val capturedMonster: Boolean = false
)

data class AetheronGameState(
    val userId: String = "",
    val trainerName: String = "Aether-Sucher",
    val playerLevel: Int = 5,
    val playerXp: Int = 0,
    val skillPoints: Int = 2,
    val gold: Int = 1250,
    val currentContinentId: Int = 1,
    val currentBiomeId: String = "syl_emerald_canopy",
    val playerX: Int = 4,
    val playerY: Int = 4,
    val stepsTakenInBiome: Int = 0,
    val currentWeather: WeatherType = WeatherType.SOLAR_FLARE,
    val currentTimeOfDay: TimeOfDay = TimeOfDay.DAY,
    val earnedBadges: Set<Int> = emptySet(),
    val capturedSpeciesIds: Set<Int> = setOf(1, 4, 81),
    val monsters: List<MonsterInstance> = emptyList(),
    val selectedMonsterId: String = "",
    val inventory: Map<String, Int> = emptyMap(),
    val playerSkills: List<PlayerSkillNode> = AetheronWorldCatalog.defaultPlayerSkills,
    val factionStandings: Map<String, Int> = mapOf(
        "sylvaria_order" to 160,
        "pyroclast_syndicate" to 120,
        "aetheris_chroniclers" to 120
    ),
    val completedQuestsCount: Int = 0,
    val activeQuests: List<DynamicQuest> = emptyList(),
    val activeBattle: BattleState? = null,
    val lastTrainingResult: TrainingSessionResult? = null,
    val statusBannerDe: String = "Willkommen in Aetheron! Erkunde die 3 Kontinente, trainiere im Dojo und erfülle dynamische Quests.",
    val isCloudSynced: Boolean = false,
    val arenaWins: Int = 0
)

class AetheronViewModel(
    private val repository: AetheronRepository,
    private val currentUserId: String,
    initialTrainerName: String = "Aether-Trainer",
    val isCloudAuthenticated: Boolean = true,
    private val localPrefs: SharedPreferences? = null,
    private val syncRepository: AetheronSyncRepository? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(createInitialGameState(currentUserId, initialTrainerName))
    val gameState: StateFlow<AetheronGameState> = _gameState.asStateFlow()

    val openTradesState: StateFlow<UiState<List<TradeOffer>>> =
        if (isCloudAuthenticated) {
            repository.observeOpenTrades()
                .map<List<TradeOffer>, UiState<List<TradeOffer>>> { UiState.Success(it) }
                .catch { e ->
                    Log.w(TAG, "Error observing trades", e)
                    emit(UiState.Error(e.localizedMessage ?: "Handelsnetzwerk nicht erreichbar"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = UiState.Loading
                )
        } else {
            MutableStateFlow<UiState<List<TradeOffer>>>(UiState.Success(emptyList())).asStateFlow()
        }

    val arenaRostersState: StateFlow<UiState<List<ArenaRoster>>> =
        if (isCloudAuthenticated) {
            repository.observeActiveArenaRosters()
                .map<List<ArenaRoster>, UiState<List<ArenaRoster>>> { UiState.Success(it) }
                .catch { e ->
                    Log.w(TAG, "Error observing arena rosters", e)
                    emit(UiState.Error(e.localizedMessage ?: "Arena-Rangliste nicht erreichbar"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = UiState.Loading
                )
        } else {
            MutableStateFlow<UiState<List<ArenaRoster>>>(UiState.Success(emptyList())).asStateFlow()
        }

    val cloudMonstersState: StateFlow<UiState<List<com.example.data.remote.Monster>>> =
        when {
            syncRepository != null -> {
                syncRepository.observeMonsters(currentUserId)
                    .map<List<com.example.data.remote.Monster>, UiState<List<com.example.data.remote.Monster>>> {
                        UiState.Success(it)
                    }
                    .catch { e ->
                        Log.w(TAG, "Error observing synced monsters", e)
                        emit(UiState.Error(e.localizedMessage ?: "Monster-Datenbank Fehler"))
                    }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000L),
                        initialValue = UiState.Loading
                    )
            }
            isCloudAuthenticated -> {
                repository.observeUserMonsters(currentUserId)
                    .map<List<com.example.data.remote.Monster>, UiState<List<com.example.data.remote.Monster>>> {
                        UiState.Success(it)
                    }
                    .catch { e ->
                        Log.w(TAG, "Error observing user monsters", e)
                        emit(UiState.Error(e.localizedMessage ?: "Monster-Datenbank nicht erreichbar"))
                    }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000L),
                        initialValue = UiState.Loading
                    )
            }
            else -> {
                MutableStateFlow<UiState<List<com.example.data.remote.Monster>>>(UiState.Success(emptyList())).asStateFlow()
            }
        }

    init {
        loadCloudSaveOrInitialize()
    }

    fun syncSingleMonsterToFirestore(instance: MonsterInstance, isNew: Boolean = false) {
        viewModelScope.launch {
            val entity = com.example.data.remote.Monster.fromMonsterInstance(currentUserId, instance)
            if (syncRepository != null) {
                syncRepository.saveMonster(entity)
                return@launch
            }
            if (!isCloudAuthenticated) {
                saveLocalProfile(silent = true)
                return@launch
            }
            if (isNew) {
                repository.createMonster(entity)
            } else {
                val existing = repository.getMonsterById(entity.id).getOrNull()
                if (existing == null) {
                    repository.createMonster(entity)
                } else {
                    repository.updateMonster(entity)
                }
            }
        }
    }

    fun releaseMonsterFromCollection(monsterId: String) {
        val state = _gameState.value
        if (state.monsters.size <= 1) return
        val remaining = state.monsters.filterNot { it.instanceId == monsterId }
        _gameState.value = state.copy(
            monsters = remaining,
            selectedMonsterId = if (state.selectedMonsterId == monsterId) remaining.first().instanceId else state.selectedMonsterId,
            statusBannerDe = "Monster in die Wildnis entlassen."
        )
        viewModelScope.launch {
            if (syncRepository != null) {
                syncRepository.deleteMonster(monsterId)
                syncToCloud(silent = true)
                return@launch
            }
            if (!isCloudAuthenticated) {
                saveLocalProfile(silent = true)
                return@launch
            }
            repository.deleteMonster(monsterId)
        }
    }

    private fun loadCloudSaveOrInitialize() {
        viewModelScope.launch {
            if (syncRepository != null) {
                if (isCloudAuthenticated) {
                    syncRepository.synchronizeWithFirestore()
                }
                val profile = syncRepository.getPlayerProfile(currentUserId).getOrNull()
                if (profile != null && profile.saveStateJson.length > 10) {
                    restoreFromCloudProfile(profile)
                    if (!isCloudAuthenticated) {
                        _gameState.update {
                            it.copy(
                                statusBannerDe = "💾 Spielstand aus lokaler Room-Datenbank geladen (${it.monsters.size} Monster, ${it.earnedBadges.size}/24 Orden)."
                            )
                        }
                    }
                } else {
                    syncToCloud(silent = true)
                }
                return@launch
            }

            if (!isCloudAuthenticated) {
                loadLocalSaveIfPresent()
                return@launch
            }
            val result = repository.getPlayerProfile(currentUserId)
            val cloudProfile = result.getOrNull()
            if (cloudProfile != null && cloudProfile.saveStateJson.length > 10) {
                restoreFromCloudProfile(cloudProfile)
            } else {
                // Save initial starter profile to Cloud
                syncToCloud(silent = true)
            }
        }
    }

    private fun loadLocalSaveIfPresent() {
        val prefs = localPrefs ?: return
        val savedJson = prefs.getString("local_save_json", null) ?: return
        if (savedJson.length <= 10) return
        val localProfile = PlayerCloudProfile(
            userId = currentUserId,
            trainerName = prefs.getString("local_trainer_name", "Lokaler Trainer") ?: "Lokaler Trainer",
            level = prefs.getLong("local_level", 14L),
            gold = prefs.getLong("local_gold", 1450L),
            continentId = prefs.getLong("local_continent", 1L),
            questsCompleted = prefs.getLong("local_quests", 2L),
            factionSylvariaRep = prefs.getLong("local_rep_syl", 160L),
            factionPyroclastRep = prefs.getLong("local_rep_pyr", 120L),
            factionAetherisRep = prefs.getLong("local_rep_aet", 120L),
            saveStateJson = savedJson
        )
        restoreFromCloudProfile(localProfile)
        _gameState.update {
            it.copy(
                statusBannerDe = "💾 Lokaler Spielstand geladen (${it.monsters.size} Monster, ${it.earnedBadges.size}/24 Orden)."
            )
        }
    }

    private fun saveLocalProfile(silent: Boolean) {
        val state = _gameState.value
        val json = serializeGameState(state)
        localPrefs?.edit()?.apply {
            putString("local_save_json", json)
            putString("local_trainer_name", state.trainerName)
            putLong("local_level", state.playerLevel.toLong())
            putLong("local_gold", state.gold.toLong())
            putLong("local_continent", state.currentContinentId.toLong())
            putLong("local_quests", state.completedQuestsCount.toLong())
            putLong("local_rep_syl", (state.factionStandings["sylvaria_order"] ?: 160).toLong())
            putLong("local_rep_pyr", (state.factionStandings["pyroclast_syndicate"] ?: 120).toLong())
            putLong("local_rep_aet", (state.factionStandings["aetheris_chroniclers"] ?: 120).toLong())
            apply()
        }
        _gameState.update {
            it.copy(
                isCloudSynced = true,
                statusBannerDe = if (silent) it.statusBannerDe else "💾 Spielstand lokal auf dem Gerät gespeichert!"
            )
        }
    }

    fun selectContinentAndBiome(continentId: Int, biomeId: String) {
        val biome = AetheronWorldCatalog.biomes.find { it.id == biomeId }
            ?: AetheronWorldCatalog.biomes.first { it.continentId == continentId }
        _gameState.update { state ->
            val newQuests = if (state.activeQuests.none { it.biomeId == biome.id && !it.isCompleted }) {
                DynamicQuestAndTrainingEngine.generateDynamicQuests(
                    continentId = continentId,
                    biome = biome,
                    playerLevel = state.playerLevel,
                    completedQuestsCount = state.completedQuestsCount,
                    factionStandings = state.factionStandings,
                    currentWeather = state.currentWeather,
                    currentTime = state.currentTimeOfDay
                )
            } else {
                state.activeQuests
            }
            state.copy(
                currentContinentId = continentId,
                currentBiomeId = biome.id,
                stepsTakenInBiome = 0,
                activeQuests = newQuests,
                statusBannerDe = "Gereist nach ${AetheronWorldCatalog.getContinentName(continentId)} — ${biome.nameDe}."
            )
        }
    }

    fun cycleWeatherAndTime(nextWeather: WeatherType? = null, nextTime: TimeOfDay? = null) {
        _gameState.update { state ->
            val w = nextWeather ?: WeatherType.entries[(state.currentWeather.ordinal + 1) % WeatherType.entries.size]
            val t = nextTime ?: TimeOfDay.entries[(state.currentTimeOfDay.ordinal + 1) % TimeOfDay.entries.size]
            state.copy(
                currentWeather = w,
                currentTimeOfDay = t,
                statusBannerDe = "Wetterwechsel: ${w.labelDe} | Tageszeit: ${t.labelDe}. (${t.npcRoutineDe})"
            )
        }
    }

    fun movePlayerOnMap(dx: Int, dy: Int) {
        val state = _gameState.value
        val newX = (state.playerX + dx).coerceIn(0, 7)
        val newY = (state.playerY + dy).coerceIn(0, 7)
        val newSteps = state.stepsTakenInBiome + 1
        val biome = AetheronWorldCatalog.biomes.find { it.id == state.currentBiomeId }
            ?: AetheronWorldCatalog.biomes.first()

        // Update exploration quests
        val updatedQuests = state.activeQuests.map { q ->
            if (q.category == QuestCategory.BIOME_EXPLORE && q.biomeId == biome.id && !q.isCompleted) {
                val p = (q.currentProgress + 1).coerceAtMost(q.requiredCount)
                q.copy(currentProgress = p, isCompleted = p >= q.requiredCount)
            } else q
        }

        // Check for resource discovery on map
        val alchemistRank = state.playerSkills.find { it.id == "skill_alchemist_guild" }?.currentRank ?: 0
        val discoveryRoll = Random.nextInt(100)
        var newInventory = state.inventory
        var finalQuests = updatedQuests
        var banner = "Position ($newX, $newY) in ${biome.nameDe} erkundet."

        if (discoveryRoll < (28 + alchemistRank * 6)) {
            val dropId = biome.resourceDrops.random()
            val dropItem = AetheronWorldCatalog.items.find { it.id == dropId }
            val amount = if (alchemistRank >= 2) 2 else 1
            newInventory = newInventory + (dropId to ((newInventory[dropId] ?: 0) + amount))
            finalQuests = finalQuests.map { q ->
                if (q.category == QuestCategory.RESOURCE_GATHER && q.targetItemId == dropId && !q.isCompleted) {
                    val p = (q.currentProgress + amount).coerceAtMost(q.requiredCount)
                    q.copy(currentProgress = p, isCompleted = p >= q.requiredCount)
                } else q
            }
            banner = "Entdeckung! +$amount ${dropItem?.nameDe ?: dropId} im Gelände gefunden!"
        }

        _gameState.value = state.copy(
            playerX = newX,
            playerY = newY,
            stepsTakenInBiome = newSteps,
            inventory = newInventory,
            activeQuests = finalQuests,
            statusBannerDe = banner
        )

        // Chance for wild monster encounter on grass/crystal tiles
        if (Random.nextInt(100) < 35) {
            triggerWildEncounter()
        }
    }

    fun triggerWildEncounter() {
        val state = _gameState.value
        val biome = AetheronWorldCatalog.biomes.find { it.id == state.currentBiomeId }
            ?: AetheronWorldCatalog.biomes.first()
        val candidates = AetheronWorldCatalog.allSpecies.filter {
            it.continentId == state.currentContinentId && (it.biomeId == biome.id || !it.isLegendary)
        }

        // Weather & Time of Day dynamically influence spawn weights
        val weightedPool = mutableListOf<MonsterSpecies>()
        for (sp in candidates) {
            var weight = if (sp.biomeId == biome.id) 6 else 1
            if (sp.primaryElement == state.currentWeather.spawnBonusElement) weight += 5
            if (sp.preferredWeather == state.currentWeather) weight += 4
            if (sp.preferredTime == state.currentTimeOfDay) weight += 4
            if (sp.isLegendary) {
                weight = if (state.currentWeather == WeatherType.AETHER_MIST || biome.id.contains("sanctum") || biome.id.contains("throne") || biome.id.contains("nexus")) 3 else 0
            }
            repeat(weight.coerceAtMost(15)) { weightedPool.add(sp) }
        }

        val chosenSpecies = weightedPool.randomOrNull() ?: candidates.first()
        val enemyLevel = (biome.recommendedLevel + Random.nextInt(-2, 4)).coerceIn(3, 95)
        val enemyIv = listOf("C", "B", "B", "A", "S").random()
        val enemyInstance = MonsterInstance(
            instanceId = "wild_${UUID.randomUUID().toString().take(8)}",
            speciesId = chosenSpecies.id,
            nickname = chosenSpecies.nameDe,
            level = enemyLevel,
            ivRank = enemyIv,
            inheritedTraits = listOf(chosenSpecies.signatureTrait)
        )
        val enemyMaxHp = enemyInstance.maxHp(chosenSpecies)
        val activePlayerMonster = state.monsters.find { it.instanceId == state.selectedMonsterId }
            ?: state.monsters.first()

        _gameState.update {
            it.copy(
                activeBattle = BattleState(
                    isGymBattle = false,
                    isArenaPvp = false,
                    opponentTitleDe = if (chosenSpecies.isLegendary) "⚡ LEGENDÄRES MONSTER: ${chosenSpecies.nameDe}" else "Wildes ${chosenSpecies.nameDe} (${state.currentWeather.labelDe})",
                    playerMonsterId = activePlayerMonster.instanceId,
                    enemyMonster = enemyInstance.copy(currentHp = enemyMaxHp, currentStamina = 100),
                    enemyMaxHp = enemyMaxHp,
                    battleLog = listOf(
                        "Ein wildes ${chosenSpecies.nameDe} (Lv.$enemyLevel, Gen-Rang $enemyIv) erscheint bei ${state.currentWeather.labelDe}!",
                        "Aktives Wetter-Feld: ${state.currentWeather.descriptionDe}"
                    )
                )
            )
        }
    }

    fun startGymLeaderBattle(badge: GymLeaderBadge) {
        val state = _gameState.value
        val aceSpecies = AetheronWorldCatalog.getSpecies(badge.aceSpeciesId)
        val enemyInstance = MonsterInstance(
            instanceId = "gym_${badge.badgeIndex}",
            speciesId = aceSpecies.id,
            nickname = "${badge.leaderNameDe}'s ${aceSpecies.nameDe}",
            level = badge.recommendedLevel,
            ivRank = "S",
            inheritedTraits = listOf(aceSpecies.signatureTrait),
            trainedAtk = 35,
            trainedDef = 35,
            trainedSpd = 30
        )
        val maxHp = enemyInstance.maxHp(aceSpecies)
        val activePlayerMonster = state.monsters.find { it.instanceId == state.selectedMonsterId }
            ?: state.monsters.first()

        _gameState.update {
            it.copy(
                activeBattle = BattleState(
                    isGymBattle = true,
                    isArenaPvp = false,
                    opponentTitleDe = "Orden #${badge.badgeIndex}: ${badge.leaderNameDe} (${badge.badgeNameDe})",
                    gymBadgeToWin = badge,
                    playerMonsterId = activePlayerMonster.instanceId,
                    enemyMonster = enemyInstance.copy(currentHp = maxHp, currentStamina = 100),
                    enemyMaxHp = maxHp,
                    battleLog = listOf(
                        "${badge.leaderNameDe}: \"${badge.mottoDe}\"",
                        "${badge.leaderNameDe} schickt ${aceSpecies.nameDe} (Lv.${badge.recommendedLevel}) in den Kampf!"
                    )
                )
            )
        }
    }

    fun startOnlineArenaDuel(challenger: ArenaRoster) {
        val state = _gameState.value
        val species = AetheronWorldCatalog.getSpecies(challenger.leadSpeciesId.toInt().coerceIn(1, 240))
        val approxLevel = (challenger.combatPower.toInt() / 35).coerceIn(15, 95)
        val enemyInstance = MonsterInstance(
            instanceId = "arena_${challenger.id}",
            speciesId = species.id,
            nickname = "${challenger.trainerName}'s ${species.nameDe}",
            level = approxLevel,
            ivRank = "S+",
            inheritedTraits = listOf(species.signatureTrait),
            trainedAtk = 50,
            trainedDef = 45,
            trainedSpd = 50
        )
        val maxHp = enemyInstance.maxHp(species)
        val activePlayerMonster = state.monsters.find { it.instanceId == state.selectedMonsterId }
            ?: state.monsters.first()

        _gameState.update {
            it.copy(
                activeBattle = BattleState(
                    isGymBattle = false,
                    isArenaPvp = true,
                    opponentTitleDe = "Online-Arena-Duell gegen ${challenger.trainerName}",
                    playerMonsterId = activePlayerMonster.instanceId,
                    enemyMonster = enemyInstance.copy(currentHp = maxHp, currentStamina = 100),
                    enemyMaxHp = maxHp,
                    battleLog = listOf(
                        "Online-Arena-Match gegen Trainer ${challenger.trainerName} (${challenger.badgesCount} Orden) gestartet!",
                        "Team-Aufstellung: ${challenger.teamSummary}"
                    )
                )
            )
        }
    }

    fun executeCombatTurn(skill: CombatSkill) {
        val state = _gameState.value
        val battle = state.activeBattle ?: return
        if (battle.isFinished) return

        val playerMon = state.monsters.find { it.instanceId == battle.playerMonsterId } ?: state.monsters.first()
        val playerSpecies = AetheronWorldCatalog.getSpecies(playerMon.speciesId)
        val enemyMon = battle.enemyMonster
        val enemySpecies = AetheronWorldWorldSpecies(enemyMon.speciesId)

        val logs = battle.battleLog.toMutableList()
        val commanderRank = state.playerSkills.find { it.id == "skill_tactical_commander" }?.currentRank ?: 1
        val cmdBonus = 1.0f + commanderRank * 0.06f

        // Check stamina
        if (playerMon.currentStamina < skill.staminaCost) {
            val restedStamina = (playerMon.currentStamina + 45).coerceAtMost(100)
            val updatedPlayerMon = playerMon.copy(currentStamina = restedStamina)
            logs.add("${playerMon.nickname} sammelt Aether-Atem (+45 Ausdauer)!")
            processEnemyCounterAttack(state, battle, updatedPlayerMon, enemyMon, enemySpecies, logs)
            return
        }

        // Player damage formula with Weather, Element Effectiveness, Trained Stats & Genetic Traits
        val typeMult = skill.element.effectivenessAgainst(enemySpecies.primaryElement)
        val weatherMult = when (skill.element) {
            state.currentWeather.boostedElement -> 1.25f
            state.currentWeather.weakenedElement -> 0.80f
            else -> 1.0f
        }
        val atkStat = (playerMon.effectiveAtk(playerSpecies) + playerMon.effectiveElem(playerSpecies)) / 2f
        val defStat = enemyMon.effectiveDef(enemySpecies).coerceAtLeast(10).toFloat()
        val rawDmg = ((skill.power * (atkStat / defStat) * 0.72f + 12f) * typeMult * weatherMult * cmdBonus).roundToInt().coerceAtLeast(8)

        val newEnemyHp = (enemyMon.currentHp - rawDmg).coerceAtLeast(0)
        val lifesteal = playerMon.inheritedTraits.maxOfOrNull { it.lifestealRatio } ?: 0f
        val healedHp = if (lifesteal > 0f) (rawDmg * lifesteal).roundToInt() else 0
        val newPlayerHp = (playerMon.currentHp + healedHp).coerceAtMost(playerMon.maxHp(playerSpecies))
        val newPlayerStamina = (playerMon.currentStamina - skill.staminaCost + 12).coerceIn(0, 100)

        val effNote = when {
            typeMult > 1.2f -> " (SEHR EFFEKTIV!)"
            typeMult < 0.85f -> " (Wenig effektiv)"
            else -> ""
        }
        logs.add("${playerMon.nickname} nutzt ${skill.nameDe} → $rawDmg Schaden!$effNote")
        if (healedHp > 0) {
            logs.add("Gen-Merkmal Vampir-Klaue heilt +$healedHp KP!")
        }

        val updatedPlayerMon = playerMon.copy(currentHp = newPlayerHp, currentStamina = newPlayerStamina)

        if (newEnemyHp <= 0) {
            handleBattleVictory(state, battle, updatedPlayerMon, enemyMon.copy(currentHp = 0), enemySpecies, logs)
        } else {
            processEnemyCounterAttack(
                state,
                battle,
                updatedPlayerMon,
                enemyMon.copy(currentHp = newEnemyHp),
                enemySpecies,
                logs
            )
        }
    }

    private fun processEnemyCounterAttack(
        state: AetheronGameState,
        battle: BattleState,
        playerMon: MonsterInstance,
        enemyMon: MonsterInstance,
        enemySpecies: MonsterSpecies,
        logs: MutableList<String>
    ) {
        val playerSpecies = AetheronWorldCatalog.getSpecies(playerMon.speciesId)
        val enemySkill = enemySpecies.skills.random()
        val typeMult = enemySkill.element.effectivenessAgainst(playerSpecies.primaryElement)
        val atk = enemyMon.effectiveAtk(enemySpecies).toFloat()
        val def = playerMon.effectiveDef(playerSpecies).coerceAtLeast(12).toFloat()
        val enemyDmg = ((enemySkill.power * (atk / def) * 0.58f + 7f) * typeMult).roundToInt().coerceAtLeast(5)

        val finalPlayerHp = (playerMon.currentHp - enemyDmg).coerceAtLeast(0)
        logs.add("${enemyMon.nickname} kontert mit ${enemySkill.nameDe} → $enemyDmg Schaden!")

        val updatedPlayerMon = if (finalPlayerHp <= 0) {
            logs.add("${playerMon.nickname} wurde erschöpft, erholt sich aber dank Notfall-Schild auf 25% KP!")
            playerMon.copy(currentHp = (playerMon.maxHp(playerSpecies) * 0.25f).roundToInt(), currentStamina = 100)
        } else {
            playerMon.copy(currentHp = finalPlayerHp)
        }

        val updatedMonsters = state.monsters.map {
            if (it.instanceId == updatedPlayerMon.instanceId) updatedPlayerMon else it
        }

        _gameState.value = state.copy(
            monsters = updatedMonsters,
            activeBattle = battle.copy(
                enemyMonster = enemyMon,
                turnNumber = battle.turnNumber + 1,
                battleLog = logs.takeLast(6),
                lastEffectText = "${enemySkill.element.iconCode} -$enemyDmg KP",
                lastEffectColorHex = 0xFFFF5252,
                isFinished = finalPlayerHp <= 0,
                playerWon = false
            )
        )
    }

    private fun handleBattleVictory(
        state: AetheronGameState,
        battle: BattleState,
        playerMon: MonsterInstance,
        enemyMon: MonsterInstance,
        enemySpecies: MonsterSpecies,
        logs: MutableList<String>
    ) {
        val xpWon = 45 + enemyMon.level * 12 + (if (battle.isGymBattle) 180 else 0)
        val goldWon = 80 + enemyMon.level * 10 + (if (battle.isGymBattle) 450 else 0)
        logs.add("SIEG! +$xpWon EP & +$goldWon Gold gewonnen!")

        val totalXp = playerMon.xp + xpWon
        val threshold = playerMon.level * 60
        val leveledUp = totalXp >= threshold && playerMon.level < 100
        val newLevel = if (leveledUp) playerMon.level + 1 else playerMon.level
        if (leveledUp) {
            logs.add("🎉 ${playerMon.nickname} erreicht Level $newLevel!")
        }

        val playerSpecies = AetheronWorldCatalog.getSpecies(playerMon.speciesId)
        val updatedMon = playerMon.copy(
            level = newLevel,
            xp = if (leveledUp) totalXp - threshold else totalXp,
            currentHp = playerMon.maxHp(playerSpecies),
            currentStamina = 100
        )

        val newBadges = if (battle.gymBadgeToWin != null) {
            logs.add("🏆 ORDEN ERRUNGEN: ${battle.gymBadgeToWin.badgeNameDe} (#${battle.gymBadgeToWin.badgeIndex}/24)!")
            state.earnedBadges + battle.gymBadgeToWin.badgeIndex
        } else state.earnedBadges

        // Update monster hunt & bounty quests
        val updatedQuests = state.activeQuests.map { q ->
            val matchesHunt = q.category == QuestCategory.MONSTER_HUNT && (q.targetSpeciesId == enemySpecies.id || q.continentId == state.currentContinentId)
            val matchesBounty = q.category == QuestCategory.FACTION_BOUNTY && q.continentId == state.currentContinentId
            if ((matchesHunt || matchesBounty) && !q.isCompleted) {
                val p = (q.currentProgress + 1).coerceAtMost(q.requiredCount)
                q.copy(currentProgress = p, isCompleted = p >= q.requiredCount)
            } else q
        }

        _gameState.value = state.copy(
            playerLevel = (state.playerLevel + (if (leveledUp) 1 else 0)).coerceAtMost(100),
            skillPoints = state.skillPoints + (if (leveledUp) 1 else 0),
            gold = state.gold + goldWon,
            earnedBadges = newBadges,
            arenaWins = state.arenaWins + (if (battle.isArenaPvp) 1 else 0),
            monsters = state.monsters.map { if (it.instanceId == updatedMon.instanceId) updatedMon else it },
            activeQuests = updatedQuests,
            activeBattle = battle.copy(
                enemyMonster = enemyMon,
                battleLog = logs.takeLast(6),
                lastEffectText = "SIEG! +$xpWon EP",
                lastEffectColorHex = 0xFF69F0AE,
                isFinished = true,
                playerWon = true
            ),
            statusBannerDe = "Kampf gewonnen! +$xpWon EP & +$goldWon Gold."
        )
        syncToCloud(silent = true)
    }

    fun attemptCaptureInBattle(useMasterOrb: Boolean) {
        val state = _gameState.value
        val battle = state.activeBattle ?: return
        if (battle.isGymBattle || battle.isArenaPvp || battle.isFinished) return

        val orbId = if (useMasterOrb) "orb_aether_master" else "orb_aether_basic"
        val currentCount = state.inventory[orbId] ?: 0
        if (currentCount <= 0) return

        val newInv = state.inventory + (orbId to (currentCount - 1))
        val enemy = battle.enemyMonster
        val enemySpecies = AetheronWorldCatalog.getSpecies(enemy.speciesId)
        val hpRatio = enemy.currentHp.toFloat() / battle.enemyMaxHp.coerceAtLeast(1)
        val tamerSkill = state.playerSkills.find { it.id == "skill_tamer_mastery" }?.currentRank ?: 1
        val catchChance = ((1.1f - hpRatio * 0.65f) * (if (useMasterOrb) 85f else 52f) + tamerSkill * 8f).roundToInt()

        val logs = battle.battleLog.toMutableList()
        if (Random.nextInt(100) < catchChance.coerceAtLeast(35)) {
            val capturedInstance = enemy.copy(
                instanceId = "mon_${UUID.randomUUID().toString().take(8)}",
                currentHp = enemy.maxHp(enemySpecies),
                currentStamina = 100
            )
            logs.add("✨ GEFANGEN! ${enemySpecies.nameDe} (Gen-Rang ${enemy.ivRank}) gehört jetzt zu deinem Team!")

            val updatedQuests = state.activeQuests.map { q ->
                if ((q.category == QuestCategory.MONSTER_HUNT || q.category == QuestCategory.FACTION_BOUNTY) && !q.isCompleted) {
                    val p = (q.currentProgress + 1).coerceAtMost(q.requiredCount)
                    q.copy(currentProgress = p, isCompleted = p >= q.requiredCount)
                } else q
            }

            _gameState.value = state.copy(
                inventory = newInv,
                capturedSpeciesIds = state.capturedSpeciesIds + enemySpecies.id,
                monsters = state.monsters + capturedInstance,
                activeQuests = updatedQuests,
                activeBattle = battle.copy(
                    battleLog = logs.takeLast(6),
                    lastEffectText = "GEFANGEN! #${enemySpecies.id}",
                    lastEffectColorHex = 0xFFFFB300,
                    isFinished = true,
                    playerWon = true,
                    capturedMonster = true
                ),
                statusBannerDe = "${enemySpecies.nameDe} (#${enemySpecies.id}/240) erfolgreich gefangen!"
            )
            syncToCloud(silent = true)
        } else {
            logs.add("${enemySpecies.nameDe} hat sich aus der Sphäre befreit! Schwäche es weiter!")
            _gameState.value = state.copy(
                inventory = newInv,
                activeBattle = battle.copy(battleLog = logs.takeLast(6))
            )
        }
    }

    fun usePotionInBattle() {
        val state = _gameState.value
        val battle = state.activeBattle ?: return
        val potCount = state.inventory["pot_vital_elixir"] ?: 0
        if (potCount <= 0) return

        val playerMon = state.monsters.find { it.instanceId == battle.playerMonsterId } ?: return
        val sp = AetheronWorldCatalog.getSpecies(playerMon.speciesId)
        val healedMon = playerMon.copy(
            currentHp = (playerMon.currentHp + 120).coerceAtMost(playerMon.maxHp(sp)),
            currentStamina = (playerMon.currentStamina + 50).coerceAtMost(100)
        )
        val logs = (battle.battleLog + "Vital-Elixier genutzt: +120 KP & +50 Ausdauer für ${playerMon.nickname}!").takeLast(6)

        _gameState.value = state.copy(
            inventory = state.inventory + ("pot_vital_elixir" to (potCount - 1)),
            monsters = state.monsters.map { if (it.instanceId == healedMon.instanceId) healedMon else it },
            activeBattle = battle.copy(battleLog = logs)
        )
    }

    fun closeBattleOverlay() {
        _gameState.update { it.copy(activeBattle = null) }
    }

    fun selectActiveMonster(instanceId: String) {
        _gameState.update { it.copy(selectedMonsterId = instanceId) }
    }

    /**
     * Interaktives Monster-Trainingssystem (Dojo Mini-Game Abschluss)
     */
    fun completeTrainingMiniGame(
        monsterId: String,
        exercise: TrainingExerciseType,
        scorePercent: Int
    ) {
        val state = _gameState.value
        val monster = state.monsters.find { it.instanceId == monsterId } ?: return
        val dojoRank = state.playerSkills.find { it.id == "skill_dojo_mentor" }?.currentRank ?: 1
        val hasBracer = (state.inventory["gear_power_bracer"] ?: 0) > 0
        val pyroRep = state.factionStandings["pyroclast_syndicate"] ?: 100
        val factionMult = if (pyroRep >= 250) 1.2f else 1.0f

        val result = DynamicQuestAndTrainingEngine.evaluateTrainingMiniGame(
            monster = monster,
            exercise = exercise,
            scorePercent = scorePercent,
            dojoSkillRank = dojoRank,
            hasPowerBracer = hasBracer,
            factionBonusMultiplier = factionMult
        )

        var newInv = state.inventory
        if (result.bonusItemRewardId != null) {
            val id = result.bonusItemRewardId
            newInv = newInv + (id to ((newInv[id] ?: 0) + 1))
        }

        _gameState.value = state.copy(
            monsters = state.monsters.map { if (it.instanceId == monsterId) result.updatedMonster else it },
            inventory = newInv,
            lastTrainingResult = result,
            statusBannerDe = "Dojo-Training (${result.gradeRank}-Rang): ${monster.nickname} erhält +${result.primaryStatGained} ${exercise.primaryStat.shortLabel} & +${result.secondaryStatGained} ${exercise.secondaryStat.shortLabel}!"
        )
        syncToCloud(silent = true)
    }

    /**
     * Evolution durch Level/Trainings-Statistiken oder seltene Evolutions-Artefakte
     */
    fun evolveMonster(monsterId: String, branch: EvolutionBranch): Boolean {
        val state = _gameState.value
        val monster = state.monsters.find { it.instanceId == monsterId } ?: return false
        val targetSpecies = AetheronWorldCatalog.getSpecies(branch.targetSpeciesId)

        var newInv = state.inventory
        if (branch.requiredArtifactId != null) {
            val artCount = newInv[branch.requiredArtifactId] ?: 0
            if (artCount <= 0) return false
            newInv = newInv + (branch.requiredArtifactId to (artCount - 1))
        } else {
            if (monster.level < branch.minLevel) return false
            if (branch.requiredDominantStat != null) {
                val statVal = when (branch.requiredDominantStat) {
                    StatType.ATK -> monster.trainedAtk
                    StatType.DEF -> monster.trainedDef
                    StatType.SPD -> monster.trainedSpd
                    StatType.ELEM -> monster.trainedElem
                    StatType.HP -> monster.trainedHp
                }
                if (statVal < branch.minTrainedStatValue) return false
            }
        }

        val evolved = monster.copy(
            speciesId = targetSpecies.id,
            nickname = targetSpecies.nameDe,
            inheritedTraits = (monster.inheritedTraits + targetSpecies.signatureTrait).distinctBy { it.id }.take(3)
        )
        val evolvedFullHp = evolved.copy(currentHp = evolved.maxHp(targetSpecies))

        _gameState.value = state.copy(
            monsters = state.monsters.map { if (it.instanceId == monsterId) evolvedFullHp else it },
            capturedSpeciesIds = state.capturedSpeciesIds + targetSpecies.id,
            inventory = newInv,
            statusBannerDe = "🌟 EVOLUTION! ${monster.nickname} hat sich zu ${targetSpecies.nameDe} (#${targetSpecies.id}) weiterentwickelt!"
        )
        syncToCloud(silent = true)
        return true
    }

    private val breedingService = com.example.service.BreedingService()

    /**
     * Zucht- & Genetik-Engine (inklusive Verstärkung gefangener Legendärer Monster!)
     */
    fun breedMonsters(parentAId: String, parentBId: String) {
        if (parentAId == parentBId) return
        val state = _gameState.value
        val parentA = state.monsters.find { it.instanceId == parentAId } ?: return
        val parentB = state.monsters.find { it.instanceId == parentBId } ?: return

        val finalChild = breedingService.breedDomainInstances(parentA, parentB)
        val childSpecies = AetheronWorldCatalog.getSpecies(finalChild.speciesId)

        _gameState.value = state.copy(
            monsters = state.monsters + finalChild,
            capturedSpeciesIds = state.capturedSpeciesIds + childSpecies.id,
            statusBannerDe = "🧬 Zucht erfolgreich! ${finalChild.nickname} (Gen-Rang ${finalChild.ivRank}, Generation ${finalChild.generation}) mit ${finalChild.inheritedTraits.size} vererbten Merkmalen geschlüpft!"
        )
        syncToCloud(silent = true)
    }

    /**
     * Dynamisches Quest-System: Neue Quests für aktuellen Standort/Level/Fraktion generieren & Belohnungen einlösen
     */
    fun refreshDynamicQuests() {
        val state = _gameState.value
        val biome = AetheronWorldCatalog.biomes.find { it.id == state.currentBiomeId }
            ?: AetheronWorldCatalog.biomes.first()
        val generated = DynamicQuestAndTrainingEngine.generateDynamicQuests(
            continentId = state.currentContinentId,
            biome = biome,
            playerLevel = state.playerLevel,
            completedQuestsCount = state.completedQuestsCount,
            factionStandings = state.factionStandings,
            currentWeather = state.currentWeather,
            currentTime = state.currentTimeOfDay
        )
        _gameState.value = state.copy(
            activeQuests = generated,
            statusBannerDe = "4 neue dynamische Quests für ${biome.nameDe} (Stufe ${state.playerLevel}) generiert!"
        )
    }

    fun simulateQuestStepProgress(questId: String) {
        val state = _gameState.value
        val updated = state.activeQuests.map { q ->
            if (q.id == questId && !q.isCompleted) {
                val p = (q.currentProgress + 1).coerceAtMost(q.requiredCount)
                q.copy(currentProgress = p, isCompleted = p >= q.requiredCount)
            } else q
        }
        _gameState.value = state.copy(
            activeQuests = updated,
            statusBannerDe = "Quest-Einsatz durchgeführt! Fortschritt aktualisiert."
        )
    }

    fun claimQuestReward(questId: String) {
        val state = _gameState.value
        val quest = state.activeQuests.find { it.id == questId } ?: return
        if (!quest.isCompleted) return

        val newRep = (state.factionStandings[quest.factionId] ?: 100) + quest.rewardFactionRep
        val newInv = state.inventory + (quest.rewardItemId to ((state.inventory[quest.rewardItemId] ?: 0) + quest.rewardItemCount))
        val rewardItemName = AetheronWorldCatalog.items.find { it.id == quest.rewardItemId }?.nameDe ?: quest.rewardItemId

        val remainingQuests = state.activeQuests.filterNot { it.id == questId }
        val biome = AetheronWorldCatalog.biomes.find { it.id == state.currentBiomeId }
            ?: AetheronWorldCatalog.biomes.first()
        val replacement = DynamicQuestAndTrainingEngine.generateDynamicQuests(
            continentId = state.currentContinentId,
            biome = biome,
            playerLevel = state.playerLevel + 1,
            completedQuestsCount = state.completedQuestsCount + 1,
            factionStandings = state.factionStandings + (quest.factionId to newRep),
            currentWeather = state.currentWeather,
            currentTime = state.currentTimeOfDay
        ).first { it.category == quest.category }

        _gameState.value = state.copy(
            gold = state.gold + quest.rewardGold,
            playerLevel = (state.playerLevel + 1).coerceAtMost(100),
            skillPoints = state.skillPoints + 1,
            completedQuestsCount = state.completedQuestsCount + 1,
            factionStandings = state.factionStandings + (quest.factionId to newRep),
            inventory = newInv,
            activeQuests = remainingQuests + replacement,
            statusBannerDe = "Quest abgeschlossen! +${quest.rewardGold} Gold, +${quest.rewardFactionRep} Ruf (${quest.factionNameDe}) & +${quest.rewardItemCount}x $rewardItemName!"
        )
        syncToCloud(silent = true)
    }

    /**
     * Crafting & Spieler-Skilltree
     */
    fun craftRecipe(recipe: CraftingRecipe) {
        val state = _gameState.value
        if (state.gold < recipe.goldCost) return
        val hasAllIngredients = recipe.ingredients.all { (itemId, req) ->
            (state.inventory[itemId] ?: 0) >= req
        }
        if (!hasAllIngredients) return

        val mutableInv = state.inventory.toMutableMap()
        recipe.ingredients.forEach { (itemId, req) ->
            mutableInv[itemId] = (mutableInv[itemId] ?: 0) - req
        }
        val alchemistRank = state.playerSkills.find { it.id == "skill_alchemist_guild" }?.currentRank ?: 0
        val bonusCount = if (alchemistRank >= 3) 1 else 0
        val totalCreated = recipe.resultCount + bonusCount
        mutableInv[recipe.resultItemId] = (mutableInv[recipe.resultItemId] ?: 0) + totalCreated

        val itemName = AetheronWorldCatalog.items.find { it.id == recipe.resultItemId }?.nameDe ?: recipe.resultItemId
        _gameState.value = state.copy(
            gold = state.gold - recipe.goldCost,
            inventory = mutableInv,
            statusBannerDe = "Geschmiedet: +${totalCreated}x $itemName hergestellt!"
        )
        syncToCloud(silent = true)
    }

    fun upgradePlayerSkill(skillId: String) {
        val state = _gameState.value
        if (state.skillPoints <= 0) return
        val node = state.playerSkills.find { it.id == skillId } ?: return
        if (node.currentRank >= node.maxRank) return

        val updatedSkills = state.playerSkills.map {
            if (it.id == skillId) it.copy(currentRank = it.currentRank + 1) else it
        }
        _gameState.value = state.copy(
            skillPoints = state.skillPoints - 1,
            playerSkills = updatedSkills,
            statusBannerDe = "Charakter-Skill '${node.nameDe}' auf Rang ${node.currentRank + 1}/${node.maxRank} gesteigert!"
        )
        syncToCloud(silent = true)
    }

    /**
     * Online-Handel (Tauschbörse) & Online-Arena-Registrierung über Firestore
     */
    fun postMonsterTradeOffer(monsterId: String, requestedSpeciesId: Int) {
        if (!isCloudAuthenticated) {
            _gameState.update {
                it.copy(statusBannerDe = "Für den Online-Spielerhandel bitte oben rechts mit Google anmelden!")
            }
            return
        }
        val state = _gameState.value
        if (state.monsters.size <= 1) {
            _gameState.update { it.copy(statusBannerDe = "Du musst mindestens 1 Monster im Team behalten!") }
            return
        }
        val mon = state.monsters.find { it.instanceId == monsterId } ?: return
        val requestedSpecies = AetheronWorldCatalog.getSpecies(requestedSpeciesId)

        viewModelScope.launch {
            val res = repository.createTradeOffer(
                ownerName = state.trainerName,
                offeredSpeciesId = mon.speciesId,
                offeredMonsterName = mon.nickname,
                offeredLevel = mon.level,
                offeredIvRank = mon.ivRank,
                offeredTrait = mon.inheritedTraits.firstOrNull()?.nameDe ?: "Solar-Ader",
                offeredTrainedStats = "ATK+${mon.trainedAtk} / DEF+${mon.trainedDef} / SPD+${mon.trainedSpd}",
                requestedSpeciesId = requestedSpecies.id,
                requestedSpeciesName = requestedSpecies.nameDe
            )
            if (res.isSuccess) {
                _gameState.update {
                    it.copy(
                        statusBannerDe = "Handelsangebot veröffentlicht: ${mon.nickname} gegen ${requestedSpecies.nameDe}!"
                    )
                }
            }
        }
    }

    fun acceptTradeOffer(offer: TradeOffer) {
        if (!isCloudAuthenticated) {
            _gameState.update {
                it.copy(statusBannerDe = "Für den Online-Spielerhandel bitte oben rechts mit Google anmelden!")
            }
            return
        }
        viewModelScope.launch {
            val res = repository.fulfillTradeOffer(offer.id)
            if (res.isSuccess) {
                val receivedSpecies = AetheronWorldCatalog.getSpecies(offer.offeredSpeciesId.toInt().coerceIn(1, 240))
                val receivedMonster = MonsterInstance(
                    instanceId = "trade_${UUID.randomUUID().toString().take(8)}",
                    speciesId = receivedSpecies.id,
                    nickname = offer.offeredMonsterName,
                    level = offer.offeredLevel.toInt().coerceIn(1, 100),
                    ivRank = offer.offeredIvRank,
                    inheritedTraits = listOf(receivedSpecies.signatureTrait),
                    trainedAtk = 25,
                    trainedSpd = 25
                )
                _gameState.update { state ->
                    state.copy(
                        monsters = state.monsters + receivedMonster.copy(currentHp = receivedMonster.maxHp(receivedSpecies)),
                        capturedSpeciesIds = state.capturedSpeciesIds + receivedSpecies.id,
                        statusBannerDe = "Online-Tausch abgeschlossen! Du hast ${offer.offeredMonsterName} (Gen ${offer.offeredIvRank}) erhalten!"
                    )
                }
                syncToCloud(silent = true)
            }
        }
    }

    fun publishSquadToOnlineArena() {
        if (!isCloudAuthenticated) {
            _gameState.update {
                it.copy(statusBannerDe = "Für die globale Online-Arena bitte oben rechts mit Google anmelden! (Lokale Trainings-Arena ist verfügbar)")
            }
            return
        }
        val state = _gameState.value
        val topThree = state.monsters.take(3)
        val totalPower = topThree.sumOf { it.combatPower(AetheronWorldCatalog.getSpecies(it.speciesId)) }
        val leadId = topThree.firstOrNull()?.speciesId ?: 1
        val summary = topThree.joinToString(" | ") { "${it.nickname} Lv.${it.level} (${it.ivRank})" }

        viewModelScope.launch {
            val res = repository.publishArenaRoster(
                trainerName = state.trainerName,
                combatPower = totalPower,
                badgesCount = state.earnedBadges.size,
                wins = state.arenaWins,
                leadSpeciesId = leadId,
                teamSummary = summary
            )
            if (res.isSuccess) {
                _gameState.update {
                    it.copy(statusBannerDe = "Dein Team (Kampfkraft $totalPower) ist jetzt in der globalen Online-Arena aktiv!")
                }
            }
        }
    }

    fun syncToCloud(silent: Boolean = false) {
        val state = _gameState.value
        viewModelScope.launch {
            val json = serializeGameState(state)
            val profile = PlayerCloudProfile(
                userId = currentUserId,
                trainerName = state.trainerName,
                level = state.playerLevel.toLong(),
                gold = state.gold.toLong(),
                continentId = state.currentContinentId.toLong(),
                badgesCount = state.earnedBadges.size.toLong(),
                capturedCount = state.capturedSpeciesIds.size.toLong(),
                questsCompleted = state.completedQuestsCount.toLong(),
                factionSylvariaRep = (state.factionStandings["sylvaria_order"] ?: 100).toLong(),
                factionPyroclastRep = (state.factionStandings["pyroclast_syndicate"] ?: 100).toLong(),
                factionAetherisRep = (state.factionStandings["aetheris_chroniclers"] ?: 100).toLong(),
                saveStateJson = json
            )

            if (syncRepository != null) {
                syncRepository.savePlayerProfile(profile)
                state.monsters.forEach { instance ->
                    val monsterEntity = com.example.data.remote.Monster.fromMonsterInstance(currentUserId, instance)
                    syncRepository.saveMonster(monsterEntity)
                }
                val syncReport = syncRepository.synchronizeWithFirestore().getOrNull()
                _gameState.update {
                    val msg = if (syncReport?.isCloudAvailable == true) {
                        "☁️ Room & Firestore synchronisiert (${state.monsters.size} Monster)!"
                    } else {
                        "💾 Spielstand & ${state.monsters.size} Monster in lokaler Room-Datenbank gespeichert!"
                    }
                    it.copy(
                        isCloudSynced = true,
                        statusBannerDe = if (silent) it.statusBannerDe else msg
                    )
                }
                return@launch
            }

            if (!isCloudAuthenticated) {
                saveLocalProfile(silent = silent)
                return@launch
            }
            val res = repository.savePlayerProfile(profile)
            if (res.isSuccess) {
                _gameState.update {
                    it.copy(
                        isCloudSynced = true,
                        statusBannerDe = if (silent) it.statusBannerDe else "☁️ Cloud-Speicherung erfolgreich synchronisiert!"
                    )
                }
            }
        }
    }

    private fun serializeGameState(state: AetheronGameState): String {
        return try {
            val root = JSONObject()
            val badgesArr = JSONArray(state.earnedBadges.toList())
            val capturedArr = JSONArray(state.capturedSpeciesIds.toList())
            val monstersArr = JSONArray()
            state.monsters.take(25).forEach { m ->
                val obj = JSONObject()
                obj.put("id", m.instanceId)
                obj.put("sp", m.speciesId)
                obj.put("nick", m.nickname)
                obj.put("lv", m.level)
                obj.put("iv", m.ivRank)
                obj.put("tHp", m.trainedHp)
                obj.put("tAtk", m.trainedAtk)
                obj.put("tDef", m.trainedDef)
                obj.put("tSpd", m.trainedSpd)
                obj.put("tEl", m.trainedElem)
                obj.put("gen", m.generation)
                monstersArr.put(obj)
            }
            val invObj = JSONObject()
            state.inventory.forEach { (k, v) -> invObj.put(k, v) }
            root.put("badges", badgesArr)
            root.put("captured", capturedArr)
            root.put("monsters", monstersArr)
            root.put("inventory", invObj)
            root.toString()
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun restoreFromCloudProfile(profile: PlayerCloudProfile) {
        try {
            val root = JSONObject(profile.saveStateJson)
            val badges = mutableSetOf<Int>()
            val badgesArr = root.optJSONArray("badges")
            if (badgesArr != null) {
                for (i in 0 until badgesArr.length()) badges.add(badgesArr.getInt(i))
            }
            val captured = mutableSetOf<Int>()
            val capArr = root.optJSONArray("captured")
            if (capArr != null) {
                for (i in 0 until capArr.length()) captured.add(capArr.getInt(i))
            }
            val restoredMonsters = mutableListOf<MonsterInstance>()
            val monArr = root.optJSONArray("monsters")
            if (monArr != null) {
                for (i in 0 until monArr.length()) {
                    val o = monArr.getJSONObject(i)
                    val spId = o.optInt("sp", 1).coerceIn(1, 240)
                    val sp = AetheronWorldCatalog.getSpecies(spId)
                    val m = MonsterInstance(
                        instanceId = o.optString("id", "mon_$i"),
                        speciesId = spId,
                        nickname = o.optString("nick", sp.nameDe),
                        level = o.optInt("lv", 10),
                        ivRank = o.optString("iv", "A"),
                        inheritedTraits = listOf(sp.signatureTrait),
                        trainedHp = o.optInt("tHp", 0),
                        trainedAtk = o.optInt("tAtk", 0),
                        trainedDef = o.optInt("tDef", 0),
                        trainedSpd = o.optInt("tSpd", 0),
                        trainedElem = o.optInt("tEl", 0),
                        generation = o.optInt("gen", 1)
                    )
                    restoredMonsters.add(m.copy(currentHp = m.maxHp(sp)))
                }
            }
            val invMap = mutableMapOf<String, Int>()
            val invObj = root.optJSONObject("inventory")
            if (invObj != null) {
                invObj.keys().forEach { k -> invMap[k] = invObj.optInt(k, 0) }
            }

            _gameState.update { current ->
                current.copy(
                    trainerName = profile.trainerName,
                    playerLevel = profile.level.toInt().coerceIn(1, 100),
                    gold = profile.gold.toInt(),
                    currentContinentId = profile.continentId.toInt().coerceIn(1, 3),
                    earnedBadges = if (badges.isNotEmpty()) badges else current.earnedBadges,
                    capturedSpeciesIds = if (captured.isNotEmpty()) captured else current.capturedSpeciesIds,
                    monsters = if (restoredMonsters.isNotEmpty()) restoredMonsters else current.monsters,
                    selectedMonsterId = restoredMonsters.firstOrNull()?.instanceId ?: current.selectedMonsterId,
                    inventory = if (invMap.isNotEmpty()) invMap else current.inventory,
                    completedQuestsCount = profile.questsCompleted.toInt(),
                    factionStandings = mapOf(
                        "sylvaria_order" to profile.factionSylvariaRep.toInt(),
                        "pyroclast_syndicate" to profile.factionPyroclastRep.toInt(),
                        "aetheris_chroniclers" to profile.factionAetherisRep.toInt()
                    ),
                    isCloudSynced = true,
                    statusBannerDe = "☁️ Cloud-Spielstand geladen (${restoredMonsters.size} Monster, ${badges.size}/24 Orden)."
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fallback to default game state", e)
        }
    }

    private fun AetheronWorldWorldSpecies(id: Int): MonsterSpecies = AetheronWorldCatalog.getSpecies(id)

    companion object {
        private const val TAG = "AetheronVM"

        fun createInitialGameState(userId: String, trainerName: String): AetheronGameState {
            val sp1 = AetheronWorldCatalog.getSpecies(1)   // Sylphling (Continent 1 Starter)
            val sp2 = AetheronWorldCatalog.getSpecies(81)  // Pyroling (Continent 2 Starter)
            val sp3 = AetheronWorldCatalog.getSpecies(161) // Aetherling (Continent 3 Starter)
            val spLeg = AetheronWorldCatalog.getSpecies(76) // Sylvarion-Rex (Continent 1 Legendary for breeding demo)

            val m1 = MonsterInstance(
                instanceId = "starter_sylph",
                speciesId = sp1.id,
                nickname = sp1.nameDe,
                level = 14,
                ivRank = "S",
                inheritedTraits = listOf(sp1.signatureTrait),
                trainedAtk = 18,
                trainedSpd = 14
            )
            val m2 = MonsterInstance(
                instanceId = "starter_pyro",
                speciesId = sp2.id,
                nickname = sp2.nameDe,
                level = 15,
                ivRank = "A",
                inheritedTraits = listOf(sp2.signatureTrait),
                trainedAtk = 20,
                trainedDef = 16
            )
            val m3 = MonsterInstance(
                instanceId = "starter_aether",
                speciesId = sp3.id,
                nickname = sp3.nameDe,
                level = 16,
                ivRank = "S",
                inheritedTraits = listOf(sp3.signatureTrait),
                trainedElem = 24,
                trainedSpd = 18
            )
            val m4 = MonsterInstance(
                instanceId = "leg_sylvarion",
                speciesId = spLeg.id,
                nickname = spLeg.nameDe,
                level = 30,
                ivRank = "S+",
                inheritedTraits = listOf(spLeg.signatureTrait),
                trainedAtk = 30,
                trainedDef = 30
            )

            val starterTeam = listOf(
                m1.copy(currentHp = m1.maxHp(sp1)),
                m2.copy(currentHp = m2.maxHp(sp2)),
                m3.copy(currentHp = m3.maxHp(sp3)),
                m4.copy(currentHp = m4.maxHp(spLeg))
            )

            val startBiome = AetheronWorldCatalog.biomes.first()
            val initialStandings = mapOf(
                "sylvaria_order" to 160,
                "pyroclast_syndicate" to 120,
                "aetheris_chroniclers" to 120
            )
            val initialQuests = DynamicQuestAndTrainingEngine.generateDynamicQuests(
                continentId = 1,
                biome = startBiome,
                playerLevel = 14,
                completedQuestsCount = 2,
                factionStandings = initialStandings,
                currentWeather = WeatherType.SOLAR_FLARE,
                currentTime = TimeOfDay.DAY
            )

            return AetheronGameState(
                userId = userId,
                trainerName = trainerName,
                playerLevel = 14,
                gold = 1450,
                earnedBadges = setOf(1, 2),
                capturedSpeciesIds = setOf(1, 81, 161, 76),
                monsters = starterTeam,
                selectedMonsterId = starterTeam.first().instanceId,
                inventory = mapOf(
                    "orb_aether_basic" to 8,
                    "orb_aether_master" to 3,
                    "pot_vital_elixir" to 6,
                    "pot_phoenix_nectar" to 2,
                    "mat_sylva_leaf" to 4,
                    "mat_aether_shard" to 4,
                    "mat_iron_ore" to 3,
                    "mat_sun_amber" to 2,
                    "mat_magma_core" to 2,
                    "art_solar_core" to 1,
                    "gear_power_bracer" to 1
                ),
                factionStandings = initialStandings,
                completedQuestsCount = 2,
                activeQuests = initialQuests
            )
        }
    }
}
