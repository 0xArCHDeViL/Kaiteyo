import os

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

replacements = {
    'androidx.compose.foundation.interaction.MutableInteractionSource': 'MutableInteractionSource',
    'androidx.compose.foundation.interaction.collectIsPressedAsState': 'collectIsPressedAsState',
    'androidx.compose.animation.core.animateFloatAsState': 'animateFloatAsState',
    'androidx.compose.animation.core.spring': 'spring',
    'androidx.compose.animation.core.Spring.': 'Spring.',
    'androidx.compose.material3.Surface': 'Surface',
    '.androidx.compose.ui.draw.shadow': '.shadow',
    'androidx.compose.ui.text.font.FontWeight.': 'FontWeight.',
    'androidx.compose.foundation.shape.CircleShape': 'CircleShape',
    'androidx.compose.animation.animateColorAsState': 'animateColorAsState',
    'androidx.compose.animation.core.tween': 'tween',
    'androidx.compose.animation.AnimatedVisibility': 'AnimatedVisibility'
}

def clean_file(filepath, required_imports):
    with open(filepath, 'r') as f:
        content = f.read()

    last_import_idx = content.rfind('\nimport ')
    if last_import_idx == -1: return
    end_of_last_import = content.find('\n', last_import_idx + 1)
    
    header = content[:end_of_last_import]
    body = content[end_of_last_import:]
    
    for imp in required_imports:
        if f"import {imp}" not in header:
            header += f"\nimport {imp}"
            
    for old, new in replacements.items():
        body = body.replace(old, new)
    
    with open(filepath, 'w') as f:
        f.write(header + body)

for f, imps in files_info.items():
    clean_file(f, imps)
