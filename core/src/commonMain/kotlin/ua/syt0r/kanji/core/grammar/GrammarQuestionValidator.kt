package ua.syt0r.kanji.core.grammar

object GrammarQuestionValidator {

    fun cloze(question: ClozeQuestion): Boolean =
        question.answer.isNotBlank() &&
            question.options.size == 4 &&
            question.options.distinct().size == question.options.size &&
            question.answer in question.options &&
            question.sentence.contains("____")

    fun conjugation(question: ConjugationQuestion): Boolean =
        question.dictionaryForm.isNotBlank() &&
            question.answer.isNotBlank() &&
            question.syllables.isNotEmpty() &&
            question.syllables.joinToString("").contains(question.answer.first())

    fun scramble(question: ScrambleQuestion): Boolean =
        question.tokens.size >= 3 &&
            question.tokens.isNotEmpty() &&
            question.sentence.isNotBlank()

    fun dialogue(question: DialogueQuestion): Boolean =
        question.lines.size >= 2 &&
            question.options.size == 4 &&
            question.options.distinct().size == question.options.size &&
            question.answer in question.options
}
