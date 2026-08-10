import json
import re

with open('core/src/commonMain/composeResources/files/bunpou_data.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

for chap in data:
    if chap['id'] in (9, 10):
        for pt in chap['points']:
            if pt['number'] == '【１４１】':
                pt['formula_title'] = 'KK ない'
            elif pt['number'] == '【１４２】':
                pt['formula_title'] = 'KK ない で、〜'
            elif pt['number'] == '【１４３】':
                pt['formula_title'] = 'KK ない で ください'
            elif pt['number'] == '【１４４】':
                pt['formula_title'] = 'KK ~~ない~~ くても いいです'
            elif pt['number'] == '【１４５】':
                pt['formula_title'] = 'KK ~~ない~~ くては いけません／だめ です'
            elif pt['number'] == '【１４６】':
                pt['formula_title'] = 'KK ~~ない~~ ければ なりません'
            elif pt['number'] == '【１４７】':
                pt['formula_title'] = 'KK ない と （いけません）'
            elif pt['number'] == '【１４８】':
                pt['formula_title'] = 'KK ない ほうが いいです'

    for pt in chap['points']:
        new_examples = []
        for ex in pt['examples']:
            # Split by A： and B：
            ex = ex.replace("A：", "\n\nA：").replace("B：", "\n\nB：").replace("C：", "\n\nC：")
            # Also try to put a newline after a full stop `。` if it's followed by Indonesian translation
            ex = re.sub(r'。([A-Z])', r'。\n\1', ex)
            
            blocks = ex.split("\n\n")
            for b in blocks:
                b = b.strip()
                if b and not b.startswith("*)"):
                    new_examples.append(b)
                elif b.startswith("*)"):
                    pt['notes'] += "\n" + b
        
        pt['examples'] = new_examples

with open('core/src/commonMain/composeResources/files/bunpou_data.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

