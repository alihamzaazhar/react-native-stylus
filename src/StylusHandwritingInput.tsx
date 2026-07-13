import React from 'react';
import type {ViewProps} from 'react-native';
import NativeInput from './specs/StylusHandwritingInputNativeComponent';

export interface StylusHandwritingInputProps extends ViewProps {
  value?: string;
  placeholder?: string;
  textColor?: string;
  placeholderTextColor?: string;
  textSize?: number;
  multiline?: boolean;
  autoHandwritingEnabled?: boolean;
  handwritingDelegate?: boolean;
  handwritingBounds?: {left: number; top: number; right: number; bottom: number};
  selectAllOnFocus?: boolean;
  hoverFocusEnabled?: boolean;
  onChangeText?: (text: string) => void;
  onHandwritingStatus?: (status: {supported: boolean; focused: boolean}) => void;
}

export function StylusHandwritingInput({value, placeholder, placeholderTextColor, handwritingBounds, onChangeText, onHandwritingStatus, ...props}: StylusHandwritingInputProps) {
  return <NativeInput {...props} text={value} hint={placeholder} hintColor={placeholderTextColor}
    handwritingBoundsLeft={handwritingBounds?.left} handwritingBoundsTop={handwritingBounds?.top}
    handwritingBoundsRight={handwritingBounds?.right} handwritingBoundsBottom={handwritingBounds?.bottom}
    onTextChanged={onChangeText ? event => onChangeText(event.nativeEvent.text) : undefined}
    onHandwritingStatus={onHandwritingStatus ? event => onHandwritingStatus(event.nativeEvent) : undefined} />;
}
