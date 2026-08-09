import os

files = [
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/general_dashboard/GeneralDashboardScreenUI.kt',
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/home/screen/search/ui/SearchScreenUI.kt',
    'core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/library/LibraryScreen.kt'
]

imports = [
    'androidx.compose.foundation.interaction.collectIsPressedAsState',
    'androidx.compose.ui.graphics.graphicsLayer',
    'androidx.compose.foundation.shape.RoundedCornerShape',
    'androidx.compose.foundation.background',
    'androidx.compose.ui.draw.alpha',
    'androidx.compose.ui.draw.clip',
    'androidx.compose.ui.draw.shadow'
]

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    last_import_idx = content.rfind('\nimport ')
    end_of_last_import = content.find('\n', last_import_idx + 1)
    
    imports_to_add = ""
    for imp in imports:
        if f"import {imp}" not in content:
            imports_to_add += f"\nimport {imp}"
            
    if imports_to_add:
        content = content[:end_of_last_import] + imports_to_add + content[end_of_last_import:]
        
    content = content.replace('.androidx.compose.ui.graphics.graphicsLayer', '.graphicsLayer')
    content = content.replace('.androidx.compose.ui.draw.alpha', '.alpha')
    content = content.replace('.androidx.compose.ui.draw.clip', '.clip')
    content = content.replace('.androidx.compose.foundation.interaction.collectIsPressedAsState', '.collectIsPressedAsState')
    content = content.replace('.androidx.compose.foundation.background', '.background')
    
    with open(f, 'w') as file:
        file.write(content)

