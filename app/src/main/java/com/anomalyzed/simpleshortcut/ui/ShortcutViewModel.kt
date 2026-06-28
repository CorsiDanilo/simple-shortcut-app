package com.anomalyzed.simpleshortcut.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anomalyzed.simpleshortcut.data.ShortcutDatabase
import com.anomalyzed.simpleshortcut.data.ShortcutEntity
import com.anomalyzed.simpleshortcut.data.ShortcutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ShortcutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortcutRepository by lazy {
        val db = ShortcutDatabase.getInstance(application)
        ShortcutRepository(db.shortcutDao(), application)
    }

    val shortcuts: StateFlow<List<ShortcutEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Create a new shortcut: save to Room and request pin. */
    fun saveAndPin(name: String, iconName: String, packages: List<String>) {
        viewModelScope.launch {
            val systemId = "shortcut_${System.currentTimeMillis()}"
            val entity = ShortcutEntity(
                name = name,
                iconName = iconName,
                shortcutSystemId = systemId,
                appPackages = Json.encodeToString(packages)
            )
            repository.saveAndPin(entity)
        }
    }

    /** Update an existing shortcut in Room (no re-pin needed — dialog reloads from DB). */
    fun update(existing: ShortcutEntity, name: String, iconName: String, packages: List<String>) {
        viewModelScope.launch {
            val updated = existing.copy(
                name = name,
                iconName = iconName,
                appPackages = Json.encodeToString(packages)
            )
            repository.upsert(updated)
        }
    }

    /** Delete a shortcut from Room (and best-effort from ShortcutManager). */
    fun delete(entity: ShortcutEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }

    /** Re-pin an existing shortcut to the home screen. */
    fun repin(entity: ShortcutEntity) {
        repository.pinShortcut(entity)
    }

    /** Load a single shortcut by id (for ShortcutDialogActivity). */
    suspend fun getById(id: Long): ShortcutEntity? = repository.getById(id)
}
