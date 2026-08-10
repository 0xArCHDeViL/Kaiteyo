import os
import re

filepath = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_common/PracticeCommonUI.kt"

with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("shadowElevation = 8.dp", "shadowElevation = Dimens.ElevationLg")
content = content.replace("padding(20.dp)", "padding(Dimens.Space5)")
content = content.replace("height(56.dp)", "height(Dimens.ButtonHeight)")
content = content.replace("padding(vertical = 50.dp)", "padding(vertical = Dimens.Space10)")

with open(filepath, 'w') as f:
    f.write(content)
