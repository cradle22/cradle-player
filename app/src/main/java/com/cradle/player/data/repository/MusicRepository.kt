package com.cradle.player.data.repository

import android.content.Context
import android.net.Uri
import com.cradle.player.data.db.PlaylistDao
import com.cradle.player.data.db.TrackDao
import com.cradle.player.data.db.entities.PlaylistEntity
import com.cradle.player.data.db.entities.TrackEntity
import com.cradle.player.data.scanner.MediaScanner
import com.cradle.player.data.scanner.ScanProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val scanner: MediaScanner
) {
    fun scanFolder(treeUri: Uri): Flow<ScanProgress> =
        scanner.scan(treeUri).onEach { progress ->
            if (progress is ScanProgress.Done) {
                trackDao.deleteByFolder(treeUri.toString())
                trackDao.insertAll(progress.tracks)
            }
        }

    fun getAllTracks(): Flow<List<TrackEntity>> = trackDao.getAll()

    fun search(query: String): Flow<List<TrackEntity>> = trackDao.search(query)

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun insertPlaylist(playlist: PlaylistEntity) = playlistDao.insertPlaylist(playlist)

    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)
}
