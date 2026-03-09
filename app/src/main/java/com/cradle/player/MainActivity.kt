package com.cradle.player

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cradle.player.player.MusicService
import com.cradle.player.ui.LocalMediaController
import com.cradle.player.ui.MainScreen
import com.cradle.player.ui.theme.CradlePlayerTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently; app still works via SAF */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request audio read permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            @Suppress("DEPRECATION")
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        setContent {
            CradlePlayerTheme {
                var mediaController by remember { mutableStateOf<MediaController?>(null) }

                DisposableEffect(Unit) {
                    val sessionToken = SessionToken(
                        this@MainActivity,
                        ComponentName(this@MainActivity, MusicService::class.java)
                    )
                    val future = MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
                    controllerFuture = future
                    future.addListener(
                        { mediaController = future.get() },
                        MoreExecutors.directExecutor()
                    )
                    onDispose {
                        MediaController.releaseFuture(future)
                        mediaController = null
                    }
                }

                CompositionLocalProvider(LocalMediaController provides mediaController) {
                    MainScreen()
                }
            }
        }
    }
}
