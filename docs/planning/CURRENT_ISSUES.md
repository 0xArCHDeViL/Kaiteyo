# Current Issues

This is the active reliability backlog for the Android-only product. Historical completed work belongs in `COMPLETED.md`; it must not be copied back into this file as active scope.

## P0 — Release Blockers

| Area | Required outcome | Evidence |
|---|---|---|
| Vocabulary detail | Missing optional metadata cannot crash rendering or data mapping | Regression fixture and Android UI/device validation |
| Grammar practice | Invalid/unavailable items are recoverable and can be skipped without review side effects | Queue/state regression tests |
| Data release | Exported app database passes integrity checks and checksum verification | Export log and integrity gate |
| Review persistence | Malformed historical rows do not crash statistics or streak calculation | Repository regression tests |

## P1 — Android Device Validation

| Device class | Required validation |
|---|---|
| Phone, Android 12+, ARM64 | Portrait lock, navigation, keyboard/insets, practice flows, process recreation |
| Tablet/pad, Android 12+, ARM64 | Portrait dan landscape, adaptive strip/rail, navigation, dialogs, writing canvas, practice flows, process recreation |

## P2 — Engineering Quality

- Measure cold start, heap footprint, query latency, and Compose recomposition hotspots before tuning.
- Add fixtures for future unknown JMdict metadata and malformed optional source records.
- Expand deterministic grammar-generation coverage and answer-quality fixtures.
- Verify accessibility semantics and touch targets across phone and tablet layouts.

No desktop, iOS, web, Appearance Studio, Theme Studio, floating-window, or hover-navigation work is in the active product backlog.
