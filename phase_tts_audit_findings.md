# TTS Audit Findings

## Baseline

Repository memakai dua jalur suara Android: `AndroidAppTtsManager` berbasis platform `TextToSpeech` untuk teks bebas/grammar/vocab, serta `AndroidKanaTtsManager` berbasis satu asset suara Neural2B + ExoPlayer clipping untuk Kana. `AppTtsManager` hanya mengekspos `suspend fun speak(text, language)` dan `stop()`.

## Defect utama AndroidAppTtsManager

`TextToSpeech` diinisialisasi lazy dengan satu `CompletableDeferred<Boolean>`, tetapi readiness belum aman terhadap lifecycle shutdown, init failure berulang, atau concurrent speak. Setiap `speak` langsung melakukan `QUEUE_FLUSH`, sehingga rapid calls saling membatalkan dan membuat UX tidak deterministik. Locale dipasang pada setiap call tanpa capability check, fallback locale, voice selection, atau rate/pitch policy. Tidak ada `UtteranceProgressListener`, tidak ada dedupe untuk teks sama, tidak ada length/chunk policy untuk input panjang, dan tidak ada `shutdown()`.

## Defect utama Kana audio

`AndroidKanaTtsManager` terdaftar sebagai factory dan ExoPlayer juga factory, sehingga player dapat dibuat berulang tanpa ownership/lifecycle yang jelas. Map romaji-to-media-item dibangun lazy per instance. Playback berpindah ke Main dispatcher, tetapi tidak ada `stop`, cancellation policy, atau guard ketika reading tidak ada pada map. Ini bukan general Japanese TTS dan tidak mencakup Kanji.

## Kanji Writing flow

Kanji Writing tersedia sebagai `LetterPracticeReviewState.Writing` dengan `LetterPracticeItemData.KanjiWritingData`; data yomikata sudah tersedia sebagai `on: List<String>` dan `kun: List<String>`. `CharacterWritingProgress.Completed.Idle` adalah completion state yang tepat untuk gambar selesai; `Completed.Animating` adalah replay/reveal dan tidak boleh memicu auto speech. Saat ini `LetterPracticeViewModel.kanaAutoReadFlow()` hanya menangani KanaWritingData dan KanaReadingData. Kanji writing belum memiliki auto-read yomikata.

## Keputusan desain awal

Engine baru harus mempertahankan `AppTtsManager` sebagai contract kompatibilitas, tetapi implementasi Android perlu single-flight queue, readiness state, locale fallback, Japanese text normalization, bounded chunking, dedupe/cancellation yang eksplisit, dan lifecycle shutdown. Auto-yomikata harus hanya aktif untuk `KanjiWritingData`, memakai reading pertama yang valid dengan prioritas `kun` lalu `on`, dan dipicu sekali pada transisi ke `Completed.Idle` setelah user selesai menggambar. Ini bukan vocab learning dan tidak memakai contoh kata.
