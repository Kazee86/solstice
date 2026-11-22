# Solstice 🎵

Pemutar musik modern berbasis **Kotlin Multiplatform (KMP)** & **Compose Multiplatform** untuk **Windows (Desktop)** dan **Android**. Mendukung pemutaran file lokal offline dan streaming online (YouTube Music) dengan tampilan Liquid Glass editorial serta lirik sinkron otomatis.

---

## Fitur Utama

- **Offline Local Playback**:
  - **Android**: Otomatis scan penyimpanan perangkat via native `MediaStore` API.
  - **Windows**: Pilih folder musik apa saja, scan otomatis subfolder, dan baca tag ID3 (Judul, Artis, Album, Cover Art) menggunakan `jaudiotagger`.
  - Format didukung: MP3, FLAC, M4A, WAV, OGG.
- **Online Streaming**:
  - Cari dan streaming lagu langsung dari katalog YouTube Music via lightweight InnerTube API client.
- **Synced Lyrics**:
  - Lirik sinkron otomatis baris demi baris via [LRCLIB](https://lrclib.net/) API.
  - Klik baris lirik untuk melompat langsung ke detik lagu tersebut.
- **Modern UI & Liquid Glass**:
  - Material 3 Dark Theme dengan aksen Indigo & container Glassmorphism transparan.
  - Persistent mini-player bar di bawah dan expandable fullscreen player.
- **Android Background Service**:
  - Menggunakan `androidx.media3` (ExoPlayer + `MediaSessionService`) agar lagu tetap menyala di latar belakang, terintegrasi di lockscreen & notifikasi kontrol sistem.

---

## Struktur Modul

```text
solstice/
├── .github/workflows/
│   └── build-and-release.yml   # CI/CD Pipeline (Build Windows MSI & Android APK)
├── composeApp/
│   └── src/
│       ├── commonMain/         # Shared UI, ViewModel, Model, Network
│       ├── androidMain/        # Android Activity, MediaStore Scanner, PlaybackService
│       └── jvmMain/            # Desktop Window, File Scanner, Audio Engine
├── gradle/
│   └── libs.versions.toml      # Dependency Version Catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Cara Menjalankan di Lokal

### Prasyarat
- **JDK 17 atau 21**
- **Android Studio** (untuk menjalankan target Android di emulator/HP)

### 1. Jalankan Desktop (Windows / Linux)
```bash
./gradlew :composeApp:run
```

### 2. Jalankan Android (Emulator / Perangkat Terhubung)
```bash
./gradlew :composeApp:installDebug
```

---

## Cara Build & Deploy Aplikasi

Proyek ini telah dikonfigurasi dengan **GitHub Actions** untuk membangun installer secara otomatis di cloud tanpa perlu setup manual WiX Toolset atau SDK di komputer lokal:

### 1. Build Otomatis via Git Tag (Rilis Baru)
Untuk merilis versi baru, cukup buat tag versi dan push ke GitHub:
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions akan secara otomatis:
1. Menjalankan runner Linux untuk mengompilasi **Android APK Release**.
2. Menjalankan runner Windows untuk mengemas **Windows Installer (.msi & .exe)**.
3. Membuat **GitHub Release** baru dan mengunggah installer tersebut ke tab Releases.

### 2. Build Manual lewat Web GitHub
1. Masuk ke halaman repo di GitHub.
2. Buka tab **Actions** > Pilih workflow **Build & Release Solstice**.
3. Klik tombol **Run workflow** > Jalankan pada branch `main`.
4. Setelah selesai, unduh file `.apk` dan `.msi` dari bagian **Artifacts**.

---

## Lisensi
Proyek ini dibuat untuk tujuan edukasi dan penggunaan pribadi.
