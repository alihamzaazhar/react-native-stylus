/**
 * @format
 */

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';

jest.mock('react-native-stylus', () => ({
  Stylus: {
    addDeviceChangeListener: jest.fn(() => jest.fn()),
    getCapabilities: jest.fn(async () => ({})),
    getDevices: jest.fn(async () => []),
    getPlatformFeatures: jest.fn(async () => ({})),
    isSupported: jest.fn(async () => false),
    setImmersiveMode: jest.fn(async () => true),
    showInputMethodPicker: jest.fn(),
  },
  StylusCanvas: 'StylusCanvas',
  StylusHandwritingInput: 'StylusHandwritingInput',
}));

import App from '../App';

test('renders correctly', async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});
