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
import com.example.data.repository.AetheronRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.BreedingAndDexScreen
import com.example.ui.screens.DojoTrainingAndEvolutionScreen
import com.example.ui.screens.QuestsCraftingAndOnlineScreen
import com.example.ui.screens.WorldAndBattleScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetheronViewModel
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
                val localPrefs = app.getSharedPreferences("aetheron_local_save", android.content.Context.MODE_PRIVATE)
                AetheronViewModel(
                    repository = AetheronRepository(db),
                    currentUserId = currentUserId,
                    initialTrainerName = displayName,
                    isCloudAuthenticated = isCloudAuthenticated,
                    localPrefs = localPrefs
                )
            }
        }
    )

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val openTradesState by viewModel.openTradesState.collectAsStateWithLifecycle()
    val arenaRostersState by viewModel.arenaRostersState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }

    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MidnightSlate,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = DeepTacticalSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AETHERON • ${gameState.trainerName} (Lv.${gameState.playerLevel})",
                                style = MaterialTheme.typography.titleLarge,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏆 ${gameState.earnedBadges.size}/24 Orden • 🐉 ${gameState.capturedSpeciesIds.size}/240 Monster • 💰 ${gameState.gold} Gold",
                                style = MaterialTheme.typography.labelMedium,
                                color = BadgeGold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.syncToCloud(silent = false) },
                                modifier = Modifier.testTag("top_cloud_save_button")
                            ) {
                                Icon(
                                    imageVector = if (isCloudAuthenticated) Icons.Default.CloudDone else Icons.Default.Save,
                                    contentDescription = if (isCloudAuthenticated) "Cloud Speichern" else "Lokal Speichern",
                                    tint = if (gameState.isCloudSynced) ElementTerra else AetherCyan
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
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = if (isCloudAuthenticated) "Abmelden" else "Zum Startbildschirm / Google Login",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Status Ticker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElevatedTacticalCard, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = gameState.statusBannerDe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            maxLines = 2
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepTacticalSurface,
                contentColor = TextPrimary,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Welt & Kampf") },
                    label = { Text("Welt & Orden") },
                    modifier = Modifier.testTag("nav_tab_world")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Dojo & Evolution") },
                    label = { Text("Dojo & Team") },
                    modifier = Modifier.testTag("nav_tab_dojo")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Zucht & 240 Dex") },
                    label = { Text("Zucht & Dex") },
                    modifier = Modifier.testTag("nav_tab_breeding")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Quests & Online") },
                    label = { Text("Quests & Hub") },
                    modifier = Modifier.testTag("nav_tab_quests")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> WorldAndBattleScreen(gameState = gameState, viewModel = viewModel)
                1 -> DojoTrainingAndEvolutionScreen(gameState = gameState, viewModel = viewModel)
                2 -> BreedingAndDexScreen(gameState = gameState, viewModel = viewModel)
                3 -> QuestsCraftingAndOnlineScreen(
                    gameState = gameState,
                    openTradesState = openTradesState,
                    arenaRostersState = arenaRostersState,
                    viewModel = viewModel
                )
            }
        }
    }
}
