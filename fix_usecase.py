import re

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/use_case/GetGrammarPracticeConjugationDataUseCase.kt', 'r') as f:
    content = f.read()

# Make sure it can handle the full options!
replacement = """        val target = when {
            formula.contains("~~ます~~") -> stem + formula.substringAfter("~~ます~~").replace(" ", "").replace("／だめです", "")
            formula.contains("~~ない~~") -> naiStem + formula.substringAfter("~~ない~~").replace(" ", "").replace("／だめです", "")
            formula.contains("ないで") -> naiStem + "ないで" + formula.substringAfter("ないで").replace(" ", "").replace("ください", "ください")
            formula.contains("ない") -> naiStem + "ない" + formula.substringAfter("ない").replace(" ", "").replace("／", "")
            formula.contains("て") || formula.contains("で") -> teForm + formula.substringAfter("て").substringAfter("で").replace(" ", "")
            else -> dict
        }"""

content = re.sub(r'        val target = when \{.*?        \}', replacement, content, flags=re.DOTALL)

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/use_case/GetGrammarPracticeConjugationDataUseCase.kt', 'w') as f:
    f.write(content)

