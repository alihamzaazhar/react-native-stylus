# react-native-stylus

Generic Android stylus input and pressure-sensitive drawing for React Native 0.86 New Architecture applications.

## Features

- Fabric `StylusCanvas` rendered natively without routing drawing through the JS thread
- TurboModule device discovery and hot-plug events
- Pressure, tilt, orientation, hover distance, contact size, pointer IDs, and buttons
- Historical/coalesced samples and Jetpack motion prediction
- Stylus, hardware eraser, finger, and mouse tool classification
- Android palm-rejection cancellation through `ACTION_CANCEL` and `FLAG_CANCELED`
- Pen, translucent highlighter, and stroke eraser tools
- Undo, redo, clear, controlled stroke import, and JSON stroke export
- React Native 0.86 Codegen, TurboModules, Fabric, and autolinking

This package is device-neutral. Samsung Air Actions, S Pen insertion state, and the proprietary Samsung Remote SDK belong in `react-native-s-pen`.

## Install

```sh
npm install react-native-stylus
```

Android is currently supported. React Native New Architecture must be enabled.

## Canvas

```tsx
import {useState} from 'react';
import {StylusCanvas, type StylusStroke} from 'react-native-stylus';

export function Drawing() {
  const [clearToken, setClearToken] = useState(0);
  const [undoToken, setUndoToken] = useState(0);
  const [strokes, setStrokes] = useState<StylusStroke[]>([]);

  return (
    <StylusCanvas
      style={{height: 500}}
      color="#152238"
      strokeWidth={10}
      tool="pen"
      pressureEnabled
      predictionEnabled
      clearToken={clearToken}
      undoToken={undoToken}
      onStylusEvent={(event) => console.log(event.point.pressure, event.point.tilt)}
      onStrokesChange={(next) => setStrokes(next)}
    />
  );
}
```

Increment `clearToken`, `undoToken`, or `redoToken` to invoke the corresponding native operation. Pass previously exported `strokes` to restore a drawing.

## Device API

```ts
import {Stylus} from 'react-native-stylus';

const supported = await Stylus.isSupported();
const devices = await Stylus.getDevices();
const capabilities = await Stylus.getCapabilities();

const remove = Stylus.addDeviceChangeListener((nextDevices) => {
  console.log(nextDevices);
});
```

## Latency behavior

The canvas renders synchronously in a native Android `View`, consumes batched historical samples, and uses AndroidX `MotionEventPredictor` for temporary predicted points. Predicted points are emitted separately and are never committed to exported strokes. Android reports whether front-buffer rendering is supported through `lowLatencyFrontBufferSupported`; the current renderer does not claim to use OpenGL front-buffer rendering.

## Platform details

- Minimum Android API: 24
- `FLAG_CANCELED` palm metadata requires Android 13; `ACTION_CANCEL` works on older versions.
- Motion prediction uses `androidx.input:input-motionprediction:1.0.0-beta01`.
- Hover distance is hardware-specific and must not be interpreted as a physical unit.

See the [Android stylus guide](https://developer.android.com/develop/ui/views/touch-and-input/stylus-input) for platform semantics.
