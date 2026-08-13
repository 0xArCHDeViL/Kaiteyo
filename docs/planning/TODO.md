# Active Engineering TODO

This document contains planned work for the Android-only Kaiteyo product. It intentionally excludes desktop, iOS, web, Appearance Studio, Theme Studio, floating-window controls, and hover-based navigation.

## Reliability

- [ ] Complete device validation for vocabulary details with absent optional metadata.
- [ ] Complete device validation for grammar unavailable-state skip behavior.
- [ ] Add malformed review-history fixtures for statistics and streak resilience.
- [ ] Extend future-unknown JMdict metadata fixtures without relaxing known metadata parsing.

## Android Device Contract

- [ ] Verify phone portrait lock, insets, navigation, keyboard, process recreation, and deep links on Android 12+ ARM64 hardware.
- [ ] Verify tablet/pad portrait and landscape, adaptive strip/rail composition, dialogs, writing canvas, and practice flows on Android 12+ ARM64 hardware.
- [ ] Add instrumentation coverage for the most failure-prone vocabulary and grammar paths when a device/emulator is available.

## Data and Learning

- [ ] Run source-data export integrity checks before each data release.
- [ ] Keep parser normalization streaming and bounded in memory.
- [ ] Expand deterministic grammar generation and Japanese pronunciation fixtures.
- [ ] Preserve FSRS/SRS review semantics and migration compatibility through regression tests.

## Performance and Accessibility

- [ ] Establish reproducible measurements for startup time, heap use, dictionary query latency, and recomposition hotspots.
- [ ] Verify long-list scrolling and memory behavior on both device classes.
- [ ] Validate touch target sizing, semantic labels, focus, and system font scaling.

## Repository Hygiene

- [ ] Keep root-level source limited to Gradle entry points, modules, documentation, product assets, and version-control configuration.
- [ ] Do not commit generated directories, Node dependencies, temporary scripts, logs, local editor metadata, or intermediate data dumps.
- [ ] Require `git diff --check`, Android compile, unit tests, and debug APK assembly for cleanup changes.
