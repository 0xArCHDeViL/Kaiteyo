# Low-level performance guidance

Referensi resmi yang dipakai untuk audit Kaiteyo:

- Compose performance: cache expensive calculations with `remember`, use stable lazy keys, `derivedStateOf` for rapidly changing state, defer reads, use lambda modifiers for frequently changing values, and avoid backwards writes.
- Compose performance overview: validate performance in release/R8 builds and use app-specific Baseline Profiles for startup and critical journeys.
- Baseline Profiles: include startup, navigation, scrolling, and other critical flows; profiles let ART precompile hot code paths.
- Coroutines on Android: keep long-running/blocking work off the UI thread, use structured scopes/cancellation, and make repository functions main-safe with dispatcher boundaries.

References:

[1]: https://developer.android.com/develop/ui/compose/performance/bestpractices
[2]: https://developer.android.com/develop/ui/compose/performance
[3]: https://developer.android.com/topic/performance/baselineprofiles/overview
[4]: https://developer.android.com/kotlin/coroutines
