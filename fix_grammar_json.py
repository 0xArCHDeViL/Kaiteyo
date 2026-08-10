import json

with open('grammar_parsed.json', 'r') as f:
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

with open('grammar_parsed.json', 'w') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

