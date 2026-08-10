import re

files_to_fix = [
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/library/screen/grammar/GrammarScreen.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeFlashcardUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeClozeUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeDialogueUI.kt"
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()
    
    if "GrammarScreen.kt" in filepath:
        content = content.replace("text = point.formulaTitle,", "text = point.formulaTitle,")
        content = re.sub(r'Text\(\s*text = point\.formulaTitle,', r'FormulaText(\n                                text = point.formulaTitle,', content)
    else:
        content = re.sub(r'Text\(\s*text = state\.title,', r'FormulaText(\n                                text = state.title,', content)

    with open(filepath, 'w') as f:
        f.write(content)
