package com.anomalyzed.simpleshortcut.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,         // Material icon name, e.g. "Bolt", "Star", "Home"
    val shortcutSystemId: String, // ID used by ShortcutManager, e.g. "shortcut_3"
    val appPackages: String,      // JSON array of package names: ["com.spotify.music", ...]
    val createdAt: Long = System.currentTimeMillis()
)
