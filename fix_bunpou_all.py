import re

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Replace KK〜て, KK〜た, KK〜ない, KK〜る, KK〜たら, KK〜たり
text = re.sub(r'KK〜て', r'KK ~~ます~~ て', text)
text = re.sub(r'KK〜た', r'KK ~~ます~~ た', text)
text = re.sub(r'KK〜ない', r'KK ~~ます~~ ない', text)
text = re.sub(r'KK〜る', r'KK ~~ます~~ る', text)

# For those that already got "na~~i~~", let's fix them to "KK ~~ます~~ な~~い~~"
text = re.sub(r'KK〜な~~い~~', r'KK ~~ます~~ な~~い~~', text)

# Also fix KK 〜ます (with space) that didn't get caught
text = re.sub(r'KK\s*〜ます', r'KK ~~ます~~', text)

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'w', encoding='utf-8') as f:
    f.write(text)

print("Done writing to /storage/emulated/0/Download/BUNPOU_REVISED.txt")
