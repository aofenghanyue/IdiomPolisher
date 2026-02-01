package com.example.idiompolisher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IdiomDao {
    @Query("SELECT * FROM idiom_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<IdiomRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: IdiomRecord): Long

    @Delete
    fun delete(record: IdiomRecord): Int
}
