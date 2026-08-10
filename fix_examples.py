import re

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeFlashcardUI.kt', 'r') as f:
    content = f.read()

replacement = """                                if (state.examples.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(Dimens.Space6))
                                    state.examples.forEach { example ->
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.Space2)
                                        ) {
                                            Text(
                                                text = example,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(Dimens.Space4)
                                            )
                                        }
                                    }
                                }"""

content = re.sub(r'                                if \(state\.examples\.isNotEmpty\(\)\) \{.*?                                \}', replacement, content, flags=re.DOTALL)

with open('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeFlashcardUI.kt', 'w') as f:
    f.write(content)

