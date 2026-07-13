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
}

export interface StylusCapabilities {
  androidApiLevel: number;
  stylusSupported: boolean;
  motionPredictionSupported: boolean;
  canceledFlagSupported: boolean;
  lowLatencyFrontBufferSupported: boolean;
  devices: StylusDevice[];
}
