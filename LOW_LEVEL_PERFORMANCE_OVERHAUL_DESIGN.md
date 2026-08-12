# Low-level performance overhaul design

## Baseline

Kaiteyo memiliki 517 file Kotlin dan sekitar 85.379 baris common/android source. Release build sudah memakai R8 dan resource shrinking. Activity sudah mengaktifkan edge-to-edge; hardware acceleration Android tetap menjadi default dan tidak ditemukan konfigurasi yang mematikannya. Baseline Profile app-specific belum tersedia.

## Prioritized hotspots

| Priority | Hotspot | Problem | Planned correction | Measurement |
|---|---|---|---|---|
| P0 | `Paginateable.kt` | `snapshotFlow { layoutInfo }` memetakan boolean yang sama tanpa `distinctUntilChanged`, sehingga scrolling dekat akhir list dapat memicu event load-more berulang. | Deduplicate threshold transitions before invoking load-more. | Unit behavior test plus source-level count; no duplicate requests while threshold remains true. |
| P0 | `NavLayoutManager` | `Dispatchers.Unconfined`, unmanaged scope, and one persistence coroutine for every layout change. Drag/resize can create write storm and execute DataStore work on caller thread. | Use a supervised background scope and conflated update channel; persist only the latest layout snapshot. | Compile/test, persistence behavior test, and source audit that no Unconfined remains in this path. |
| P1 | Compose state reads | Frequent state is read high in the tree and some animations use graphics layers. | Apply lambda modifiers/derived state only where a measured hotspot is found; do not blanket-rewrite stable UI. | Compose compiler/recomposition evidence where available. |
| P1 | Startup and critical journeys | No app-specific Baseline Profile source exists. | Add only if the repository has compatible benchmark/test module and can generate it reproducibly; otherwise document as next release pipeline task rather than ship unverifiable rules. | Release/profile task availability. |
| P2 | Broad code smells | Many `!!`, ad-hoc scopes, and large feature files exist, but broad syntax rewrites risk behavior regressions. | Refactor only code on hot paths or ownership boundaries; preserve contracts. | Regression suite and diff review. |

## Non-goals

No blanket conversion of every collection to sequences, no forced use of `graphicsLayer`, no `android:largeHeap`, no fake Baseline Profile rules, and no claim of 100% GPU usage. GPU acceleration is controlled by Android/Compose rendering; the correct goal is to avoid unnecessary work and verify frame behavior in release builds.

## References

[1]: https://developer.android.com/develop/ui/compose/performance/bestpractices "Compose performance best practices"

[2]: https://developer.android.com/develop/ui/compose/performance "Jetpack Compose performance"

[3]: https://developer.android.com/topic/performance/baselineprofiles/overview "Baseline Profiles overview"

[4]: https://developer.android.com/kotlin/coroutines "Kotlin coroutines on Android"
