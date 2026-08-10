import os

filepath = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_letter/ui/LetterPracticeWritingUI.kt"
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.BottomSheetScaffoldState", "androidx.compose.material3.BottomSheetScaffoldState")
content = content.replace("androidx.compose.material.BottomSheetValue", "androidx.compose.material3.SheetValue")
content = content.replace("androidx.compose.material.ExperimentalMaterialApi", "androidx.compose.material3.ExperimentalMaterial3Api")
content = content.replace("androidx.compose.material.rememberBottomSheetScaffoldState", "androidx.compose.material3.rememberBottomSheetScaffoldState")

# We might also need to replace BottomSheetValue.Collapsed/Expanded with SheetValue.Hidden/Expanded/PartiallyExpanded
content = content.replace("BottomSheetValue.Collapsed", "SheetValue.Hidden")
content = content.replace("BottomSheetValue.Expanded", "SheetValue.Expanded")

with open(filepath, 'w') as f:
    f.write(content)
print("Fixed M3 imports in LetterPracticeWritingUI.kt")
