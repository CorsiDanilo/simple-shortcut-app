package com.anomalyzed.simpleshortcut.ui.editor

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anomalyzed.simpleshortcut.R
import com.anomalyzed.simpleshortcut.data.ShortcutEntity
import com.anomalyzed.simpleshortcut.ui.ShortcutViewModel
import com.anomalyzed.simpleshortcut.ui.icons.PREDEFINED_ICONS
import com.anomalyzed.simpleshortcut.ui.icons.iconByName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditorScreen(
    viewModel: ShortcutViewModel,
    existingEntity: ShortcutEntity? = null,    // null → create mode
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val isEditMode = existingEntity != null

    var name by remember { mutableStateOf(existingEntity?.name ?: "") }
    var iconName by remember { mutableStateOf(existingEntity?.iconName ?: PREDEFINED_ICONS.first().name) }
    var selectedPackages by remember {
        mutableStateOf(
            existingEntity?.let {
                runCatching { Json.decodeFromString<List<String>>(it.appPackages) }.getOrElse { emptyList() }
            } ?: emptyList()
        )
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showAppSelector by remember { mutableStateOf(false) }

    // Validate
    val isValid = name.isNotBlank() && selectedPackages.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) stringResource(R.string.edit_shortcut)
                        else stringResource(R.string.create_shortcut)
                    )
                },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Name ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.shortcut_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null) }
            )

            // --- Icon ---
            Text(stringResource(R.string.icon_label), style = MaterialTheme.typography.labelLarge)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIconPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = iconByName(iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(iconName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            // --- Selected apps ---
            Text(stringResource(R.string.selected_apps), style = MaterialTheme.typography.labelLarge)
            if (selectedPackages.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    itemsIndexed(selectedPackages) { index, pkg ->
                        val label = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }.getOrElse { pkg }
                        val icon = runCatching { pm.getApplicationIcon(pkg) }.getOrNull()
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (icon != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(icon).build(),
                                        contentDescription = label,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                // Reorder Up
                                IconButton(
                                    onClick = {
                                        val mutable = selectedPackages.toMutableList()
                                        val item = mutable.removeAt(index)
                                        mutable.add(index - 1, item)
                                        selectedPackages = mutable
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move Up")
                                }
                                // Reorder Down
                                IconButton(
                                    onClick = {
                                        val mutable = selectedPackages.toMutableList()
                                        val item = mutable.removeAt(index)
                                        mutable.add(index + 1, item)
                                        selectedPackages = mutable
                                    },
                                    enabled = index < selectedPackages.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move Down")
                                }
                                // Remove
                                IconButton(
                                    onClick = { selectedPackages = selectedPackages - pkg },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showAppSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_app))
            }

            // --- Action button ---
            Button(
                onClick = {
                    if (isEditMode && existingEntity != null) {
                        viewModel.update(existingEntity, name, iconName, selectedPackages)
                    } else {
                        viewModel.saveAndPin(name, iconName, selectedPackages)
                    }
                    onBack()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (isEditMode) Icons.Filled.Save else Icons.Filled.AddToHomeScreen,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditMode) stringResource(R.string.update)
                    else stringResource(R.string.save_and_add_to_home)
                )
            }
        }

        if (showIconPicker) {
            IconPickerSheet(
                selectedIconName = iconName,
                onIconSelected = { iconName = it },
                onDismiss = { showIconPicker = false }
            )
        }

        if (showAppSelector) {
            AppSelectorSheet(
                alreadySelected = selectedPackages,
                onConfirm = { selectedPackages = it },
                onDismiss = { showAppSelector = false }
            )
        }
    }
}
