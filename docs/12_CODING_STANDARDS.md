# Coding Standards

## Kotlin and Coroutines

Use four-space indentation, explicit imports, and names that expose domain meaning. Prefer immutable values and immutable UI state. Public behavior should be modeled with data classes, sealed hierarchies, and focused use cases rather than nullable sentinel values.

Suspend code must preserve cancellation: never swallow `CancellationException` and never use `catch (Throwable)` as an application error boundary. Missing or malformed learning data must become an explicit unavailable/error state; do not substitute invented questions, answers, readings, or review outcomes.

## Compose Android

Composable functions use PascalCase. Keep `modifier: Modifier = Modifier` as the final optional parameter. Keep state hoisted at the nearest sensible screen/state-holder boundary, use stable keys in dynamic lists, and avoid blocking work during composition.

The product layout contract is mandatory: phone UIs target portrait interaction, while tablet/pad UIs target the landscape rail-and-content shell. Do not introduce desktop window controls, hover-dependent navigation, mouse-only affordances, or platform-neutral UI abstractions that lack an Android requirement.

## Data and Persistence

Use SQLDelight interfaces and repositories as boundaries between serialized data and domain models. Validate optional metadata, parse defensively, and log recoverable malformed records at the repository boundary. Preserve migrations and the meaning of review history; schema changes require a migration and regression coverage.

## Testing and Hygiene

Every bug fix needs a focused regression test when behavior can be tested deterministically. Before commit, run relevant Android compile tasks, `:core:testDebugUnitTest`, and `:app:assembleDebug` for packaging/resource changes.

Do not commit generated build output, `.gradle`, `.kotlin`, IDE state, Node dependencies, temporary scripts, logs, screenshots, or analysis artifacts. Keep the repository root limited to build entry points, modules, documentation, version-control configuration, and product assets.
