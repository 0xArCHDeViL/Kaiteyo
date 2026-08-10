import sqlite3
import json

def export_dictionary():
    db_path = "/home/Kaiteyo/core/src/commonMain/composeResources/files/kanji-dojo-data-base-v15.sql"
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    c = conn.cursor()

    characters = []
    
    # Get all kanji from kanji_data
    c.execute("SELECT * FROM kanji_data")
    kanji_rows = c.fetchall()
    
    for row in kanji_rows:
        kanji = row['kanji']
        
        # Get strokes
        c.execute("SELECT stroke_path FROM character_stroke WHERE character = ? ORDER BY stroke_number", (kanji,))
        strokes = [r['stroke_path'] for r in c.fetchall()]
        
        # Get readings
        c.execute("SELECT reading FROM kanji_reading WHERE kanji = ?", (kanji,))
        readings = [r['reading'] for r in c.fetchall()]
        
        # Get meanings
        c.execute("SELECT meaning FROM kanji_meaning WHERE kanji = ? ORDER BY priority", (kanji,))
        meanings = [r['meaning'] for r in c.fetchall()]
        
        characters.append({
            "id": len(characters) + 1,
            "literal": kanji,
            "type": "kanji",
            "strokeCount": len(strokes),
            "readings": readings,
            "meanings": meanings,
            "radicals": [], # simplified for now
            "variants": [],
            "svgPaths": strokes
        })
        
    with open('/home/Kaiteyo/webapp/public/dictionary.json', 'w', encoding='utf-8') as f:
        json.dump(characters, f, ensure_ascii=False)
    
    print(f"Exported {len(characters)} characters to dictionary.json")

if __name__ == "__main__":
    export_dictionary()
