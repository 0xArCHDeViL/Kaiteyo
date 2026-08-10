import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

# 11.sp -> labelSmall
# 12.sp -> bodySmall
# 13.sp -> bodySmall
# 14.sp -> bodyMedium
# 16.sp -> bodyLarge
# 18.sp -> titleMedium
# 20.sp -> titleLarge
# 22.sp -> titleLarge
# 24.sp -> headlineSmall
# 28.sp -> headlineMedium
# 32.sp -> headlineLarge
# 36.sp -> displaySmall

font_mapping = {
    "11.sp": "labelSmall",
    "12.sp": "bodySmall",
    "13.sp": "bodySmall",
    "14.sp": "bodyMedium",
    "15.sp": "bodyMedium",
    "16.sp": "bodyLarge",
    "18.sp": "titleMedium",
    "20.sp": "titleLarge",
    "22.sp": "titleLarge",
    "24.sp": "headlineSmall",
    "28.sp": "headlineMedium",
    "32.sp": "headlineLarge",
    "36.sp": "displaySmall",
    "48.sp": "displayMedium",
    "57.sp": "displayLarge",
    "10.sp": "labelSmall",
    "8.sp": "labelSmall"
}

def replace_fontsize(match):
    val = match.group(1)
    if val in font_mapping:
        return f"style = androidx.compose.material3.MaterialTheme.typography.{font_mapping[val]}"
    return match.group(0)

# We will just replace `fontSize = X.sp` with `style = ...`
# If there is already a style, the compiler will complain, we can fix it manually because there shouldn't be many.
for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") and "theme" not in root:
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = re.sub(r'fontSize\s*=\s*([0-9]+\.sp)', replace_fontsize, content)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
