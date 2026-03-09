package com.cradle.player.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cradle.player.data.db.entities.TrackEntity
import com.cradle.player.data.repository.MusicRepository
import com.cradle.player.data.scanner.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    val tracks: StateFlow<List<TrackEntity>> = repository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanProgress = MutableStateFlow<ScanProgress?>(null)
    val scanProgress: StateFlow<ScanProgress?> = _scanProgress.asStateFlow()

    private val _scannedFolders = MutableStateFlow<List<Uri>>(emptyList())
    val scannedFolders: StateFlow<List<Uri>> = _scannedFolders.asStateFlow()

    fun scanFolder(treeUri: Uri) {
        if (treeUri !in _scannedFolders.value) {
            _scannedFolders.value = _scannedFolders.value + treeUri
        }
        viewModelScope.launch {
            repository.scanFolder(treeUri).collect { progress ->
                _scanProgress.value = progress
            }
        }
    }
}
