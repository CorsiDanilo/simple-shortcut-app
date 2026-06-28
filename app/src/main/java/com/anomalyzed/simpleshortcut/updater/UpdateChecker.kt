package com.anomalyzed.simpleshortcut.updater

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.anomalyzed.simpleshortcut.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val tagName: String,
    val apkUrl: String,
    val sha256Url: String
)

object UpdateChecker {

    private const val RELEASES_API =
        "https://api.github.com/repos/CorsiDanilo/simple-shortcut-app/releases/latest"

    /**
     * Checks for a newer release on GitHub.
     * Returns a human-readable status message (for display in Settings).
     */
    suspend fun check(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val json = URL(RELEASES_API).readText()
            val obj = JSONObject(json)
            val latestTag = obj.getString("tag_name").removePrefix("v")
            val current = BuildConfig.VERSION_NAME

            if (latestTag == current) {
                return@withContext context.getString(com.anomalyzed.simpleshortcut.R.string.up_to_date)
            }

            // Find APK asset
            val assets = obj.getJSONArray("assets")
            var apkUrl = ""
            var sha256Url = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                val url = asset.getString("browser_download_url")
                when {
                    name.endsWith(".apk") -> apkUrl = url
                    name.endsWith(".sha256") -> sha256Url = url
                }
            }

            if (apkUrl.isNotEmpty()) {
                downloadApk(context, UpdateInfo(latestTag, apkUrl, sha256Url))
                context.getString(com.anomalyzed.simpleshortcut.R.string.update_downloading, latestTag)
            } else {
                context.getString(com.anomalyzed.simpleshortcut.R.string.update_available_no_apk, latestTag)
            }
        } catch (e: Exception) {
            context.getString(com.anomalyzed.simpleshortcut.R.string.update_check_failed)
        }
    }

    private fun downloadApk(context: Context, info: UpdateInfo) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("Simple Shortcut v${info.tagName}")
            .setDescription("Downloading update…")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "simple-shortcut-signed.apk")
            .setMimeType("application/vnd.android.package-archive")
        dm.enqueue(request)
    }
}
