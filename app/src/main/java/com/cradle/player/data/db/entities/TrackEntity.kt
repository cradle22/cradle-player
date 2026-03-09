package com.cradle.player.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,       // content URI string
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,               // ms
    val trackNumber: Int,
    val year: Int,
    val folderUri: String,            // which scanned folder it belongs to
    val albumArt: ByteArray?          // embedded art
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
