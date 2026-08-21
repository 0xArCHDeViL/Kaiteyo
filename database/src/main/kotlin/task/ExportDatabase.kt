package task

import ProjectData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import export.db.*
import export.json.JsonCharacterData
import export.json.JsonExporter
import org.apache.commons.csv.CSVFormat
import parser.CompositeJMdictParser
import parser.LegacyExpressionFallback
import parser.RadkFileParser
import parser.StreamingJMdictParser
import parser.StreamingJMnedictParser
import parser.withFallback
import java.io.File

const val ExportFileNameTemplate = "kanji-dojo-data-base-v%d.sql"
private const val DefaultExportDatabaseSchemaVersion = 22
val ExportDatabaseVersion: Int
    get() = System.getProperty("appDataVersion")?.toIntOrNull() ?: 22
val ExportDatabaseSchemaVersion: Int
    get() = System.getProperty("appDataSchemaVersion")?.toIntOrNull()
        ?: DefaultExportDatabaseSchemaVersion

private const val MaxExamplesPerLetter = 5

fun main() {

    val charactersDir = ProjectData.exportCharactersDir
    val characterFiles = charactersDir.listFiles()?.sortedBy { it.name }
    if (characterFiles.isNullOrEmpty())
        throw IllegalStateException("No characters data found")

    val radicals = RadkFileParser().parse(ProjectData.radkFile)

    val gson = Gson()
    val characters = characterFiles.map { JsonCharacterData.readFromFile(it, gson) }

    val kanjiCharacters = characters.filterIsInstance<JsonCharacterData.Kanji>()

    val exportStrokesData = characters.map { DatabaseCharacterStrokeData(it.value, it.strokes) }

    val exportKanjiData = kanjiCharacters.map {
        val meanings = it.meanings?.firstOrNull { it.locale == "en" }?.values
        if (meanings == null) println("Warning! ${it.value} has no meanings")
        DatabaseKanjiData(
            kanji = it.value,
            meanings = meanings ?: emptyList(),
            onReadings = it.onReadings ?: emptyList(),
            kunReadings = it.kunReadings ?: emptyList(),
            frequency = it.frequency,
            variantFamily = it.variantsFamily
        )
    }

    val exportKanjiRadicals = kanjiCharacters.flatMap { kanjiData ->
        kanjiData.radicals?.map {
            DatabaseKanjiRadical(
                kanji = kanjiData.value,
                radical = it.radical,
                startPosition = it.startStroke,
                strokesCount = it.strokes
            )
        } ?: emptyList()
    }

    val radicalsInUse = exportKanjiRadicals.map { it.radical }.toSet()
    val filteredRadicals = radicals.filter { it.extraData == null && radicalsInUse.contains(it.radical) }
        .map { it.radical }
        .toSet()

    val filteredOutRadicals = radicals.map { it.radical }.toSet().minus(filteredRadicals)

    println("Filtering out weird and unused radicals [${filteredOutRadicals.size}]: $filteredOutRadicals")

    val exportRadicals: List<DatabaseRadical> = radicals.filter { filteredRadicals.contains(it.radical) }
        .map { DatabaseRadical(it.radical, it.strokes) }

    val exportKanjiClassifications = ProjectData.exportLetterDecksDir.listFiles()!!
        .sortedBy { it.name }
        .flatMap { file ->
            file.readText().split("\n").map {
                DatabaseKanjiClassification(
                    kanji = it,
                    classification = file.nameWithoutExtension
                )
            }
        }

    val exportLetterVocabExamples = readExportVocabExamples()

    val canonicalVocabIds = ProjectData.supportedVocab
        .readLines()
        .map { it.toLong() }
        .toSet()
    val jmdictVocabIds = collectJmdictEntryIds(ProjectData.jMdictWithExamplesFile)
    val exportVocabIdSet = canonicalVocabIds + jmdictVocabIds
    println(
        "Exporting full JMdict vocabulary: ${jmdictVocabIds.size} source entries " +
            "+ ${canonicalVocabIds subtract jmdictVocabIds} canonical fallback IDs"
    )

    val parsedVocabData = CompositeJMdictParser.parse(exportVocabIdSet)
    val missingVocabIds = exportVocabIdSet - parsedVocabData.entries.map { it.id }.toSet()
    val exportVocabData = parsedVocabData.withFallback(
        LegacyExpressionFallback.parseMissing(missingVocabIds)
    )
    val exportVocabDeckCards: List<Vocab_deck_card> = getVocabImports()
    val exportNames = getExportNames()

    val exportSentences = getExportSentences()

    assertVocabData(exportVocabData, exportVocabIdSet)
    assertVocabDeckCards(exportVocabDeckCards, exportVocabIdSet)

    val outputFile = File(ExportFileNameTemplate.format(ExportDatabaseVersion))
    DatabaseExporter(
        file = outputFile,
        schemaVersion = ExportDatabaseSchemaVersion,
        packRevision = ExportDatabaseVersion,
    ).apply {
        writeStrokes(exportStrokesData)
        writeKanjiData(exportKanjiData)
        writeKanjiRadicals(exportKanjiRadicals)
        writeRadicals(exportRadicals)
        writeKanjiClassifications(exportKanjiClassifications)
        writeLetterVocabExamples(exportLetterVocabExamples)
        writeVocab(exportVocabData)
        writeNames(exportNames)
        writeVocabDeckCards(exportVocabDeckCards)
        writeSentences(exportSentences)

        val (learningNodes, learningEdges) = buildLearningGraph(
            radicals = exportRadicals,
            kanjiData = exportKanjiData,
            kanjiRadicals = exportKanjiRadicals,
            vocabulary = exportVocabData,
            sentences = exportSentences,
        )
        writeLearningGraph(learningNodes, learningEdges)
        compact()
    }

    DatabaseIntegrityValidator.validate(
        file = outputFile,
        expectedKanjiCount = exportKanjiData.size,
        expectedVocabCount = exportVocabIdSet.size,
        expectedSentenceCount = exportSentences.size,
        expectedDeckCardCount = exportVocabDeckCards.size
    )
    println("Validated database artifact ${outputFile.name}")
}

private fun collectJmdictEntryIds(file: File): Set<Long> = buildSet {
    StreamingJMdictParser.forEachEntry(file) { entry ->
        add(entry.entrySequence)
    }
}

fun getExportNames(): List<DatabaseName> = buildList {
    StreamingJMnedictParser.forEach(ProjectData.jMnedictFile) { entry ->
        val kanji = entry.kanji.firstOrNull()
        val kana = entry.readings.firstOrNull() ?: return@forEach
        val meaning = entry.englishTranslations.distinct().joinToString("; ")
        if (meaning.isNotEmpty()) {
            add(
                DatabaseName(
                    id = entry.entrySequence,
                    kanji = kanji,
                    kana = kana,
                    nameType = entry.nameTypes.distinct().joinToString("; ").takeIf { it.isNotEmpty() },
                    meaning = meaning
                )
            )
        }
    }
}

fun getExportSentences(): List<Sentence> {
    val gson = Gson()
    return JsonExporter.getSentences().map {
        Sentence(
            tatoeba_id = it.tatoebaId,
            sentence = it.sentence,
            translation = it.translation,
            score = it.score.toDouble(),
            furigana = it.furigana
                .map { DatabaseFuriganaItem(it.text, it.annotation) }
                .let { gson.toJson(it) }
        )
    }
}

fun getVocabImports(): List<Vocab_deck_card> {
    val csvFormat = CSVFormat.Builder.create().get()
    return ProjectData.exportVocabDecksDir.listFiles()!!
        .sortedBy { it.name }
        .flatMap { file -> csvFormat.parse(file.reader()).toList().map { file.nameWithoutExtension to it.values() } }
        .map { (fileName, values) ->
            Vocab_deck_card(
                jmdict_seq = values[0].toLong(),
                kanji = values[1].takeIf { it.isNotEmpty() },
                kana = values[2],
                definition = values.getOrNull(3),
                priority = null,
                deck = fileName
            )
        }
}

private fun readExportVocabExamples(): List<Letter_vocab_example> {
    val typeToken = object : TypeToken<List<LetterRepresentationItem>>() {}
    return Gson().fromJson(ProjectData.letterVocabExamples.readText(), typeToken).flatMap {
        it.vocabExamples.take(MaxExamplesPerLetter).map { vocab ->
            Letter_vocab_example(
                letter = it.letter,
                vocab_id = vocab.id,
                kanji = vocab.kanjiReading,
                kana = vocab.kanaReading
            )
        }
    }
}

private fun assertVocabData(exportVocabData: DatabaseVocabData, expectedVocabIds: Set<Long>) {
    val actualWords = exportVocabData.entries.map { it.id }.toSet()
    if (actualWords != expectedVocabIds) {
        val missing = expectedVocabIds.minus(actualWords)
        val extra = actualWords.minus(expectedVocabIds)
        error("Missing exported vocab $missing, extra exported vocab $extra")
    }
}

private fun assertVocabDeckCards(vocabDeckCards: List<Vocab_deck_card>, exportedVocabIds: Set<Long>) {
    val importedVocabIdSet = vocabDeckCards.map { it.jmdict_seq }.toSet()
    if (!exportedVocabIds.containsAll(importedVocabIdSet)) {
        val missing = importedVocabIdSet.minus(exportedVocabIds)
        error("Missing vocab used in imports $missing")
    }
}
