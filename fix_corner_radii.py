import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

radius_mapping = {
    "4.dp": "Dimens.RadiusXs",
    "6.dp": "Dimens.RadiusSm",
    "8.dp": "Dimens.RadiusSm",
    "10.dp": "Dimens.RadiusMd",
    "12.dp": "Dimens.RadiusMd",
    "14.dp": "Dimens.RadiusLg",
    "16.dp": "Dimens.RadiusLg",
    "18.dp": "Dimens.RadiusXl",
    "20.dp": "Dimens.RadiusXl",
    "24.dp": "Dimens.RadiusXl",
    "32.dp": "Dimens.Radius2xl",
    "48.dp": "Dimens.Radius2xl"
}

def replace_rounded_corner(match):
    value = match.group(1)
    if value in radius_mapping:
        return f"RoundedCornerShape({radius_mapping[value]})"
    return match.group(0)

# also replace CircleShape with RoundedCornerShape(Dimens.RadiusFull) 
# wait, CircleShape is perfectly valid M3. Let's NOT replace CircleShape unless it's used in cards.
# Actually, the plan says replace CircleShape usage in cards, but we can just leave CircleShape as it's standard Compose shape. Let's focus on RoundedCornerShape first.

import sys
for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") and "theme" not in root:
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            # regex for RoundedCornerShape(X.dp)
            new_content = re.sub(r'RoundedCornerShape\(\s*([0-9]+\.dp)\s*\)', replace_rounded_corner, content)
            
            # we also need to add import ua.syt0r.kanji.presentation.common.theme.Dimens if it was changed
            if new_content != content:
                if "import ua.syt0r.kanji.presentation.common.theme.Dimens" not in new_content:
                    # add import after package
                    new_content = re.sub(r'(package [^\n]+)\n', r'\1\n\nimport ua.syt0r.kanji.presentation.common.theme.Dimens\n', new_content)
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
