# Kaiteyo — Android Release Process

Kaiteyo hanya didistribusikan sebagai aplikasi **Android ARM64-v8a** untuk perangkat **Android 12 atau lebih baru**. Phone menggunakan orientation portrait-only; tablet/pad mendukung portrait dan landscape dengan navigasi adaptif berbasis lebar jendela.

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

### Preview Build

Preview menggunakan build type `preview`, yang mewarisi seluruh optimasi `release`: R8/ProGuard, resource shrinking, ABI `arm64-v8a`, `minSdk 31`, dan database release yang sama. Preview memakai debug signer ephemeral dari runner sehingga tidak memerlukan GitHub secret. Ia dapat diinstal sebagai APK optimized, tetapi Android tidak menjamin update in-place ke preview lain atau release lama karena certificate signer dapat berbeda antar runner.

### Release Candidate

Release candidate dibuat setelah unit test, build release, database validation, dan device smoke test lulus. Candidate digunakan untuk QA pada phone portrait serta tablet/pad portrait dan landscape.

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

./gradlew :app:assemblePreview \
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

Instrumentation harus mencakup minimal phone portrait, tablet/pad portrait dan landscape, Letter Practice, Vocabulary Detail, Grammar Practice, database migration, dan deep link.

## Orientation Contract

| Device class | Minimum width | Orientation | Layout contract |
|---|---:|---|---|
| Android phone | `< 600dp` | Portrait locked | Single-column touch UI |
| Android tablet/pad | `≥ 600dp` | Portrait dan landscape | Hierarchy navigasi dan flow layar mobile yang sama |

Orientation ditetapkan pada Android activity berdasarkan kelas perangkat: phone dikunci portrait, sementara tablet/pad dibiarkan mengikuti orientasi perangkat. Tablet/pad tidak memiliki rail, strip, atau shell navigasi khusus; manifest dan runtime tidak boleh menyediakan jalur desktop, iOS, atau arbitrary freeform window.

## Data Pipeline

Aplikasi dapat memakai database release internal atau membangun database dari source vendored:

```bash
./gradlew :database:exportAppDatabase \
  -PappDataVersion=15 \
  --no-daemon --max-workers=1
```

Untuk database yang dibangun dari source, artifact harus diverifikasi sebagai SQLite, checksum harus direkam, lalu integrity gate harus lulus sebelum APK dibuat. Release build menggunakan `data-v15` atau release tag yang ditentukan oleh workflow.

## GitHub Actions

Workflow dispatch manual membuat artifact `preview` secara default. Setelah application data tervalidasi, unit test core dan build APK berjalan pada job terpisah secara paralel. Preview tetap memakai R8/ProGuard dan resource shrinking; pipeline tidak menurunkan optimasi untuk mengejar waktu build. Workflow tag `v*.*` memilih varian `release` dan tetap memerlukan release signer original.

1. Checkout source pada commit/tag release dan pulihkan Gradle cache.
2. Setup JDK 17 dan Android SDK.
3. Prepare atau download internal application database, lalu jalankan integrity validation.
4. Jalankan unit test core dan build `preview` atau signed `release` secara paralel.
5. Verifikasi signature APK, mapping R8, dan simpan SHA-256 certificate digest bersama artifact.
6. Upload artifact Android hanya setelah test dan verification berhasil.
7. Publish GitHub release jika tag release valid.

## Release Signing Contract

Android hanya menerima update in-place apabila `applicationId` sama, `versionCode` meningkat, dan APK baru ditandatangani oleh **certificate yang sama** dengan instalasi lama. Karena itu workflow dengan sengaja gagal sebelum membuat `assembleRelease` apabila material signing tidak lengkap; workflow tidak pernah fallback ke debug signer atau membuat keystore baru.

Simpan **keystore release original**—bukan keystore debug dan bukan keystore yang baru dibuat—sebagai repository secrets berikut:

| Secret | Nilai |
|---|---|
| `KEYSTORE_BASE64` | Base64 satu-baris dari file keystore original (`.jks`/`.keystore`) |
| `KEYSTORE_PASSWORD` | Password keystore original |
| `KEY_ALIAS` | Alias key release original |
| `KEY_PASSWORD` | Password key release original |

Generate nilai file secret secara lokal tanpa memasukkannya ke Git:

```bash
base64 -w 0 /path/ke/original-release.jks
```

Setelah secret tersedia, workflow men-decode keystore hanya ke temporary directory runner, membangun APK release dengan R8/ProGuard aktif, lalu menjalankan `apksigner --print-certs`. Bandingkan `Signer #1 certificate SHA-256 digest` dengan APK release yang sudah terpasang sebelumnya sebelum distribusi pertama setelah migrasi CI.

## Artifact Naming

```text
Kaiteyo-{version}-arm64-v8a-android.apk
Kaiteyo-{version}-arm64-v8a-android.aab
```

## Pre-Release Checklist

- [ ] `minSdk` app dan core tetap `31`.
- [ ] APK hanya berisi ABI `arm64-v8a`.
- [ ] Phone terkunci portrait.
- [ ] Tablet/pad tervalidasi pada portrait dan landscape dengan hierarchy mobile yang sama tanpa overlap atau navigasi khusus.
- [ ] Full core unit test lulus.
- [ ] Preview release-equivalent lulus dengan R8 mapping dan resource shrinking.
- [ ] Release memakai keystore original; SHA-256 certificate digest APK baru sama dengan APK release sebelumnya.
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
