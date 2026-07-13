# react-native-stylus

Device-neutral Android stylus APIs for React Native 0.86 New Architecture applications. The package uses a TurboModule for platform services and Codegen Fabric components for drawing and handwriting input.

Samsung Air Actions and S Pen insertion state are intentionally outside this package.

## Capabilities

| Area | Implementation |
| --- | --- |
| Pressure, tilt, orientation, position, distance, size, buttons | Native `MotionEvent` samples, history, and typed JS events |
| Drawing, note-taking, sketching, annotation | Fabric `StylusCanvas` with persistent stroke data, undo, redo, clear, import, and export |
| Low-latency ink | AndroidX Ink 1.0 `InProgressStrokesView`; uses its API 29+ front-buffer renderer and compatible fallbacks |
| Motion prediction | AndroidX `MotionEventPredictor`; predicted samples are emitted but never persisted |
| Brushes | AndroidX Ink pressure pen, marker, and highlighter plus calligraphy/custom committed-stroke rendering |
| Hover | Hover events, brush preview, focus behavior, and pointer icon selection |
| Palm rejection | Honors `ACTION_CANCEL` and Android 13+ `FLAG_CANCELED`; canceled strokes are discarded |
| Handwriting | Native `EditText` Fabric component with Android 14+ automatic handwriting, bounds offsets, and delegation |
| Text selection and navigation | Native text editor behavior supplied by Android and the selected IME |
| Immersive drawing | TurboModule API for transient system bars and fullscreen drawing |
| Device discovery | Connected stylus enumeration and hot-plug notifications |
| Phones, tablets, foldables, ChromeOS | Runtime feature reporting; layouts remain controlled by the host app |

Handwriting recognition is performed by the user's compatible Android IME, not by this npm package. Drag/drop policy, document semantics, WebView content, custom `InputConnection` editors, and large-screen layout are application concerns; this library exposes stylus data and native primitives without pretending to own those workflows.

## Requirements

- React Native `0.86.x`
- New Architecture enabled
- Android API 24 minimum
- Android 14/API 34 or newer for platform handwriting APIs
- A handwriting-capable IME for handwriting recognition

## Install

```sh
npm install react-native-stylus
```

Rebuild the Android application after installation. Autolinking registers the TurboModule and both Fabric components.

## Low-Latency Canvas

```tsx
import {useState} from 'react';
import {StylusCanvas, type StylusStroke} from 'react-native-stylus';

export function Drawing() {
  const [strokes, setStrokes] = useState<StylusStroke[]>([]);
  const [undoToken, setUndoToken] = useState(0);

  return (
    <StylusCanvas
      style={{height: 500, backgroundColor: '#fffdf5'}}
      color="#16332c"
      strokeWidth={8}
      tool="pen"
      brush="pressurePen"
      brushDynamics={{
        minimumWidth: 1,
        maximumWidth: 24,
        pressureGamma: 0.8,
        velocitySensitivity: 0.25,
        smoothing: 0.35,
      }}
      eraserMode="partial"
      pressureEnabled
      tiltEnabled
      directionEnabled
      predictionEnabled
      brushPreviewEnabled
      pointerIcon="crosshair"
      undoToken={undoToken}
      onStylusEvent={event => {
        console.log(event.point.pressure, event.point.tilt, event.predicted);
      }}
      onStrokesChange={setStrokes}
    />
  );
}
```

Increment `clearToken`, `undoToken`, or `redoToken` to execute that native operation. Pass exported `strokes` back to restore a drawing. Set `fingerDrawingEnabled` when touch drawing is desired; it defaults to stylus-only input.

Every `onStylusEvent` payload includes `diagnostics` with event age, historical sample count, prediction availability, and estimated sample rate. `resolveStylusButtonActions()` maps Android button-state masks to application actions such as erase, select, pan, context menu, undo, or custom commands.

## Handwriting Input

```tsx
import {StylusHandwritingInput} from 'react-native-stylus';

<StylusHandwritingInput
  style={{minHeight: 140}}
  hint="Write here with a stylus"
  multiline
  autoHandwritingEnabled
  handwritingBoundsOffsetLeft={24}
  handwritingBoundsOffsetTop={24}
  handwritingBoundsOffsetRight={24}
  handwritingBoundsOffsetBottom={24}
  hoverFocusEnabled
  onTextChange={setText}
  onFocusChange={setFocused}
/>
```

On Android 14+, compatible IMEs can write directly into the native editor. Use `Stylus.showInputMethodPicker()` to let the tester select a handwriting-capable keyboard.

### Handwriting Delegation

Link a placeholder and editor using the same `delegationId`. The placeholder focuses the editor natively during the active stylus sequence, as required by Android's handwriting delegation contract.

```tsx
<StylusHandwritingDelegator
  delegationId="search-editor"
  label="Write to search"
  onActivate={console.log}
/>
<StylusHandwritingInput
  delegationId="search-editor"
  handwritingDelegate
  value={query}
  onChangeText={setQuery}
/>
```

Android 14+ native WebView text widgets already enable compatible IME handwriting by default. Consumers using `react-native-webview` should keep its Android WebView current; this package does not ship or take ownership of a browser engine. Fully custom non-EditText editors must still implement their text layout, `InputConnection`, cursor data, and handwriting gesture semantics natively.

### Drag, Drop, And Shortcuts

```tsx
const shortcut = createStylusShortcutHandler(
  {stylusPrimary: 'erase', stylusSecondary: 'undo'},
  {erase: enableEraser, undo},
);

<StylusDropZone draggable payload={JSON.stringify(selection)} onStylusDrag={console.log}>
  <SelectionPreview />
</StylusDropZone>
<StylusDropZone dropEnabled onStylusDrag={handleDrop}>
  <CanvasTarget />
</StylusDropZone>
```

`StylusCanvas` also accepts `pointerIconResource` and hotspot coordinates for bitmap cursor resources supplied by the Android host application.

## Editable Documents

The document API keeps native stroke capture separate from application editing state. It supports versioned JSON, layers, metadata, shapes, lasso selection, duplication, affine transforms, whole-stroke erasing, bounds, and SVG export.

```ts
import {
  createStylusDocument,
  createStylusLayer,
  exportStylusDocumentToSvg,
  selectStrokesByLasso,
  serializeStylusDocument,
  transformStrokes,
} from 'react-native-stylus';

const document = createStylusDocument({metadata: {title: 'Meeting notes'}});
const secondLayer = createStylusLayer('Annotations');
document.layers.push(secondLayer);

const selectedIds = selectStrokesByLasso(document.strokes, lassoPoints);
const moved = transformStrokes(
  document.strokes.filter(stroke => selectedIds.includes(stroke.id)),
  {translateX: 24, translateY: 12, rotation: Math.PI / 16},
);

const json = serializeStylusDocument(document);
const svg = exportStylusDocumentToSvg(document);
```

## Platform API

```ts
import {Stylus} from 'react-native-stylus';

const supported = await Stylus.isSupported();
const devices = await Stylus.getDevices();
const capabilities = await Stylus.getCapabilities();
const platform = await Stylus.getPlatformFeatures();

const remove = Stylus.addDeviceChangeListener(nextDevices => {
  console.log(nextDevices);
});

await Stylus.setImmersiveMode(true);
Stylus.showInputMethodPicker();
```

## Image And PDF Annotation

Annotation documents preserve the source URI/page and editable ink in source-document coordinates. Render the source with your preferred image or PDF component, place `StylusCanvas` above it, and use the viewport mapping helpers when the source is letterboxed.

```ts
const annotation = createAnnotationDocument({
  type: 'pdf',
  uri: selectedPdfUri,
  page: 3,
  width: 1440,
  height: 1920,
});

const sourcePoint = viewportPointToAnnotation(
  annotation,
  eventX,
  eventY,
  viewportWidth,
  viewportHeight,
);

const autosave = new StylusAutosaveController(storageAdapter, annotation.id);
autosave.schedule(annotation);
await autosave.flush(annotation);

Stylus.setClipboardText(serializeAnnotationDocument(annotation), 'Stylus annotation');
```

The storage interface is injectable, so applications can use AsyncStorage, SQLite, files, or cloud persistence without adding those dependencies to this package. PDF/image flattening remains the responsibility of the selected renderer; the library exports editable JSON and SVG overlays.

## Example

The standalone React Native 0.86 app in `example/` has separate screens for capabilities, raw motion data, AndroidX Ink drawing, handwriting, hover, palm cancellation, selection/navigation, and immersive mode.

```sh
cd example
npm install
npm start
```

In another terminal:

```sh
cd example
npm run android
```

Use physical stylus hardware for pressure, tilt, hover, palm cancellation, and latency testing. Emulator mouse input cannot validate those hardware signals.

## Android Dependencies

The package declares AndroidX Ink `1.0.0` modules and `androidx.input:input-motionprediction:1.0.0-beta01` transitively. Consumers do not copy JAR or AAR files.

## References

- [Android stylus input](https://developer.android.com/develop/ui/views/touch-and-input/stylus-input)
- [Advanced stylus features](https://developer.android.com/develop/ui/views/touch-and-input/stylus-input/advanced-stylus-features)
- [AndroidX Ink](https://developer.android.com/jetpack/androidx/releases/ink)
