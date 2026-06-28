package com.anomalyzed.simpleshortcut.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {

    @Query("SELECT * FROM shortcuts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE id = :id")
    suspend fun getById(id: Long): ShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shortcut: ShortcutEntity): Long

    @Delete
    suspend fun delete(shortcut: ShortcutEntity)
}
