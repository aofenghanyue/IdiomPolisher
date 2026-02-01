package com.example.idiompolisher.data

import com.google.gson.annotations.SerializedName

/**
 * API Response Model based on thought.md
 */
data class IdiomResponse(
    @SerializedName("original") val original: String,
    @SerializedName("idiom") val idiom: String,
    @SerializedName("alternatives") val alternatives: List<String>,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("tone_score") val toneScore: String // Keeping as String as per example, could be Int/Float
)