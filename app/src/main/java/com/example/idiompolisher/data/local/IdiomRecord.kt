package com.example.idiompolisher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "idiom_history")
data class IdiomRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalText: String,
    val idiom: String,
    val explanation: String,
    val toneScore: String,
    val timestamp: Long = System.currentTimeMillis()
)
