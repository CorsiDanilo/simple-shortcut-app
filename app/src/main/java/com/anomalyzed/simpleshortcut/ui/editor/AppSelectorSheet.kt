package com.anomalyzed.simpleshortcut.ui.editor

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anomalyzed.simpleshortcut.R

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Any // Drawable
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorSheet(
    selectedPackages: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Load installed apps once
    val allApps: List<InstalledApp> = remember {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    label = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info.packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    var query by remember { mutableStateOf("") }

    val filtered = remember(query, allApps) {
        if (query.isBlank()) allApps
        else allApps.filter { it.label.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            // App list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val isChecked = app.packageName in selectedPackages
                    ListItem(
                        headlineContent = { Text(app.label) },
                        leadingContent = {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(app.icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = app.label,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { _ -> onToggle(app.packageName) }
                            )
                        },
                        modifier = Modifier.clickable { onToggle(app.packageName) }
                    )
                    HorizontalDivider()
                }
            }

            // Bottom actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
