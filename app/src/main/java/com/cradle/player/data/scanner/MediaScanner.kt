package com.cradle.player.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.cradle.player.data.db.entities.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ScanProgress {
    data class Progress(val current: Int, val total: Int) : ScanProgress()
    data class Done(val tracks: List<TrackEntity>) : ScanProgress()
}

private val AUDIO_MIME_TYPES = setOf(
    "audio/mpeg", "audio/flac", "audio/ogg", "audio/opus",
    "audio/mp4", "audio/aac", "audio/x-wav", "audio/wav"
)

private val AUDIO_EXTENSIONS = setOf(
    ".mp3", ".flac", ".ogg", ".opus", ".m4a", ".aac", ".wav"
)

private const val MAX_CONCURRENT_IO_OPERATIONS = 8

class MediaScanner @Inject constructor(
    private val context: Context
) {
    fun scan(treeUri: Uri): Flow<ScanProgress> = flow {
        val audioFiles = collectAudioFiles(treeUri)
        val total = audioFiles.size

        if (total == 0) {
            emit(ScanProgress.Done(emptyList()))
            return@flow
        }

        emit(ScanProgress.Progress(0, total))

        val semaphore = Semaphore(MAX_CONCURRENT_IO_OPERATIONS)
        val tracks = withContext(Dispatchers.IO) {
            audioFiles.map { docFile ->
                async {
                    semaphore.withPermit {
                        readTrack(docFile, treeUri.toString())
                    }
                }
            }.awaitAll().filterNotNull()
        }

        emit(ScanProgress.Done(tracks))
    }.flowOn(Dispatchers.IO)

    private fun collectAudioFiles(treeUri: Uri): List<DocumentFile> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val result = mutableListOf<DocumentFile>()
        collectAudioFilesRecursive(root, result)
        return result
    }

    private fun collectAudioFilesRecursive(dir: DocumentFile, result: MutableList<DocumentFile>) {
        for (file in dir.listFiles()) {
            when {
                file.isDirectory -> collectAudioFilesRecursive(file, result)
                isAudioFile(file) -> result.add(file)
            }
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        val mime = file.type ?: ""
        if (mime in AUDIO_MIME_TYPES) return true
        val name = file.name?.lowercase() ?: return false
        return AUDIO_EXTENSIONS.any { name.endsWith(it) }
    }

    private fun readTrack(file: DocumentFile, folderUri: String): TrackEntity? {
        val uri = file.uri
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.name?.substringBeforeLast('.') ?: uri.lastPathSegment ?: "Unknown"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.substringBefore('/')?.toIntOrNull() ?: 0
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0
            val albumArt = retriever.embeddedPicture

            TrackEntity(
                id = uri.toString(),
                uri = uri.toString(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                trackNumber = trackNumber,
                year = year,
                folderUri = folderUri,
                albumArt = albumArt
            )
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}
