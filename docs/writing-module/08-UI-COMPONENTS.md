# Writing Module UI Components

This document outlines the UI components used within the writing module of the application. These composables are responsible for rendering the letter practice screens, managing user interactions, and displaying relevant information.

## `LetterPracticeScreenUI`

The top-level screen composable that orchestrates the entire letter practice experience. It accepts a `State<ScreenState>` and routes the user interface to the appropriate sub-composable based on the current state:

- **Loading**: Renders `FancyLoading` while initial data is being fetched.
- **Configuring**: Renders `ConfiguringState` (via `PracticeConfigurationContainer`), allowing the user to set up their practice session.
- **Review**: Renders `ReviewState`, which delegates to either `LetterPracticeWritingUI` or `LetterPracticeReadingUI` depending on the practice mode.
- **Summary**: Renders `SummaryState` (via `PracticeSummaryContainer`) upon completion of the practice session.

### Auxiliary Components
- **Toolbar**: Uses `PracticeToolbar` configured with `PracticeToolbarState`. The state can be `Idle`, `Configuration`, or `Review` (which includes progress tracking).
- **Early Finish Dialog**: Utilizes `PracticeEarlyFinishDialog` in conjunction with `MultiplatformBackHandler` to confirm if the user wants to end their practice session prematurely.

## `LetterPracticeWritingUI`

This is the primary screen for the writing review mode. It adapts its layout based on the device orientation and user preferences.

### Portrait Layout

```text
┌─────────────────────┐
│  Info Section       │  ← LetterPracticeWritingInfoSection
│  (scrollable)       │     (readings, meanings, radicals, kana info)
│                     │
├─────────────────────┤
│  Brush Selector     │  ← BrushSelector
├─────────────────────┤
│  ┌───────────────┐  │
│  │  Drawing      │  │  ← LetterPracticeWritingInputSection
│  │  Canvas       │  │     (CharacterWriter + decorations)
│  │  (1:1 aspect) │  │
│  └───────────────┘  │
├─────────────────────┤
│  Answer Buttons     │  ← AnswerButtons (Hidden/StudyButtons/DefaultButtons)
└─────────────────────┘
```

### Landscape Layout

```text
┌────────────┬────────────┐
│ Info       │ Brush      │
│ Section    │ Selector   │
│ (scroll)   ├────────────┤
│            │ Drawing    │
│            │ Canvas     │
│            │ (1:1)      │
├────────────┴────────────┤
│     Answer Buttons      │
└─────────────────────────┘
```
*Note: When `leftHandedMode` is enabled, the left and right panels are swapped to accommodate the user.*

### Bottom Sheet

The `LetterPracticeWritingWordsBottomSheet` component displays example words using `LetterPracticeExampleWord` data. It is implemented using the `Material3BottomSheetScaffold`.

## `LetterPracticeWritingInfoSection`

This component renders the contextual information for the character being practiced, based on `WritingPracticeInfoSectionData`.

- **`KanjiWritingData`**: Displays:
  - ON/KUN readings using `KanjiReadingsContainer`.
  - Meanings of the kanji.
  - Radicals comprising the kanji via `KanjiRadicalsSection`.
  - Variant family characters.
- **`KanaWritingData`**: Displays:
  - The kana system name (Hiragana/Katakana) and its Nihon-Shiki reading.
  - `KanaVoiceMenu` with auto-play capabilities.
- **Example Words Button**: A trigger to open the bottom sheet containing example words.

## `LetterPracticeWritingInputSection`

A wrapper around the `CharacterWriter` that sets up the drawing area.

- Accepts a `State<LetterPracticeReviewState.Writing>`.
- Renders the `CharacterWriter` passing the current `writerState`.
- Incorporates `CharacterWriterDecorations`, such as the grid background.
- Applies the active `BrushSettings`.

## Answer Buttons

The buttons at the bottom of the screen manage the user's progression and self-evaluation. They operate in three states dictated by `LetterWritingButtonsState`:

1. **Hidden**: No buttons are shown. This state is active while the user is actively drawing.
2. **StudyButtons**: Displays a "Next →" button to transition the user from study mode to review mode.
3. **DefaultButtons**: Renders `PracticeAnswerButtonsRow` featuring the standard Spaced Repetition System (SRS) buttons (Again, Hard, Good, Easy). Each button carries the specific SRS answer and the accumulated mistakes for the current review item.

*Animations*: The buttons transition between states using `slideIn`/`slideOut` combined with `fadeIn`/`fadeOut` animations.

## Summary Screen (`PracticeSummaryContainer`)

Displayed at the end of a session, showing aggregate statistics:
- **Duration**: Total time spent in the session.
- **Reviews count**: Number of items reviewed.
- **Accuracy**: For writing practice, this is calculated as `(correctStrokes / totalStrokes) * 100`.
- **Per-item List**: Renders a `PracticeSummaryItem` for each reviewed item, displaying the letter, total reviews (resolved via `produceState`), and the next interval.

## `WritingPracticeVocabHeadline`

Displays an example word associated with the current character, employing conditional rendering logic:
- **Romaji available**: Rendered as plain text.
- **Revealed state**: Rendered using `FuriganaWordHeadline` to show full furigana.
- **Hidden state**: Rendered using `FuriganaText` with the target letter encoded (hidden).

## `KanjiVariantsRow`

A row component that reveals variant characters upon interaction (click). The reveal uses an animated alpha transition for a smooth visual effect.

## `KanaVoiceMenu`

A component providing audio playback for Kana characters. It includes:
- An auto-play toggle switch.
- A speak button featuring a wave animation during playback.
- Relies on `KanaTtsManager` for text-to-speech functionality.
