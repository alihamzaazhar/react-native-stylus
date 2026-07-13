import React, {type PropsWithChildren} from 'react';
import type {ViewProps} from 'react-native';
import NativeDropZone from './specs/StylusDropZoneNativeComponent';

export interface StylusDragEvent {action: 'start' | 'started' | 'entered' | 'location' | 'exited' | 'drop' | 'ended'; x: number; y: number; payload: string; mimeType: string}
export interface StylusDropZoneProps extends ViewProps {payload?: string; draggable?: boolean; dropEnabled?: boolean; onStylusDrag?: (event: StylusDragEvent) => void}
export function StylusDropZone({children, onStylusDrag, ...props}: PropsWithChildren<StylusDropZoneProps>) {
  return <NativeDropZone {...props} onStylusDrag={onStylusDrag ? event => onStylusDrag(event.nativeEvent as StylusDragEvent) : undefined}>{children}</NativeDropZone>;
}
