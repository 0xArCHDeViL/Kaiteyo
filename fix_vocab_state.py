import re

filepath = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_vocab/data/VocabPracticeScreenData.kt"

with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    "interface Writing : VocabReviewState {\n        val charactersData:",
    "interface Writing : VocabReviewState {\n        val summaryReading: FuriganaString\n        val charactersData:"
)

with open(filepath, 'w') as f:
    f.write(content)
