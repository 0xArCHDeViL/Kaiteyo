import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

alpha_mapping = {
    "0.08f": "Dimens.Alpha.Subtle",
    "0.1f": "Dimens.Alpha.Subtle",
    "0.12f": "Dimens.Alpha.Light",
    "0.15f": "Dimens.Alpha.Light",
    "0.2f": "Dimens.Alpha.Light",
    "0.20f": "Dimens.Alpha.Light",
    "0.25f": "Dimens.Alpha.Medium",
    "0.3f": "Dimens.Alpha.Medium",
    "0.35f": "Dimens.Alpha.Medium",
    "0.5f": "Dimens.Alpha.SemiOpaque",
    "0.6f": "Dimens.Alpha.SemiOpaque",
    "0.85f": "Dimens.Alpha.HighEmphasis",
    "0.87f": "Dimens.Alpha.HighEmphasis"
}

def replace_alpha(match):
    value = match.group(1)
    if value in alpha_mapping:
        return f".copy(alpha = {alpha_mapping[value]})"
    return match.group(0)

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") and "theme" not in root:
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = re.sub(r'\.copy\(\s*alpha\s*=\s*([0-9]+\.[0-9]+f)\s*\)', replace_alpha, content)
            
            if new_content != content:
                if "import ua.syt0r.kanji.presentation.common.theme.Dimens" not in new_content:
                    new_content = re.sub(r'(package [^\n]+)\n', r'\1\n\nimport ua.syt0r.kanji.presentation.common.theme.Dimens\n', new_content)
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
