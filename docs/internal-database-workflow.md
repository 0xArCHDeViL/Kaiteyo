# Internal Database Workflow

Kaiteyo now owns the Kanji Dojo data source under `database/`. The directory contains the canonical exported data, the generator, parser snapshots, source credits, and the reconciliation code needed to keep application vocabulary stable when external dictionary snapshots drift.

## Build modes

The default application build uses the internally published GitHub Release asset:

```bash
./gradlew app:assembleRelease -PappDataSource=release -PappDataVersion=15 -PappDataReleaseTag=data-v15
```

The source mode compiles the vendored dataset first and bundles the generated SQLite database directly:

```bash
./gradlew app:assembleRelease -PappDataSource=source -PappDataVersion=15 -PappDataReleaseTag=data-v15
```

The generated file is named `database/kanji-dojo-data-base-v15.sql` for compatibility with the existing asset contract. It is a SQLite database, not a SQL script, and is ignored by Git because release mode is the normal path for application builds.

## GitHub Actions

Open **Actions → Build All → Run workflow** and choose `data_source`:

| Input | Meaning |
|---|---|
| `release` | Download `kanji-dojo-data-base-v<version>.sql` from the internal `data-v<version>` release. |
| `source` | Run `:database:exportAppDatabase`, validate the SQLite header and SHA-256, then bundle the generated file. |

Set `publish_data_release=true` only when the source data has been reviewed and the generated artifact should become the internal release asset consumed by later release-mode builds. The workflow publishes both the database and its `.sha256` manifest under the `data-v<version>` release tag.

A normal application tag release uses `release` mode and therefore consumes the previously published internal database asset. This separates data generation from application packaging and makes application builds reproducible.

## Updating data

Update the canonical files under `database/data/`, refresh parser snapshots under `database/parser_data/`, verify `SNAPSHOTS.sha256`, and run:

```bash
./gradlew :database:exportAppDatabase -PappDataVersion=16
file database/kanji-dojo-data-base-v16.sql
sha256sum database/kanji-dojo-data-base-v16.sql
```

Then run the source-mode application build and publish the reviewed artifact through `Build All`. Update the default version and release tag in `buildSrc/src/main/kotlin/AppAssets.kt` only when the new database becomes the application default.

## Provenance

The source tree retains the upstream credits and licenses in `database/README.md`. Any new source or derived dataset must be added with its provenance, license, version/date, and checksum where applicable.
