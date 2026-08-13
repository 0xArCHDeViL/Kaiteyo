# Contributing to Kaiteyo Android

Kaiteyo accepts changes that improve the Android application, its validated data pipeline, or the tooling required to build and release those components. The supported product is ARM64-v8a Android 12+; phones are portrait-only and tablets/pads support portrait and landscape through adaptive layouts.

## Contribution Rules

1. Work directly from the current integration branch when that is the requested workflow.
2. Keep each change focused: do not combine behavioral work with unrelated formatting or generated artifacts.
3. Preserve learning-data semantics, SRS/FSRS behavior, database migration safety, and coroutine cancellation.
4. Do not add desktop, iOS, web, cross-platform product wrappers, Appearance Studio, or Theme Studio scope.
5. Do not commit build outputs, IDE state, dependency caches, Node modules, temporary scripts, logs, or analysis dumps.
6. Treat vendored dictionary data as source material: preserve integrity gates and document any source-data transformation.

## Required Validation

| Change type | Minimum evidence |
|---|---|
| Pure domain/data logic | Focused regression test plus `:core:testDebugUnitTest` |
| Compose/UI or navigation | Android core/app compile plus unit tests; device instrumentation when possible |
| Manifest, packaging, resources, dependencies | `:app:assembleDebug` plus relevant unit tests |
| Database export/parser | Export/integrity validation plus consumer compile/test |
| Cleanup/removal | Reference audit, `git diff --check`, Android compile, and unit tests |

## Pull/Commit Quality

Describe the problem, the invariant preserved, the files removed or changed, and exact validation commands. A cleanup is complete only when the source tree remains internally referenced, the tracked diff contains no generated clutter, and all relevant Gradle tasks pass.
