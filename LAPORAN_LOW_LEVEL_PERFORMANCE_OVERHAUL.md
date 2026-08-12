# Laporan Low-level Performance Overhaul

## Ringkasan

Kaiteyo telah menerima refactor low-level terfokus pada jalur yang berpotensi memengaruhi frame rendering, scroll/pagination, persistence, auth state, theme state, dan subsystem SRS. Perubahan dilakukan langsung pada branch `develop` dan tidak mengubah kontrak domain, navigation, database, atau FSRS.

> Prinsip yang dipakai: **kurangi pekerjaan yang tidak perlu, pindahkan kerja blocking dari UI thread, coalesce state yang berubah cepat, batasi invalidation, dan jangan mengklaim GPU/performance tanpa measurement.**

## Baseline dan scope

| Metrik | Baseline / hasil |
|---|---:|
| File Kotlin common/android | 517 |
| Baris source common/android | 85.379 |
| File yang diubah pada overhaul ini | 9 |
| `Dispatchers.Unconfined` di commonMain | 5 terdeteksi pada audit awal; 0 setelah refactor |
| Test final | 12 |
| Failure/error final | 0 / 0 |
| Hardware acceleration | Ditetapkan eksplisit `android:hardwareAccelerated="true"` |
| Release R8 | Sudah aktif pada konfigurasi release sebelum overhaul |
| App-specific Baseline Profile | Belum tersedia; tidak dibuat secara palsu |

## Perubahan implementasi

### Pagination dan scroll

`PaginationLoadLaunchedEffect` sebelumnya membaca `LazyListLayoutInfo` pada setiap perubahan layout lalu memetakan hasilnya ke boolean `isNearListEnd`. Ketika boolean tetap `true`, setiap perubahan scroll masih dapat memanggil `loadMore()`. Sekarang hasil threshold melewati `distinctUntilChanged()`, sehingga transisi `false → true` menjadi event pemuatan, bukan setiap frame ketika pengguna masih berada di area prefetch.

### Custom auto-padding

`ExtraListSpacerState` sebelumnya menyimpan object `LayoutCoordinates` yang mutable dan membaca ulang posisi dari flow snapshot. Implementasi baru menyimpan snapshot immutable berisi `Offset` dan `IntSize`, mengabaikan callback ketika layout detached, dan hanya menulis state jika snapshot berubah. Nilai spacer juga hanya ditulis ketika hasil spacing benar-benar berbeda. Ini mengurangi invalidation yang tidak perlu pada screen yang memakai overlay bottom.

### Navigation persistence

`NavLayoutManager` sebelumnya menggunakan `Dispatchers.Unconfined` dan membuat coroutine untuk setiap perubahan konfigurasi layout. Ini tidak ideal saat drag atau resize karena dapat menjalankan persistence inline pada caller dan menghasilkan write storm. Implementasi baru memakai `SupervisorJob + Dispatchers.IO`, `MutableStateFlow` sebagai pending snapshot, dan satu consumer yang menulis konfigurasi terbaru secara serial. DataStore write tidak dibatalkan di tengah, sementara snapshot antar-write tetap di-coalesce oleh StateFlow.

### Scope dan error isolation

Scope pada `PreferencesManager`, `NetworkClients`, `DeepLinkHandler`, `ThemeManager`, dan SRS DI kini memakai dispatcher eksplisit serta `SupervisorJob`. Satu kegagalan background child tidak lagi membatalkan listener lain secara implisit. Auth refresh juga tidak lagi memakai force-unwrap untuk `id_token`; response malformed masuk ke failure path terkontrol.

### Hardware acceleration

Manifest application sekarang mendeklarasikan `android:hardwareAccelerated="true"`. Compose tetap menggunakan renderer hardware Android pada jalur UI normal. Klaim yang lebih kuat seperti “100% semua operasi berjalan di GPU” tidak valid secara teknis, karena sebagian kerja Compose tetap berupa composition/layout dan Android dapat memilih jalur CPU untuk operasi tertentu. Target yang benar adalah mengurangi kerja CPU/layout yang tidak diperlukan dan membiarkan renderer hardware mengoptimalkan drawing.

## Validasi

| Validasi | Hasil |
|---|---|
| `git diff --check` | Lulus |
| `Dispatchers.Unconfined` scan pada commonMain | Tidak ada hasil |
| `:core:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| `FsrsSchedulerTest` | 2 test, 0 failure |
| `GrammarQuestionEngineTest` | 6 test, 0 failure |
| `SrsMicroMlEngineTest` | 4 test, 0 failure |
| Total | 12 test, 0 failure, 0 error |

Release build juga telah dimulai untuk memvalidasi jalur Kotlin release, resource processing, R8 pipeline, dan startup-profile merge. Proses dihentikan sebelum selesai ketika compiler memakai hampir seluruh RAM sandbox; ini adalah batas environment, bukan source error. Karena itu laporan ini tidak mengklaim `assembleRelease` sukses. Debug compile/test tetap selesai sukses setelah seluruh perubahan.

## Keputusan yang sengaja tidak dilakukan

Tidak ada blanket conversion semua collection menjadi `Sequence`, penghapusan semua `graphicsLayer`, penambahan `android:largeHeap`, atau pembuatan Baseline Profile fiktif. Dokumentasi Compose menekankan penggunaan `remember`, stable lazy keys, `derivedStateOf`, deferred reads, dan lambda modifiers hanya pada state yang memang berubah cepat [1] [2]. Baseline Profiles membutuhkan generator/benchmark journey yang dapat direproduksi; repository saat ini belum memiliki benchmark module yang sesuai, sehingga penambahannya dipisahkan sebagai pekerjaan pipeline release berikutnya [3].

## Referensi

[1]: https://developer.android.com/develop/ui/compose/performance/bestpractices "Compose performance best practices"

[2]: https://developer.android.com/develop/ui/compose/performance "Jetpack Compose performance"

[3]: https://developer.android.com/topic/performance/baselineprofiles/overview "Baseline Profiles overview"

[4]: https://developer.android.com/kotlin/coroutines "Kotlin coroutines on Android"
