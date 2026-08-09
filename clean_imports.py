import os
import re

files_info = {
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/general_dashboard/GeneralDashboardScreenUI.kt': [
        'androidx.compose.foundation.interaction.MutableInteractionSource',
        'androidx.compose.foundation.interaction.collectIsPressedAsState',
        'androidx.compose.animation.core.animateFloatAsState',
        'androidx.compose.animation.core.spring',
        'androidx.compose.animation.core.Spring',
        'androidx.compose.material3.Surface',
        'androidx.compose.ui.draw.shadow',
        'androidx.compose.ui.text.font.FontWeight'
    ],
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/library/LibraryScreen.kt': [
        'androidx.compose.foundation.shape.CircleShape',
        'androidx.compose.foundation.interaction.collectIsPressedAsState',
        'androidx.compose.animation.core.animateFloatAsState',
        'androidx.compose.animation.core.spring',
        'androidx.compose.animation.core.Spring',
        'androidx.compose.animation.animateColorAsState',
        'androidx.compose.animation.core.tween'
    ],
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/search/ui/SearchScreenUI.kt': [
        'androidx.compose.animation.AnimatedVisibility'
    ]
}

def clean_file(filepath, required_imports):
    with open(filepath, 'r') as f:
        content = f.read()

    # Split into imports and body
    last_import_idx = content.rfind('\nimport ')
    if last_import_idx == -1: return
    end_of_last_import = content.find('\n', last_import_idx + 1)
    
    header = content[:end_of_last_import]
    body = content[end_of_last_import:]
    
    for imp in required_imports:
        if f"import {imp}" not in header:
            header += f"\nimport {imp}"
            
    # Clean up prefixes in body
    # match something like androidx.compose.foundation.interaction.collectIsPressedAsState
    # and replace with collectIsPressedAsState
    body = re.sub(r'androidx\.compose\.[a-zA-Z0-9_\.]+\.([A-Z][a-zA-Z0-9_]*)', r'\1', body)
    body = re.sub(r'androidx\.compose\.[a-zA-Z0-9_\.]+\.([a-z][a-zA-Z0-9_]*)', r'\1', body)
    
    # Also clean up accidental leading dots like `.androidx.compose...` 
    body = re.sub(r'\.androidx\.compose\.[a-zA-Z0-9_\.]+\.([a-zA-Z0-9_]+)', r'.\1', body)
    
    with open(filepath, 'w') as f:
        f.write(header + body)

for f, imps in files_info.items():
    clean_file(f, imps)
