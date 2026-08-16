package ua.syt0r.kanji.core.app_data.data

import kotlinx.datetime.Instant

/**
 * Stable dictionary navigation identity.
 *
 * Display strings are deliberately not part of the identity because one JMdict
 * entry can contain multiple kanji/kana elements and different entries can share
 * the same reading.
 */
sealed interface DictionaryDetailTarget {
    data class Kanji(val character: String) : DictionaryDetailTarget

    data class Vocabulary(
        val entryId: Long,
        val elementId: Long? = null,
        val kanji: String? = null,
        val kana: String? = null,
    ) : DictionaryDetailTarget {
        init {
            require(entryId > 0) { "Vocabulary entryId must be positive" }
            require(elementId == null || elementId > 0) {
                "Vocabulary elementId must be positive when present"
            }
            require(kanji != null || kana != null) {
                "Vocabulary target needs at least one display form"
            }
        }
    }
}

data class DetailSourceMetadata(
    val sourceName: String,
    val sourceVersion: String,
    val coverage: Coverage = Coverage.Complete,
    val licenseLabel: String? = null,
) {
    enum class Coverage {
        Complete,
        Partial,
        Unavailable,
    }
}

data class StudySnapshot(
    val practiceType: Long,
    val status: Status,
    val lastReview: Instant? = null,
    val expectedReview: Instant? = null,
    val intervalSeconds: Long? = null,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val reviewCount: Long? = null,
) {
    enum class Status {
        New,
        Learning,
        Review,
        Due,
        Unavailable,
    }
}

data class DetailStudySummary(
    val snapshots: List<StudySnapshot> = emptyList(),
    val deckCount: Int = 0,
    val lastReview: Instant? = null,
    val totalReviewCount: Long = 0,
) {
    val hasBeenStudied: Boolean
        get() = snapshots.any { it.status != StudySnapshot.Status.New }
}

data class KanjiDetailIdentity(
    val character: String,
    val source: DetailSourceMetadata,
)

data class VocabularyDetailIdentity(
    val entryId: Long,
    val elementId: Long?,
    val kanji: String?,
    val kana: String,
    val source: DetailSourceMetadata,
)
