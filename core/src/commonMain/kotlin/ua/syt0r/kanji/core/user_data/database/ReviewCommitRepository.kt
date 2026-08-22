package ua.syt0r.kanji.core.user_data.database

import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithmVersion
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard

interface ReviewCommitRepository {
    suspend fun commitReview(
        key: SrsCardKey,
        card: FsrsCard,
        review: ReviewHistoryItem,
        algorithmVersion: FsrsAlgorithmVersion,
        parameterSetId: String,
    )
}
