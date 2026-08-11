import re

def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()

    content = content.replace("bottomSheetState.isExpanded", "bottomSheetState.currentValue == androidx.compose.material3.SheetValue.Expanded")
    content = content.replace("bottomSheetState.collapse()", "bottomSheetState.partialExpand()")

    with open(path, 'w') as f:
        f.write(content)

fix_file("core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_letter/ui/LetterPracticeWritingUI.kt")
