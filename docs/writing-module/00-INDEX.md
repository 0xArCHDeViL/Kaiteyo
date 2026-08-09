# 📖 Kaiteyo — Writing Module Documentation

> **Comprehensive technical documentation for the Kanji/Kana stroke-writing practice system.**
> Written to enable any AI or engineer to fully replicate the Writing Module in another project.

---

## Table of Contents

| # | Document | Description |
|---|----------|-------------|
| 1 | [Architecture Overview](01-ARCHITECTURE.md) | High-level system architecture, layer diagram, dependency graph, design patterns |
| 2 | [Navigation & Entry Points](02-NAVIGATION.md) | How users reach the writing screen, `MainNavigation`, deck details flow |
| 3 | [Data Models](03-DATA-MODELS.md) | Every data class, sealed interface, and enum used across the module |
| 4 | [Configuration & Preferences](04-CONFIGURATION.md) | Pre-practice configuration screen, user preferences, SRS card selection |
| 5 | [Practice Queue & SRS](05-PRACTICE-QUEUE-SRS.md) | Queue management, spaced repetition scheduling, FSRS algorithm integration |
| 6 | [Stroke Evaluation Engine](06-STROKE-EVALUATOR.md) | Default and Alternative evaluators, path approximation, DTW algorithm |
| 7 | [Character Writer (Core)](07-CHARACTER-WRITER.md) | `CharacterWriterState`, input modes, stroke processing, animation states |
| 8 | [UI Components](08-UI-COMPONENTS.md) | All Compose UI: writing screen, info section, input section, answer buttons |
| 9 | [Canvas & Rendering](09-CANVAS-RENDERING.md) | Kanji rendering, SVG path parsing, `StrokeInput`, animated strokes |
| 10 | [Use Cases](10-USE-CASES.md) | Business logic use cases: configuration, queue data, item data, review state |
| 11 | [Dependency Injection](11-DEPENDENCY-INJECTION.md) | Koin module wiring, ViewModel instantiation, scoped dependencies |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin (Kotlin Multiplatform) |
| UI Framework | Jetpack Compose Multiplatform |
| DI | Koin |
| Database | SQLDelight |
| SRS Algorithm | FSRS (Free Spaced Repetition Scheduler) |
| Serialization | kotlinx.serialization |
| Time | kotlinx-datetime |
| Analytics | Custom `AnalyticsManager` |
| TTS | Custom `KanaTtsManager` |

---

## Quick Reference — Key Files

```
core/src/commonMain/kotlin/ua/syt0r/kanji/
├── core/
│   ├── app_data/
│   │   ├── AppData.kt                          # AppDataRepository interface (getStrokes, getMeanings, etc.)
│   │   └── SqlDelightAppDataRepository.kt       # SQLDelight implementation
│   ├── srs/
│   │   ├── SrsManager.kt                        # Abstract SRS manager base
│   │   ├── LetterSrsManager.kt                  # Letter-specific SRS (Writing + Reading)
│   │   ├── SrsScheduler.kt                      # FSRS scheduling
│   │   └── SrsCardRepository.kt                 # Card persistence
│   └── stroke_evaluator/
│       ├── KanjiStrokeEvaluator.kt              # Interface
│       ├── DefaultKanjiStrokeEvaluator.kt        # Error-threshold evaluator (22-point interpolation)
│       └── AltKanjiStrokeEvaluator.kt            # DTW-based evaluator (5-unit segments)
├── presentation/
│   ├── common/
│   │   ├── ScreenPracticeType.kt                 # ScreenLetterPracticeType enum (Writing, Reading)
│   │   ├── theme/
│   │   │   ├── Dimens.kt                         # Design tokens: spacing, radius, sizing
│   │   │   └── Shapes.kt                         # Material3 shape definitions
│   │   └── ui/kanji/
│   │       ├── Kanji.kt                          # Kanji composable, StrokeInput, parseKanjiStrokes
│   │       └── (AnimatedStroke, Stroke, etc.)
│   └── screen/main/screen/
│       ├── practice_common/
│       │   ├── CharacterWriter.kt                # Main writer composable
│       │   ├── CharacterWriterState.kt           # State machine for writing
│       │   ├── CharacterWriterDecorations.kt     # Grid lines, background decorations
│       │   ├── BrushSelector.kt                  # Brush thickness/style UI
│       │   ├── BrushSettings.kt                  # Brush data model
│       │   ├── PracticeAnswerButtonsUI.kt        # Answer buttons (Again/Hard/Good/Easy)
│       │   ├── PracticeCommonUI.kt               # Shared UI: toolbar, config, summary
│       │   └── PracticeQueue.kt                  # Abstract queue with SRS integration
│       └── practice_letter/
│           ├── DefaultLetterPracticeScreenContent.kt  # Screen entry point
│           ├── LetterPracticeScreenContract.kt        # Contract (ViewModel interface + ScreenState)
│           ├── LetterPracticeViewModel.kt             # ViewModel implementation
│           ├── LetterPracticeQueue.kt                 # Letter-specific queue
│           ├── LetterPracticeScreenModule.kt          # Koin DI module
│           ├── data/
│           │   ├── LetterPracticeScreenData.kt        # Configuration, ReviewState, ItemData, Layout
│           │   └── LetterPracticeQueueData.kt         # QueueState, QueueItem, SummaryItem, Descriptors
│           ├── ui/
│           │   ├── LetterPracticeScreenUI.kt          # Main screen layout + state routing
│           │   ├── LetterPracticeWritingUI.kt         # Writing review composable
│           │   ├── LetterPracticeWritingInfoSection.kt    # Character info panel
│           │   ├── LetterPracticeWritingInputSection.kt   # Drawing canvas wrapper
│           │   └── LetterPracticeWritingWordsBottomSheet.kt  # Example words bottom sheet
│           └── use_case/
│               ├── GetLetterPracticeConfigurationUseCase.kt
│               ├── GetLetterPracticeQueueDataUseCase.kt
│               ├── GetLetterPracticeQueueItemDataUseCase.kt
│               ├── GetLetterPracticeReviewStateUseCase.kt
│               └── UpdateLetterPracticeConfigurationUseCase.kt
```
