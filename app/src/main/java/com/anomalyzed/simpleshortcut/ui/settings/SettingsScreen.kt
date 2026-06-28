package com.anomalyzed.simpleshortcut.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.anomalyzed.simpleshortcut.BuildConfig
import com.anomalyzed.simpleshortcut.R
import com.anomalyzed.simpleshortcut.updater.UpdateChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showChangelogDialog by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    // Language options: "" → system default, "it" → Italian, "en" → English
    val languageOptions = listOf(
        "" to context.getString(R.string.language_system),
        "it" to context.getString(R.string.language_italian),
        "en" to context.getString(R.string.language_english)
    )
    var selectedLang by remember {
        val current = AppCompatDelegate.getApplicationLocales()
        mutableStateOf(if (current.isEmpty) "" else current[0]?.language ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Language ---
            SettingsSectionHeader(stringResource(R.string.general))

            languageOptions.forEach { (code, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = {
                        Icon(Icons.Filled.Language, contentDescription = null)
                    },
                    trailingContent = {
                        RadioButton(
                            selected = selectedLang == code,
                            onClick = {
                                selectedLang = code
                                val localeList = if (code.isEmpty()) {
                                    LocaleListCompat.getEmptyLocaleList()
                                } else {
                                    LocaleListCompat.forLanguageTags(code)
                                }
                                AppCompatDelegate.setApplicationLocales(localeList)
                            }
                        )
                    },
                    modifier = Modifier.clickable {
                        selectedLang = code
                        val localeList = if (code.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                        else LocaleListCompat.forLanguageTags(code)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
                )
            }

            HorizontalDivider()

            // --- Updates ---
            SettingsSectionHeader(stringResource(R.string.updates))

            ListItem(
                headlineContent = { Text(stringResource(R.string.check_for_updates)) },
                leadingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                },
                modifier = Modifier.clickable(enabled = !isCheckingUpdate) {
                    scope.launch {
                        isCheckingUpdate = true
                        updateMessage = UpdateChecker.check(context)
                        isCheckingUpdate = false
                    }
                }
            )

            updateMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            HorizontalDivider()

            // --- Changelog ---
            SettingsSectionHeader(stringResource(R.string.about))

            ListItem(
                headlineContent = { Text(stringResource(R.string.changelog)) },
                leadingContent = { Icon(Icons.Filled.List, contentDescription = null) },
                modifier = Modifier.clickable { showChangelogDialog = true }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) }
            )
        }
    }

    if (showChangelogDialog) {
        ChangelogDialog(onDismiss = { showChangelogDialog = false })
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ChangelogDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val changelogText = remember {
        runCatching {
            context.assets.open("CHANGELOG.md").bufferedReader().readText()
        }.getOrElse { "No changelog available." }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.changelog)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(changelogText, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
