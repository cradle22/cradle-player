package com.cradle.player.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cradle.player.data.db.entities.TrackEntity
import com.cradle.player.data.scanner.ScanProgress
import com.cradle.player.ui.LocalMediaController
import com.cradle.player.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val scannedFolders by viewModel.scannedFolders.collectAsStateWithLifecycle()
    val mediaController = LocalMediaController.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.scanFolder(uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Library") })

        Button(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = null)
            Text("  Add Folder", modifier = Modifier.padding(start = 4.dp))
        }

        // Scanned folders
        if (scannedFolders.isNotEmpty()) {
            Text(
                text = "${scannedFolders.size} folder(s) scanned",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Scan progress
        when (val p = scanProgress) {
            is ScanProgress.Progress -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scanning… ${p.current}/${p.total}")
                    LinearProgressIndicator(
                        progress = { if (p.total > 0) p.current.toFloat() / p.total else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> {}
        }

        Text(
            text = "${tracks.size} tracks",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        HorizontalDivider()

        LazyColumn {
            items(tracks, key = { it.id }) { track ->
                TrackItem(
                    track = track,
                    onPlayNow = {
                        mediaController?.let { mc ->
                            val items = tracks.map {
                                androidx.media3.common.MediaItem.fromUri(it.uri)
                            }
                            val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                            mc.setMediaItems(items, index, 0)
                            mc.prepare()
                            mc.play()
                        }
                    },
                    onAddToQueue = {
                        mediaController?.addMediaItem(
                            androidx.media3.common.MediaItem.fromUri(track.uri)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackItem(
    track: TrackEntity,
    onPlayNow: () -> Unit,
    onAddToQueue: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        ListItem(
            headlineContent = {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Text(
                    "${track.artist} • ${track.album}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            modifier = Modifier.combinedClickable(
                onClick = onPlayNow,
                onLongClick = { showMenu = true }
            )
        )

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Play Now") },
                onClick = { showMenu = false; onPlayNow() }
            )
            DropdownMenuItem(
                text = { Text("Add to Queue") },
                onClick = { showMenu = false; onAddToQueue() }
            )
        }
    }
}
