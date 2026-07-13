import type {CodegenTypes, HostComponent, ViewProps} from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface NativeProps extends ViewProps {
  payload?: string;
  draggable?: boolean;
  dropEnabled?: boolean;
  onStylusDrag?: CodegenTypes.DirectEventHandler<Readonly<{action: string; x: CodegenTypes.Float; y: CodegenTypes.Float; payload: string; mimeType: string}>>;
}
export default codegenNativeComponent<NativeProps>('StylusDropZone') as HostComponent<NativeProps>;
