import os
import re

def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()

    # Pattern for SpanStyle with style
    # We want to replace SpanStyle(..., style = MaterialTheme.typography.something, ...)
    # with MaterialTheme.typography.something.toSpanStyle().copy(...)
    
    # We will do a generic replacement for TextStyle(style = A, color = B) -> A.copy(color = B)
    # Because there are many variations, let's write regex for specific common cases we see.
    
    # Case 1: textStyle = androidx.compose.ui.text.TextStyle(style = X)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*style\s*=\s*([a-zA-Z0-9_\.\(\)]+)\s*\)', r'textStyle = \1', content)
    
    # Case 2: textStyle = TextStyle(style = X, color = Y)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*color\s*=\s*([^,]+),\s*style\s*=\s*([^\)]+)\s*\)', r'textStyle = \2.copy(color = \1)', content)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*style\s*=\s*([^,]+),\s*color\s*=\s*([^\)]+)\s*\)', r'textStyle = \1.copy(color = \2)', content)

    # Case 3: textStyle = TextStyle(style = X, color = Y, lineHeight = Z)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*style\s*=\s*([^,]+),\s*color\s*=\s*([^,]+),\s*lineHeight\s*=\s*([^\)]+)\s*\)', r'textStyle = \1.copy(color = \2, lineHeight = \3)', content)

    # Case 4: textStyle = TextStyle(style = X, fontFamily = Y)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*style\s*=\s*([^,]+),\s*fontFamily\s*=\s*([^\)]+)\s*\)', r'textStyle = \1.copy(fontFamily = \2)', content)

    # Case 5: textStyle = TextStyle(color = X, style = Y, fontWeight = Z)
    content = re.sub(r'textStyle\s*=\s*(?:androidx\.compose\.ui\.text\.)?TextStyle\s*\(\s*color\s*=\s*([^,]+),\s*style\s*=\s*([^,]+),\s*fontWeight\s*=\s*([^\)]+)\s*\)', r'textStyle = \2.copy(color = \1, fontWeight = \3)', content)


    # SpanStyle fixes
    # SpanStyle( fontWeight = X, style = Y )
    content = re.sub(r'SpanStyle\s*\(\s*fontWeight\s*=\s*([^,]+),\s*style\s*=\s*([^\)]+),?\s*\)', r'\2.toSpanStyle().copy(fontWeight = \1)', content)
    
    # SpanStyle( fontWeight = X, color = Y, style = Z )
    content = re.sub(r'SpanStyle\s*\(\s*fontWeight\s*=\s*([^,]+),\s*color\s*=\s*([^,]+),\s*style\s*=\s*([^\)]+)\s*\)', r'\3.toSpanStyle().copy(fontWeight = \1, color = \2)', content)
    
    # Text( text = X, style = Y, modifier = Z, style = W.copyCentered() ) -> The second style was added!
    # Let's fix HighlightedLetter.kt:
    if "HighlightedLetter.kt" in path:
        content = re.sub(r'        style = androidx\.compose\.material3\.MaterialTheme\.typography\.headlineMedium,(\s*modifier = constraintModifier[\s\S]*?wrapContentSize\(unbounded = true\)),\s*style = MaterialTheme\.typography\.bodyLarge\.copyCentered\(\)', r'        \1, style = MaterialTheme.typography.headlineMedium.copyCentered()', content)


    with open(path, 'w') as f:
        f.write(content)

for root, dirs, files in os.walk('core/src/commonMain/kotlin/ua/syt0r/kanji/presentation'):
    for file in files:
        if file.endswith('.kt'):
            fix_file(os.path.join(root, file))

