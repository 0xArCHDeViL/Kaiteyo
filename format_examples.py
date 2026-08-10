import json
import re

with open("core/src/commonMain/composeResources/files/bunpou_data.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# We want to insert a newline before every capital letter that follows a Japanese punctuation (。！？)
# or just insert newlines to separate JP and ID.
# Actually, the user says "jangan full satu paragraf".
# If we replace 。 with 。\n, it helps. But let's try to do it smartly:
# Find 。 or ？ or ！ followed by optional space, followed by Latin capital letter.
def reformat_example(text):
    # Insert newline after Japanese punctuation if followed by Latin letter
    text = re.sub(r'([。！？])\s*([A-Z])', r'\1\n\2', text)
    # Also separate A: and B:
    text = re.sub(r'\s+(A：)', r'\n\1', text)
    text = re.sub(r'\s+(B：)', r'\n\1', text)
    # Also separate *)
    text = re.sub(r'\s+(\*\))', r'\n\n\1', text)
    return text

for chapter in data:
    for point in chapter["points"]:
        new_examples = []
        for ex in point["examples"]:
            new_examples.append(reformat_example(ex))
        point["examples"] = new_examples

with open("core/src/commonMain/composeResources/files/bunpou_data.json", "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
