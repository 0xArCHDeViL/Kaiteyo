import os

files = [
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeClozeUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeConjugationUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeScrambleUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeDialogueUI.kt",
    "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/ui/GrammarPracticeFlashcardUI.kt"
]

import_dimens = "import ua.syt0r.kanji.presentation.common.theme.Dimens\n"

for path in files:
    if not os.path.exists(path):
        continue
    
    with open(path, 'r') as f:
        content = f.read()
    
    if import_dimens not in content:
        content = content.replace('import androidx.compose.ui.unit.dp\n', 'import androidx.compose.ui.unit.dp\n' + import_dimens)
    
    # Remove ALL shadowElevation lines
    import re
    content = re.sub(r'\s*shadowElevation\s*=\s*[^,]+,?', '', content)
    
    # Replace padding(24.dp) with padding(Dimens.WindowPadding)
    content = content.replace('padding(24.dp)', 'padding(Dimens.WindowPadding)')
    content = content.replace('padding(32.dp)', 'padding(Dimens.Space8)')
    content = content.replace('padding(16.dp)', 'padding(Dimens.Space4)')
    content = content.replace('padding(8.dp)', 'padding(Dimens.Space2)')
    content = content.replace('padding(horizontal = 24.dp', 'padding(horizontal = Dimens.Space6')
    content = content.replace('padding(horizontal = 20.dp', 'padding(horizontal = Dimens.Space5')
    content = content.replace('padding(horizontal = 16.dp', 'padding(horizontal = Dimens.Space4')
    content = content.replace('padding(vertical = 14.dp)', 'padding(vertical = Dimens.Space3)')
    content = content.replace('padding(vertical = 16.dp)', 'padding(vertical = Dimens.Space4)')
    content = content.replace('padding(vertical = 18.dp)', 'padding(vertical = Dimens.Space4)')
    content = content.replace('padding(vertical = 8.dp)', 'padding(vertical = Dimens.Space2)')
    
    # Replace Spacers
    content = content.replace('Spacer(modifier = Modifier.height(48.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space12))')
    content = content.replace('Spacer(modifier = Modifier.height(40.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space10))')
    content = content.replace('Spacer(modifier = Modifier.height(32.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space8))')
    content = content.replace('Spacer(modifier = Modifier.height(24.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space6))')
    content = content.replace('Spacer(modifier = Modifier.height(16.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space4))')
    content = content.replace('Spacer(modifier = Modifier.height(8.dp))', 'Spacer(modifier = Modifier.height(Dimens.Space2))')
    
    # Replace Shapes
    content = content.replace('RoundedCornerShape(32.dp)', 'MaterialTheme.shapes.extraLarge')
    content = content.replace('RoundedCornerShape(24.dp)', 'MaterialTheme.shapes.extraLarge')
    content = content.replace('RoundedCornerShape(16.dp)', 'MaterialTheme.shapes.large')
    content = content.replace('RoundedCornerShape(12.dp)', 'MaterialTheme.shapes.medium')
    content = content.replace('RoundedCornerShape(8.dp)', 'MaterialTheme.shapes.small')
    content = content.replace('RoundedCornerShape(\n                            topStart = 20.dp,\n                            topEnd = 20.dp,\n                            bottomStart = if (isUser) 20.dp else 4.dp,\n                            bottomEnd = if (isUser) 4.dp else 20.dp\n                        )', 'MaterialTheme.shapes.large')
    
    # Replace Arrangement.spacedBy
    content = content.replace('Arrangement.spacedBy(16.dp)', 'Arrangement.spacedBy(Dimens.Space4)')
    content = content.replace('Arrangement.spacedBy(12.dp', 'Arrangement.spacedBy(Dimens.Space3')
    content = content.replace('Arrangement.spacedBy(8.dp', 'Arrangement.spacedBy(Dimens.Space2')
    
    # Other DP replacements
    content = content.replace('56.dp', 'Dimens.Space12')
    content = content.replace('80.dp', 'Dimens.Space20')
    content = content.replace('100.dp', '100.dp') # leave for minHeight

    with open(path, 'w') as f:
        f.write(content)

print("Applied standard Dimens and Shapes!")
