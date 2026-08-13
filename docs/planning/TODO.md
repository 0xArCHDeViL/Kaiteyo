# 📋 Master Implementation Plan (TODO)

This document is the actionable master implementation plan, indexed directly from `CURRENT_ISSUES.md` and `FUTURE_IDEAS.md`. It tracks active development tasks categorized by priority.

## 🔴 Phase 1: Critical Fixes (P0)
**Goal:** Resolve all blocking usability and baseline design issues.

### Desktop Window Dynamics
- [ ] Restrict `WindowDraggableArea` to the top 44dp only.
- [ ] Fix interactive components capturing drag events unnecessarily.
- [ ] Optimize hover, theme, and window animations to 60 FPS.
- [ ] Fix panel jumps, spacing changes, and animation glitches on window resize.
- [ ] Unify hover animations across all interactive components.

### UI/UX Design System Base
- [ ] Audit and enforce consistent padding/margin (4dp grid).
- [ ] Fix alignment across cards, lists, and settings panels.
- [ ] Establish clear visual hierarchy (primary, secondary, tertiary).
- [ ] Refactor rounded corners to use a consistent `MaterialTheme.shapes` strategy.
- [ ] Ensure consistent typography styles.

## 🟡 Phase 2: High Priority Android Validation (P1)
**Goal:** Prove the Android-only phone/tablet contract on real device classes.

### Device Matrix
- [ ] Phone: Android 12+, ARM64-v8a, portrait-only startup and recreation.
- [ ] Tablet/pad: Android 12+, ARM64-v8a, landscape-only startup and tablet rail/content shell.
- [ ] Deep links, process recreation, database migration, keyboard, insets, and back handling.
- [ ] Letter Practice, Vocabulary Detail, and Grammar Practice instrumentation flows.

### Reliability Gate
- [ ] Reproduce and validate the no-optional-metadata vocabulary path.
- [ ] Validate unknown JMdict metadata aliases and future unknown-safe behavior.
- [ ] Validate unavailable grammar item skip without SRS/review-history side effects.
- [ ] Validate malformed review-history rows do not crash dashboards.

## 🟢 Phase 3: Medium Priority Android Engineering (P2)
**Goal:** Improve measurable Android performance and learning correctness.

### Data and Learning Engine
- [ ] Database export integrity, foreign-key checks, domain coverage, and release checksum.
- [ ] FSRS/SRS review persistence and boundary tests.
- [ ] Grammar generator validity, deterministic seeds, and answer quality fixtures.
- [ ] Contextual Japanese pronunciation and TTS fallback fixtures.

### Performance and Accessibility
- [ ] Measure cold start, heap footprint, query latency, and recomposition hotspots.
- [ ] Verify long-list rendering and scrolling on phone and tablet.
- [ ] Verify touch targets, semantics, keyboard, and reduced-motion behavior where applicable.

## 🔵 Phase 4: Low Priority & Future Pipeline (P3)
**Goal:** Optimization, accessibility, and exploration of community/cloud features.

### Performance & Accessibility
- [x] Profile and optimize recompositions / Lazy loading.
- [x] Image caching & APK/MSI size reduction.
- [x] Full keyboard navigation and screen reader support.

### Evaluated Future Ideas
- [ ] **AI Review Scheduling:** Optimize SRS intervals.
- [ ] **Cross-device Sync & Cloud Backup.**
- [ ] **Community:** Shared decks and Theme marketplace.
- [ ] **Platform Expansion:** Linux Snap/Flatpak, Web version.
