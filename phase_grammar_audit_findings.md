# Baseline Audit Grammar Learning

## Dataset

- 10 chapter, 161 grammar point, 348 example.
- 1 grammar point tanpa example.
- 56 formula title menggunakan marker `~~...~~`; seluruhnya balanced.
- 270 example memakai newline sebagai pemisah, 292 memakai `。`, dan 21 tidak mempunyai delimiter Jepang/Indonesia yang jelas.
- 309 segmen Jepang memiliki spasi/ideographic space. Ini berarti split whitespace tidak otomatis salah untuk seluruh dataset, tetapi tetap rapuh karena tidak mewakili tokenisasi linguistik yang konsisten.

## Temuan logic

1. Cloze selalu memakai example pertama, mencari particle dari daftar statis, dan mengandalkan regex whitespace. Fallback jawaban selalu `は`, sehingga grammar point non-particle dapat menghasilkan soal palsu.
2. Conjugation memilih satu dari lima verba hardcoded secara random; bentuk stem, nai, te, dan ta ditulis manual untuk lima verba saja. Formula diproses dengan `contains()` terhadap marker string, bukan grammar rule terstruktur.
3. Syllable pool conjugation dibuat dari setiap karakter target ditambah tiga distractor global, lalu di-shuffle. Tidak ada token identity, duplicate handling, atau validasi bahwa jawaban unik.
4. Scramble mengambil example pertama dan hanya `split(" ")`; validasi jawaban membandingkan string tanpa spasi. Untuk kalimat Jepang, ini bukan sentence segmentation/phrase tokenization yang eksplisit.
5. Dialogue masih mock: speaker fixed Sensei/You/Sensei dan distractor respons berasal dari tujuh string tetap. Context hanya template string.
6. Semua builder membaca resource JSON dan decode ulang sendiri; belum ada shared repository/parser/cache atau generator abstraction.

## Temuan UI/UX

1. Feedback memakai campuran bahasa dan copy generik (`Excellent!`, `Continue`, `Submit Answer`, `Spot on!`, `Oops!`) tanpa informasi pedagogis yang konsisten.
2. `FormulaText` sudah mencoba merender `~~...~~` sebagai line-through, tetapi parser berbasis `split("~~")` tidak menangani marker dengan semantic model dan field raw masih dipakai di TTS (`state.title.replace("~", "")`).
3. Conjugation builder menganggap tap pada area jawaban sebagai reset seluruh jawaban, tidak punya undo token-per-token, tidak membedakan token yang sudah dipilih, dan menerima `builtConjugation += syllable`, sehingga duplicate token ambigu.
4. Scramble menghapus token dengan `selectedParts - part`, sehingga duplicate token identik dapat terhapus dari posisi/instance yang salah; pilihan chip tidak membawa identity.
5. Mode UI per file mengulang pola layout, feedback, tombol, dan animasi, sehingga behavior antar mode tidak konsisten dan sulit dijaga.
6. Semua mode mengandalkan `answeredCorrectly: Boolean?`; state tidak membawa expected answer, explanation, confidence, validation source, atau reason ketika jawaban salah.

## Target desain

- FSRS/SRS dan navigation/database contract tetap dipertahankan.
- Grammar source diparsing satu kali melalui shared repository/cache.
- Formula marker menjadi segment model: visible text, struck text, dan semantic role; TTS memakai normalized text.
- Generator deterministic seeded per review key, tetapi menghasilkan variasi dari data dan rule registry, bukan random hardcoded.
- Offline-first: deterministic grammar rule engine menjadi authority; validator eksternal/LLM hanya optional asynchronous pre-publish or developer/content pipeline, tidak menjadi dependency runtime practice.
- Setiap soal melewati invariant gate: answer exists, answer unique, distractors valid, expected output deterministic, JLPT/point scope matches, and no malformed display text.

## Riset validator eksternal

- LanguageTool Proofreading API resmi menjelaskan dukungan bahasa lebih dari 30 bahasa, tetapi daftar bahasa yang tampil pada halaman publiknya tidak mencantumkan Japanese; layanan ini juga memakai skema akses/kuota berbayar, sehingga tidak layak dijadikan dependency runtime gratis untuk practice. Sumber: https://languagetool.org/proofreading-api
- EDRDG JMdict/EDICT adalah kamus Japanese-English/multilingual, bukan grammar checker atau validator JLPT. EDICT2/JMdict dapat dipakai sebagai lexical support untuk memeriksa kata, reading, dan kemungkinan POS, dengan attribution/share-alike sesuai halaman lisensinya. Sumber: https://www.edrdg.org/jmdict/edict.html
- Keputusan arsitektur: runtime practice harus tetap offline-first dan deterministic. Validator online hanya opsional untuk pipeline authoring/CI atau developer preview; hasilnya tidak boleh menentukan correctness saat user sedang belajar. Authority runtime adalah rule engine + invariant gate + source examples.
