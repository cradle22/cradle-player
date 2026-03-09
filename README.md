# Cradle Player

A simple, fast, 100% locally running native Android music player written in **Kotlin + Jetpack Compose**.

No server. No cloud. No MediaStore. Everything runs on-device using files you explicitly grant access to.

---

## Features

### Current (MVP)
- 📁 **Folder-based scanning** — pick any folder via the system file picker (Storage Access Framework), no MediaStore needed
- 🎵 **Playback** — Media3/ExoPlayer foreground service with media session and notification
- 🎨 **Embedded album art** — reads ID3/Vorbis embedded images via `MediaMetadataRetriever`
- 🔍 **Search** — debounced search by title or artist
- ▶️ **Now Playing** — album art, seek bar, ±10 s skip, previous/next
- 📋 **Queue** — view and jump to any track in the queue
- 📱 **Adaptive layout** — bottom nav on phones, side rail + two-pane on tablets / car head units
- 🌑 **Dark theme** — Material3 dynamic color, dark by default

### Planned
- Playlists (create, edit, reorder)
- Drag-to-reorder queue
- Sleep timer
- Equalizer
- Car mode / Android Auto support

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 + WindowSizeClass |
| Playback | Media3 (ExoPlayer) + MediaSessionService |
| File access | Storage Access Framework (SAF) — **no MediaStore** |
| Tag reading | MediaMetadataRetriever (built-in) |
| Database | Room |
| DI | Hilt |
| Images | Coil |
| Architecture | MVVM + StateFlow |

- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35

---

## Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 17+

### Steps
```bash
git clone https://github.com/cradle22/cradle-player.git
cd cradle-player
./gradlew assembleDebug
```

Install on a connected device / emulator:
```bash
./gradlew installDebug
```

---

## Notes

- **No MediaStore** — all file access is through SAF (`DocumentFile`). The app never queries `MediaStore.Audio` or any content provider it doesn't own.
- Permissions requested: `READ_MEDIA_AUDIO` (Android 13+) or `READ_EXTERNAL_STORAGE` (older), `FOREGROUND_SERVICE`, `WAKE_LOCK`.

---

## License

MIT — see [LICENSE](LICENSE).
