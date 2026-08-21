package ua.syt0r.kanji.core.connected_learning

/**
 * Stable identity for an entity in the connected-learning graph.
 *
 * The canonical key is deliberately based on source identity rather than
 * rendered surface text. This is what keeps entries such as 主夫 and 主婦
 * separate even when their reading is the same.
 */
sealed interface ConnectedEntityKey {
    fun canonicalKey(): String

    data class Component(val key: String) : ConnectedEntityKey {
        override fun canonicalKey(): String = "component:${KeyCodec.encode(key)}"
    }

    data class Kanji(val character: String) : ConnectedEntityKey {
        init {
            require(character.isNotEmpty()) { "Kanji character must not be empty" }
        }

        override fun canonicalKey(): String = "kanji:${KeyCodec.encode(character)}"
    }

    data class Reading(
        val kanji: String,
        val reading: String,
    ) : ConnectedEntityKey {
        override fun canonicalKey(): String = buildString {
            append("reading:")
            append(KeyCodec.encode(kanji))
            append('|')
            append(KeyCodec.encode(reading))
        }
    }

    data class VocabularyElement(
        val entryId: Long,
        val elementId: Long,
        val reading: String,
    ) : ConnectedEntityKey {
        init {
            require(entryId > 0) { "Vocabulary entry ID must be positive" }
            require(elementId > 0) { "Vocabulary element ID must be positive" }
        }

        override fun canonicalKey(): String = buildString {
            append("vocab-element:")
            append(entryId)
            append('|')
            append(elementId)
            append('|')
            append(KeyCodec.encode(reading))
        }
    }

    data class LegacyVocabularyEntry(val entryId: Long) : ConnectedEntityKey {
        init {
            require(entryId > 0) { "Vocabulary entry ID must be positive" }
        }

        override fun canonicalKey(): String = "vocab-entry:${entryId}"
    }

    data class Sense(
        val entryId: Long,
        val senseId: Long,
    ) : ConnectedEntityKey {
        init {
            require(entryId > 0) { "Vocabulary entry ID must be positive" }
            require(senseId > 0) { "Sense ID must be positive" }
        }

        override fun canonicalKey(): String = "sense:$entryId|$senseId"
    }

    data class Sentence(val sentenceId: Long) : ConnectedEntityKey {
        init {
            require(sentenceId > 0) { "Sentence ID must be positive" }
        }

        override fun canonicalKey(): String = "sentence:$sentenceId"
    }

    data class Grammar(val grammarId: String) : ConnectedEntityKey {
        init {
            require(grammarId.isNotEmpty()) { "Grammar ID must not be empty" }
        }

        override fun canonicalKey(): String = "grammar:${KeyCodec.encode(grammarId)}"
    }
}

@JvmInline
value class ConnectedNodeKey(val value: String) {
    init {
        require(value.isNotEmpty()) { "Node key must not be empty" }
    }

    override fun toString(): String = value

    companion object {
        fun from(entity: ConnectedEntityKey): ConnectedNodeKey =
            ConnectedNodeKey(entity.canonicalKey())
    }
}

@JvmInline
value class ConnectedItemKey(val value: String) {
    init {
        require(value.isNotEmpty()) { "Item key must not be empty" }
    }

    override fun toString(): String = value

    companion object {
        fun from(
            entity: ConnectedEntityKey,
            variant: String = "default",
        ): ConnectedItemKey {
            require(variant.isNotEmpty()) { "Item variant must not be empty" }
            return ConnectedItemKey(
                "item:${entity.canonicalKey()}|variant:${KeyCodec.encode(variant)}"
            )
        }
    }
}

enum class GraphNodeKind {
    COMPONENT,
    KANJI,
    READING,
    VOCABULARY_ELEMENT,
    LEGACY_VOCABULARY_ENTRY,
    SENSE,
    SENTENCE,
    GRAMMAR,
}

enum class GraphEdgeKind {
    COMPOSED_OF,
    HAS_READING,
    READING_OF,
    HAS_VOCABULARY,
    HAS_SENSE,
    SENSE_RESTRICTS_READING,
    APPEARS_IN_SENTENCE,
    ILLUSTRATES_GRAMMAR,
    PREREQUISITE_OF,
    RELATED_BY_COMPONENT,
}

enum class GraphProvenance {
    SOURCE_DATA,
    DERIVED_AT_EXPORT,
    LEARNING_ASSOCIATION,
    USER_AUTHORED,
}

enum class ReviewDimension {
    COMPONENT_RECOGNITION,
    KANJI_MEANING,
    KANJI_READING,
    VOCAB_MEANING,
    VOCAB_READING,
    VOCAB_SENSE,
    SENTENCE_COMPREHENSION,
    GRAMMAR_APPLICATION,
    WRITING_PRODUCTION,
    LETTER_WRITING,
    LETTER_READING,
    VOCAB_WRITING,
    GRAMMAR_FLASHCARD,
    GRAMMAR_CLOZE,
    GRAMMAR_CONJUGATION,
    GRAMMAR_SENTENCE_SCRAMBLE,
    GRAMMAR_SURVIVAL_DIALOGUE,
}

@JvmInline
value class GraphRevision(val value: String) {
    init {
        require(value.isNotEmpty()) { "Graph revision must not be empty" }
    }
}

private object KeyCodec {
    private val hex = "0123456789ABCDEF"

    fun encode(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character) {
                '%', ':', '|', '=' -> {
                    append('%')
                    append(hex[character.code ushr 4 and 0xF])
                    append(hex[character.code and 0xF])
                }
                else -> append(character)
            }
        }
    }
}
