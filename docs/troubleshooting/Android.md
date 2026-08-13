# Android Build and Runtime Diagnostics

## Environment

Use JDK 17 and configure the Android SDK before Gradle execution:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
```

The application requires Android API 31 or newer and packages `arm64-v8a`. Keep `local.properties`, signing files, SDK installations, Gradle caches, and build outputs outside version control.

## Standard Validation

```bash
./gradlew :core:compileDebugKotlinAndroid \
  -PappDataSource=release -PappDataVersion=15 -PappDataReleaseTag=data-v15

./gradlew :core:testDebugUnitTest \
  -PappDataSource=release -PappDataVersion=15 -PappDataReleaseTag=data-v15

./gradlew :app:assembleDebug \
  -PappDataSource=release -PappDataVersion=15 -PappDataReleaseTag=data-v15
```

On limited-memory machines, add `--no-daemon --max-workers=1`, `-Dorg.gradle.workers.max=1`, and `-Pkotlin.compiler.execution.strategy=in-process`.

## Device Validation

Verify the appropriate device contract after UI, navigation, orientation, resource, or lifecycle changes:

| Device | Required checks |
|---|---|
| Phone (`smallestScreenWidthDp < 600`) | Portrait lock, navigation, keyboard/insets, vocabulary and grammar practice, process recreation |
| Tablet/pad (`smallestScreenWidthDp >= 600`) | Landscape lock, navigation rail, content surface, dialogs, practice flows, process recreation |

Collect `adb logcat` around a reproducible crash or rendering failure. Include the exact screen, source data/deck item, Android version, device class, and full exception chain in the issue record.

## Cleanup Validation

For repository cleanup, confirm that removed paths have no source/build/workflow references, run `git diff --check`, compile Android, run unit tests, assemble the debug APK, and keep generated outputs untracked.
