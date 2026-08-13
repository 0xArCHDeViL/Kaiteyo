# Android Development Guide

## Prerequisites

Use JDK 17 and an Android SDK containing API 35 build tools. The application minimum SDK is API 31 and release artifacts target `arm64-v8a` only.

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
```

The app-data release inputs must be supplied when a task needs application database assets:

```bash
-PappDataSource=release \
-PappDataVersion=15 \
-PappDataReleaseTag=data-v15
```

## Primary Commands

| Purpose | Command |
|---|---|
| Compile Android library | `./gradlew :core:compileDebugKotlinAndroid` |
| Compile Android app | `./gradlew :app:compileDebugKotlin` |
| Run core unit tests | `./gradlew :core:testDebugUnitTest` |
| Build debug APK | `./gradlew :app:assembleDebug` |
| Build release APK | `./gradlew :app:assembleRelease` |

For constrained environments, use one worker and in-process Kotlin compilation:

```bash
./gradlew :core:testDebugUnitTest \
  -PappDataSource=release -PappDataVersion=15 -PappDataReleaseTag=data-v15 \
  -Pkotlin.compiler.execution.strategy=in-process \
  --no-daemon --max-workers=1 -Dorg.gradle.workers.max=1
```

## Change Workflow

1. Read the affected module and identify the runtime invariant or bug reproduction.
2. Make the smallest coherent change that preserves cancellation, persistence, and learning semantics.
3. Add or update deterministic unit tests for domain/data behavior.
4. Run the relevant compile task and `:core:testDebugUnitTest`.
5. Build `:app:assembleDebug` for any manifest, resource, dependency, packaging, or UI-shell change.
6. For phone/tablet UI behavior, perform instrumentation on an Android device or emulator when available. Report it as device-pending when it cannot be run.

Do not add desktop, iOS, web, or Appearance Studio build tasks. `mediaGenerator` may be run only as internal media tooling and is not an application target.
