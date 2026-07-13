import type {CodegenTypes, HostComponent, ViewProps} from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface NativeProps extends ViewProps {
  color?: string;
  strokeWidth?: CodegenTypes.Float;
  opacity?: CodegenTypes.Float;
  tool?: string;
  brush?: string;
  tiltEnabled?: boolean;
  directionEnabled?: boolean;
  brushPreviewEnabled?: boolean;
  pointerIcon?: string;
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
}

export default codegenNativeComponent<NativeProps>('StylusCanvas') as HostComponent<NativeProps>;
