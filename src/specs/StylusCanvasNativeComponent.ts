import {codegenNativeComponent} from 'react-native';
import type {CodegenTypes, ViewProps} from 'react-native';

export interface NativeProps extends ViewProps {
  color?: string;
  strokeWidth?: CodegenTypes.Float;
  opacity?: CodegenTypes.Float;
  tool?: string;
  brush?: string;
  brushDynamicsJson?: string;
  eraserMode?: string;
  viewportScale?: CodegenTypes.Float;
  viewportOffsetX?: CodegenTypes.Float;
  viewportOffsetY?: CodegenTypes.Float;
  viewportRotation?: CodegenTypes.Float;
  viewportGesturesEnabled?: boolean;
  resetViewportToken?: CodegenTypes.Int32;
  selectionMode?: string;
  selectedStrokeIdsJson?: string;
  selectionTransformJson?: string;
  deleteSelectionToken?: CodegenTypes.Int32;
  duplicateSelectionToken?: CodegenTypes.Int32;
  tiltEnabled?: boolean;
  directionEnabled?: boolean;
  brushPreviewEnabled?: boolean;
  pointerIcon?: string;
  pointerIconResource?: string;
  pointerIconHotspotX?: CodegenTypes.Float;
  pointerIconHotspotY?: CodegenTypes.Float;
  pressureEnabled?: boolean;
  predictionEnabled?: boolean;
  fingerDrawingEnabled?: boolean;
  hoverEnabled?: boolean;
  clearToken?: CodegenTypes.Int32;
  undoToken?: CodegenTypes.Int32;
  redoToken?: CodegenTypes.Int32;
  strokesJson?: string;
  onStylusEvent?: CodegenTypes.DirectEventHandler<Readonly<{payload: string}>>;
  onStrokesChanged?: CodegenTypes.DirectEventHandler<Readonly<{
    strokesJson: string;
    strokeCount: CodegenTypes.Int32;
    canUndo: boolean;
    canRedo: boolean;
  }>>;
  onViewportChanged?: CodegenTypes.DirectEventHandler<Readonly<{scale: CodegenTypes.Float; offsetX: CodegenTypes.Float; offsetY: CodegenTypes.Float; rotation: CodegenTypes.Float}>>;
  onSelectionChanged?: CodegenTypes.DirectEventHandler<Readonly<{strokeIdsJson: string; boundsJson: string}>>;
}

export default codegenNativeComponent<NativeProps>('StylusCanvas');
