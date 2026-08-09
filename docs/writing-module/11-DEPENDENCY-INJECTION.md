# Dependency Injection (Koin)

This document outlines the Dependency Injection (DI) configuration for the writing module, utilizing the Koin framework. The setup defines how components, use cases, and ViewModels are instantiated and how their scopes and lifecycles are managed.

## Module Definition

The entire DI graph for the letter practice feature is encapsulated within the `letterPracticeScreenModule`:

```kotlin
val letterPracticeScreenModule = module { 
    // Dependency declarations go here
}
```

## Registered Dependencies

The following table summarizes the key dependencies registered within the module, their concrete implementations, their Koin scope, and the dependencies they require.

| Interface / Type | Implementation | Scope | Dependencies |
|------------------|----------------|-------|--------------|
| `GetLetterPracticeConfigurationUseCase` | `DefaultGetLetterPracticeConfigurationUseCase` | `factory` | `practicePreferences`, `srsCardRepository` |
| `UpdateLetterPracticeConfigurationUseCase` | `DefaultUpdateLetterPracticeConfigurationUseCase` | `factory` | `practicePreferences` |
| `GetLetterPracticeQueueDataUseCase` | `DefaultGetLetterPracticeQueueDataUseCase` | `factory` | `practicePreferences`, `srsCardRepository`, `configurationUpdateScope` (passed as parameter) |
| `GetLetterPracticeQueueItemDataUseCase` | `DefaultGetLetterPracticeQueueItemDataUseCase` | `factory` | `appDataRepository` |
| `GetLetterPracticeReviewStateUseCase` | `DefaultGetLetterPracticeReviewStateUseCase` | `factory` | `characterWriterCoroutineScope` (passed as parameter) |
| `LetterPracticeQueue` | `DefaultLetterPracticeQueue` | `factory` (named) | `coroutineScope` (parameter), `timeUtils`, `srsCardRepository`, `srsScheduler`, `getQueueItemDataUseCase`, `reviewHistoryRepository`, `analyticsManager` |
| `LetterPracticeScreenContract.ViewModel` | `LetterPracticeViewModel` | `multiplatformViewModel` | `viewModelScope` (parameter), all aforementioned use cases, `practiceQueue`, `analyticsManager`, `kanaTtsManager` |
| `LetterPracticeScreenContract.Content` | `DefaultLetterPracticeScreenContent` | `single` | - |

## Scoped Parameters

To properly manage coroutine lifecycles and prevent memory leaks, Koin's parameter passing mechanism (`parametersOf`) is heavily utilized to inject `CoroutineScope` instances from the ViewModel layer down to the underlying business logic.

- `viewModelScope` is passed down to:
  - `getQueueDataUseCase`
  - `practiceQueue`
  - `getReviewStateUseCase`
- **Rationale**: This guarantees that background tasks, StateFlows, and asynchronous operations initiated by these components share the precise lifecycle of the ViewModel. If the ViewModel is cleared, all associated coroutines are automatically canceled.

## Named Qualifier

The system registers `LetterPracticeQueue` using a specific Koin string qualifier: `named<LetterPracticeScreenContract>()`.

**Purpose**: This prevents dependency resolution conflicts with other queue implementations in the app, most notably the `VocabPracticeQueue`. By explicitly naming the dependency, the module ensures that the `LetterPracticeViewModel` receives the correct queue implementation specifically tailored for letter and stroke practice.

## `multiplatformViewModel`

The ViewModel is registered using the `multiplatformViewModel` function instead of the standard Android `viewModel` function.

**Purpose**: This is a custom Koin extension designed to bridge the gap between Android and Desktop (JVM) targets. It ensures that the appropriate ViewModel implementation is created based on the platform:
- On Android: It wires the class to the standard Android Architecture Components ViewModel, binding to the Fragment/Activity lifecycle.
- On Desktop: It provides a standard class instance with an internally managed coroutine scope that simulates the ViewModel lifecycle behavior.
