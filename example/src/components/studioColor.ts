export interface RGBColor {r: number; g: number; b: number}
export interface HSVColor {h: number; s: number; v: number}

const clampByte = (value: number) => Math.max(0, Math.min(255, Math.round(value)));

export function normalizeHex(value: string): string | null {
  const raw = value.trim().replace(/^#/, '');
  if (!/^[0-9a-fA-F]{6}$/.test(raw)) return null;
  return `#${raw.toUpperCase()}`;
}

export function rgbToHex({r, g, b}: RGBColor): string {
  return `#${[r, g, b].map(value => clampByte(value).toString(16).padStart(2, '0')).join('').toUpperCase()}`;
}

export function hexToRgb(value: string): RGBColor {
  const hex = normalizeHex(value) ?? '#000000';
  return {r: parseInt(hex.slice(1, 3), 16), g: parseInt(hex.slice(3, 5), 16), b: parseInt(hex.slice(5, 7), 16)};
}

export function rgbToHsv({r, g, b}: RGBColor): HSVColor {
  const red = r / 255, green = g / 255, blue = b / 255;
  const maximum = Math.max(red, green, blue), minimum = Math.min(red, green, blue), delta = maximum - minimum;
  let hue = 0;
  if (delta !== 0) {
    if (maximum === red) hue = 60 * (((green - blue) / delta) % 6);
    else if (maximum === green) hue = 60 * ((blue - red) / delta + 2);
    else hue = 60 * ((red - green) / delta + 4);
  }
  return {h: hue < 0 ? hue + 360 : hue, s: maximum === 0 ? 0 : delta / maximum, v: maximum};
}

export function hsvToRgb({h, s, v}: HSVColor): RGBColor {
  const chroma = v * s;
  const section = ((h % 360) + 360) % 360 / 60;
  const x = chroma * (1 - Math.abs(section % 2 - 1));
  let red = 0, green = 0, blue = 0;
  if (section < 1) {red = chroma; green = x;}
  else if (section < 2) {red = x; green = chroma;}
  else if (section < 3) {green = chroma; blue = x;}
  else if (section < 4) {green = x; blue = chroma;}
  else if (section < 5) {red = x; blue = chroma;}
  else {red = chroma; blue = x;}
  const match = v - chroma;
  return {r: clampByte((red + match) * 255), g: clampByte((green + match) * 255), b: clampByte((blue + match) * 255)};
}
