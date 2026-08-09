# Writing Module Use Cases

This document describes the core business logic components (Use Cases) utilized within the writing module. These use cases encapsulate the operations required to configure, fetch, and manage the state of a practice session.

## `GetLetterPracticeConfigurationUseCase`

**Interface**:
```kotlin
suspend fun invoke(config: LetterPracticeScreenConfiguration): LetterPracticeConfiguration
```

**Implementation**: `DefaultGetLetterPracticeConfigurationUseCase`
**Dependencies**: `PracticePreferences`, `SrsCardRepository`

**Logic**:
1. Fetches all existing Spaced Repetition System (SRS) cards from the database using `srsCardRepository.getAll()`.
2. Iterates over the items provided in `configuration.cards` and determines if each item is "new" (i.e., it does not exist in the cached SRS cards list).
3. Segregates the requested cards into two distinct lists: `newCards` and `nonNewCards`.
4. Initializes a `PracticeConfigurationCardsSelectorState`, applying the user's preferred shuffling and `newCardsOrder` settings retrieved from `PracticePreferences`.
5. Constructs the `unfilteredResultCardsList` using a `derivedStateOf` block, ordering the cards based on `PreferencesNewCardsOrder`:
   - `First`: `newCards` (shuffled if applicable) followed by `nonNewCards` (shuffled if applicable).
   - `Last`: `nonNewCards` followed by `newCards`.
   - `Mixed`: All cards combined and shuffled as a single list.
6. **For Writing Practice**: Returns `LetterPracticeConfiguration.Writing`, populating all its `MutableState` fields with the corresponding values loaded from preferences.
7. **For Reading Practice**: Returns a `LetterPracticeConfiguration.Reading` instance.

## `GetLetterPracticeQueueDataUseCase`

**Interface**:
```kotlin
suspend fun invoke(config: LetterPracticeConfiguration): List<LetterPracticeQueueItemDescriptor>
```

**Implementation**: `DefaultGetLetterPracticeQueueDataUseCase`
**Dependencies**: `PracticePreferences`, `SrsCardRepository`, `configurationUpdateScope`

**Logic**:
1. Retrieves the `kanaAutoPlay` user preference and establishes a `snapshotFlow` to automatically persist any changes made during the session.
2. **For Writing Practice**:
   - Loads the `radicalsHighlight` preference and sets up a persistence flow for it.
   - Constructs a `WritingLayoutConfiguration` object encapsulating all relevant layout settings.
   - Selects the appropriate stroke evaluation mechanism based on the `altStrokeEvaluatorEnabled` flag.
   - Iterates over the configured cards (up to the `selectedCount` limit):
     - Calculates `shouldStudy` based on the configured `hintMode` and the card's learning history.
     - Generates a `LetterPracticeQueueItemDescriptor.Writing` object containing this data.
3. **For Reading Practice**: Performs a simplified descriptor creation process without stroke or layout specifics.

## `GetLetterPracticeQueueItemDataUseCase`

**Interface**:
```kotlin
suspend fun invoke(descriptor: LetterPracticeQueueItemDescriptor, coroutineScope: CoroutineScope): LetterPracticeItemData
```

**Implementation**: `DefaultGetLetterPracticeQueueItemDataUseCase`
**Dependency**: `AppDataRepository`

**Logic**:
1. Inspects the character string to determine if it is a kana or kanji character.
2. Loads paginated example words associated with the character:
   - For kana: Calls `getKanaWords`.
   - For kanji: Calls `getWordsWithText`.
   - Fetches the primary set of examples using `getWordExamples`.
   - Wraps the results in a pagination wrapper via `paginateable()`, enabling preloading of subsequent pages.
3. **For Writing + Kana**:
   - Retrieves stroke data via `appDataRepository.getStrokes(character)` and parses it using `parseKanjiStrokes`.
   - Retrieves classification and reading data via `getKanaInfo`.
   - Returns a `KanaWritingData` object.
4. **For Writing + Kanji**:
   - Retrieves and parses stroke data.
   - Fetches kanji-specific details: `getRadicalsInCharacter`, `getReadings` (ON/KUN), `getMeanings`, and `getData` (for variant families).
   - Returns a `KanjiWritingData` object.

## `GetLetterPracticeReviewStateUseCase`

**Interface**:
```kotlin
fun invoke(queueState: LetterPracticeQueueState.Review): LetterPracticeReviewState
```

**Implementation**: `DefaultGetLetterPracticeReviewStateUseCase`
**Dependency**: `characterWriterCoroutineScope`

**Logic (For Writing)**:
1. **Study State Generation**: Evaluates if a study phase is required (`if (shouldStudy && firstRepeat)`). If true, it creates a `studyWriterState` initialized with `DefaultCharacterWriterState` using `StrokeInput(isStudyMode = true)`.
2. **Review State Generation**: Creates the active `reviewWriterState` based on the configured input mode:
   - If `inputMode == Stroke`: Initializes `StrokeInput(isStudyMode = false)`.
   - If `inputMode == Character`: Initializes `CharacterInput`.
3. Constructs and returns `LetterPracticeReviewState.Writing`, bundling both the study and review states for the UI to consume.

## `UpdateLetterPracticeConfigurationUseCase`

This use case is responsible for persisting all transient configuration adjustments made by the user in the UI (e.g., changing brush settings, toggling hints) back to the permanent storage via `PracticePreferences`.
