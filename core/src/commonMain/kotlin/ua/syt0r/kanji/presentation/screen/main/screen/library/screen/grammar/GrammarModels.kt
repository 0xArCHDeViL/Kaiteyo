package ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrammarChapter(
    val id: Int,
    val title: String,
    val points: List<GrammarPoint>
)

@Serializable
data class GrammarPoint(
    val number: String,
    @SerialName("formula_title")
    val formulaTitle: String,
    val meaning: String,
    val formulas: List<String>,
    val examples: List<String>,
    val notes: String
)
