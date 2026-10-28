package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.AetheronLocalDatabase
import com.example.data.repository.AetheronRepository
import com.example.data.repository.AetheronSyncRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.BreedingAndDexScreen
import com.example.ui.screens.DojoTrainingAndEvolutionScreen
import com.example.ui.screens.QuestsCraftingAndOnlineScreen
import com.example.ui.screens.SystemWorldAndBadgesSection
import com.example.ui.screens.WorldAndBattleScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronViewModel
import com.example.ui.viewmodel.MonsterViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AetheronRootApp()
            }
        }
    }
}

@Composable
fun AetheronRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var isLocalSessionActive by remember { mutableStateOf(false) }
    val user = currentUser

    if (user == null && !isLocalSessionActive) {
        AuthScreen(
            onAuthSuccess = { /* AuthStateListener updates currentUser automatically */ },
            onStartWithoutGoogle = { isLocalSessionActive = true }
        )
    } else {
        val isCloudAuthenticated = user != null
        AetheronAuthenticatedContent(
            currentUserId = user?.uid ?: "local_trainer",
            displayName = user?.displayName ?: "Aether-Trainer",
            isCloudAuthenticated = isCloudAuthenticated,
            onExitLocalMode = { isLocalSessionActive = false }
        )
    }
}

private enum class SystemMenuSection(val titleDe: String, val badgeTag: String) {
    WELT("🌍 Welt", "nav_tab_world"),
    ORDEN("🏆 Orden", "system_tab_badges"),
    DOJO("🥋 Dojo", "nav_tab_dojo"),
    QUESTS("📜 Quests", "nav_tab_quests"),
    DEX("🧬 Zucht & Dex", "nav_tab_breeding")
}

@Composable
fun AetheronAuthenticatedContent(
    currentUserId: String,
    displayName: String,
    isCloudAuthenticated: Boolean = true,
    onExitLocalMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: AetheronViewModel = viewModel(
        key = "${currentUserId}_$isCloudAuthenticated",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val cloudRepo = AetheronRepository(db)
                val localDatabase = AetheronLocalDatabase.getInstance(app)
                val syncRepo = AetheronSyncRepository(
                    cloudRepository = cloudRepo,
                    localMonsterDao = localDatabase.localMonsterDao(),
                    localProfileDao = localDatabase.localPlayerProfileDao(),
                    appContext = app,
                    authUidProvider = { if (isCloudAuthenticated) currentUserId else null }
                )
                val localPrefs = app.getSharedPreferences("aetheron_local_save", android.content.Context.MODE_PRIVATE)
                AetheronViewModel(
                    repository = cloudRepo,
                    currentUserId = currentUserId,
                    initialTrainerName = displayName,
                    isCloudAuthenticated = isCloudAuthenticated,
                    localPrefs = localPrefs,
                    syncRepository = syncRepo
                )
            }
        }
    )

    val monsterViewModel: MonsterViewModel = viewModel(
        key = "monster_vm_${currentUserId}_$isCloudAuthenticated",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val cloudRepo = AetheronRepository(db)
                val localDatabase = AetheronLocalDatabase.getInstance(app)
                val syncRepo = AetheronSyncRepository(
                    cloudRepository = cloudRepo,
                    localMonsterDao = localDatabase.localMonsterDao(),
                    localProfileDao = localDatabase.localPlayerProfileDao(),
                    appContext = app,
                    authUidProvider = { if (isCloudAuthenticated) currentUserId else null }
                )
                MonsterViewModel(
                    syncRepository = syncRepo,
                    userId = currentUserId
                )
            }
        }
    )

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val openTradesState by viewModel.openTradesState.collectAsStateWithLifecycle()
    val arenaRostersState by viewModel.arenaRostersState.collectAsStateWithLifecycle()

    var isSystemMenuOpen by remember { mutableStateOf(false) }
    var activeSystemSection by remember { mutableStateOf(SystemMenuSection.WELT) }

    if (isSystemMenuOpen) {
        BackHandler {
            isSystemMenuOpen = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MidnightSlate,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = DeepTacticalSurface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${gameState.trainerName} (Lv.${gameState.playerLevel})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AetherCyan,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "🏆 ${gameState.earnedBadges.size}/24 • 🐉 ${gameState.capturedSpeciesIds.size}/240 • 💰 ${gameState.gold}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BadgeGold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = gameState.statusBannerDe,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { isSystemMenuOpen = !isSystemMenuOpen },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("top_system_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSystemMenuOpen) BadgeGold else AetherCyan.copy(alpha = 0.2f),
                                    contentColor = if (isSystemMenuOpen) MidnightSlate else AetherCyan
                                )
                            ) {
                                Icon(
                                    imageVector = if (isSystemMenuOpen) Icons.Default.Map else Icons.Default.Apps,
                                    contentDescription = "System Menü",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSystemMenuOpen) "Karte" else "System",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.syncToCloud(silent = false)
                                    monsterViewModel.refreshAndSyncMonsters()
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("top_cloud_save_button")
                            ) {
                                Icon(
                                    imageVector = if (isCloudAuthenticated) Icons.Default.CloudDone else Icons.Default.Save,
                                    contentDescription = if (isCloudAuthenticated) "Cloud Speichern" else "Lokal Speichern",
                                    tint = if (gameState.isCloudSynced) ElementTerra else AetherCyan,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (isCloudAuthenticated) {
                                        signOut(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    } else {
                                        viewModel.syncToCloud(silent = true)
                                        onExitLocalMode()
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = if (isCloudAuthenticated) "Abmelden" else "Zum Startbildschirm / Google Login",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Main Uncluttered 2.5D Overworld Screen
            WorldAndBattleScreen(
                gameState = gameState,
                viewModel = viewModel,
                onOpenSystemMenu = { initialIndex ->
                    activeSystemSection = SystemMenuSection.entries.getOrElse(initialIndex) { SystemMenuSection.WELT }
                    isSystemMenuOpen = true
                }
            )

            // Central System Menu Overlay housing Welt, Orden, Dojo, Quests, and Zucht & Dex
            if (isSystemMenuOpen) {
                Surface(
                    color = MidnightSlate.copy(alpha = 0.97f),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("system_menu_overlay")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // System Menu Header + Quick Overview Pills + Close Button
                        Surface(
                            color = DeepTacticalSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "⚙️ SYSTEM-ZENTRALE",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AetherCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Welt • Arena-Orden • Dojo & Team • Quests • Zucht & Dex",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = { isSystemMenuOpen = false },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("close_system_menu_button"),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = CrimsonAccent.copy(alpha = 0.22f),
                                            contentColor = TextPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Schließen", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Zur 2.5D Karte", style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 5 Category Selector Buttons inside System Menu (Welt, Orden, Dojo, Quests, Zucht & Dex)
                                ScrollableTabRow(
                                    selectedTabIndex = activeSystemSection.ordinal,
                                    containerColor = ElevatedTacticalCard,
                                    contentColor = AetherCyan,
                                    edgePadding = 4.dp,
                                    divider = {}
                                ) {
                                    SystemMenuSection.entries.forEach { section ->
                                        val selected = activeSystemSection == section
                                        Tab(
                                            selected = selected,
                                            onClick = { activeSystemSection = section },
                                            modifier = Modifier.testTag(section.badgeTag),
                                            text = {
                                                val subtitle = when (section) {
                                                    SystemMenuSection.WELT -> "Kontinent ${gameState.currentContinentId}"
                                                    SystemMenuSection.ORDEN -> "${gameState.earnedBadges.size}/24"
                                                    SystemMenuSection.DOJO -> "${gameState.monsters.size} Team"
                                                    SystemMenuSection.QUESTS -> "${gameState.activeQuests.size} Aktiv"
                                                    SystemMenuSection.DEX -> "${gameState.capturedSpeciesIds.size}/240"
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = section.titleDe,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = if (selected) AetherCyan else TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = subtitle,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (selected) BadgeGold else TextSecondary
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Active System Section Content
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (activeSystemSection) {
                                SystemMenuSection.WELT -> SystemWorldAndBadgesSection(
                                    gameState = gameState,
                                    viewModel = viewModel,
                                    showBadgesSection = false,
                                    onCloseSystemMenu = { isSystemMenuOpen = false }
                                )
                                SystemMenuSection.ORDEN -> SystemWorldAndBadgesSection(
                                    gameState = gameState,
                                    viewModel = viewModel,
                                    showBadgesSection = true,
                                    onCloseSystemMenu = { isSystemMenuOpen = false }
                                )
                                SystemMenuSection.DOJO -> DojoTrainingAndEvolutionScreen(
                                    gameState = gameState,
                                    viewModel = viewModel
                                )
                                SystemMenuSection.QUESTS -> QuestsCraftingAndOnlineScreen(
                                    gameState = gameState,
                                    openTradesState = openTradesState,
                                    arenaRostersState = arenaRostersState,
                                    viewModel = viewModel
                                )
                                SystemMenuSection.DEX -> BreedingAndDexScreen(
                                    gameState = gameState,
                                    viewModel = viewModel,
                                    monsterViewModel = monsterViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
