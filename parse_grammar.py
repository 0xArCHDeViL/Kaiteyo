import re
import json

with open('/storage/emulated/0/Download/BUNPOU_REVISED.txt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

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
        # Check if it's a note
        if line.startswith("*)") or line.startswith("*"):
            current_notes.append(line)
        # Check if it's an example (usually contains Indonesian translation after Japanese)
        elif "。" in line or "？" in line or "！" in line:
            # Example parsing is tricky. Try to split by Japanese punctuation to separate JA and ID
            # e.g., 私 は タマ です。Saya adalah Tama.
            parts = re.split(r'(?<=[。？！])', line)
            
            # Reconstruct examples
            # Some lines are like: A：レイナさん は いしゃ です か。Apakah Reina (seorang) dokter ?
            speaker = ""
            if line.startswith("A：") or line.startswith("B：") or line.startswith("C："):
                speaker = line[:2]
                line = line[2:]
                
            parts = re.split(r'(?<=[。？！])\s*', line)
            if len(parts) >= 2:
                ja = parts[0].strip()
                id = "".join(parts[1:]).strip()
                if id == "":
                    # Maybe the Indonesian translation doesn't have a punctuation mark at the end
                    # Let's just store the whole line as japanese for now, if no ID is found
                    current_examples.append({"speaker": speaker, "japanese": line, "indonesian": ""})
                else:
                    current_examples.append({"speaker": speaker, "japanese": ja, "indonesian": id})
            else:
                current_examples.append({"speaker": speaker, "japanese": line, "indonesian": ""})
        else:
            # If no punctuation and not a note, it's likely a sub-formula (e.g., KB1 は KB2 です)
            current_point["formulas"].append(line)
            
save_point()

with open('grammar_parsed.json', 'w', encoding='utf-8') as f:
    json.dump(chapters, f, ensure_ascii=False, indent=2)
