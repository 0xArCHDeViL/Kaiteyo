# Kaiteyo — Planning Superior Detail Kanji dan Kotoba

**Status:** Planning dan audit arsitektur

**Tanggal:** 15 Agustus 2026

**Ruang lingkup:** Android-only, API 31+, ARM64-v8a, phone portrait locked, tablet/pad portrait dan landscape, full JMdict v21, APK tetap di bawah 70 MB, ProGuard/R8 tetap aktif.

## 1. Keputusan desain utama

Halaman detail Kanji dan Kotoba tidak akan dibuat sebagai dua layar yang terpisah secara filosofis. Keduanya akan memakai satu **Detail Information System** dengan shell, hierarchy, spacing, state handling, action model, provenance display, dan responsive rules yang sama. Perbedaannya hanya pada content schema dan urutan sub-seksi yang memang berbeda secara linguistik.

Referensi visual pengguna akan dipakai sebagai **target density dan information hierarchy**, bukan disalin secara buta. Target Kaiteyo harus mempertahankan keterbacaan bahasa Indonesia, ketepatan identity entry, akses offline, integrasi FSRS, serta perilaku adaptif yang benar pada tablet. Tidak akan ada metadata yang ditampilkan sebagai fakta jika sumbernya belum tersedia atau belum tervalidasi.

> Prinsip inti: **source data, user study state, dan presentation state harus dipisahkan secara eksplisit.** UI hanya merender kontrak domain yang sudah dibentuk oleh use case; UI tidak boleh menebak meaning, JLPT, pitch accent, entry identity, atau status review.

## 2. Hasil audit repository saat ini

Feature existing berada di `presentation/screen/main/screen/info`. Satu feature tersebut sudah memiliki dua jalur state utama, yaitu `Loaded.Letter` untuk Kanji/Kana dan `Loaded.Vocab` untuk vocabulary. Kanji sudah memiliki stroke paths, meanings, on/kun readings, grade, JLPT, frequency, radicals, vocabulary pagination, dan sentences. Vocabulary sudah memiliki selected `JapaneseWord`, `DetailedJapaneseWord`, matching senses, detailed readings dengan `elementId`, dan sentence pagination.

Fondasi database juga sudah cukup kuat. App database mempunyai `character_stroke`, `kanji_data`, `kanji_reading`, `kanji_meaning`, `kanji_classification`, `kanji_radical`, vocabulary elements, sense glosses, part-of-speech tags, priority/common tags, furigana, names, dan sentences. Full JMdict v21 sudah menjadi sumber vocabulary lokal dengan 218.429 entry.

Batasan utama bukan lagi ketiadaan data dasar, melainkan **projection dan identity contract**. `JapaneseWord` masih memproyeksikan satu selected reading, glossary, dan part-of-speech list. Detail page baru memerlukan seluruh sense, seluruh reading element, restriction, tag, source label, related entry, serta study snapshot secara terstruktur. `YomichanJlptVocabParser` sudah ada, tetapi keberadaannya belum cukup untuk menyatakan bahwa JLPT vocabulary sudah benar-benar join ke database v21; pipeline join dan integrity check harus dibuat eksplisit.

Klasifikasi Kanji sudah mendukung JLPT `n1`–`n5`, grade, dan WaniKani melalui `CharacterClassification`. Sebaliknya, schema vocabulary yang diaudit belum menunjukkan kolom native untuk pitch accent atau etimologi/form explanation. Karena itu pitch accent dan etimologi harus menjadi **optional enriched dataset** dengan provenance, bukan placeholder hardcoded.

## 3. Source-of-truth matrix

| Data | Source of truth | Status | Aturan rendering |
|---|---|---:|---|
| Kanji character | App database `kanji_data` | Ready | Selalu memakai character sebagai identity |
| Stroke order | `character_stroke` | Ready | Render sebagai path data lokal; fallback hanya berupa state “tidak tersedia” |
| Kanji meaning | `kanji_meaning` | Ready | Urut berdasarkan priority; jangan menggabungkan tanpa batas |
| Kanji on/kun/nanori reading | `kanji_reading` | Ready, perlu projection lengkap | Kelompokkan berdasarkan `reading_type`; jangan menyamakan semua reading sebagai on-reading |
| JLPT/grade/WaniKani Kanji | `kanji_classification` | Ready | Tampilkan source badge atau provenance pada detail/diagnostic layer |
| Kanji frequency | `kanji_data.frequency` | Ready | Label harus “frequency/rank” sesuai makna source; jangan menyebut sequence jika field bukan sequence |
| Radicals/components | `kanji_radical`, radical data | Ready | Setiap item clickable dengan target Kanji/component typed |
| Vocabulary entry | Full JMdict v21 | Ready | Entry identity memakai `entryId`, bukan display text |
| Vocabulary kanji/kana elements | `vocab_kanji_element`, `vocab_kana_element` | Ready | Pertahankan `elementId`, `kanji`, `kana`, `priority`, dan restriction |
| Vocabulary senses | JMdict sense tables | Ready | Render per sense; POS, field, misc, dialect, gloss, restrictions tidak boleh diratakan menjadi satu string |
| Common/priority tags | JMdict priority exporter | Ready | `common` hanya jika source priority memenuhi rule exporter |
| Vocabulary JLPT | Yomichan JLPT parser/source | Partial | Wajib join berdasarkan JMdict sequence plus reading identity; tampilkan hanya jika integrity check lulus |
| Indonesian gloss | Dataset terjemahan terpisah | Not established | Jangan mengklaim sebagai source JMdict; tampilkan source/locale label |
| Pitch accent | Dataset pitch accent terpisah | Not present in current JMdict projection | Fitur ditunda sampai dataset, lisensi, join key, dan coverage tervalidasi |
| Etymology/form explanation | Dataset explanation terpisah | Not present in current app database | Fitur ditunda; state kosong lebih benar daripada narasi palsu |
| Favorite, tags, flags, notes | User database | Ready as infrastructure | Diakses melalui repository/use case, tidak melalui direct SQL dari Composable |
| FSRS card | `FsrsCardRepository` | Ready as infrastructure | Kanji key memakai character; vocabulary key memakai word ID dan practice type |
| Deck membership | Letter/Vocab practice repository | Ready as infrastructure | Diproyeksikan sebagai count/list, bukan dicampur dengan dictionary source |
| Review history | `ReviewHistoryRepository` | Ready as infrastructure | Dipakai untuk count, last review, study time, dan progress summary |

## 4. Information architecture final

### 4.1 Shared detail shell

Kedua halaman memakai shell berikut, dengan section yang dapat memiliki isi berbeda tetapi tidak memakai hierarchy yang saling bertentangan:

1. **Top app bar.** Back, title contextual, search, overflow, dan state-safe navigation. Title tidak boleh bergantung pada string yang terpotong.
2. **Action row.** Favorite, add-to-deck, practice/review, copy/share, pronunciation, dan edit/notes hanya ditampilkan jika action tersebut valid untuk target.
3. **Identity hero.** Character atau vocabulary utama, reading/furigana, rōmaji, badge source, badge level, dan identity index bila satu entry memiliki beberapa form.
4. **Study overview.** Card status, progress, last review, due state, review count, deck membership, dan quick action menuju practice.
5. **Primary linguistic content.** Stroke/meaning/reading untuk Kanji; sense/reading/POS/restriction untuk Kotoba.
6. **Relations and examples.** Components, related words, Kanji constituents, sentences, dan vocabulary examples.
7. **Optional enrichment.** Pitch accent, etimologi, form explanation, atau Indonesian gloss hanya muncul jika source dan coverage valid.
8. **Source and diagnostic footer.** Dataset source, version, coverage state, dan fallback/error state yang tidak mengganggu layar normal.

### 4.2 Detail Kanji

Urutan final pada phone adalah satu kolom vertikal:

| Urutan | Section | Isi wajib |
|---:|---|---|
| 1 | Identity hero | Kanji besar, optional alternate/variant, favorite, level badges |
| 2 | Study overview | Writing/reading FSRS, progress, last review, due, deck count |
| 3 | Metadata grid | JLPT, grade, frequency/rank, stroke count, WaniKani jika ada, classification |
| 4 | Stroke order | Hero stroke animation, play/pause/replay, step count, numbered stroke thumbnails |
| 5 | Meanings | Meaning list dengan urutan source priority dan selection/copy behavior |
| 6 | Readings | On, kun, nanori/other reading type; setiap reading dapat dicopy dan dipakai untuk search |
| 7 | Decomposition | Radical/component graph atau ordered component list, clickable |
| 8 | Vocabulary examples | Paginated words, grouped by reading/priority bila memungkinkan |
| 9 | Sentences | Sentence pagination dengan highlighted character |
| 10 | Notes/source | User notes, provenance, missing-data state |

Pada medium dan expanded width, urutan section tetap sama. Yang berubah hanya cara reflow: metadata menjadi adaptive grid, stroke gallery memakai grid dengan minimum width, dan vocabulary/sentence list dapat memakai supporting pane. Tidak boleh ada sidebar fixed yang membuat hierarchy mobile hilang atau menyebabkan overlap.

### 4.3 Detail Kotoba

| Urutan | Section | Isi wajib |
|---:|---|---|
| 1 | Identity hero | Kanji/kana form yang dipilih, furigana, rōmaji, form index, favorite |
| 2 | Source badges | Common, JLPT vocabulary jika tervalidasi, priority, field/dialect/misc summary |
| 3 | Reading visualization | Furigana, kana reading, optional pitch accent jika source valid, audio action jika tersedia |
| 4 | Sense list | Sense number, POS, glosses, Indonesian gloss bila tersedia, field/misc/dialect, restrictions |
| 5 | Alternate forms | Semua kanji/kana elements dengan `elementId`, priority, info flags, no-kanji state |
| 6 | Referenced words | Related entries yang memiliki relasi valid; setiap item memakai `entryId + elementId` |
| 7 | Kanji section | Setiap Kanji penyusun, reading, meaning summary, JLPT/grade, stroke preview |
| 8 | Sentences | Sentence pagination dengan selected form/readings |
| 9 | Study section | Flashcard/reading picker/writing FSRS states, deck membership, review summary |
| 10 | Source/notes | JMdict sequence, source version, personal notes, data availability |

`Referenced words` tidak boleh diisi dengan hasil pencarian substring yang longgar. Relasi harus berasal dari vocabulary entry, shared Kanji/reading, atau explicit source relation dan harus memiliki label tipe relasi agar pengguna tidak menganggap semua item sebagai sinonim.

## 5. Domain model yang direncanakan

Navigation target wajib typed dan identity-safe:

```kotlin
sealed interface DetailTarget {
    data class Kanji(val character: String) : DetailTarget
    data class Vocabulary(
        val entryId: Long,
        val elementId: Long?,
        val kanji: String?,
        val kana: String
    ) : DetailTarget
}
```

`elementId` bersifat nullable hanya ketika navigasi berasal dari context yang belum memilih element. Setelah repository berhasil resolve, seluruh UI harus membawa selected `elementId`. Display string seperti `主夫【しゅふ】` tidak boleh digunakan sebagai primary identity.

Model detail dibagi menjadi tiga lapisan:

```text
DictionaryDetailData
├── KanjiDetailData / VocabularyDetailData
├── SourceMetadata
├── RelationData
└── StudySnapshot

DictionaryDetailData + UserDataSnapshot
→ DetailScreenState
→ shared DetailShell
→ typed Kanji/Vocab sections
```

Kontrak minimum yang diusulkan:

| Model | Kontrak penting |
|---|---|
| `SourceMetadata` | source name, source version, checksum/build version, coverage status, optional license label |
| `KanjiDetailData` | character, variant, frequency, classifications, meanings, typed readings, stroke paths, radicals, examples |
| `VocabularyDetailData` | entryId, selected element, all elements, all senses, restrictions, tags, related entries, sentences |
| `StudySnapshot` | practice type, existence, card status, due/expected review, interval, stability/difficulty jika tersedia, reps/lapses, last review, review count |
| `DeckMembership` | deck ID, title, archived state, card ID, identity match state |
| `RelationTarget` | target type, entryId/elementId or character, relation type, display payload |
| `ProvenanceValue<T>` | value, source, confidence/coverage state, validation status |

## 6. FSRS dan user-state integration

Detail page tidak boleh membuat algoritme FSRS baru. Semua card harus memakai `FsrsCardRepository`, `SrsCardKey`, practice type yang sudah ada, dan review history yang sudah tersedia.

| Entity | Card key | Practice types |
|---|---|---|
| Kanji | `character` | `LetterWriting`, `LetterReading` |
| Vocabulary | `entryId.toString()` | `VocabFlashcard`, `VocabReadingPicker`, `VocabWriting` |
| Grammar | Tidak ditampilkan sebagai Kanji/Kotoba card | Tetap berada pada grammar feature |

`StudySnapshot` harus bersifat read model. Tombol action mengirim event ke use case yang sudah bertanggung jawab atas perubahan FSRS, deck, tag, atau note. UI hanya menampilkan state terbaru dari flow/reload. Setiap mutation harus idempotent, race-safe, dan tidak memengaruhi dictionary database.

Ringkasan default yang ditampilkan adalah `New`, `Learning`, `Review`, atau `Due`, lalu detail sekunder seperti expected review, interval, last review, repetitions, lapses, dan total reviews. Nilai nullable harus memiliki state visual `Not studied`, bukan angka nol yang menyesatkan.

## 7. Responsive dan visual system

Android resmi mengklasifikasikan available width secara dinamis: compact di bawah 600 dp, medium 600–840 dp, expanded 840–1200 dp, large 1200–1600 dp, dan extra-large di atas 1600 dp. Window size class tidak boleh diganti dengan logika `isTablet`, karena split-screen, resize, foldable, dan orientasi dapat mengubah ruang aplikasi saat runtime [1].

Kaiteyo akan menggunakan strategi berikut:

| Width | Layout | Aturan |
|---|---|---|
| Compact `<600dp` | Single-column detail | Hierarchy penuh tetap terlihat; sections collapsible hanya untuk content sekunder; no horizontal overflow |
| Medium `600–840dp` | Single scroll surface dengan adaptive grids | Hero dan metadata reflow; content width dibatasi agar tidak terlalu melebar; supporting content dapat menjadi bottom sheet atau secondary region |
| Expanded `840–1200dp` | Primary detail + supporting content | Main content tetap menjadi 65–70%; related words/sentences/study summary dapat menjadi supporting pane; section order tetap mengikuti mobile |
| Large/XL | Same system dengan max content width | Tidak memperbesar glyph/text tanpa batas; gunakan margins dan max width; hindari “desktop rewrite” |

Canonical Android layouts mendukung list-detail dan supporting-pane untuk layar besar. Supporting pane ideal untuk konten sekunder yang hanya bermakna bersama konten utama, sementara detail Kanji/Kotoba tetap menjadi primary content [2]. Material 3 menyediakan adaptive scaffolds, pane margins, edge-to-edge, dan adaptive navigation pada library Compose Material 3 Adaptive [3] [4].

Aturan visual konkret:

| Area | Rule |
|---|---|
| Grid | Basis spacing 4 dp; section spacing 24 dp; content padding 16 dp compact dan 24–32 dp medium/expanded |
| Touch target | Minimum 48 dp untuk semua action dan chip yang dapat diklik |
| Typography | Kanji hero memakai display style; reading dan meaning memakai hierarchy yang jelas; tidak memakai font size ekstrem yang menyebabkan clipping |
| Color | Material 3 semantic color roles; accent hanya untuk selected/progress/action, bukan dekorasi acak |
| Cards | Card hanya dipakai jika memberi grouping atau elevation meaning; divider dipakai untuk row relationship, bukan setiap line |
| Stroke panel | Hero dan numbered thumbnails memakai satu visual language; playback state harus terlihat tanpa mengandalkan warna saja |
| Long content | Lazy rendering, stable keys, section state saveable, no nested unbounded scrolling |
| Accessibility | Content description, selectable text, TalkBack order, semantic headings, contrast, keyboard/stylus compatibility |
| Animation | Short, purposeful, interruptible; tidak boleh mengubah identity atau reset scroll secara tak terduga |

## 8. Repository/query plan

Repository tidak boleh memuat detail page dengan puluhan query per row. Query dirancang berdasarkan bounded aggregate dan batch loading:

```text
loadDetail(target)
├── load dictionary aggregate
├── load typed relations in bounded batches
├── load study snapshots for fixed practice types
├── load deck membership
├── load review summary
└── combine into immutable DetailScreenState
```

API yang perlu ditambahkan atau dibentuk ulang secara bertahap:

| API | Tujuan |
|---|---|
| `getKanjiDetail(character)` | Satu aggregate Kanji: metadata, readings, stroke, radicals, source |
| `getVocabularyDetail(entryId, elementId?)` | Semua senses/elements dengan selected-element resolution |
| `getVocabularyRelations(entryIds)` | Related words dan Kanji relations tanpa N+1 |
| `getStudySnapshots(keys)` | Batch FSRS read model untuk Kanji/Kotoba |
| `getDeckMemberships(targets)` | Batch membership dengan identity-safe matching |
| `getReviewSummaries(keys)` | Last review/count/time summary dari review history |
| `getSentencesForTarget(target, page)` | Sentence pagination dengan stable target semantics |

SQL projection harus mengembalikan primitive/DTO yang stabil. Mapping ke presentation model dilakukan satu kali di use case. Query detail tidak boleh memuat seluruh katalog Kanji atau seluruh vocabulary hanya untuk satu halaman.

## 9. Dataset enrichment pipeline

### 9.1 JLPT vocabulary

Parser `YomichanJlptVocabParser` harus diubah dari parser pasif menjadi bagian pipeline yang tervalidasi. Join key minimum adalah JMdict sequence/entry ID dan reading identity (`kanji`, `kana`). Jika satu JLPT record tidak dapat dipetakan dengan tepat, record tersebut masuk reject report dan tidak ditampilkan sebagai badge.

Pipeline harus menghasilkan:

```text
source file
→ parser
→ normalized JLPT record
→ identity join report
→ coverage/mismatch report
→ SQLite table
→ runtime query
→ UI badge
```

Integrity checks wajib mencakup jumlah source rows, jumlah matched entries, jumlah ambiguous matches, jumlah orphan rows, duplicate identity, dan sample assertions untuk entry yang memiliki banyak forms.

### 9.2 Pitch accent

Pitch accent tidak boleh diambil dari JMdict biasa. Fitur hanya diaktifkan setelah dataset dipilih, lisensi diverifikasi, coverage bahasa/form dipahami, dan join key dapat dibuktikan. Model harus mendukung accent pattern per reading, bukan satu angka global per vocabulary entry. Jika data tidak ada, UI menampilkan `Pitch accent unavailable for this form` hanya pada diagnostic/optional section, bukan membuat visual palsu.

### 9.3 Indonesian gloss

JMdict glossary utama adalah English. Indonesian gloss harus menjadi layer locale terpisah dengan `source`, `translationVersion`, dan `coverage`. Untuk entry yang belum memiliki Indonesian translation, English tetap menjadi canonical fallback dengan label yang jelas. Terjemahan otomatis tidak boleh dipermanenkan ke database tanpa review dan confidence policy.

### 9.4 Etymology/form explanation

Form explanation dan decomposition prose harus disimpan sebagai content source terpisah dari structural decomposition. Decomposition berbasis radicals/components dapat digunakan sekarang; narasi sejarah/etimologi hanya ditampilkan jika source memiliki provenance dan tingkat kepercayaan yang jelas.

## 10. Tahapan implementasi migration-safe

### P0 — Contract dan read model

Buat `DetailTarget`, `SourceMetadata`, `StudySnapshot`, `KanjiDetailData`, dan `VocabularyDetailData`. Pertahankan adapter dari `InfoScreenData` lama agar navigation existing tetap bekerja. Tambahkan identity tests untuk multi-form dan shared-reading entries.

### P1 — Repository aggregate

Implementasikan query aggregate Kanji dan vocabulary dengan batch relations. Wire `elementId` dari search result, detail navigation, referenced word, Kanji example, text analysis, dan practice cards. Pastikan `主夫` dan `主婦` tidak pernah ter-collapse hanya karena reading sama.

### P2 — Shared detail shell

Refactor existing `InfoScreen` menjadi shared shell dengan typed content blocks. State loading, error, empty, retry, pagination, and scroll behavior dibuat satu kali. Jangan membuat page baru yang menggandakan scaffold.

### P3 — Kanji experience

Implement hero, metadata grid, stroke gallery, meanings, typed readings, decomposition, vocabulary examples, sentences, notes, dan study summary. Semua section harus memakai stable keys dan lazy bounded rendering.

### P4 — Kotoba experience

Implement hero reading, badges, per-sense rendering, full alternate forms, reading flags, related words, Kanji section, sentences, and vocabulary FSRS summary. Glossary tidak boleh lagi dipaksa menjadi satu string per entry.

### P5 — Enrichment dan provenance

Wire JLPT vocabulary hanya setelah join/integrity report lulus. Pitch accent, Indonesian gloss, dan etymology masuk sebagai feature gates berdasarkan dataset readiness, bukan berdasarkan UI readiness saja.

### P6 — Visual/performance hardening

Validasi compact, medium, expanded, rotation, split-screen, dark/light theme, dynamic font scale, TalkBack, touch/stylus, memory, and scroll performance. Baru setelah itu commit, build preview, dan CI.

## 11. Regression dan acceptance criteria

### Data correctness

| Test | Acceptance criterion |
|---|---|
| Shared reading | Search/detail `しゅふ` menampilkan `主夫` dan `主婦` sebagai dua entry terpisah |
| Element identity | Memilih form tertentu selalu menampilkan pasangan Kanji–kana yang dipilih; tidak fallback ke element lain tanpa state/log |
| Multi-sense | Semua sense tampil bernomor, POS/field/misc/dialect tidak hilang, restrictions dihormati |
| Kanji readings | On, kun, nanori, dan reading type lain tidak tertukar atau dipaksa menjadi satu kategori |
| Full dataset | Database v21 tetap memuat 218.429 vocabulary entries; detail query tidak mengurangi coverage |
| JLPT vocabulary | Badge hanya muncul untuk row yang lolos identity join dan integrity checks |
| Missing metadata | Pitch/etymology/translation yang belum tersedia ditampilkan sebagai unavailable/omitted, bukan fabricated |

### UI/UX correctness

| Test | Acceptance criterion |
|---|---|
| Phone portrait | Satu kolom, tidak ada clipping/overlap, section order lengkap |
| Tablet portrait | Hierarchy sama; grid dan supporting content reflow tanpa fixed sidebar yang merusak layout |
| Tablet landscape | Primary detail tetap dominan; supporting content tidak menimpa stroke/meaning/reading |
| Rotation | Scroll/selected section/identity state dipertahankan secara masuk akal |
| Font scale | Tidak ada text truncation yang menghilangkan meaning atau reading |
| Accessibility | Semua action dapat dijangkau TalkBack dan touch target memadai |
| Theme | Semantic colors tetap terbaca pada dark/light dan accent scheme yang tersedia |

### Performance correctness

| Test | Acceptance criterion |
|---|---|
| Query count | Detail page tidak melakukan N+1 query per sense, element, atau relation |
| Memory | Tidak memuat seluruh katalog vocabulary/Kanji untuk satu detail page |
| Scroll | Stroke gallery dan paginated list tidak menyebabkan jank karena recompose berantai |
| State race | Navigasi cepat antar-entry tidak membuat data lama menimpa entry baru |
| Offline | Detail core tetap bekerja setelah runtime pack tersedia dan tidak bergantung pada network |
| Build | APK tetap di bawah 70 MB; ProGuard/R8 tetap aktif; database tetap di luar APK |

## 12. Prioritas keputusan

**Wajib untuk milestone pertama:** typed identity, shared shell, Kanji metadata/stroke/readings/decomposition, vocabulary full senses/elements, relation-safe navigation, FSRS summary, responsive phone/tablet, and data regression.

**Wajib setelah source validation:** vocabulary JLPT badges, richer deck membership, review history summary, and source diagnostics.

**Tidak boleh dipaksakan sebelum dataset siap:** pitch accent, Indonesian translation corpus, historical etymology, form explanation prose, dan visual yang menyiratkan fakta dari data yang belum ada.

## 13. Definition of done

Overhaul dianggap selesai hanya jika seluruh target berikut terpenuhi secara bersamaan: halaman Kanji dan Kotoba memakai shared information architecture; semua identity memakai `entryId/elementId/character`; full JMdict tetap tersedia offline; `主夫` dan `主婦` sama-sama tampil; semua sense dan reading forms terjaga; FSRS dan deck state berasal dari repository yang benar; tablet portrait dan landscape tidak memakai layout yang rusak; metadata non-JMdict memiliki provenance; tidak ada hardcoded linguistic facts; unit/instrumentation/visual/data tests lulus; APK size dan ProGuard/R8 guard tetap lulus; dan workflow CI berhasil.

## Referensi

[1]: https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes "Android Developers — Use window size classes"

[2]: https://developer.android.com/develop/adaptive-apps/guides/canonical-layouts "Android Developers — Canonical layouts"

[3]: https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive "AndroidX — Compose Material 3 Adaptive release notes"

[4]: https://m3.material.io/foundations/adaptive-design/canonical-layouts "Material 3 — Canonical layout examples"
