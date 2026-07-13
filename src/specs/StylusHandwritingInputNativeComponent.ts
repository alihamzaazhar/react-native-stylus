import {codegenNativeComponent} from 'react-native';
import type {CodegenTypes, ViewProps} from 'react-native';

export interface NativeProps extends ViewProps {
  text?: string;
  hint?: string;
  textColor?: string;
  hintColor?: string;
  textSize?: CodegenTypes.Float;
  multiline?: boolean;
  autoHandwritingEnabled?: boolean;
  handwritingDelegate?: boolean;
  delegationId?: string;
  allowedDelegatorPackage?: string;
  handwritingDelegateFlags?: CodegenTypes.Int32;
  handwritingBoundsLeft?: CodegenTypes.Float;
  handwritingBoundsTop?: CodegenTypes.Float;
  handwritingBoundsRight?: CodegenTypes.Float;
  handwritingBoundsBottom?: CodegenTypes.Float;
  selectAllOnFocus?: boolean;
  hoverFocusEnabled?: boolean;
  onTextChanged?: CodegenTypes.DirectEventHandler<Readonly<{text: string}>>;
  onHandwritingStatus?: CodegenTypes.DirectEventHandler<Readonly<{supported: boolean; focused: boolean}>>;
  onHandwritingGesture?: CodegenTypes.DirectEventHandler<Readonly<{gestureType: string; phase: string; result: CodegenTypes.Int32}>>;
}

export default codegenNativeComponent<NativeProps>('StylusHandwritingInput');
