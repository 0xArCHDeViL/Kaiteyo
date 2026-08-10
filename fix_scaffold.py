import os
import re

directory = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/"

def replace_scaffold(content):
    # Check if Scaffold is imported
    if "import androidx.compose.material3.Scaffold" in content:
        # replace Scaffold(...) with KaiteyoScaffold(...)
        # but only if it's the exact word Scaffold (not BottomSheetScaffold)
        new_content = re.sub(r'\bScaffold\(', 'KaiteyoScaffold(', content)
        
        if new_content != content:
            # We also need to add import ua.syt0r.kanji.presentation.common.ui.KaiteyoScaffold
            if "import ua.syt0r.kanji.presentation.common.ui.KaiteyoScaffold" not in new_content:
                new_content = re.sub(r'(package [^\n]+)\n', r'\1\n\nimport ua.syt0r.kanji.presentation.common.ui.KaiteyoScaffold\n', new_content)
            return new_content
    return content

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") and "KaiteyoScaffold.kt" not in file:
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = replace_scaffold(content)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
