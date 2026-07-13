import React from 'react';
import type {ViewProps} from 'react-native';
import NativeStylusCanvas from './specs/StylusCanvasNativeComponent';
import type {StylusInputEvent, StylusStroke} from './types';

export interface StylusCanvasProps extends ViewProps {
  color?: string;
  strokeWidth?: number;
  opacity?: number;
  tool?: 'pen' | 'highlighter' | 'eraser';
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

export function StylusCanvas({strokes, onStylusEvent, onStrokesChange, ...props}: StylusCanvasProps) {
  return <NativeStylusCanvas {...props} strokesJson={strokes ? JSON.stringify(strokes) : undefined}
    onStylusEvent={onStylusEvent ? (event) => onStylusEvent(JSON.parse(event.nativeEvent.payload) as StylusInputEvent) : undefined}
    onStrokesChanged={onStrokesChange ? (event) => onStrokesChange(JSON.parse(event.nativeEvent.strokesJson) as StylusStroke[], event.nativeEvent) : undefined} />;
}
