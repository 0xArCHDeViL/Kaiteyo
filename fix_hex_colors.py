import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

color_mapping = {
    "0xFFC2FC8B": "ua.syt0r.kanji.presentation.common.theme.semanticSuccess",
    "0xFF7BC8FF": "ua.syt0r.kanji.presentation.common.theme.semanticInfo",
    "0xFFA78BFA": "ua.syt0r.kanji.presentation.common.theme.semanticNew",
    "0xFFFEAB57": "ua.syt0r.kanji.presentation.common.theme.semanticWarning",
    "0xFFFF6B6B": "ua.syt0r.kanji.presentation.common.theme.semanticError",
    "0xFFB0B0B0": "androidx.compose.ui.graphics.Color.Gray",
    "0xFF808080": "androidx.compose.ui.graphics.Color.DarkGray",
    "0xFFFFD93D": "ua.syt0r.kanji.presentation.common.theme.semanticWarning",
    
    # Text colors
    "0xFFF0F0F0": "androidx.compose.material3.MaterialTheme.colorScheme.onSurface",
    "0xFF000000": "androidx.compose.ui.graphics.Color.Black",
    "0xFFFFFFFF": "androidx.compose.ui.graphics.Color.White"
}

def replace_color(match):
    value = match.group(1)
    if value in color_mapping:
        return color_mapping[value]
    return match.group(0)

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") and "theme" not in root:
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = re.sub(r'Color\(\s*(0xFF[0-9a-fA-F]+)\s*\)', replace_color, content)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
