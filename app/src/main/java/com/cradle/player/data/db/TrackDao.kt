package com.cradle.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cradle.player.data.db.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks ORDER BY artist, album, trackNumber, title")
    fun getAll(): Flow<List<TrackEntity>>

    @Query(
        "SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title"
    )
    fun search(query: String): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Query("SELECT * FROM tracks WHERE folderUri = :folderUri ORDER BY artist, album, trackNumber, title")
    fun getByFolder(folderUri: String): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks WHERE folderUri = :folderUri")
    suspend fun deleteByFolder(folderUri: String)
}
