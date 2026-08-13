# Kaiteyo — Android Release Process

Kaiteyo hanya didistribusikan sebagai aplikasi **Android ARM64-v8a** untuk perangkat **Android 12 atau lebih baru**. Phone menggunakan orientation portrait-only; tablet/pad menggunakan landscape-only shell.

## Versioning

Kaiteyo follows [Semantic Versioning](https://semver.org/). Version metadata is managed in `buildSrc/src/main/kotlin/AppVersion.kt`:

```kotlin
object AppVersion {
    const val versionCode = 2210
    const val versionName = "2.2.1"
}
```

`versionCode` harus meningkat untuk setiap artifact yang didistribusikan. `versionName` mengikuti format SemVer.

## Release Types

### Development Build

Development builds berasal dari branch `develop`, digunakan untuk validasi internal, dan tidak dianggap release publik.

### Release Candidate

Release candidate dibuat setelah unit test, build release, database validation, dan device smoke test lulus. Candidate digunakan untuk QA pada phone portrait dan tablet landscape.

### Stable Release

Stable release dibuat dari tag `v{version}` dan menghasilkan APK/AAB ARM64-v8a Android.

## Required Validation

Sebelum release, jalankan validasi berikut:

```bash
./gradlew :core:testDebugUnitTest \
  -PappDataSource=release \
  -PappDataVersion=15 \
  -PappDataReleaseTag=data-v15 \
  --no-daemon --max-workers=1

./gradlew :app:assembleDebug \
  -PappDataSource=release \
  -PappDataVersion=15 \
  -PappDataReleaseTag=data-v15 \
  --no-daemon --max-workers=1

./gradlew :app:assembleRelease \
  -PappDataSource=release \
  -PappDataVersion=15 \
  -PappDataReleaseTag=data-v15 \
  --no-daemon --max-workers=1
```

Jika device atau emulator tersedia, lanjutkan dengan:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -PappDataSource=release \
  -PappDataVersion=15 \
  -PappDataReleaseTag=data-v15 \
  --no-daemon --max-workers=1
```

Instrumentation harus mencakup minimal phone portrait, tablet landscape, Letter Practice, Vocabulary Detail, Grammar Practice, database migration, dan deep link.

## Orientation Contract

| Device class | Minimum width | Orientation | Layout contract |
|---|---:|---|---|
| Android phone | `< 600dp` | Portrait locked | Single-column touch UI |
| Android tablet/pad | `≥ 600dp` | Landscape locked | Dedicated rail + content shell |

Orientation ditetapkan pada Android activity berdasarkan `smallestScreenWidthDp`. Manifest dan runtime tidak boleh menyediakan jalur desktop, iOS, atau arbitrary freeform window.

## Data Pipeline

Aplikasi dapat memakai database release internal atau membangun database dari source vendored:

```bash
./gradlew :database:exportAppDatabase \
  -PappDataVersion=15 \
  --no-daemon --max-workers=1
```

Untuk database yang dibangun dari source, artifact harus diverifikasi sebagai SQLite, checksum harus direkam, lalu integrity gate harus lulus sebelum APK dibuat. Release build menggunakan `data-v15` atau release tag yang ditentukan oleh workflow.

## GitHub Actions

Workflow release hanya mengunggah artifact Android. Workflow harus menjalankan langkah berikut secara berurutan:

1. Checkout source pada commit/tag release.
2. Setup JDK 17 dan Android SDK.
3. Prepare atau download internal application database.
4. Jalankan integrity validation.
5. Jalankan unit test core.
6. Build APK/AAB dengan ABI `arm64-v8a` dan `minSdk 31`.
7. Upload artifact Android.
8. Publish GitHub release jika tag release valid.

## Artifact Naming

```text
Kaiteyo-{version}-arm64-v8a-android.apk
Kaiteyo-{version}-arm64-v8a-android.aab
```

## Pre-Release Checklist

- [ ] `minSdk` app dan core tetap `31`.
- [ ] APK hanya berisi ABI `arm64-v8a`.
- [ ] Phone terkunci portrait.
- [ ] Tablet/pad terkunci landscape.
- [ ] Dedicated tablet shell merender rail dan content tanpa desktop drag/resize overlay.
- [ ] Full core unit test lulus.
- [ ] Android debug dan release build lulus.
- [ ] Instrumentation phone dan tablet lulus jika device tersedia.
- [ ] Application database lolos integrity check dan checksum validation.
- [ ] Changelog dan migration notes diperbarui.
- [ ] Tidak ada artifact desktop, iOS, atau web pada release output.

## Distribution

| Channel | Artifact | Target |
|---|---|---|
| Google Play | AAB | Android 12+, ARM64-v8a |
| GitHub Releases | APK | Android 12+, ARM64-v8a |
| F-Droid | APK/build metadata | Android 12+, ARM64-v8a |

## Hotfix Process

Untuk crash produksi atau kerusakan data:

1. Reproduksi dengan stack trace atau fixture yang dapat diverifikasi.
2. Perbaiki akar masalah di branch `develop` sesuai repository workflow.
3. Tambahkan regression test.
4. Jalankan unit test dan Android build.
5. Jalankan device smoke test jika device tersedia.
6. Push commit hotfix dan catat hash pada changelog.
7. Tag patch release bila perubahan didistribusikan.
