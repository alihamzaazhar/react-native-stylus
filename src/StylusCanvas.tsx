import React from 'react';
import type {ViewProps} from 'react-native';
import NativeStylusCanvas from './specs/StylusCanvasNativeComponent';
import type {StylusBrushDynamics, StylusInputEvent, StylusStroke} from './types';

export interface StylusCanvasProps extends ViewProps {
  color?: string;
  strokeWidth?: number;
  opacity?: number;
  tool?: 'pen' | 'highlighter' | 'eraser';
  brush?: 'pressurePen' | 'marker' | 'highlighter' | 'calligraphy' | 'custom';
  brushDynamics?: StylusBrushDynamics;
  eraserMode?: 'wholeStroke' | 'partial';
  tiltEnabled?: boolean;
  directionEnabled?: boolean;
  brushPreviewEnabled?: boolean;
  pointerIcon?: 'default' | 'crosshair' | 'hand' | 'text' | 'none';
  pressureEnabled?: boolean;
  predictionEnabled?: boolean;
  fingerDrawingEnabled?: boolean;
  hoverEnabled?: boolean;
  clearToken?: number;
  undoToken?: number;
  redoToken?: number;
  strokes?: StylusStroke[];
  onStylusEvent?: (event: StylusInputEvent) => void;
  onStrokesChange?: (strokes: StylusStroke[], state: {canUndo: boolean; canRedo: boolean}) => void;
}

export function StylusCanvas({strokes, brushDynamics, onStylusEvent, onStrokesChange, ...props}: StylusCanvasProps) {
  return <NativeStylusCanvas {...props} brushDynamicsJson={brushDynamics ? JSON.stringify(brushDynamics) : undefined} strokesJson={strokes ? JSON.stringify(strokes) : undefined}
    onStylusEvent={onStylusEvent ? (event) => onStylusEvent(JSON.parse(event.nativeEvent.payload) as StylusInputEvent) : undefined}
    onStrokesChanged={onStrokesChange ? (event) => onStrokesChange(JSON.parse(event.nativeEvent.strokesJson) as StylusStroke[], event.nativeEvent) : undefined} />;
}
