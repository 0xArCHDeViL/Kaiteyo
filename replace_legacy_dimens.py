import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

replacements = {
    "Dimens.SpacingTiny": "Dimens.Space1",
    "Dimens.SpacingSmall": "Dimens.Space1",
    "Dimens.SpacingMid": "Dimens.Space2",
    "Dimens.SpacingBig": "Dimens.Space3"
}

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = content
            for old, new in replacements.items():
                new_content = new_content.replace(old, new)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
