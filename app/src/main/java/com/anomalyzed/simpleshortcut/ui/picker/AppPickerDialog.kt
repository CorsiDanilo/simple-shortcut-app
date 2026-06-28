package com.anomalyzed.simpleshortcut.ui.picker

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anomalyzed.simpleshortcut.R

data class PickableApp(val packageName: String, val label: String, val icon: Any)

@Composable
fun AppPickerDialog(
    shortcutName: String,
    packages: List<String>,
    onAppSelected: (Intent) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Filter only installed apps that have a launch intent
    val apps = packages.mapNotNull { pkg ->
        val intent = pm.getLaunchIntentForPackage(pkg) ?: return@mapNotNull null
        val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull() ?: return@mapNotNull null
        PickableApp(
            packageName = pkg,
            label = pm.getApplicationLabel(info).toString(),
            icon = pm.getApplicationIcon(pkg)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = shortcutName,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (apps.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_apps_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        Column(
                            modifier = Modifier
                                .clickable {
                                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                                    if (launchIntent != null) onAppSelected(launchIntent)
                                }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(app.icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = app.label,
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
