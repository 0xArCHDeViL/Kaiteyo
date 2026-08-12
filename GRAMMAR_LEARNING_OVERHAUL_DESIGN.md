# Grammar Learning Overhaul — Design

## Tujuan

Grammar practice harus berhenti membuat soal dari template yang kebetulan terlihat benar. Runtime akan memakai satu sumber data grammar, satu parser contoh, rule engine deterministic, dan quality gate sebelum sebuah soal diberikan kepada pelajar. FSRS, SRS key, queue, navigation, dan database contract tidak diubah.

> **Prinsip:** generator boleh bervariasi, tetapi correctness tidak boleh bergantung pada random atau koneksi internet.

## Arsitektur

| Lapisan | Tanggung jawab | Keputusan |
|---|---|---|
| `GrammarContentRepository` | Load dan cache `bunpou_data.json` sekali | Menghapus decode berulang di setiap use case |
| `GrammarMarkup` | Parse `~~...~~` menjadi segment normal/struck dan text untuk TTS | Tidak ada marker raw yang bocor ke UI atau audio |
| `GrammarExampleParser` | Memisahkan line Jepang, label speaker, dan meaning | Tidak mengandalkan `examples.firstOrNull()` saja |
| `GrammarQuestionEngine` | Menentukan tipe yang applicable dan menghasilkan cloze, conjugation, scramble, dialogue | Seed deterministic dari deck, point, dan mode |
| `GrammarRuleRegistry` | Mengenali particle, verb transformation, adjective transformation, dan formula family | Rule data-driven; bukan `contains()` acak per use case |
| `GrammarQuestionValidator` | Mengecek answer, uniqueness, option validity, scope, token reconstruction, dan marker integrity | Authority runtime offline |
| UI feedback layer | Menampilkan feedback konsisten, expected answer, explanation, dan action next | Satu pola UX lintas lima mode |
| External validation adapter | Integrasi opsional untuk content authoring/CI | Tidak dipanggil saat practice dan tidak menjadi sumber correctness |

## Determinism dan variasi

Seed dibuat dari `deckId`, `pointNumber`, dan `PracticeMode`. Shuffle memakai seeded PRNG lokal sehingga soal reproducible untuk test dan bug report, tetapi berbeda antar grammar point dan mode. Random global Kotlin tidak dipakai dalam generator.

## Rule engine

Cloze mencari kandidat target dari segment struck pada formula, kemudian mencari token tersebut pada example yang valid. Jika formula mengandung particle family, target dicari dari particle yang benar-benar muncul pada example. Distractor diambil dari family yang sama; tidak ada fallback global selalu `は`. Jika tidak ada kandidat yang dapat divalidasi, mode cloze tidak direncanakan untuk point tersebut.

Conjugation memakai lexicon pemula sebagai content, bukan sebagai logic. Logic inflection menangani ichidan, godan, dan irregular (`する`, `くる`) untuk `ます`, `ない`, `て`, `た`, dan bentuk dictionary. Formula yang tidak mengandung target verb transformation tidak diberi mode conjugation.

Scramble memakai token identity, bukan string identity, sehingga duplicate token tidak saling menghapus. Mode hanya aktif bila token reconstruction menghasilkan sentence dengan minimal tiga token dan target sentence berbeda dari hasil kosong.

Dialogue memakai example conversation yang benar-benar ada pada point, memilih line target secara deterministic, dan mengambil distractor dari example lain pada scope yang sama. Template speaker hanya menjadi fallback label, bukan sumber jawaban palsu.

## UI contract

State queue lama dipertahankan. `answeredCorrectly` tetap menjadi trigger SRS yang sudah ada, tetapi setiap UI memakai shared feedback presentation: status semantic, expected answer saat salah, audio action bila tersedia, dan satu CTA konsisten. Formula renderer memakai segment model agar `~~karakter~~` benar-benar dicoret dengan `TextDecoration.LineThrough`; plain text untuk TTS tidak memuat marker.

Conjugation dan scramble memakai token ID/index untuk undo satu token, bukan menghapus semua string yang sama. Submit tidak aktif sebelum state valid, dan reset hanya menghapus token yang dipilih bila pengguna menekan clear.

## Validasi eksternal

Tidak ada API gratis publik yang layak dijadikan authority grammar Jepang di runtime. LanguageTool Proofreading API menyatakan dukungan bahasa publiknya tanpa Japanese dan memakai akses berkuota/berbayar [1]. JMdict/EDICT adalah kamus leksikal dan bukan grammar checker/JLPT validator [2]. Karena itu app memakai offline validator sebagai authority dan menyediakan abstraction point untuk validator eksternal di pipeline authoring atau CI bila endpoint yang dapat dipercaya dan credential/legal terms sudah dipilih.

## Quality gates

Sebuah generated question ditolak jika answer kosong, correct index di luar range, opsi duplikat, opsi tidak sesuai family, sentence tidak dapat direkonstruksi, formula marker tidak balanced, target tidak ada pada source sentence, atau mode tidak applicable terhadap formula. Test mencakup deterministic seed, marker rendering, verb classes, duplicate token handling, cloze option uniqueness, dan semua existing FSRS/grammar regression.

## References

[1]: https://languagetool.org/proofreading-api "LanguageTool Proofreading API"

[2]: https://www.edrdg.org/jmdict/edict.html "EDICT Dictionary File — EDRDG"
