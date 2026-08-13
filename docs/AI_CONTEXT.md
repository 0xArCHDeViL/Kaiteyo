# Kaiteyo AI Context

This document is written for AI assistants. Read it before making changes.

## Project Overview

Kaiteyo is an **Android-only Japanese language learning application**. The supported product target is ARM64-v8a on Android 12 or newer. Phones are portrait-only; tablets and pads support both portrait and landscape through an adaptive navigation shell. Desktop, iOS, web, and freeform cross-platform views are not product targets.

**Tech Stack:**

- Kotlin 2.1.x
- Jetpack Compose for Android
- Gradle with version catalog (`gradle/libs.versions.toml`)
- Koin for dependency injection
- SQLDelight for local database
- Ktor for HTTP
- DataStore for preferences
- Android SDK 35, `minSdk 31`, ABI `arm64-v8a`

## Architecture

```text
app/         → Android application entry point, manifest, release packaging
core/        → Android library containing UI, data, and business logic
  commonMain/ → Shared source organization used by the Android target
  androidMain/ → Android implementations, drivers, lifecycle, platform APIs
database/    → Vendored source-data pipeline and SQLite export tooling
mediaGenerator/ → JVM-only build tooling for media generation; not an app target
```

`commonMain` remains a source organization inside the Android library. It must not be treated as a promise to support another runtime. New platform-specific behavior belongs in `androidMain` unless it is pure Android-independent business logic.

## Key Files

| File | Purpose |
|------|---------|
| `app/src/main/java/ua/syt0r/kanji/presentation/screen/main/MainActivity.kt` | Android launcher activity |
| `core/src/androidMain/.../KaiteyoActivity.kt` | Android lifecycle, deep links, orientation policy |
| `core/src/commonMain/.../KaiteyoApp.kt` | Root Compose content and theme setup |
| `core/src/commonMain/.../common/nav/NavShell.kt` | Width-adaptive content shell, compact strip, and compact rail |
| `core/src/commonMain/.../app_data/` | Application dictionary and vocabulary data access |
| `core/src/commonMain/.../user_data/` | User database, SRS, review history, migrations |
| `database/src/` | Streaming parser, exporter, integrity validation |

## Product Layout Contract

| Device class | Rule |
|---|---|
| Phone (`smallestScreenWidthDp < 600`) | Portrait locked; single-column touch layout |
| Tablet/pad (`smallestScreenWidthDp >= 600`) | Portrait and landscape; compact navigation strip on narrow widths and compact rail from `840dp` |
| ABI | `arm64-v8a` only |
| Minimum OS | Android 12 / API 31 |

The tablet shell must be intentional rather than a stretched phone screen. It selects a bounded navigation strip or a compact rail from available width, preserves content space, avoids fixed large sidebars, and keeps touch targets sized for large displays.

## Coding Style

- Use 4-space indentation and explicit imports.
- Prefer `val`, immutable state, data classes, and sealed hierarchies.
- Composable functions use PascalCase; `modifier` is the last parameter with a default value.
- Modifier order: size → padding → background/clip → clickable → align → graphicsLayer.
- Preserve cancellation semantics. Do not catch `Throwable` around suspend work; rethrow cancellation.
- Do not replace missing learning data with fabricated questions or answers. Use an explicit unavailable state or skip path.

## Current Execution Priorities

1. Crash-free vocabulary detail and metadata parsing.
2. Correct FSRS/SRS persistence, grammar generation, queue transitions, and review history.
3. Database parser/export integrity and release-data validation.
4. Android phone portrait plus tablet/pad portrait and landscape device validation.
5. TTS pronunciation correctness and low-memory performance.
6. Only then, functional UI polish that is proven to be a blocker.

## Things That Must Never Be Changed Without Explicit Request

- SRS/FSRS algorithm semantics and review-history meaning.
- SQLDelight schema and migrations without a migration plan.
- Package namespace `ua.syt0r.kanji`.
- Android-only target contract: API 31+, ARM64-v8a.
- Database source/release integrity gates.
- Existing regression tests and crash reproductions.

## Safe Change Areas

- Android Compose layouts and adaptive tablet/phone composition.
- Android lifecycle, manifest, and orientation behavior.
- Error boundaries that preserve cancellation and expose actionable state.
- Documentation and release checklists.
- Parser normalization with fixture and integrity tests.

## Validation Contract

A runtime fix is not complete after compilation alone. It requires a reproducible failure or invariant, a regression test, Android compile/build validation, and device instrumentation when the path is UI-facing. If no device is available, report the status as **build-validated, device-pending**.
