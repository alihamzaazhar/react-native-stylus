import React from 'react';
import type {ViewProps} from 'react-native';
import NativeDelegator from './specs/StylusHandwritingDelegatorNativeComponent';

export interface StylusHandwritingDelegatorStatus {supported: boolean; delegateFound: boolean; handwriting: boolean}
export interface StylusHandwritingDelegatorProps extends ViewProps {
  delegationId: string;
  label?: string;
  textColor?: string;
  textSize?: number;
  allowedDelegatePackage?: string;
  onActivate?: (status: StylusHandwritingDelegatorStatus) => void;
}

export function StylusHandwritingDelegator({onActivate, ...props}: StylusHandwritingDelegatorProps) {
  return <NativeDelegator {...props} onDelegationActivated={onActivate ? event => onActivate(event.nativeEvent) : undefined} />;
}
