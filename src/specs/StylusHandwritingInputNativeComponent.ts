import type {CodegenTypes, HostComponent, ViewProps} from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface NativeProps extends ViewProps {
  text?: string;
  hint?: string;
  textColor?: string;
  hintColor?: string;
  textSize?: CodegenTypes.Float;
  multiline?: boolean;
  autoHandwritingEnabled?: boolean;
  handwritingDelegate?: boolean;
  handwritingBoundsLeft?: CodegenTypes.Float;
  handwritingBoundsTop?: CodegenTypes.Float;
  handwritingBoundsRight?: CodegenTypes.Float;
  handwritingBoundsBottom?: CodegenTypes.Float;
  selectAllOnFocus?: boolean;
  hoverFocusEnabled?: boolean;
  onTextChanged?: CodegenTypes.DirectEventHandler<Readonly<{text: string}>>;
  onHandwritingStatus?: CodegenTypes.DirectEventHandler<Readonly<{supported: boolean; focused: boolean}>>;
}

export default codegenNativeComponent<NativeProps>('StylusHandwritingInput') as HostComponent<NativeProps>;
