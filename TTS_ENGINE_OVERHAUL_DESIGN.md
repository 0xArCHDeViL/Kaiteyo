# TTS Engine Overhaul Design

## Tujuan

TTS harus menangani teks Jepang bebas—hiragana, katakana, Kanji, punctuation, dan kombinasi Unicode—dengan latency rendah, footprint kecil, dan perilaku yang deterministik. Engine tidak menambahkan network call atau model suara baru pada runtime. Android platform `TextToSpeech` tetap menjadi synthesizer utama; asset clip Kana tetap dipakai karena lebih presisi dan murah untuk satu karakter.

## Architecture

| Jalur | Peran | Kebijakan |
|---|---|---|
| `AppTtsManager` | Teks bebas grammar, vocab, Kanji, Kana | Singleton platform TTS, latest request wins, locale fallback, bounded chunking |
| `KanaTtsManager` | Auto-play clip Kana yang sudah tersedia | Singleton ExoPlayer, immutable clip index, no-crash missing clip guard |
| Kanji Writing auto-read | Membaca yomikata setelah gambar selesai | Hanya `KanjiWritingData` + transisi `Completed.Idle`, prioritas kun lalu on |
| Normalizer | Membersihkan input suara | Unicode normalization, marker/speaker cleanup, whitespace/control cleanup |

## Android TTS policy

The Android API requires an instance to finish initialization before synthesis and exposes `isLanguageAvailable`, `getMaxSpeechInputLength`, `setOnUtteranceProgressListener`, `stop`, and `shutdown` [1]. The implementation therefore starts initialization once, selects the first supported locale from the requested locale and Japanese fallbacks, and never calls `speak` before readiness.

Every request is normalized and split at sentence/punctuation boundaries under the platform maximum. The first chunk uses `QUEUE_FLUSH`; following chunks use `QUEUE_ADD`. This gives a latest-wins behavior suitable for flashcard navigation without allocating an unbounded app-side queue. Utterance IDs are stable per submission and completion/error callbacks are used only for lifecycle bookkeeping. Android documents `onDone` as occurring after audible output is complete and `onStop` for an utterance flushed or stopped [2].

The TTS instance is registered as a Koin singleton because every ViewModel should share one native engine. The Android manifest declares the TTS service query required for Android 11+ discovery [1]. A release/shutdown method is retained on the platform implementation so the owner can release native resources when the app container is destroyed.

## Text normalization

The common normalizer preserves Japanese semantics while removing UI-only artifacts: speaker prefixes, `~~` formula markers, zero-width/control characters, repeated whitespace, and inline translation after Japanese sentence termination. It uses NFKC normalization for compatibility characters and never transliterates Kanji to romaji; the platform Japanese voice must receive the actual Japanese text. Punctuation is retained where it conveys prosody.

## Kanji Writing behavior

Kanji writing data already contains `on` and `kun` reading lists. Auto-read observes the writer's `CharacterWritingProgress`, not every mutable writer object. It only emits once when the review writer reaches `CharacterWritingProgress.Completed.Idle`; `Completed.Animating` is intentionally ignored because it is a reveal/replay state rather than user completion. The selected reading is the first non-empty `kun` reading, then the first non-empty `on` reading. Vocab examples are not consulted, so the feature remains Kanji Writing and not vocab learning.

The feature reuses the existing `LetterPracticeViewModel` reactive flow pattern. It is guarded against duplicate emissions when Compose recomposes or the writer state is revisited.

## Compatibility and graceful failure

Unsupported or missing locale data must not crash practice. A missing Japanese voice or TTS engine causes a no-op speech request while the visual answer remains fully usable. Missing Kana clip entries are ignored rather than passed through `Map.getValue`; the app's text TTS path remains available for general Japanese text. The engine never assumes that every installed device has the same voice package.

## References

[1]: https://developer.android.com/reference/android/speech/tts/TextToSpeech "Android TextToSpeech API reference"

[2]: https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener "Android UtteranceProgressListener API reference"
