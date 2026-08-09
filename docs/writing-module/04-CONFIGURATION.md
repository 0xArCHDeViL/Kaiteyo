# Writing Module Configuration System

This document outlines the pre-practice configuration system for the writing module, detailing how user preferences and SRS data are aggregated to form the practice session state.

## Configuration Flow

The configuration flow dictates how a practice session is prepared before the user starts reviewing characters.

1. **`GetLetterPracticeConfigurationUseCase`**: This use case is the entry point. It loads user preferences from `PreferencesContract.PracticePreferences` and retrieves the SRS card cache.
2. **Determine Card Status**: It calls `SrsCardRepository.getAll()` to differentiate between new and non-new cards for the selected character set.
3. **`PracticeConfigurationCardsSelectorState`**: Creates the selector state taking into account the `shuffle` toggle and `newCardsOrder` (First, Last, Mixed).
4. **`unfilteredResultCardsList`**: Builds a `derivedStateOf` list of cards based on the shuffle and order preferences. This list updates dynamically if configuration options change.
5. **Return Value**: Returns `LetterPracticeConfiguration.Writing` populated with all `MutableState` fields, allowing the UI to reactively observe and modify configuration before starting the queue.

## Writing Configuration Options

The following options configure the writing practice session. They are exposed as mutable states in the configuration object.

| Option | Data Type | Default | Persistence Key | UI Control |
|---|---|---|---|---|
| `selectorState` | `PracticeConfigurationCardsSelectorState` | Count: 10, Shuffle: true, Order: Mixed | `practice_cards_count`, `practice_shuffle`, `practice_new_cards_order` | Slider (`PracticeConfigurationItemsSelector`), Option Toggles |
| `hintMode` | `WritingPracticeHintMode` | `OnlyNew` | `writing_hint_mode` | `PracticeConfigurationEnumSelector` (OnlyNew, All, None) |
| `inputMode` | `WritingPracticeInputMode` | `Stroke` | `writing_input_mode` | `PracticeConfigurationEnumSelector` (Stroke, Character) |
| `useRomajiForKanaWords`| `Boolean` | `false` | `writing_use_romaji` | `PracticeConfigurationOption` (Toggle) |
| `noTranslationsLayout` | `Boolean` | `false` | `writing_no_translations` | `PracticeConfigurationOption` (Toggle) |
| `leftHandedMode` | `Boolean` | `false` | `writing_left_handed` | `PracticeConfigurationOption` (Toggle) |
| `altStrokeEvaluatorEnabled`| `Boolean` | `false` | `writing_alt_evaluator` | `PracticeConfigurationOption` (Toggle) |

## Study Mode Logic

The "Study Mode" determines whether a user is shown the character strokes animating *before* they are prompted to write it. It is calculated dynamically based on the current hint mode and the SRS state of the specific card.

```kotlin
val shouldStudy = when (configuration.hintMode.value) {
    WritingPracticeHintMode.OnlyNew -> srsCardRepository.get(key) == null  // only new cards
    WritingPracticeHintMode.All -> true
    WritingPracticeHintMode.None -> false
}
```

In Study Mode, the character strokes animate sequentially. Once the study phase is complete, the user transitions to Review Mode where they must recall and write the character from memory.

## UpdateLetterPracticeConfigurationUseCase

Once the user confirms their configuration and starts the practice session, `UpdateLetterPracticeConfigurationUseCase` is invoked. It takes the current values from the `LetterPracticeConfiguration.Writing` mutable states and persists all configuration changes back to `PreferencesContract.PracticePreferences`. This ensures the user's settings are remembered for their next session.

## Configuration UI

The configuration UI is defined in `LetterPracticeScreenUI.kt` under the `ConfiguringState`. It utilizes a `PracticeConfigurationContainer` to organize the settings.

The container includes:
* **`PracticeConfigurationItemsSelector`**: A slider to select the total number of cards for the session.
* **`PracticeConfigurationCharactersPreview`**: A grid preview displaying the characters that will be practiced based on the current selector state.
* **`PracticeConfigurationEnumSelector`**: Used for enum-based preferences like `hintMode` and `inputMode`.
* **`PracticeConfigurationOption`**: Toggle switches for boolean options such as `useRomajiForKanaWords`, `noTranslationsLayout`, `leftHandedMode`, and `altStrokeEvaluatorEnabled`.
