# Architecture

Kaiteyo is an **Android-only** offline-first Japanese learning application. The distributable target is `arm64-v8a` on Android 12/API 31 or newer. The codebase uses a Kotlin Multiplatform source-set layout only to organize shared and pure Kotlin code inside the Android build; it does not support desktop, iOS, or web product targets.

## Modules

| Module | Responsibility |
|---|---|
| `app` | Android launcher, manifest, app-level packaging, Android resources, and release variants |
| `core` | UI, navigation, use cases, persistence, dictionary access, SRS/FSRS, grammar, TTS, and Android implementations |
| `database` | Vendored source-data pipeline, streaming parser, integrity checks, and application SQLite export |
| `mediaGenerator` | JVM-only internal tooling for visual asset capture; it is not a product runtime target |
| `buildSrc` | Versioning and shared Gradle build logic |

## Source-Set Ownership

| Location | Rule |
|---|---|
| `core/src/commonMain` | Pure domain logic and Compose UI that does not require Android framework APIs |
| `core/src/androidMain` | Android lifecycle, SQL drivers, filesystem/platform APIs, TTS, activity behavior, and Android-only implementations |
| `core/src/commonTest` | Deterministic domain and regression tests |
| `core/src/androidUnitTest` or `app/src/test` | Android/JVM test coverage when platform behavior is involved |
| `app/src/main` | Android manifest, launcher activity, app resources, and release configuration |

`commonMain` is an implementation detail of the Android library. New code must not add a non-Android target or platform abstraction unless an active Android use case requires it.

## Runtime Flow

```text
MainActivity
  → KaiteyoActivity lifecycle and orientation policy
    → KaiteyoApp Compose root
      → Koin-provided repositories and use cases
        → SQLDelight app/user databases and DataStore preferences
          → Compose screens and practice queues
```

The application-data database is read-only and shipped from a validated data release. User progress, review history, settings, and queues live in the user-data store. Repository boundaries normalize optional or malformed source data into explicit domain states; UI flows must present recoverable unavailable/error states rather than fabricate learning content.

## Device Contract

| Device class | Runtime rule |
|---|---|
| Phone, `smallestScreenWidthDp < 600` | Portrait locked; touch-first single-column content |
| Tablet/pad, `smallestScreenWidthDp >= 600` | Landscape locked; fixed navigation rail plus separate content surface |
| Native ABI | `arm64-v8a` only |
| Android version | API 31+ |

The navigation shell and orientation policy are part of the product contract. Any change to them requires compile validation and device-focused testing.

## Dependency Direction

`app` depends on `core`. `core` owns application behavior and may consume generated SQLDelight interfaces. `database` generates validated source-data artifacts but is not loaded by the Android runtime. `mediaGenerator` is tooling-only and must not be depended on by `app` or `core` runtime code.
