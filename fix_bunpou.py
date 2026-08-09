import re

with open('/storage/emulated/0/Download/BUNPOU.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# KS い dicoret
text = re.sub(r'KS\s*い\s*く', r'KS ~~い~~ く', text)
text = re.sub(r'KS〜いかった', r'KS ~~い~~ かった', text)
text = re.sub(r'KS\s*い\s*かった', r'KS ~~い~~ かった', text)
text = re.sub(r'KS〜いくなかった', r'KS ~~い~~ くなかった', text)
text = re.sub(r'KS\s*いく', r'KS ~~い~~ く', text)

# KS な dicoret
text = re.sub(r'KS\s*な\s*じゃ', r'KS ~~な~~ じゃ', text)
text = re.sub(r'KS〜なでした', r'KS ~~な~~ でした', text)
text = re.sub(r'KS\s*な\s*で、', r'KS ~~な~~ で、', text)
text = re.sub(r'KS\s*な\s*に', r'KS ~~な~~ に', text)
text = re.sub(r'KS〜なじゃ', r'KS ~~な~~ じゃ', text)

# KK ます dicoret
text = re.sub(r'KK〜ます\s*に\s*いく', r'KK ~~ます~~ に いく', text)
text = re.sub(r'KK〜ます\s*に\s*くる', r'KK ~~ます~~ に くる', text)
text = re.sub(r'KK〜ます\s*に\s*かえる', r'KK ~~ます~~ に かえる', text)
text = re.sub(r'KK〜ます\s*ません\s*か', r'KK ~~ます~~ ません か', text)
text = re.sub(r'KK\s*〜ます\s*ません\s*か', r'KK ~~ます~~ ません か', text)
text = re.sub(r'KK\s*〜ます\s*〜ましょう', r'KK ~~ます~~ ましょう', text)
text = re.sub(r'KK\s*〜ます\s*ましょう', r'KK ~~ます~~ ましょう', text)
text = re.sub(r'KK\s*〜ます\s*ながら', r'KK ~~ます~~ ながら', text)
text = re.sub(r'KK\s*〜ます\s*かた', r'KK ~~ます~~ かた', text)
text = re.sub(r'KK\s*〜ます\s*なさい', r'KK ~~ます~~ なさい', text)
text = re.sub(r'KK\s*〜ます\s*やすい', r'KK ~~ます~~ やすい', text)
text = re.sub(r'KK\s*〜ます\s*にくい', r'KK ~~ます~~ にくい', text)
text = re.sub(r'KK〜ます\s*たい', r'KK ~~ます~~ たい', text)
text = re.sub(r'KK〜ます\s*たくない', r'KK ~~ます~~ たくない', text)

# KK ない dicoret (i dicoret)
text = re.sub(r'KK〜ない\s*くて\s*も', r'KK〜な~~い~~ くて も', text)
text = re.sub(r'KK〜ない\s*くて\s*は', r'KK〜な~~い~~ くて は', text)
text = re.sub(r'KK〜ない\s*ければ', r'KK〜な~~い~~ ければ', text)

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'w', encoding='utf-8') as f:
    f.write(text)

print("Done writing to /storage/emulated/0/Download/BUNPOU_REVISED.txt")
