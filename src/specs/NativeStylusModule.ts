import type {CodegenTypes, TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  getCapabilities(): Promise<string>;
  getStylusDevices(): Promise<string>;
  isStylusSupported(): Promise<boolean>;
  readonly onDevicesChanged: CodegenTypes.EventEmitter<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('StylusModule');
