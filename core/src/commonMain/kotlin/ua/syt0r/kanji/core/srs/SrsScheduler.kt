package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithmVersion
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler

interface SrsScheduler {
    val algorithmVersion: FsrsAlgorithmVersion
    val parameterSetId: String

    fun newCard(): SrsCard
    fun answers(data: SrsCard, reviewTime: Instant): SrsAnswers
}

class DefaultSrsScheduler(
    private val fsrsScheduler: FsrsScheduler
) : SrsScheduler {

    override val algorithmVersion: FsrsAlgorithmVersion = fsrsScheduler.algorithmVersion
    override val parameterSetId: String = fsrsScheduler.parameterSetId

    override fun newCard(): SrsCard = SrsCard(fsrsScheduler.newCard())

    override fun answers(
        data: SrsCard,
        reviewTime: Instant
    ): SrsAnswers {
        return fsrsScheduler.schedule(data.fsrsCard, reviewTime).let {
            SrsAnswers(
                again = SrsAnswer(FsrsReviewRating.Again.grade, SrsCard(it.again)),
                hard = SrsAnswer(FsrsReviewRating.Hard.grade, SrsCard(it.hard)),
                good = SrsAnswer(FsrsReviewRating.Good.grade, SrsCard(it.good)),
                easy = SrsAnswer(FsrsReviewRating.Easy.grade, SrsCard(it.easy))
            )
        }
    }

}