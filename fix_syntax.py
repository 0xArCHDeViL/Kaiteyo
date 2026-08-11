import re
import os

def fix_japanese_word_ui():
    path = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/common/JapaneseWordUI.kt"
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace("import androidx.compose.ui.unit.dp\npackage ua.syt0r.kanji.presentation.common", "package ua.syt0r.kanji.presentation.common\nimport androidx.compose.ui.unit.dp")
    with open(path, 'w') as f:
        f.write(content)

def fix_deck_dashboard_list_item():
    path = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/dashboard_common/DeckDashboardListItem.kt"
    with open(path, 'r') as f:
        content = f.read()
    # It probably looks like: withStyle(\n androidx.compose...toSpanStyle().copy(...),\n)
    content = re.sub(r'withStyle\(\s*androidx\.compose\.material3\.MaterialTheme\.typography\.bodyMedium\.toSpanStyle\(\)\.copy\(fontWeight = FontWeight\.Light\),\s*\)', r'withStyle(androidx.compose.material3.MaterialTheme.typography.bodyMedium.toSpanStyle().copy(fontWeight = FontWeight.Light))', content)
    with open(path, 'w') as f:
        f.write(content)

def fix_last_week_study():
    path = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/stats/LastWeekStudy.kt"
    with open(path, 'r') as f:
        content = f.read()
    content = re.sub(r'withStyle\(\s*androidx\.compose\.material3\.MaterialTheme\.typography\.headlineSmall\.toSpanStyle\(\)\.copy\(fontWeight = FontWeight\.Bold\),\s*\)', r'withStyle(androidx.compose.material3.MaterialTheme.typography.headlineSmall.toSpanStyle().copy(fontWeight = FontWeight.Bold))', content)
    with open(path, 'w') as f:
        f.write(content)

def fix_grammar_screen():
    path = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/library/screen/grammar/GrammarScreen.kt"
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace("androidx.compose.material3.MaterialTheme.typography.titleMedium.toSpanStyle().copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary),", "androidx.compose.material3.MaterialTheme.typography.titleMedium.toSpanStyle().copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)")
    with open(path, 'w') as f:
        f.write(content)

def fix_conjugation_use_case():
    path = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_grammar/use_case/GetGrammarPracticeConjugationDataUseCase.kt"
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace('Triple("たべる", "Makan (Bentuk Kamus)")', 'Pair("たべる", "Makan (Bentuk Kamus)")')
    content = content.replace('Triple("いく", "Pergi (Bentuk Kamus)")', 'Pair("いく", "Pergi (Bentuk Kamus)")')
    content = content.replace('Triple("のむ", "Minum (Bentuk Kamus)")', 'Pair("のむ", "Minum (Bentuk Kamus)")')
    content = content.replace('Triple("する", "Melakukan (Bentuk Kamus)")', 'Pair("する", "Melakukan (Bentuk Kamus)")')
    content = content.replace('Triple("くる", "Datang (Bentuk Kamus)")', 'Pair("くる", "Datang (Bentuk Kamus)")')
    with open(path, 'w') as f:
        f.write(content)

fix_japanese_word_ui()
fix_deck_dashboard_list_item()
fix_last_week_study()
fix_grammar_screen()
fix_conjugation_use_case()

