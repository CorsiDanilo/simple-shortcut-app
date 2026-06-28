package com.anomalyzed.simpleshortcut.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.anomalyzed.simpleshortcut.ShortcutDialogActivity
import com.anomalyzed.simpleshortcut.ui.icons.materialIconBitmap
import kotlinx.coroutines.flow.Flow
import android.content.Intent
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF

class ShortcutRepository(
    private val dao: ShortcutDao,
    private val context: Context
) {
    fun observeAll(): Flow<List<ShortcutEntity>> = dao.observeAll()

    suspend fun getById(id: Long): ShortcutEntity? = dao.getById(id)

    /** Insert or update a shortcut in Room. Returns the row id. */
    suspend fun upsert(shortcut: ShortcutEntity): Long = dao.upsert(shortcut)

    /** Delete a shortcut from Room and remove it from ShortcutManager if possible. */
    suspend fun delete(shortcut: ShortcutEntity) {
        dao.delete(shortcut)
        try {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcut.shortcutSystemId))
        } catch (_: Exception) { /* best-effort */ }
    }

    /**
     * Save the entity to Room (upsert) and then request pinning via ShortcutManagerCompat.
     * Returns the saved entity with its generated id.
     */
    suspend fun saveAndPin(shortcut: ShortcutEntity): ShortcutEntity {
        val savedId = dao.upsert(shortcut)
        val saved = shortcut.copy(id = savedId)
        pinShortcut(saved)
        return saved
    }

    /**
     * Re-pin an existing shortcut (e.g. from long-press "Add to home again").
     */
    fun pinShortcut(entity: ShortcutEntity) {
        val intent = Intent(context, ShortcutDialogActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("shortcut_id", entity.id)
            // Flags prevent stacking multiple instances
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val icon = runCatching {
            val bmp = materialIconBitmap(entity.iconName, context)
            IconCompat.createWithBitmap(bmp)
        }.getOrElse {
            IconCompat.createWithResource(context, android.R.drawable.ic_menu_share)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, entity.shortcutSystemId)
            .setShortLabel(entity.name)
            .setLongLabel(entity.name)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
