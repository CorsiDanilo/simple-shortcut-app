# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Room entities
-keep class com.anomalyzed.simpleshortcut.data.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep the application's entry points
-keep class com.anomalyzed.simpleshortcut.MainActivity
-keep class com.anomalyzed.simpleshortcut.ShortcutDialogActivity
-keep class com.anomalyzed.simpleshortcut.updater.DownloadReceiver
