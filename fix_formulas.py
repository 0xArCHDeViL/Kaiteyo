import re

def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()

    # FormulaText( text = X, fontWeight = Y, color = Z, textAlign = W, modifier = M )
    # Let's just find FormulaText( ... ) and if it has color/fontWeight/textAlign, we convert to style = TextStyle(...)
    # Actually it's easier to just do it via regex
    # Replace `FormulaText(text = X, fontWeight = Y, color = Z, textAlign = W, modifier = M)`
    # This is too complex for simple regex. Let's just sed/replace manually for the known ones.
    pass

