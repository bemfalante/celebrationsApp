package com.celebrations.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tributes")
data class Tribute(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("date")
    val date: Long = 0,

    @SerializedName("type")
    val type: String = "", // "image", "video", "audio"

    @SerializedName("file_url")
    val fileUrl: String = "",

    val localPath: String? = null,
    val isDownloaded: Boolean = false
)
