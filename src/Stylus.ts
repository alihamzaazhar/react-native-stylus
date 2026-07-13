import NativeStylusModule from './native';
import type {StylusCapabilities, StylusDevice} from './types';

export const Stylus = {
  isSupported: (): Promise<boolean> => NativeStylusModule.isStylusSupported(),
  async getDevices(): Promise<StylusDevice[]> {
    return JSON.parse(await NativeStylusModule.getStylusDevices()) as StylusDevice[];
  },
  async getCapabilities(): Promise<StylusCapabilities> {
    return JSON.parse(await NativeStylusModule.getCapabilities()) as StylusCapabilities;
  },
  addDeviceChangeListener(listener: (devices: StylusDevice[]) => void): () => void {
    const subscription = NativeStylusModule.onDevicesChanged((json) => listener(JSON.parse(json) as StylusDevice[]));
    return () => subscription.remove();
  },
};
