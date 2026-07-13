export type StylusToolType = 'stylus' | 'eraser' | 'finger' | 'mouse' | 'unknown';
export type StylusAction = 'down' | 'move' | 'up' | 'cancel' | 'hoverEnter' | 'hoverMove' | 'hoverExit' | 'buttonPress' | 'buttonRelease';

export interface StylusPoint {
  x: number;
  y: number;
  pressure: number;
  tilt: number;
  orientation: number;
  distance: number;
  size: number;
  touchMajor: number;
  touchMinor: number;
  timestamp: number;
  pointerId: number;
  toolType: StylusToolType;
  buttons: number;
  predicted: boolean;
  historical: boolean;
}

export interface StylusInputEvent {
  action: StylusAction;
  point: StylusPoint;
  history: StylusPoint[];
  predicted: StylusPoint | null;
  canceled: boolean;
  palmRejected: boolean;
  diagnostics: StylusEventDiagnostics;
}

export interface StylusEventDiagnostics {
  eventAgeMs: number;
  historicalSampleCount: number;
  predictedSampleAvailable: boolean;
  estimatedSampleRateHz: number;
}

export interface StylusBrushDynamics {
  minimumWidth?: number;
  maximumWidth?: number;
  pressureGamma?: number;
  velocitySensitivity?: number;
  tiltSensitivity?: number;
  directionSensitivity?: number;
  smoothing?: number;
}

export type StylusButtonAction = 'none' | 'erase' | 'select' | 'pan' | 'contextMenu' | 'undo' | 'redo' | string;

export interface StylusButtonMapping {
  primary?: StylusButtonAction;
  secondary?: StylusButtonAction;
  tertiary?: StylusButtonAction;
  stylusPrimary?: StylusButtonAction;
  stylusSecondary?: StylusButtonAction;
}

export interface StylusDevice {
  id: number;
  name: string;
  descriptor: string;
  vendorId: number;
  productId: number;
  external: boolean;
  sources: number;
  hasPressure: boolean;
  hasTilt: boolean;
  hasOrientation: boolean;
  hasDistance: boolean;
  hasHover: boolean;
  hasEraser: boolean;
}

export interface StylusStroke {
  id: string;
  color: string;
  width: number;
  opacity: number;
  tool: 'pen' | 'highlighter' | 'eraser';
  points: StylusPoint[];
  layerId?: string;
  metadata?: Record<string, string | number | boolean>;
}

export interface StylusLayer {
  id: string;
  name: string;
  visible: boolean;
  locked: boolean;
  opacity: number;
}

export type StylusShapeType = 'line' | 'arrow' | 'rectangle' | 'ellipse';

export interface StylusShape {
  id: string;
  type: StylusShapeType;
  layerId: string;
  color: string;
  width: number;
  opacity: number;
  x: number;
  y: number;
  endX: number;
  endY: number;
  rotation?: number;
  fill?: string;
  metadata?: Record<string, string | number | boolean>;
}

export interface StylusDocumentMetadata {
  title?: string;
  author?: string;
  createdAt: number;
  updatedAt: number;
  tags?: string[];
  [key: string]: string | number | boolean | string[] | undefined;
}

export interface StylusDocument {
  version: 1;
  id: string;
  width: number;
  height: number;
  backgroundColor: string;
  activeLayerId: string;
  layers: StylusLayer[];
  strokes: StylusStroke[];
  shapes: StylusShape[];
  metadata: StylusDocumentMetadata;
}

export interface StylusBounds { x: number; y: number; width: number; height: number; }

export interface StylusTransform {
  translateX?: number;
  translateY?: number;
  scaleX?: number;
  scaleY?: number;
  rotation?: number;
  originX?: number;
  originY?: number;
}

export interface StylusCapabilities {
  androidApiLevel: number;
  stylusSupported: boolean;
  motionPredictionSupported: boolean;
  canceledFlagSupported: boolean;
  lowLatencyFrontBufferSupported: boolean;
  devices: StylusDevice[];
}

export interface StylusPlatformFeatures {
  androidApiLevel: number;
  handwritingTextFields: boolean;
  handwritingDelegation: boolean;
  handwritingBounds: boolean;
  canceledPalmFlag: boolean;
  motionPrediction: boolean;
  inkApi: boolean;
  frontBuffer: boolean;
  dragAndDrop: boolean;
  customPointerIcons: boolean;
  chromeOs: boolean;
  largeScreen: boolean;
}
