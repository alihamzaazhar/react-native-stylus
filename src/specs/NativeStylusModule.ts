import type {CodegenTypes, TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  getCapabilities(): Promise<string>;
  getStylusDevices(): Promise<string>;
  isStylusSupported(): Promise<boolean>;
  getPlatformFeatures(): Promise<string>;
  setImmersiveMode(enabled: boolean): Promise<boolean>;
  showInputMethodPicker(): void;
  setClipboardText(label: string, value: string): void;
  getClipboardText(): Promise<string>;
  readonly onDevicesChanged: CodegenTypes.EventEmitter<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('StylusModule');
