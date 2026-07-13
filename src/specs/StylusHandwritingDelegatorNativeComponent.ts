import {codegenNativeComponent} from 'react-native';
import type {CodegenTypes, ViewProps} from 'react-native';

export interface NativeProps extends ViewProps {
  label?: string;
  textColor?: string;
  textSize?: CodegenTypes.Float;
  delegationId: string;
  allowedDelegatePackage?: string;
  onDelegationActivated?: CodegenTypes.DirectEventHandler<Readonly<{supported: boolean; delegateFound: boolean; handwriting: boolean}>>;
}

export default codegenNativeComponent<NativeProps>('StylusHandwritingDelegator');
