import re

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeConjugationUI.kt', 'r') as f:
    content = f.read()

# Add import for FormulaText if not present
if "FormulaText" not in content:
    content = "import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.FormulaText\n" + content

replacement = """        Text(
            text = "Conjugate: " + state.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimens.Space2))
        
        FormulaText(
            text = state.formula,
        )"""

content = re.sub(r'        Text\(\s*text = "Conjugate the Verb",\s*style = MaterialTheme\.typography\.titleMedium,\s*color = MaterialTheme\.colorScheme\.primary,\s*fontWeight = FontWeight\.Bold\s*\)', replacement, content, flags=re.DOTALL)

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeConjugationUI.kt', 'w') as f:
    f.write(content)

