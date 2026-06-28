package com.anomalyzed.simpleshortcut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anomalyzed.simpleshortcut.data.ShortcutDatabase
import com.anomalyzed.simpleshortcut.data.ShortcutRepository
import com.anomalyzed.simpleshortcut.ui.ShortcutViewModel
import com.anomalyzed.simpleshortcut.ui.editor.ShortcutEditorScreen
import com.anomalyzed.simpleshortcut.ui.home.HomeScreen
import com.anomalyzed.simpleshortcut.ui.settings.SettingsScreen
import com.anomalyzed.simpleshortcut.ui.theme.SimpleShortcutTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ShortcutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleShortcutTheme {
                AppNavGraph(viewModel)
            }
        }
    }
}

@Composable
private fun AppNavGraph(viewModel: ShortcutViewModel) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreate = { navController.navigate("editor") },
                onNavigateToEdit = { id -> navController.navigate("editor/$id") }
            )
        }

        composable("editor") {
            ShortcutEditorScreen(
                viewModel = viewModel,
                existingEntity = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable("editor/{shortcutId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("shortcutId")?.toLongOrNull() ?: -1L
            var entity by remember { mutableStateOf<com.anomalyzed.simpleshortcut.data.ShortcutEntity?>(null) }
            var loaded by remember { mutableStateOf(false) }

            LaunchedEffect(id) {
                entity = viewModel.getById(id)
                loaded = true
            }

            if (loaded) {
                ShortcutEditorScreen(
                    viewModel = viewModel,
                    existingEntity = entity,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
