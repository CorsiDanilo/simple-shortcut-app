package com.anomalyzed.simpleshortcut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.anomalyzed.simpleshortcut.data.ShortcutDatabase
import com.anomalyzed.simpleshortcut.ui.picker.AppPickerDialog
import com.anomalyzed.simpleshortcut.ui.theme.SimpleShortcutTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ShortcutDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutId = intent?.getLongExtra("shortcut_id", -1L) ?: -1L
        if (shortcutId == -1L) {
            Toast.makeText(this, getString(R.string.error_shortcut_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = ShortcutDatabase.getInstance(this)

        lifecycleScope.launch {
            val entity = db.shortcutDao().getById(shortcutId)
            if (entity == null) {
                Toast.makeText(
                    this@ShortcutDialogActivity,
                    getString(R.string.error_shortcut_not_found),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            val packages = runCatching {
                Json.decodeFromString<List<String>>(entity.appPackages)
            }.getOrElse { emptyList() }

            setContent {
                SimpleShortcutTheme {
                    AppPickerDialog(
                        shortcutName = entity.name,
                        packages = packages,
                        onAppSelected = { launchIntent ->
                            startActivity(launchIntent)
                            finish()
                        },
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
}
