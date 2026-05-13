package com.celebrations.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tributes")
data class Tribute(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val date: Long = 0,
    val type: String = "", // "image", "video", "audio"
    val fileUrl: String = "",
    val localPath: String? = null,
    val isDownloaded: Boolean = false
)
