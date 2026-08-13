# Kaiteyo — Current Issues

This is a living document. Add issues as they are discovered, mark them as fixed when resolved.

## 🔴 P0 — Critical (Blocking Usability)

### Desktop Window

- [ ] **Window dragging grabs the whole UI** — The entire application acts as a drag region. Buttons cannot be clicked, lists cannot be scrolled, settings cannot be used. Only the top 44dp should be draggable.
- [ ] **Interactive components are draggable** — WindowDraggableArea is too broad, making all UI components respond to drag events.
- [ ] **Animation stuttering** — Hover animations, theme switching, and window movement are not smooth. Target 60 FPS.
- [ ] **Resize glitches** — Panels jump, spacing changes unexpectedly, animations break during window resize.
- [ ] **Hover animations are inconsistent** — Some elements animate on hover, others don't. The behavior varies across components.

### Design

- [ ] **Inconsistent spacing** — Different components use different padding/margin values. No adherence to the 4dp grid.
- [ ] **Poor component alignment** — Elements in cards, lists, and settings panels don't align properly with each other.
- [ ] **No clear visual hierarchy** — It's hard to distinguish primary, secondary, and tertiary content at a glance.
- [ ] **Sidebar looks attached** — The sidebar feels like it's glued to the window edge. It should be a floating island with elevation and shadow.
- [ ] **Rounded panels don't feel intentional** — Some elements are rounded, some are square. No consistent corner radius strategy.

### Settings
- [x] **Appearance options are disorganized** — Settings were redesigned into logical cards.

## 🟡 P1 — High (Android validation)

### Android Device UX
- [ ] **Phone portrait contract** — Verify the phone shell remains portrait-only on Android 12+ devices.
- [ ] **Tablet landscape contract** — Verify the dedicated rail/content shell on tablets and pads.
- [ ] **Touch and insets** — Verify navigation, practice controls, writing canvas, dialogs, and keyboard behavior on both device classes.
- [ ] **Configuration changes** — Verify orientation policy, process recreation, deep links, and database migration on Android devices.

### Functional UI Reliability
- [ ] **Letter Practice rendering** — Validate vocabulary detail with optional metadata absent.
- [ ] **Grammar Practice recovery** — Validate unavailable items can be skipped without an SRS review side effect.
- [ ] **Long content surfaces** — Verify scrolling and memory behavior on vocabulary, history, statistics, and deck screens.

## 🟢 P2 — Medium (Android polish)

### Android Interaction Polish
- [ ] Consistent touch target sizing and focus behavior.
- [ ] Stable tablet rail selection and content transitions.
- [ ] Accessibility semantics for practice, navigation, dialogs, and writing controls.


### Branding

- [x] Replace remaining "Kanji Dojo" references in user-facing strings
- [x] Update desktop app title/installer name
- [x] Update GitHub metadata and README
- [x] Update about page
- [x] Update splash screen

## 🔵 P3 — Low (Future)

### Performance

- [x] Profile and optimize recompositions
- [x] Lazy loading for long lists
- [x] Image caching
- [x] Reduce APK/MSI size

### Accessibility

- [x] Keyboard navigation
- [x] Screen reader support
- [x] High contrast mode
- [x] Font size adjustment

## ✅ Recently Fixed

- [x] **Persisted deck archive flag** — `is_archived` columns on `letter_deck`/`vocab_deck` (previously dead, added only by migration 13) are now in the SQLDelight schema, backed by `updateDeckArchived` repository methods and a toggle in the Deck Edit save dialog. **Follow-up:** filter archived decks from the main dashboard lists and add an "Archived" section to restore them (currently archived decks stay visible everywhere).
- [x] **Settings UI Redesign** — Transformed the old, flat settings list into a premium, card-based UI with clear categories (Preferences, Data & Sync, More) and proper visual constraints.
- [x] **Removed Sponsor Screen** — Eliminated "Buy Me a Coffee" screen and navigation to fix `UnresolvedAddressException` and unify architecture.
- [x] **Unified Library hub** — Home now has a single Library tab (replaces Kanji/Vocabulary split). Includes hub with Sections + stat summary rows and drill-down screens (Kanji Decks, Vocabulary, Word & Sentence Search). Old default-tab preference remapped.
- [x] **Import error: `animateColorAsState`** — Fixed by importing from `androidx.compose.animation`
- [x] **Import error: `animateFloatAsState`** — Fixed by importing from `androidx.compose.animation.core`
- [x] **`windowState.window!!.close()` error** — Fixed by using `window.close()` in FrameWindowScope
- [x] **Missing `@Composable` import** — Added `import androidx.compose.runtime.Composable`
