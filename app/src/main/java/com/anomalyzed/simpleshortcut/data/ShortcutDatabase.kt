package com.anomalyzed.simpleshortcut.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromJson(value: String): List<String> =
        Json.decodeFromString(value)

    @TypeConverter
    fun toJson(value: List<String>): String =
        Json.encodeToString(value)
}

@Database(entities = [ShortcutEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ShortcutDatabase : RoomDatabase() {

    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: ShortcutDatabase? = null

        fun getInstance(context: Context): ShortcutDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShortcutDatabase::class.java,
                    "shortcuts.db"
                ).build().also { INSTANCE = it }
            }
    }
}
