# Kanji Dojo Data

Data and scripts used by the [Kanji Dojo](https://github.com/syt0r/Kanji-Dojo) application.

Characters and expressions are located in `data/` folder.

Scripts to manipulate data are in the `src` directory, under `task` package. They can be executed using IntelliJ IDEA or by running a command with the required task name.

Tasks:

* `./gradlew run -Ptask=ExportCharactersJson`
* `./gradlew run -Ptask=ExportDatabase`
* `./gradlew run -Ptask=ExportExpressionsJson`
* `./gradlew run -Ptask=ValidateJson`
* `./gradlew run -Ptask=ExportVariantsInfo`
* `./gradlew run -Ptask=ApplyExpressionRanks`

If you wish to run scripts you will need to download the necessary data. Scripts and links for downloading can be found in `parser_data/` directory.

Download tasks:

* `./gradlew downloadRadkFile`
* `./gradlew downloadLeedsFrequencies`
* `./gradlew downloadjmdictFuriganaJson`
* `./gradlew downloadYomichanJlptVocab`
* TODO - replace all other bash scripts with Gradle tasks

## Data Sources and Credits

* **KanjiVG**</br>
  Provides writing strokes and radicals information.</br>
  License: Creative Commons Attribution-Share Alike 3.0</br>
  Link: https://kanjivg.tagaini.net/
* **Kanji Dic**</br>
  Provides character information, such as meanings, readings and classifications.</br>
  License: Creative Commons Attribution-Share Alike 3.0</br>
  Link: http://www.edrdg.org/wiki/index.php/KANJIDIC_Project
* **JMDict**</br>
  Japanese-Multilingual dictionary that provides expressions.</br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://www.edrdg.org/jmdict/j_jmdict.html
* **RADKFILE**</br>
  Provides a decomposition of kanji into radicals to support software which provides a lookup service using kanji components.</br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://www.edrdg.org/krad/kradinf.html
* **JmdictFurigana**</br>
  Open-source furigana resource to complement the EDICT/Jmdict and ENAMDICT/Jmnedict files.</br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://github.com/Doublevil/JmdictFurigana
* **Tanos by Jonathan Waller**</br>
  Provides JLPT classification for kanji.</br>
  License: Creative Commons BY</br>
  Link: http://www.tanos.co.uk/jlpt/
* **Frequency list by Leeds University**</br>
  Provides word rankings by frequency of usage on the internet.</br>
  License: Creative Commons BY</br>
  Link: http://corpus.leeds.ac.uk/list.html
* **yomichan-jlpt-vocab**</br>
  This meta dictionary adds JLPT-level tags to words in Yomichan and provides associations between Tanos and JMDict vocabulary.</br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://github.com/stephenmk/yomichan-jlpt-vocab

## Kaiteyo internal database source

This directory is the repository-owned source tree for the application data database. It is vendored from [syt0r/Kanji-Dojo-Data](https://github.com/syt0r/Kanji-Dojo-Data) and is intentionally maintained inside the Kaiteyo repository so future data expansion does not require a build-time dependency on the upstream repository.

The canonical source contract remains the upstream layout: exported character and expression JSON is under `data/`, parser snapshots are under `parser_data/`, and the exporter is under `src/`. The parser snapshots required by the current exporter are committed with `parser_data/SNAPSHOTS.sha256` for reproducibility. The data-source credits and licenses above must be preserved when adding new datasets.

The generated file `kanji-dojo-data-base-v<version>.sql` is a SQLite database despite the historical `.sql` extension. It is intentionally ignored by Git and is produced by the root-project task:

```bash
./gradlew :database:exportAppDatabase -PappDataVersion=21
```

The exporter includes every entry from the repository-owned `parser_data/JMdict_e_examp` snapshot. `data/supported_vocab.csv` is retained as a canonical fallback/deck vocabulary set: if a newer JMdict snapshot removes one of those IDs, `LegacyExpressionFallback` reconstructs the minimal entry instead of silently deleting it. The exporter asserts exact coverage of the full JMdict ID set plus those canonical fallback IDs, so the CSV is no longer a whitelist that can hide valid JMdict entries.

Kaiteyo application builds accept `-PappDataSource=release` (default) or `-PappDataSource=source`. Release mode consumes the internally published asset from the Kaiteyo repository's GitHub Releases. Source mode runs `:database:exportAppDatabase` and bundles its output directly. The `Build All` workflow exposes the same choice through its manual `data_source` input and can publish a source-built database release asset when `publish_data_release` is enabled.

## Runtime and parser performance contract

The JMdict composite export uses `StreamingJMdictParser`, a StAX reader that keeps only the current `<entry>` in memory while preserving all supported `k_ele`, `r_ele`, `sense`, gloss, restriction, priority, and Tatoeba example fields. Internal DTD entities are allowed only for the repository-owned JMdict snapshot; external entities remain disabled. This avoids building a DOM for the 70+ MiB source file.

`DatabaseIntegrityValidator` runs after every export. It verifies SQLite `integrity_check`, requires zero rows from `foreign_key_check`, and checks exact coverage for Kanji, vocabulary, sentence, and deck-card domains. An export that loses a source entry or produces an orphan relation fails before the artifact can be published.

The runtime schema contains explicit secondary indexes for reverse radical/classification lookups, vocabulary element/sense foreign-key access, sentence score ordering, and deck imports. The Kanji Browser consumes a single aggregated catalog query rather than separately materializing meanings, readings, classifications, and stroke-count collections. Radical explorer results use a batch relationship query instead of one query per character. App-data is opened with foreign-key enforcement and `PRAGMA query_only = ON`; read paths do not wrap every point query in a transaction, while write/batch transactions remain explicit in user-data databases.
