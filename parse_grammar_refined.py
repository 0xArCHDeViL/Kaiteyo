import re
import json

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Preprocess: separate examples by A:, B:, C: or some rules
# Actually, the simplest is to group lines manually.
lines = text.split('\n')
chapters = []
current_chapter = None
current_point = None
current_examples = []
current_notes = []

def save_point():
    global current_point, current_examples, current_notes
    if current_point:
        current_point["examples"] = current_examples
        current_point["notes"] = "\n".join(current_notes)
        if current_chapter:
            current_chapter["points"].append(current_point)
    current_point = None
    current_examples = []
    current_notes = []

# Regex for detecting grammar points: 【０１】〜は〜です ：(kata benda 1 dijelaskan dengan kata benda 2)
point_regex = re.compile(r'^(【\d+】)\s*(.*?)\s*：\s*(.*)$')
point_regex_alt = re.compile(r'^(【\d+】)\s*(.*?)$')

for line in lines:
    line = line.strip()
    if not line:
        continue
    
    if line.startswith("BAB-"):
        save_point()
        current_chapter = {
            "id": int(line.replace("BAB-", "").strip()),
            "title": line,
            "points": []
        }
        chapters.append(current_chapter)
        continue
        
    m = point_regex.match(line)
    if m:
        save_point()
        current_point = {
            "number": m.group(1).strip(),
            "formula_title": m.group(2).strip(),
            "meaning": m.group(3).strip(),
            "formulas": [],
            "examples": [],
            "notes": ""
        }
        continue
        
    m_alt = point_regex_alt.match(line)
    if m_alt and not m:
        save_point()
        current_point = {
            "number": m_alt.group(1).strip(),
            "formula_title": m_alt.group(2).strip(),
            "meaning": "",
            "formulas": [],
            "examples": [],
            "notes": ""
        }
        continue
        
    if current_point:
        if line.startswith("*)") or line.startswith("*"):
            current_notes.append(line)
        # Check if line looks like a formula (e.g., KB1 は KB2 です)
        elif not any(c in line for c in ['。', '？', '！', '：']) and not line.startswith("A:") and not line.startswith("B:"):
            current_point["formulas"].append(line)
        else:
            # It's an example. Let's just treat the entire line as a single example string for simplicity,
            # or try to split it into sentences.
            
            # Since multiple sentences can be on the same line (e.g. A: ... B: ...), let's split by Japanese punctuation followed by A:/B: or capital letters.
            # For simplicity, let's just append the whole raw string. The UI can display it as a block of text.
            # But the user asked for beautiful UI, separating JP and ID is better.
            
            # Let's split by 。 or ？ followed by a space or capital letter, but it's hard.
            # Let's just store the raw line in `raw_text` and let the UI just display it.
            current_examples.append(line)
            
save_point()

with open('/home/Kaiteyo/core/src/commonMain/composeResources/files/bunpou_data.json', 'w', encoding='utf-8') as f:
    json.dump(chapters, f, ensure_ascii=False, indent=2)

print("Saved to bunpou_data.json")
