# Canvas and Rendering System

This document details the rendering engine for characters (Kanji and Kana) in the writing module, explaining the coordinate space, SVG processing pipeline, and the Compose UI components that utilize them.

## Coordinate System

The rendering system employs a fixed, normalized coordinate space to ensure consistency across different screen sizes and aspect ratios.

- **Grid Size**: All stroke data is defined within a 109×109 coordinate space (`KanjiSize = 109`).
- **Scaling**: Canvas composables automatically scale this 109×109 grid to completely fill the available pixel space of the device.
- **Input Mapping**: User touch and drag inputs are captured in screen coordinates and inversely scaled down to the 109×109 space before processing and validation.

## SVG Path Pipeline

Characters are rendered by converting standard SVG path strings into Compose `Path` objects. The pipeline is as follows:

1. **Storage**: The database stores individual character strokes as a list of SVG path strings (e.g., `"M 10 20 C 30 40 50 60 70 80"`).
2. **Retrieval**: `AppDataRepository.getStrokes(character)` is called to retrieve the `List<String>` containing the SVG paths.
3. **Parsing** (`Kanji.kt`): The `parseKanjiStrokes(strokes)` function processes the raw strings:
   - `SvgCommandParser.parse(svgString)` converts the string into a `List<SvgCommand>`.
   - `SvgPathCreator.convert(commands)` translates these commands into an Android/Compose native `Path` object.
4. **Rendering**: The resulting `List<Path>` is distributed to all rendering composables (`Kanji`, `StrokeInput`, `AnimatedKanji`, etc.).

## `Kanji` Composable

The fundamental composable for displaying a static character.

```kotlin
@Composable
fun Kanji(
    strokes: List<Path>,
    modifier: Modifier = Modifier,
    strokeColor: Color,
    strokeWidth: Float,
    brushSettings: BrushSettings
)
```

It creates a `Canvas` that iterates through the provided `strokes` list and invokes `drawKanjiStroke` for each path.

## `drawKanjiStroke` (expect/actual)

A platform-specific drawing function (using Kotlin Multiplatform's `expect`/`actual` mechanism) to render a single stroke onto the canvas.

```kotlin
expect fun DrawScope.drawKanjiStroke(
    path: Path,
    color: Color,
    width: Float,
    drawProgress: Float? = null,
    brushSettings: BrushSettings
)
```

**Responsibilities**:
- Scales the given `path` from the 109×109 coordinate system to the actual dimensions of the `DrawScope`.
- Applies the styling defined in `BrushSettings` (width, cap, join, alpha).
- Optionally applies `drawProgress` to partially draw a stroke (used in animations).

## `StrokeInput` Composable

Handles the logic and rendering for user drawing input during practice.

1. **Measurement**: Uses `Modifier.onGloballyPositioned` to capture the exact pixel dimensions of the drawing area.
2. **Input Handling**: Uses `Modifier.pointerInput` with `detectDragGestures` to capture touch events:
   - **`onDragStart`**: Instantiates a new `Path` and moves to the starting coordinates, mapping them via `(x / areaSize) * KanjiSize` and `(y / areaSize) * KanjiSize`.
   - **`onDrag`**: Adds lines to the path using `relativeLineTo` with similarly scaled `dx` and `dy` values.
   - **`onDragEnd`**: Completes the stroke and triggers the `onUserPathDrawn(path)` callback.
3. **Rendering**: The internal `Canvas` continuously draws the currently active user path as long as it is visible.
4. **Gesture Resolution**: Includes an `ExcludeNavigationGesturesModifier` to prevent the user's drawing actions from triggering system-level gestures (like back navigation on Android).

## `AnimatedStroke` Composable

Animates the transition of a stroke from one path configuration to another. This is heavily utilized in `SingleStrokeInputContent` to show the user how their drawn stroke corrects itself to perfectly match the target stroke.

```kotlin
@Composable
fun AnimatedStroke(
    fromPath: Path,
    toPath: Path,
    progress: Float,
    modifier: Modifier = Modifier,
    strokeColor: Color,
    strokeWidth: Float,
    brushSettings: BrushSettings
)
```

It leverages `Path.lerpTo(toPath, progress)` (or equivalent path interpolation) to smoothly morph the stroke.

## `AnimatedKanji` Composable

A composable that draws the entire kanji character by sequentially animating each stroke from start to finish. It typically includes a "play" button overlay to trigger or restart the animation. Each individual stroke's animation is driven by a `drawProgress` value, usually animated with a `tween(600)` duration.

## `BrushSettings`

A data class encapsulating the visual styling for strokes.

```kotlin
data class BrushSettings(
    val strokeWidth: StrokeWidthOption,
    val strokeCap: StrokeCapOption,
    val strokeJoin: StrokeJoinOption,
    val alpha: Float
)
```

The settings are resolved into actual Compose rendering values via extension functions:
- `resolveStrokeWidth()`
- `resolveStrokeCap()`
- `resolveStrokeJoin()`
- `resolveAlpha()`

## `CharacterWriterDecorations`

A composable that renders the background guide lines within the drawing canvas. By convention, this is a 2×2 grid constructed with dashed lines, providing a spatial reference for the user while practicing.
