# Data Models

## Configuration Models

### 1. LetterPracticeScreenConfiguration
- **Signature**: `data class LetterPracticeScreenConfiguration(val practiceType: ScreenLetterPracticeType, val cards: List<Card>)`
- **Purpose**: Serializable navigation argument passed to the destination to initiate a session.
- **Relationships**: Contains `ScreenLetterPracticeType`.

### 2. LetterPracticeConfiguration
- **Signature**: `sealed interface LetterPracticeConfiguration { ... }` (Implementations: `Writing`, `Reading`)
- **Purpose**: Holds runtime configuration state during the "Configuring" phase. Fields are often backed by `MutableState` for UI binding.
- **Relationships**: Created by `GetLetterPracticeConfigurationUseCase`.

### 3. LetterPracticeLayoutConfiguration
- **Signature**: `sealed interface LetterPracticeLayoutConfiguration` (Implementations: `WritingLayoutConfiguration`, `ReadingLayoutConfiguration`)
- **Purpose**: Determines UI layout specific constraints or settings based on device orientation and screen size.

### 4. WritingPracticeHintMode
- **Signature**: `enum class WritingPracticeHintMode { OnlyNew, All, None }`
- **Purpose**: Determines when background hints/stroke order guides are shown during practice.

### 5. WritingPracticeInputMode
- **Signature**: `enum class WritingPracticeInputMode { Stroke, Character }`
- **Purpose**: Determines how input is evaluated (stroke-by-stroke or full character at once).

## Queue Models

### 6. LetterPracticeQueueState
- **Signature**: `sealed interface LetterPracticeQueueState` (Implementations: `Loading`, `Review`, `Summary`)
- **Purpose**: Represents the core state of the `LetterPracticeQueue`.

### 7. LetterPracticeQueueItemDescriptor
- **Signature**: `sealed interface LetterPracticeQueueItemDescriptor` (Implementations: `Writing`, `Reading`)
- **Purpose**: Immutable descriptor for each card in the queue, defining what needs to be practiced.

### 8. LetterPracticeQueueItem
- **Signature**: `data class LetterPracticeQueueItem(val card: SrsCard, var repeats: Int, var mistakes: Int, val lazyData: Deferred<LetterPracticeItemData>)`
- **Purpose**: Runtime representation of an item actively in the queue, tracking performance metrics.

### 9. LetterPracticeSummaryItem
- **Signature**: `sealed interface LetterPracticeSummaryItem` (Implementations: `Writing`, `Reading`)
- **Purpose**: Represents the final performance of a specific item for display on the summary screen.

## Item Data Models

### 10. LetterPracticeItemData
- **Signature**: `sealed interface LetterPracticeItemData`
- **Implementations**:
  - `WritingData` (sealed): `KanaWritingData`, `KanjiWritingData` (both contain `val strokes: List<Path>`)
  - `ReadingData` (sealed): `KanaReadingData`, `KanjiReadingData`
  - Interfaces: `KanaData` (kanaSystem, reading), `KanjiData` (radicals, on, kun, meanings, variants)
- **Purpose**: Holds the rich DB data needed to render the practice UI (strokes, meanings, readings).
- **Relationships**: Loaded by `GetLetterPracticeQueueItemDataUseCase`.

### 11. LetterPracticeExampleWord
- **Signature**: `data class LetterPracticeExampleWord(val word: JapaneseWord, val romaji: String?)`
- **Purpose**: Represents an example word shown during review to provide context.

## Review State Models

### 12. LetterPracticeReviewState
- **Signature**: `sealed interface LetterPracticeReviewState`
- **Implementations**:
  - `Writing`: `data class Writing(val studyWriterState: CharacterWriterState?, val reviewWriterState: CharacterWriterState, val isStudyMode: MutableState<Boolean>)` (Includes a derived `writerState` property).
  - `Reading`: `data class Reading(val revealed: MutableState<Boolean>)`
- **Purpose**: Holds the interactive UI state for a specific review item.

## Writer State Models (`practice_common`)

### 13. CharacterWriterState
- **Signature**: `interface CharacterWriterState`
- **Purpose**: Core interface managing the state machine of the canvas drawing experience.
- **Properties**: `character`, `strokes`, `configuration`, `content`, `progress`.

### 14. CharacterWriterConfiguration
- **Signature**: `sealed interface CharacterWriterConfiguration`
- **Implementations**: `StrokeInput(val isStudyMode: Boolean)`, `CharacterInput`
- **Purpose**: Configures how the writer accepts input.

### 15. CharacterWriterContent
- **Signature**: `sealed interface CharacterWriterContent`
- **Implementations**: `SingleStrokeInput`, `MultipleStrokeInput`, `Animation`
- **Purpose**: Represents the current active drawing mode.

### 16. CharacterWritingProgress
- **Signature**: `sealed interface CharacterWritingProgress`
- **Implementations**: `Writing`, `Completed.Idle`, `Completed.Animating`
- **Purpose**: Tracks overall progress of the character being drawn.

### 17. CharacterInputData
- **Signature**: `sealed interface CharacterInputData`
- **Implementations**: `SingleStroke`, `MultipleStrokes`
- **Purpose**: Raw input data captured from the user.

### 18. StrokeProcessingResult
- **Signature**: `sealed interface StrokeProcessingResult`
- **Implementations**: `Correct`, `Mistake`
- **Purpose**: Outcome of the evaluator processing an input stroke.

### 19. BrushSettings
- **Signature**: `data class BrushSettings(...)`
- **Purpose**: Configures the visual appearance of the brush on the canvas.

## Common Models

### 20. PracticeQueueProgress
- **Signature**: `data class PracticeQueueProgress(val pending: Int, val repeats: Int, val completed: Int)`
- **Purpose**: Used for rendering progress bars/counters.

### 21. PracticeAnswers
- **Signature**: `data class PracticeAnswers(val again: PracticeAnswer?, val hard: PracticeAnswer?, val good: PracticeAnswer?, val easy: PracticeAnswer?)`
- **Purpose**: Holds the available SRS answer buttons for the current state.

### 22. PracticeAnswer
- **Signature**: `data class PracticeAnswer(val srsAnswer: SrsAnswer, val mistakes: Int)`
- **Purpose**: Represents a specific user grading action.

### 23. PracticeSummaryItem
- **Signature**: `interface PracticeSummaryItem`
- **Purpose**: Base interface for any item appearing on a practice summary screen.

### 24. PracticeQueueItem
- **Signature**: `interface PracticeQueueItem`
- **Purpose**: Base interface for any queue item in a generic practice session.
