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

## 🟡 Phase 2: High Priority Features (P1 - v1.2)
**Goal:** Deliver the signature floating UI, full theme system, and complete Appearance Studio.

### The Floating UI
- [ ] Detach sidebar from window edges (create the "floating island" look with elevation/shadow/glow).
- [ ] Implement Dock positions (Left, Right, Top, Bottom, Floating).
- [ ] Add auto-hide behavior with hover/click reveal.
- [ ] Create spring-based expand/collapse animations.
- [ ] Implement Snap Layouts for valid positions.

### Theme & Aesthetics Engine
- [ ] Implement the 7 missing built-in themes (OLED, Dark Gray, Light, Reading, Cotton Candy, Ocean, Forest).
- [ ] Intelligent color distribution for Signature theme (balance Lime & Orange).
- [ ] Enhance glow effects (animated glows for buttons, cards, window controls).
- [ ] Apply proper gradient distribution to active states.

### Appearance Studio (Advanced Settings)
- [ ] Build a Color Editor (RGB, HSV, HSL, HEX).
- [ ] Build a Gradient Editor (stops, angle, intensity).
- [ ] Build Live Preview panels.
- [ ] Add Theme JSON Import/Export.
- [ ] Add Animation Controls (None, Minimal, Standard, Smooth, Bouncy).
- [ ] Add Layout Controls (Density, Corner Radius, Blur, Elevation).

## 🟢 Phase 3: Medium Priority Enhancements (P2 - v1.3)
**Goal:** Introduce advanced studios (Motion & Layout) and polish the brand experience.

### Motion & Layout Studios
- [ ] Motion Studio: Per-component animation control & reduced motion support.
- [ ] Layout Studio: Fine-grained controls for transparency, blur, and surface elevation.

## 🔵 Phase 4: Low Priority & Future Pipeline (P3)
**Goal:** Optimization, accessibility, and exploration of community/cloud features.

### Performance & Accessibility
- [ ] Profile and optimize recompositions / Lazy loading.
- [ ] Image caching & APK/MSI size reduction.
- [ ] Full keyboard navigation and screen reader support.

### Evaluated Future Ideas
- [ ] **AI Review Scheduling:** Optimize SRS intervals.
- [ ] **Cross-device Sync & Cloud Backup.**
- [ ] **Community:** Shared decks and Theme marketplace.
- [ ] **Platform Expansion:** Linux Snap/Flatpak, Web version.
