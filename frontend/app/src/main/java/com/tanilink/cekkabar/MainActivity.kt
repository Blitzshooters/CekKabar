package com.tanilink.cekkabar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tanilink.cekkabar.ui.screens.AnalisisScreen
import com.tanilink.cekkabar.ui.screens.DeteksiScreen
import com.tanilink.cekkabar.ui.screens.EvaluasiScreen
import com.tanilink.cekkabar.ui.screens.HasilDeteksiScreen
import com.tanilink.cekkabar.ui.screens.RiwayatScreen
import com.tanilink.cekkabar.ui.screens.WordCloudScreen
import com.tanilink.cekkabar.ui.theme.BackgroundLight
import com.tanilink.cekkabar.ui.theme.CekKabarTheme
import com.tanilink.cekkabar.ui.theme.IndigoPrimary
import com.tanilink.cekkabar.ui.theme.OnSurfaceVariant
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceWhite
import com.tanilink.cekkabar.ui.viewmodel.CekKabarViewModel

enum class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    DETEKSI("deteksi", "Deteksi", Icons.Rounded.VerifiedUser),
    ANALISIS("analisis", "Analisis", Icons.Rounded.Insights),
    WORDCLOUD("wordcloud", "WordCloud", Icons.Rounded.Cloud),
    EVALUASI("evaluasi", "Evaluasi", Icons.Rounded.Assessment),
    RIWAYAT("riwayat", "Riwayat", Icons.Rounded.History)
}

class MainActivity : ComponentActivity() {

    private var requestedTab by mutableStateOf<String?>(null)
    private var autoPasteRequestId by mutableStateOf(0)
    private var requestedHistoryId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleWidgetIntent(intent)
        enableEdgeToEdge()
        val app = application as CekKabarApp
        setContent {
            CekKabarTheme {
                val vm: CekKabarViewModel = viewModel(
                    factory = CekKabarViewModel.provideFactory(app.container.repository)
                )
                CekKabarRoot(
                    viewModel = vm,
                    requestedTab = requestedTab,
                    autoPasteRequestId = autoPasteRequestId,
                    requestedHistoryId = requestedHistoryId,
                    onConsumeRequestedTab = { requestedTab = null },
                    onConsumeHistoryId = { requestedHistoryId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent == null) return
        val historyId = intent.getLongExtra(
            com.tanilink.cekkabar.widget.CekKabarWidgetProvider.EXTRA_HISTORY_ID,
            -1L
        )
        if (historyId > 0L) {
            requestedHistoryId = historyId
            return
        }
        requestedTab = intent.getStringExtra(
            com.tanilink.cekkabar.widget.CekKabarWidgetProvider.EXTRA_START_TAB
        )
        if (intent.getBooleanExtra(
                com.tanilink.cekkabar.widget.CekKabarWidgetProvider.EXTRA_AUTO_PASTE,
                false
            )
        ) {
            autoPasteRequestId++
        }
    }
}

@Composable
fun CekKabarRoot(
    viewModel: CekKabarViewModel,
    requestedTab: String? = null,
    autoPasteRequestId: Int = 0,
    requestedHistoryId: Long? = null,
    onConsumeRequestedTab: () -> Unit = {},
    onConsumeHistoryId: () -> Unit = {}
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(requestedTab) {
        if (!requestedTab.isNullOrBlank()) {
            val targetRoute = BottomNavTab.entries.find { it.route == requestedTab }?.route
            if (targetRoute != null) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            onConsumeRequestedTab()
        }
    }

    LaunchedEffect(autoPasteRequestId) {
        if (autoPasteRequestId > 0) {
            val pasted = clipboardManager.getText()?.text?.trim().orEmpty()
            if (pasted.isNotEmpty()) {
                viewModel.onInputTextChanged(pasted)
                if (pasted.length >= 10) {
                    viewModel.detectNews(textOverride = pasted) {
                        navController.navigate("hasil")
                    }
                }
            }
        }
    }

    LaunchedEffect(requestedHistoryId) {
        val id = requestedHistoryId
        if (id != null && id > 0L) {
            viewModel.openHistoryDetail(id) {
                navController.navigate("hasil")
            }
            onConsumeHistoryId()
        }
    }

    val showBottomBar = currentRoute != "hasil"

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SurfaceWhite) {
                    BottomNavTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                indicatorColor = PrimaryContainer,
                                unselectedIconColor = OnSurfaceVariant,
                                unselectedTextColor = OnSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.DETEKSI.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavTab.DETEKSI.route) {
                DeteksiScreen(
                    uiState = uiState,
                    onInputChanged = viewModel::onInputTextChanged,
                    onClearInput = viewModel::clearInput,
                    onDetectClick = {
                        viewModel.detectNews {
                            navController.navigate("hasil")
                        }
                    },
                    onExampleClick = { example ->
                        viewModel.detectNews(textOverride = example.text) {
                            navController.navigate("hasil")
                        }
                    }
                )
            }

            composable("hasil") {
                val res = uiState.latestResult
                if (res != null) {
                    HasilDeteksiScreen(
                        result = res,
                        onBackClick = { navController.popBackStack() },
                        onCheckAgainClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(BottomNavTab.ANALISIS.route) {
                AnalisisScreen(
                    uiState = uiState,
                    onToggleMode = viewModel::setAnalysisModeGlobal,
                    onGoToDeteksi = {
                        navController.navigate(BottomNavTab.DETEKSI.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(BottomNavTab.WORDCLOUD.route) {
                WordCloudScreen(
                    uiState = uiState,
                    onSwitchCorpus = viewModel::switchWordCloudCorpus,
                    onQueryChanged = viewModel::onWordCloudQueryChanged,
                    onRetry = { viewModel.loadWordCloud() }
                )
            }

            composable(BottomNavTab.EVALUASI.route) {
                EvaluasiScreen(
                    uiState = uiState,
                    onRetry = viewModel::loadEvaluation
                )
            }

            composable(BottomNavTab.RIWAYAT.route) {
                RiwayatScreen(
                    uiState = uiState,
                    onFilterSelected = viewModel::setHistoryFilter,
                    onQueryChanged = viewModel::onHistoryQueryChanged,
                    onHistoryClick = { item ->
                        viewModel.openHistoryDetail(item.id) {
                            navController.navigate("hasil")
                        }
                    },
                    onDeleteClick = viewModel::deleteHistory,
                    onClearAllClick = viewModel::clearAllHistories
                )
            }
        }
    }
}
