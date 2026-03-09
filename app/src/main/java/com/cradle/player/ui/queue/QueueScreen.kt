package com.cradle.player.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.cradle.player.ui.LocalMediaController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen() {
    val mediaController = LocalMediaController.current

    var queueItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(-1) }

    // Poll the media controller to get up-to-date queue state
    LaunchedEffect(mediaController) {
        while (true) {
            queueItems = buildList {
                if (mediaController != null) {
                    for (i in 0 until mediaController.mediaItemCount) {
                        add(mediaController.getMediaItemAt(i))
                    }
                }
            }
            currentIndex = mediaController?.currentMediaItemIndex ?: -1
            delay(500)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Queue (${queueItems.size} tracks)") }
        )

        Button(
            onClick = { mediaController?.clearMediaItems() },
            modifier = Modifier.padding(16.dp),
            enabled = queueItems.isNotEmpty()
        ) {
            Text("Clear Queue")
        }

        HorizontalDivider()

        LazyColumn {
            itemsIndexed(queueItems) { index, item ->
                val isCurrentTrack = index == currentIndex
                ListItem(
                    headlineContent = {
                        Text(
                            item.mediaMetadata.title?.toString()
                                ?: item.localConfiguration?.uri?.lastPathSegment
                                ?: "Track ${index + 1}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isCurrentTrack) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingContent = if (isCurrentTrack) {
                        {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Now playing",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    modifier = Modifier
                        .clickable {
                            mediaController?.seekToDefaultPosition(index)
                            mediaController?.play()
                        }
                        .then(
                            if (isCurrentTrack)
                                Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            else Modifier
                        )
                )
            }
        }
    }
}
