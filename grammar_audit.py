import json
import re
from pathlib import Path

path = Path('core/src/commonMain/composeResources/files/bunpou_data.json')
data = json.loads(path.read_text(encoding='utf-8'))

summary = {
    'chapters': len(data),
    'points': 0,
    'examples': 0,
    'points_without_examples': 0,
    'formula_titles_with_strike_markers': 0,
    'formula_titles_with_unbalanced_markers': 0,
    'examples_with_newline': 0,
    'examples_with_full_stop': 0,
    'examples_with_space_in_japanese_segment': 0,
    'examples_without_clear_japanese_indonesian_split': 0,
}
examples_by_chapter = []
marker_samples = []
for chapter in data:
    points = chapter.get('points', [])
    summary['points'] += len(points)
    chapter_examples = 0
    for point in points:
        title = point.get('formula_title', '') or ''
        if '~~' in title:
            summary['formula_titles_with_strike_markers'] += 1
            if title.count('~~') % 2:
                summary['formula_titles_with_unbalanced_markers'] += 1
            if len(marker_samples) < 10:
                marker_samples.append(title)
        examples = point.get('examples', []) or []
        chapter_examples += len(examples)
        summary['examples'] += len(examples)
        if not examples:
            summary['points_without_examples'] += 1
        for raw in examples:
            if '\n' in raw:
                summary['examples_with_newline'] += 1
            if '。' in raw:
                summary['examples_with_full_stop'] += 1
            first = re.split(r'\n|。', raw, maxsplit=1)[0]
            if ' ' in first or '\u3000' in first:
                summary['examples_with_space_in_japanese_segment'] += 1
            if '\n' not in raw and '。' not in raw:
                summary['examples_without_clear_japanese_indonesian_split'] += 1
    examples_by_chapter.append({'id': chapter.get('id'), 'title': chapter.get('title'), 'points': len(points), 'examples': chapter_examples})

print(json.dumps(summary, ensure_ascii=False, indent=2))
print('CHAPTERS')
print(json.dumps(examples_by_chapter, ensure_ascii=False, indent=2))
print('MARKER_SAMPLES')
print(json.dumps(marker_samples, ensure_ascii=False, indent=2))
