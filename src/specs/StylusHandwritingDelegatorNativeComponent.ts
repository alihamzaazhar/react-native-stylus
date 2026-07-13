import type {CodegenTypes, HostComponent, ViewProps} from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface NativeProps extends ViewProps {
  label?: string;
  textColor?: string;
  textSize?: CodegenTypes.Float;
  delegationId: string;
  allowedDelegatePackage?: string;
  onDelegationActivated?: CodegenTypes.DirectEventHandler<Readonly<{supported: boolean; delegateFound: boolean; handwriting: boolean}>>;
}

export default codegenNativeComponent<NativeProps>('StylusHandwritingDelegator') as HostComponent<NativeProps>;
