import React from 'react';
import type {ViewProps} from 'react-native';
import NativeStylusCanvas from './specs/StylusCanvasNativeComponent';
import type {StylusBrushDynamics, StylusInputEvent, StylusSelectionState, StylusStroke, StylusTransform, StylusViewport} from './types';

export interface StylusCanvasProps extends ViewProps {
  color?: string;
  strokeWidth?: number;
  opacity?: number;
  tool?: 'pen' | 'highlighter' | 'eraser';
  brush?: 'pressurePen' | 'marker' | 'highlighter' | 'calligraphy' | 'custom';
  brushDynamics?: StylusBrushDynamics;
  eraserMode?: 'wholeStroke' | 'partial';
  viewport?: Partial<StylusViewport>;
  viewportGesturesEnabled?: boolean;
  resetViewportToken?: number;
  selectionMode?: 'none' | 'lasso';
  selectedStrokeIds?: string[];
  selectionTransform?: StylusTransform & {token: number};
  deleteSelectionToken?: number;
  duplicateSelectionToken?: number;
  tiltEnabled?: boolean;
  directionEnabled?: boolean;
  brushPreviewEnabled?: boolean;
  pointerIcon?: 'default' | 'crosshair' | 'hand' | 'text' | 'none';
  pointerIconResource?: string;
  pointerIconHotspotX?: number;
  pointerIconHotspotY?: number;
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
  onViewportChange?: (viewport: StylusViewport) => void;
  onSelectionChange?: (selection: StylusSelectionState) => void;
}

export function StylusCanvas({strokes, brushDynamics, viewport, selectedStrokeIds, selectionTransform, onStylusEvent, onStrokesChange, onViewportChange, onSelectionChange, ...props}: StylusCanvasProps) {
  return <NativeStylusCanvas {...props} viewportScale={viewport?.scale} viewportOffsetX={viewport?.offsetX} viewportOffsetY={viewport?.offsetY} viewportRotation={viewport?.rotation}
    brushDynamicsJson={brushDynamics ? JSON.stringify(brushDynamics) : undefined} selectedStrokeIdsJson={selectedStrokeIds ? JSON.stringify(selectedStrokeIds) : undefined}
    selectionTransformJson={selectionTransform ? JSON.stringify(selectionTransform) : undefined} strokesJson={strokes ? JSON.stringify(strokes) : undefined}
    onStylusEvent={onStylusEvent ? (event) => onStylusEvent(JSON.parse(event.nativeEvent.payload) as StylusInputEvent) : undefined}
    onStrokesChanged={onStrokesChange ? (event) => onStrokesChange(JSON.parse(event.nativeEvent.strokesJson) as StylusStroke[], event.nativeEvent) : undefined}
    onViewportChanged={onViewportChange ? event => onViewportChange(event.nativeEvent) : undefined}
    onSelectionChanged={onSelectionChange ? event => onSelectionChange({strokeIds: JSON.parse(event.nativeEvent.strokeIdsJson) as string[], bounds: event.nativeEvent.boundsJson ? JSON.parse(event.nativeEvent.boundsJson) : null}) : undefined} />;
}
