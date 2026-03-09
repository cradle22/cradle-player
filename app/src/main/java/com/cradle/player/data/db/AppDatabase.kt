package com.cradle.player.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.cradle.player.data.db.entities.PlaylistEntity
import com.cradle.player.data.db.entities.TrackEntity

class Converters {
    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? =
        value?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? =
        value?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
}

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}
