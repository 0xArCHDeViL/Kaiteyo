from __future__ import annotations

import sqlite3
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("usage: inspect_graph_export.py <sql-dump>")
    dump = Path(sys.argv[1]).resolve()
    connection = sqlite3.connect(dump)
    try:
        queries = {
            "schema_version": "PRAGMA user_version",
            "pack_revision": "PRAGMA application_id",
            "nodes": "SELECT COUNT(*) FROM learning_node",
            "edges": "SELECT COUNT(*) FROM learning_edge",
            "orphans": """
                SELECT COUNT(*)
                FROM learning_edge AS edge
                LEFT JOIN learning_node AS source ON source.node_id = edge.from_node_id
                LEFT JOIN learning_node AS target ON target.node_id = edge.to_node_id
                WHERE source.node_id IS NULL OR target.node_id IS NULL
            """,
            "duplicate_keys": """
                SELECT COUNT(*) - COUNT(DISTINCT node_key) FROM learning_node
            """,
            "element_id_collisions": """
                SELECT COUNT(*)
                FROM vocab_kanji_element AS kanji
                JOIN vocab_kana_element AS kana
                  ON kanji.entry_id = kana.entry_id
                 AND kanji.element_id = kana.element_id
            """,
            "principal_vocab": """
                SELECT 'kanji' AS element_kind, entry_id, element_id, reading
                FROM vocab_kanji_element
                WHERE reading IN ('主夫', '主婦')
                UNION ALL
                SELECT 'kana' AS element_kind, entry_id, element_id, reading
                FROM vocab_kana_element
                WHERE reading IN ('主夫', '主婦')
                ORDER BY reading, entry_id, element_id
            """,
        }
        for name, query in queries.items():
            rows = connection.execute(query).fetchall()
            print(f"{name}={rows}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
