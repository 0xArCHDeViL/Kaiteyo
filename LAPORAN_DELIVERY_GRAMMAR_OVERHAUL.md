# Laporan Delivery — Grammar Learning Overhaul

## Ringkasan

Grammar learning Kaiteyo telah di-overhaul pada lapisan content, generator soal, validator, UI, feedback, dan accessibility. Perubahan mempertahankan kontrak queue, SRS, navigation, dan database. Tidak ada network call pada runtime practice. Generator baru bersifat **offline, deterministic, seeded, dan dapat diuji ulang**.

Masalah utama sebelumnya adalah setiap use case membaca dan decode `bunpou_data.json` sendiri, mengambil contoh pertama secara naif, membuat distractor dari pool global, memakai `random` global, menyusun conjugation dari daftar kecil yang hardcoded, memecah scramble dengan `split(" ")`, serta menampilkan marker formula `~~...~~` secara tidak konsisten. Masalah-masalah tersebut kini dipusatkan pada repository dan engine bersama.

## Baseline audit

| Metrik content grammar | Nilai |
|---|---:|
| Chapter | 10 |
| Grammar point | 161 |
| Contoh | 348 |
| Point tanpa contoh | 1 |
| Formula dengan marker strike | 56 |
| Formula dengan marker tidak seimbang | 0 |
| Contoh dengan newline | 270 |
| Contoh dengan segment Jepang ber-spasi | 309 |
| Contoh tanpa pemisahan Jepang/Indonesia yang jelas | 21 |

Angka tersebut berasal dari audit `phase_grammar_data_metrics.json`. Temuan pentingnya adalah format data memang heterogen; karena itu generator tidak boleh mengandalkan `examples.firstOrNull()`, delimiter titik Jepang, atau string replacement tunggal.

## Perubahan implementasi

| Area | Perubahan |
|---|---|
| Content loading | `DefaultGrammarContentRepository` melakukan decode resource sekali, cache dengan `Mutex`, dan menyediakan lookup chapter/point terpusat. Library dan practice memakai repository yang sama. |
| Formula markup | `GrammarMarkup` mengubah `~~...~~` menjadi segment semantic. `FormulaText` memakai `TextDecoration.LineThrough`, bukan menampilkan marker raw. TTS memakai plain Japanese text tanpa `~`, `~~`, atau label speaker. |
| Question engine | `GrammarQuestionEngine` menghasilkan cloze, conjugation, scramble, dan dialogue dari point/example yang benar-benar tersedia. Seed berasal dari deck, point, dan mode sehingga hasil reproducible. |
| Cloze | Target dicari dari particle family atau segment strike yang benar-benar muncul pada token sentence. Distractor berasal dari family yang relevan dan harus unik. Fallback global `は` dihapus. |
| Conjugation | Verb lexicon diperluas dan inflection menangani ichidan, godan, `する`, `くる`, serta bentuk `ます`, `ない`, `て`, `た`, `たら`, dan `たり`. Pemilihan verb seeded, bukan `random()` global. |
| Scramble | Tokenisasi mempertahankan duplicate token sebagai identity index. User dapat menghapus token satu per satu; tidak ada lagi operasi `list - part` yang menghapus semua token identik. |
| Dialogue | Source utterance diambil dari contoh point yang nyata. Mode hanya ditawarkan bila tersedia minimal dua utterance yang dapat membentuk dialog. Mock response generik dihapus. |
| Applicability | Library hanya menambahkan mode jika engine menyatakan mode tersebut applicable. Point yang tidak cocok tidak dipaksa menjadi soal invalid. |
| Quality gate | `GrammarQuestionValidator` menolak jawaban kosong, opsi duplikat, option count salah, target yang tidak ada, dan struktur sentence/dialogue yang tidak valid. |
| UX | Feedback disatukan melalui `GrammarPracticeFeedback`, memakai copy Indonesia, expected answer saat salah, audio action, dan CTA `Lanjut`. Progress header menampilkan jumlah soal tersisa. |

## Validasi eksternal

Tidak ada API publik gratis yang terverifikasi layak dijadikan authority grammar Jepang pada runtime. Halaman resmi LanguageTool menyebut dukungan lebih dari 30 bahasa, tetapi daftar bahasa publiknya tidak mencantumkan Japanese dan API menggunakan kuota/akses komersial [1]. JMdict/EDICT adalah kamus leksikal Jepang, bukan grammar checker atau validator JLPT [2].

Keputusan yang dipakai adalah **offline validator sebagai authority**. Abstraction point untuk external validator tetap dapat ditambahkan pada authoring/CI di masa depan, tetapi hasil network tidak boleh menentukan correctness ketika user sedang belajar. Ini menjaga privacy, offline behavior, determinism, dan reproducibility.

## Bukti validasi teknis

| Validasi | Hasil |
|---|---|
| `git diff --check` | Lulus |
| `:core:compileDebugKotlinAndroid` | `BUILD SUCCESSFUL` |
| `FsrsSchedulerTest` | 2 test, 0 failure |
| `GrammarQuestionEngineTest` | 5 test, 0 failure |
| Total unit test final | 7 test, 0 failure, 0 error |
| Final Gradle task | `:core:testDebugUnitTest` — `BUILD SUCCESSFUL` |

Regression test engine mencakup deterministic cloze, uniqueness options, formula strike parsing, TTS normalization, inflection dasar lintas kelas verb, duplicate token scramble, dan applicability mode.

## File utama

`GRAMMAR_LEARNING_OVERHAUL_DESIGN.md` berisi desain arsitektur lengkap. Implementasi utama berada pada `core/src/commonMain/kotlin/ua/syt0r/kanji/core/grammar/`, sedangkan UI/feedback berada pada `practice_grammar/ui/`. Regression test berada pada `core/src/commonTest/kotlin/GrammarQuestionEngineTest.kt`.

## Batasan yang disengaja

Validator runtime memeriksa struktur, token, rule family, dan consistency; ia tidak mengklaim sebagai korpus linguistik atau penentu resmi level JLPT. Point tanpa contoh atau formula yang tidak cocok dengan mode tertentu dilewati oleh applicability gate. Validasi linguistik eksternal sebaiknya dijalankan sebagai pipeline content authoring setelah endpoint dan lisensi yang sesuai dipilih, bukan dipanggil saat practice.

## Referensi

[1]: https://languagetool.org/proofreading-api "LanguageTool Proofreading API"

[2]: https://www.edrdg.org/jmdict/edict.html "EDICT Dictionary File — EDRDG"
