import {codegenNativeComponent} from 'react-native';
import type {CodegenTypes, ViewProps} from 'react-native';

export interface NativeProps extends ViewProps {
  payload?: string;
  draggable?: boolean;
  dropEnabled?: boolean;
  onStylusDrag?: CodegenTypes.DirectEventHandler<Readonly<{action: string; x: CodegenTypes.Float; y: CodegenTypes.Float; payload: string; mimeType: string}>>;
}
export default codegenNativeComponent<NativeProps>('StylusDropZone');
