import type {StylusButtonAction, StylusButtonMapping} from './types';

export const STYLUS_BUTTONS = {primary: 1, secondary: 2, tertiary: 4, stylusPrimary: 32, stylusSecondary: 64} as const;

export function resolveStylusButtonActions(buttonState: number, mapping: StylusButtonMapping): StylusButtonAction[] {
  const actions: StylusButtonAction[] = [];
  for (const [button, mask] of Object.entries(STYLUS_BUTTONS) as Array<[keyof typeof STYLUS_BUTTONS, number]>) {
    const action = mapping[button];
    if ((buttonState & mask) !== 0 && action && action !== 'none') actions.push(action);
  }
  return actions;
}
