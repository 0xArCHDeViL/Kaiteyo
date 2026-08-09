import re

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'r', encoding='utf-8') as f:
    text = f.read()

text = re.sub(r'KS なで', r'KS ~~な~~ で', text)
text = re.sub(r'KS\s*な\s*でした', r'KS ~~な~~ でした', text)
text = re.sub(r'KS\s*な\s*だった', r'KS ~~な~~ だった', text)

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'w', encoding='utf-8') as f:
    f.write(text)

print("Done fixing KS")
