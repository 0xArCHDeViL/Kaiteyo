# Internal Database Workflow

Kaiteyo now owns the Kanji Dojo data source under `database/`. The directory contains the canonical exported data, the generator, parser snapshots, source credits, and the reconciliation code needed to keep application vocabulary stable when external dictionary snapshots drift.

## Build modes

The default application build uses the internally published GitHub Release asset:

```bash
./gradlew app:assembleRelease -PappDataSource=release -PappDataVersion=21 -PappDataReleaseTag=data-v21
```

The source mode compiles the vendored dataset first, validates it, and publishes a compressed runtime pack; the generated SQLite database is not bundled into the APK:

```bash
./gradlew app:assembleRelease -PappDataSource=source -PappDataVersion=21 -PappDataReleaseTag=data-v21
```

The generated file is named `database/kanji-dojo-data-base-v<version>.sql` for compatibility with the exporter contract. CI compresses it to `.sql.gz` with a `.sha256` manifest. Android downloads and verifies that pack on first launch, inflates it into the app database directory, and then performs all searches offline. The raw and compressed artifacts are ignored by Git; the APK carries neither one.

## GitHub Actions

Open **Actions → Build All → Run workflow** and choose `data_source`:

| Input | Meaning |
|---|---|
| `release` | Build the small APK with the compressed full-data pack URL pointing at the internal `data-v<version>` release. |
| `source` | Run `:database:exportAppDatabase`, validate the SQLite header, gzip the database, create its SHA-256 manifest, and upload the pack for release publication. |

Set `publish_data_release=true` only when the source data has been reviewed and the generated artifact should become the internal release asset consumed by later release-mode builds. The workflow publishes the `.sql.gz` pack and its `.sha256` manifest under the `data-v<version>` release tag.

A normal application tag release uses `release` mode and therefore consumes the previously published internal database asset. This separates data generation from application packaging and makes application builds reproducible.

## Updating data

Update the canonical files under `database/data/`, refresh parser snapshots under `database/parser_data/`, verify `SNAPSHOTS.sha256`, and run:

```bash
./gradlew :database:exportAppDatabase -PappDataVersion=21
gzip -1 -c database/kanji-dojo-data-base-v21.sql > database/kanji-dojo-data-base-v21.sql.gz
gzip -t database/kanji-dojo-data-base-v21.sql.gz
sha256sum database/kanji-dojo-data-base-v21.sql.gz
```

Then run the source-mode application build and publish the reviewed artifact through `Build All`. Update the default version and release tag in `buildSrc/src/main/kotlin/AppAssets.kt` only when the new database becomes the application default.

## Provenance

The source tree retains the upstream credits and licenses in `database/README.md`. Any new source or derived dataset must be added with its provenance, license, version/date, and checksum where applicable.
