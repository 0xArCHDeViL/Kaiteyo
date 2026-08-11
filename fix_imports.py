import re

def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()

    imports_to_add = """
import androidx.compose.foundation.clickable
import ua.syt0r.kanji.presentation.common.ui.AppListItem
import ua.syt0r.kanji.presentation.common.ui.AppListItemDefaults
"""
    content = content.replace("import androidx.compose.ui.unit.dp\n", "import androidx.compose.ui.unit.dp\n" + imports_to_add)

    with open(path, 'w') as f:
        f.write(content)

fix_file("core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/common/JapaneseWordUI.kt")
