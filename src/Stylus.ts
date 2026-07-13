import NativeStylusModule from './native';
import type {StylusCapabilities, StylusDevice, StylusPlatformFeatures} from './types';

export const Stylus = {
  isSupported: (): Promise<boolean> => NativeStylusModule.isStylusSupported(),
  async getDevices(): Promise<StylusDevice[]> {
    return JSON.parse(await NativeStylusModule.getStylusDevices()) as StylusDevice[];
  },
  async getCapabilities(): Promise<StylusCapabilities> {
    return JSON.parse(await NativeStylusModule.getCapabilities()) as StylusCapabilities;
  },
  async getPlatformFeatures(): Promise<StylusPlatformFeatures> {
    return JSON.parse(await NativeStylusModule.getPlatformFeatures()) as StylusPlatformFeatures;
  },
  setImmersiveMode: (enabled: boolean): Promise<boolean> => NativeStylusModule.setImmersiveMode(enabled),
  showInputMethodPicker: (): void => NativeStylusModule.showInputMethodPicker(),
  setClipboardText: (value: string, label = 'Stylus content'): void => NativeStylusModule.setClipboardText(label, value),
  getClipboardText: (): Promise<string> => NativeStylusModule.getClipboardText(),
  exportDocumentPng: (document: import('./types').StylusDocument, outputName = 'stylus-document.png', maxDimension = 4096): Promise<string> => NativeStylusModule.exportDocumentPng(JSON.stringify(document), outputName, maxDimension),
  flattenAnnotationPng: (document: import('./types').StylusAnnotationDocument, outputName = 'stylus-annotation.png', maxDimension = 4096): Promise<string> => NativeStylusModule.flattenAnnotationPng(JSON.stringify(document), outputName, maxDimension),
  addDeviceChangeListener(listener: (devices: StylusDevice[]) => void): () => void {
    const subscription = NativeStylusModule.onDevicesChanged((json) => listener(JSON.parse(json) as StylusDevice[]));
    return () => subscription.remove();
  },
};
