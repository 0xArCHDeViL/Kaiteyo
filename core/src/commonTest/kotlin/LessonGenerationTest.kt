import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LessonCandidateFeatures
import ua.syt0r.kanji.core.connected_learning.LessonCandidateScorer
import ua.syt0r.kanji.core.connected_learning.LessonSelectionPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryLevel

class LessonGenerationTest {

    @Test
    fun scoreUsesNormalizedWeightsAndIsBounded() {
        val candidate = LessonCandidateFeatures(
            nodeKey = ConnectedNodeKey("kanji:休"),
            nodeKind = GraphNodeKind.KANJI,
            prerequisiteReadiness = 1.0,
            learnerWeakness = 1.0,
            graphLeverage = 1.0,
            levelRelevance = 1.0,
            novelty = 1.0,
            mastery = MasteryLevel.New,
        )

        assertEquals(1.0, LessonCandidateScorer.score(candidate).score)
    }

    @Test
    fun rankingIsDeterministicWithStableTieBreaker() {
        val candidates = listOf(
            candidate("kanji:明", GraphNodeKind.KANJI, graphLeverage = 0.5),
            candidate("kanji:休", GraphNodeKind.KANJI, graphLeverage = 0.5),
        )

        val first = LessonCandidateScorer.rank(candidates).map { it.features.nodeKey.value }
        val second = LessonCandidateScorer.rank(candidates.reversed()).map { it.features.nodeKey.value }

        assertEquals(first, second)
        assertEquals(listOf("kanji:休", "kanji:明"), first)
    }

    @Test
    fun selectionExcludesDueReviewAndCapsNodeKind() {
        val candidates = listOf(
            candidate("kanji:休", GraphNodeKind.KANJI, graphLeverage = 1.0),
            candidate("kanji:明", GraphNodeKind.KANJI, graphLeverage = 0.9),
            candidate("reading:休|やすむ", GraphNodeKind.READING, graphLeverage = 0.8),
            candidate("kanji:旧", GraphNodeKind.KANJI, graphLeverage = 1.0, isDueReview = true),
        )

        val selected = LessonCandidateScorer.select(
            candidates = candidates,
            policy = LessonSelectionPolicy(
                size = 3,
                maxPerKind = 1,
                includeDueReview = false,
            ),
        )

        assertEquals(
            listOf("kanji:休", "reading:休|やすむ"),
            selected.map { it.features.nodeKey.value },
        )
        assertTrue(selected.none { it.features.isDueReview })
    }

    @Test
    fun weaknessMappingFollowsMasteryOrder() {
        assertTrue(
            LessonCandidateScorer.weaknessFromMastery(MasteryLevel.New) >
                LessonCandidateScorer.weaknessFromMastery(MasteryLevel.Stable)
        )
        assertEquals(0.0, LessonCandidateScorer.weaknessFromMastery(MasteryLevel.Mastered))
    }

    private fun candidate(
        key: String,
        kind: GraphNodeKind,
        graphLeverage: Double,
        isDueReview: Boolean = false,
    ) = LessonCandidateFeatures(
        nodeKey = ConnectedNodeKey(key),
        nodeKind = kind,
        prerequisiteReadiness = 1.0,
        learnerWeakness = 1.0,
        graphLeverage = graphLeverage,
        levelRelevance = 1.0,
        novelty = 1.0,
        mastery = MasteryLevel.New,
        isDueReview = isDueReview,
    )
}
