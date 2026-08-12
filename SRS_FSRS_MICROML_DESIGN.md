# SRS/FSRS Micro-ML Design

## Tujuan

Kaiteyo akan mempertahankan FSRS sebagai **memory model dan scheduler utama**, lalu menambahkan lapisan personalisasi kecil yang belajar dari review lokal pengguna. Lapisan ini tidak menggantikan FSRS dengan neural network atau reinforcement learning. Ia hanya mengestimasi koreksi personal yang terukur terhadap **request retention** dan interval review, sehingga sistem tetap offline, deterministik, mudah diaudit, dan ringan di perangkat.

FSRS sendiri memiliki dua bagian yang berbeda: scheduler yang menghitung state dan interval, serta optimizer yang mempelajari pola memori dari review history.[1] Kaiteyo saat ini sudah memiliki scheduler FSRS-5, tetapi belum memiliki optimizer/personalizer; `Fsrs5` memakai 19 bobot default dan `requestRetention = 0.9` secara tetap.

## Keputusan arsitektur

| Keputusan | Rationale |
|---|---|
| Tidak memakai TensorFlow/Lite atau model neural | Tidak ada kebutuhan inferensi tensor; overhead binary, startup, dan memory tidak sebanding dengan feature vector kecil |
| Tidak mengubah 19 bobot FSRS secara online pada fase pertama | Perubahan bobot global dari sedikit data mudah overfit dan dapat merusak stability/difficulty semantics |
| Memakai online logistic calibration | Satu bias dan beberapa koefisien kecil dapat di-update O(1) per review, deterministik, dan mudah di-reset |
| Personalization hanya memengaruhi target retention dan interval bounded | FSRS tetap menjadi source of truth; guardrail mencegah interval ekstrem dan starvation |
| Profile disimpan sebagai JSON DataStore | Tidak perlu SQL migration; ukurannya hanya puluhan angka dan metadata |
| Profil di-update setelah review tercatat | Training sample memakai outcome review yang nyata, bukan rating yang baru dipilih sebelum hasil tersedia |
| Fallback ke FSRS murni ketika data belum cukup atau confidence rendah | Cold-start aman dan behavior existing tetap menjadi baseline |

## Model micro-ML

### Feature vector

Untuk sebuah card pada waktu review `t`, engine membentuk feature vector yang seluruhnya dapat dihitung dari state FSRS dan review history lokal:

| Feature | Definisi | Guardrail |
|---|---|---|
| `baseRetrievability` | Forgetting curve FSRS dari elapsed time dan stability | `0.01..0.999` |
| `latenessRatio` | elapsed time / predicted interval | `0..4` |
| `difficulty` | FSRS difficulty yang dinormalisasi | `0..1` |
| `stabilityLog` | `ln(1 + stability)` dinormalisasi | `0..1` |
| `recentFailureRate` | exponential moving average rating Again dalam window lokal | `0..1` |
| `recentMistakeRate` | EMA mistakes yang sudah dinormalisasi | `0..1` |
| `responseEffort` | EMA duration capped per review | `0..1` |
| `reviewCount` | jumlah review capped dan diubah ke confidence | `0..1` |

Model menghitung `pRecall` dengan sigmoid sederhana:

```text
z = bias + Σ(weight[i] * feature[i])
pRecall = sigmoid(z)
calibrationError = pRecall - observedRecall
```

`observedRecall` adalah 1 untuk `Hard`, `Good`, atau `Easy`, dan 0 untuk `Again`. Rating tidak diperlakukan sama persis pada stability update FSRS; rating hanya dipakai sebagai label coarse pada calibration layer. Mistakes dan duration menjadi sinyal pendukung, bukan label pengganti.

### Online update

Setiap sample diperbarui dengan gradient descent yang dibatasi:

```text
bias += learningRate * calibrationError
weight[i] += learningRate * calibrationError * feature[i]
weight[i] = clamp(weight[i], -weightBound, weightBound)
```

Learning rate menurun sesuai sample count dan diberi floor kecil. Profile hanya boleh aktif setelah minimum sample threshold, misalnya 32 review valid yang mencakup setidaknya satu success dan satu failure. Jika hanya terdapat success atau hanya failure, model tetap berada pada cold-start baseline.

### Cara model memengaruhi FSRS

FSRS menghitung baseline interval menggunakan `requestRetention`. Personalization mengubahnya dalam batas kecil:

```text
effectiveRetention = clamp(
    baseRetention + confidence * retentionDelta,
    0.82,
    0.97
)
```

Untuk menghindari duplikasi formula FSRS dan perubahan global configuration, implementasi awal menggunakan **interval correction factor** setelah `FsrsScheduler` menghasilkan answer:

```text
correction = clamp(
    targetRetentionAdjustment(effectiveRetention, baseRetention),
    0.85,
    1.15
)
correctedInterval = clamp(
    baseInterval * correction,
    minimumIntervalForState,
    maxInterval
)
```

Koreksi hanya diterapkan pada `Review` dan `Relearning` dengan interval setidaknya satu hari. Learning steps (`1m`, `5m`, `10m`) tidak disentuh. Ordering invariant dipertahankan dengan menghitung correction per answer lalu menegakkan `Again <= Hard <= Good <= Easy` menggunakan bounded monotonic projection.

## Data lifecycle

1. Scheduler membaca card state dan profile snapshot yang immutable.
2. FSRS menghasilkan empat candidate answers.
3. Personalization decorator melakukan correction bounded dan menjaga ordering.
4. Practice flow memilih answer dan menyimpan `FsrsCard` seperti biasa.
5. Review history menyimpan sample yang sudah ada (`grade`, `mistakes`, `duration`, `timestamp`).
6. Profile trainer menerima sample tersebut dan melakukan satu online update.
7. Profile di-persist secara throttled, bukan pada setiap recomposition.

Tidak ada network call, tidak ada upload profile, dan tidak ada background training job.

## Guardrails

Profil dipause apabila numerik tidak finite, sample count corrupt, atau error berturut-turut melebihi threshold. Pada kondisi tersebut engine kembali ke FSRS murni dan menulis profile default yang aman. Interval tidak boleh keluar dari max interval FSRS, tidak boleh menjadi nol pada Review, dan tidak boleh melampaui 15% koreksi pada fase beta. Semua operasi memakai `Double` finite checks dan `coerceIn`.

Profile menggunakan version number. Jika formula berubah, version mismatch menyebabkan reset profile, bukan interpretasi data lama dengan semantics baru. Reset profile tidak menghapus review history dan dapat dilakukan tanpa SQL migration.

## Test strategy

Test wajib mencakup cold start equivalence, profile update determinism, persistence round-trip, finite-value safety, ordering invariant, interval bounds, monotonicity terhadap confidence, dan regression vector FSRS existing. Benchmark target adalah satu schedule plus personalization correction di bawah 100 microseconds pada JVM debug tanpa alokasi collection besar.

## Referensi

[1]: https://github.com/open-spaced-repetition/fsrs4anki "FSRS4Anki — scheduler and optimizer overview"
[2]: https://github.com/open-spaced-repetition/free-spaced-repetition-scheduler "Free Spaced Repetition Scheduler — DSR model and local execution"
[3]: https://github.com/open-spaced-repetition/awesome-fsrs/wiki/The-Algorithm "Awesome FSRS — Algorithm and research links"
