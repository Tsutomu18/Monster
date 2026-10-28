package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.remote.Monster
import com.example.data.repository.AetheronSyncRepository
import com.example.domain.model.MonsterInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sorting options for the captured monster collection.
 */
enum class MonsterSortOrder(val labelDe: String) {
    LEVEL_DESC("Stufe ↓"),
    ATTACK_DESC("Angriff ↓"),
    SPEED_DESC("Initiative ↓"),
    EVOLUTION_DESC("Evolution % ↓"),
    NAME_ASC("Name A–Z")
}

/**
 * Filter criteria applied to the captured monster collection in [MonsterViewModel].
 */
data class MonsterFilterState(
    val searchQuery: String = "",
    val selectedElement: String? = null,
    val selectedIvRank: String? = null,
    val minLevel: Int = 1,
    val sortOrder: MonsterSortOrder = MonsterSortOrder.LEVEL_DESC
)

/**
 * Represents the synchronization state between the local Room database and Firebase Firestore.
 */
data class MonsterSyncStatus(
    val isSyncing: Boolean = false,
    val isCloudAvailable: Boolean = false,
    val statusMessageDe: String = "Lokale Room-Datenbank aktiv",
    val lastSyncedCount: Int = 0
)

/**
 * Central ViewModel for retrieving, filtering, sorting, and synchronizing the player's
 * captured [Monster] collection across the local Room database and Firebase Firestore.
 */
class MonsterViewModel(
    private val syncRepository: AetheronSyncRepository,
    private val userId: String = AetheronSyncRepository.LOCAL_DEFAULT_USER_ID
) : ViewModel() {

    private val _filterState = MutableStateFlow(MonsterFilterState())
    val filterState: StateFlow<MonsterFilterState> = _filterState.asStateFlow()

    private val _syncStatus = MutableStateFlow(
        MonsterSyncStatus(
            isCloudAvailable = syncRepository.canSyncWithCloud(),
            statusMessageDe = if (syncRepository.canSyncWithCloud()) {
                "☁️ Verbunden mit Room & Firestore Cloud"
            } else {
                "💾 Offline-Modus: Lokale Room-Datenbank aktiv"
            }
        )
    )
    val syncStatus: StateFlow<MonsterSyncStatus> = _syncStatus.asStateFlow()

    /**
     * Unfiltered stream of all captured monsters for [userId] observed reactively from Room.
     */
    val allMonsters: StateFlow<List<Monster>> = syncRepository.observeMonsters(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Filtered and sorted stream of captured monsters based on [filterState].
     */
    val filteredMonsters: StateFlow<List<Monster>> = combine(
        allMonsters,
        _filterState
    ) { monsters, filter ->
        monsters
            .asSequence()
            .filter { monster ->
                val matchesQuery = filter.searchQuery.isBlank() ||
                    monster.name.contains(filter.searchQuery, ignoreCase = true) ||
                    monster.geneticTraitName.contains(filter.searchQuery, ignoreCase = true) ||
                    monster.primaryElement.contains(filter.searchQuery, ignoreCase = true)

                val matchesElement = filter.selectedElement == null ||
                    monster.primaryElement.equals(filter.selectedElement, ignoreCase = true)

                val matchesIv = filter.selectedIvRank == null ||
                    monster.geneticIvRank.equals(filter.selectedIvRank, ignoreCase = true)

                val matchesLevel = monster.currentLevel >= filter.minLevel

                matchesQuery && matchesElement && matchesIv && matchesLevel
            }
            .let { seq ->
                when (filter.sortOrder) {
                    MonsterSortOrder.LEVEL_DESC -> seq.sortedByDescending { it.currentLevel }
                    MonsterSortOrder.ATTACK_DESC -> seq.sortedByDescending { it.attack + it.trainedAttack }
                    MonsterSortOrder.SPEED_DESC -> seq.sortedByDescending { it.speed + it.trainedSpeed }
                    MonsterSortOrder.EVOLUTION_DESC -> seq.sortedByDescending { it.evolutionProgressPercent }
                    MonsterSortOrder.NAME_ASC -> seq.sortedBy { it.name.lowercase() }
                }
            }
            .toList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        refreshAndSyncMonsters()
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectElementFilter(element: String?) {
        _filterState.update { current ->
            val next = if (current.selectedElement == element) null else element
            current.copy(selectedElement = next)
        }
    }

    fun selectIvRankFilter(ivRank: String?) {
        _filterState.update { current ->
            val next = if (current.selectedIvRank == ivRank) null else ivRank
            current.copy(selectedIvRank = next)
        }
    }

    fun selectMinLevel(minLevel: Int) {
        _filterState.update { it.copy(minLevel = minLevel.coerceIn(1, 100)) }
    }

    fun selectSortOrder(sortOrder: MonsterSortOrder) {
        _filterState.update { it.copy(sortOrder = sortOrder) }
    }

    fun clearFilters() {
        _filterState.value = MonsterFilterState()
    }

    /**
     * Seeds the local Room database with initial starter monsters if the user's collection is empty.
     */
    fun seedStarterMonstersIfEmpty(starterInstances: List<MonsterInstance>) {
        viewModelScope.launch {
            val currentList = syncRepository.getMonsters(userId).getOrNull().orEmpty()
            if (currentList.isEmpty() && starterInstances.isNotEmpty()) {
                starterInstances.forEach { instance ->
                    val entity = Monster.fromMonsterInstance(userId, instance)
                    syncRepository.saveMonster(entity)
                }
            }
        }
    }

    /**
     * Saves or updates a [Monster] in Room and synchronizes with Firestore if authenticated.
     */
    fun saveMonster(monster: Monster) {
        viewModelScope.launch {
            syncRepository.saveMonster(monster.copy(userId = userId))
            _syncStatus.update {
                val cloudActive = syncRepository.canSyncWithCloud()
                it.copy(
                    isCloudAvailable = cloudActive,
                    statusMessageDe = if (cloudActive) {
                        "☁️ '${monster.name}' in Room & Firestore gespeichert"
                    } else {
                        "💾 '${monster.name}' lokal in Room gespeichert (Sync vorgemerkt)"
                    }
                )
            }
        }
    }

    /**
     * Deletes a [Monster] from the collection (Room + Firestore tombstone sync).
     */
    fun deleteMonster(monsterId: String) {
        viewModelScope.launch {
            syncRepository.deleteMonster(monsterId)
            _syncStatus.update {
                it.copy(statusMessageDe = "Monster aus Sammlung entfernt.")
            }
        }
    }

    /**
     * Triggers a bidirectional synchronization pass between the local Room database and Firestore.
     */
    fun refreshAndSyncMonsters() {
        viewModelScope.launch {
            _syncStatus.update {
                it.copy(
                    isSyncing = true,
                    isCloudAvailable = syncRepository.canSyncWithCloud(),
                    statusMessageDe = "Synchronisiere Room-Datenbank mit Firestore..."
                )
            }
            val result = syncRepository.synchronizeWithFirestore()
            val report = result.getOrNull()
            if (report != null && report.isCloudAvailable) {
                _syncStatus.value = MonsterSyncStatus(
                    isSyncing = false,
                    isCloudAvailable = true,
                    statusMessageDe = "☁️ Sync abgeschlossen: ↑${report.pushedMonstersCount} gesendet, ↓${report.pulledMonstersCount} geladen",
                    lastSyncedCount = report.pushedMonstersCount + report.pulledMonstersCount
                )
            } else {
                val localCount = syncRepository.getMonsters(userId).getOrNull()?.size ?: 0
                _syncStatus.value = MonsterSyncStatus(
                    isSyncing = false,
                    isCloudAvailable = false,
                    statusMessageDe = "💾 Lokale Room-Datenbank aktiv ($localCount Monster gespeichert)",
                    lastSyncedCount = localCount
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            syncRepository: AetheronSyncRepository,
            userId: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MonsterViewModel(
                    syncRepository = syncRepository,
                    userId = userId
                )
            }
        }
    }
}
