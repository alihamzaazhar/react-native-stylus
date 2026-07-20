import React, {useEffect, useState} from 'react';
import {Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {StudioSlider} from './StudioSlider';
import {hexToRgb, hsvToRgb, normalizeHex, rgbToHex, rgbToHsv} from './studioColor';

interface Props {
  color: string;
  recentColors: string[];
  favoriteColors: string[];
  onColorChange: (color: string) => void;
  onToggleFavorite: (color: string) => void;
  onEyedropper: () => void;
}

export function StudioColorControls({color, recentColors, favoriteColors, onColorChange, onToggleFavorite, onEyedropper}: Props) {
  const [hexInput, setHexInput] = useState(color);
  const rgb = hexToRgb(color);
  const hsv = rgbToHsv(rgb);
  useEffect(() => setHexInput(color), [color]);

  const setHsv = (next: Partial<typeof hsv>) => onColorChange(rgbToHex(hsvToRgb({...hsv, ...next})));
  const setChannel = (channel: 'r' | 'g' | 'b', value: string) => {
    const parsed = Number(value);
    if (Number.isFinite(parsed)) onColorChange(rgbToHex({...rgb, [channel]: parsed}));
  };
  const commitHex = () => {
    const normalized = normalizeHex(hexInput);
    if (normalized) onColorChange(normalized); else setHexInput(color);
  };
  const previewStyle = {backgroundColor: color};

  return <View style={styles.container}>
    <View style={styles.heading}>
      <View><Text style={styles.title}>Color spectrum</Text><Text style={styles.helper}>HSV, RGB and HEX</Text></View>
      <View style={[styles.preview, previewStyle]} />
    </View>
    <StudioSlider label="Hue" value={Math.round(hsv.h)} minimumValue={0} maximumValue={359} valueLabel={`${Math.round(hsv.h)} deg`} onValueChange={h => setHsv({h})} />
    <StudioSlider label="Saturation" value={Math.round(hsv.s * 100)} minimumValue={0} maximumValue={100} valueLabel={`${Math.round(hsv.s * 100)}%`} onValueChange={s => setHsv({s: s / 100})} />
    <StudioSlider label="Brightness" value={Math.round(hsv.v * 100)} minimumValue={0} maximumValue={100} valueLabel={`${Math.round(hsv.v * 100)}%`} onValueChange={v => setHsv({v: v / 100})} />
    <View style={styles.inputRow}>
      <TextInput accessibilityLabel="HEX color" value={hexInput} onChangeText={setHexInput} onEndEditing={commitHex} autoCapitalize="characters" maxLength={7} style={[styles.input, styles.hexInput]} />
      {(['r', 'g', 'b'] as const).map(channel => <View key={channel} style={styles.channel}><Text style={styles.channelLabel}>{channel.toUpperCase()}</Text><TextInput accessibilityLabel={`${channel.toUpperCase()} color channel`} value={String(rgb[channel])} onChangeText={value => setChannel(channel, value)} keyboardType="number-pad" maxLength={3} style={styles.input} /></View>)}
    </View>
    <View style={styles.actionRow}>
      <Pressable onPress={onEyedropper} style={styles.action}><Text style={styles.actionText}>Eyedropper</Text></Pressable>
      <Pressable onPress={() => onToggleFavorite(color)} style={styles.action}><Text style={styles.actionText}>{favoriteColors.includes(color) ? 'Unfavorite' : 'Favorite'}</Text></Pressable>
    </View>
    <ColorList label="Recent" colors={recentColors} selected={color} onSelect={onColorChange} />
    <ColorList label="Favorites" colors={favoriteColors} selected={color} onSelect={onColorChange} empty="No favorites yet" />
  </View>;
}

function ColorList({label, colors, selected, onSelect, empty}: {label: string; colors: string[]; selected: string; onSelect: (color: string) => void; empty?: string}) {
  return <View style={styles.listRow}><Text style={styles.listLabel}>{label}</Text><View style={styles.swatches}>{colors.length === 0 && <Text style={styles.empty}>{empty}</Text>}{colors.map(value => {
    const swatchStyle = {backgroundColor: value};
    return <Pressable key={value} accessibilityLabel={`${label} color ${value}`} onPress={() => onSelect(value)} style={[styles.swatch, swatchStyle, selected === value && styles.selected]} />;
  })}</View></View>;
}

const styles = StyleSheet.create({
  container: {gap: 12}, heading: {flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'},
  title: {color: '#f0f0f2', fontSize: 14, fontWeight: '800'}, helper: {color: '#85868c', fontSize: 10, marginTop: 2},
  preview: {width: 34, height: 34, borderRadius: 17, borderWidth: 2, borderColor: '#fff'},
  inputRow: {flexDirection: 'row', gap: 6, alignItems: 'flex-end'}, input: {minWidth: 46, height: 38, borderRadius: 9, paddingHorizontal: 8, color: '#fff', backgroundColor: '#1d1e22', borderWidth: 1, borderColor: '#44454b', textAlign: 'center'},
  hexInput: {flex: 1, textAlign: 'left'}, channel: {gap: 3}, channelLabel: {color: '#85868c', fontSize: 9, textAlign: 'center'},
  actionRow: {flexDirection: 'row', gap: 8}, action: {flex: 1, borderRadius: 10, padding: 10, alignItems: 'center', backgroundColor: '#414249'}, actionText: {color: '#fff', fontSize: 11, fontWeight: '800'},
  listRow: {gap: 6}, listLabel: {color: '#aaaab0', fontSize: 10, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 1}, swatches: {minHeight: 30, flexDirection: 'row', gap: 8, flexWrap: 'wrap', alignItems: 'center'},
  swatch: {width: 28, height: 28, borderRadius: 14, borderWidth: 2, borderColor: 'transparent'}, selected: {borderColor: '#fff'}, empty: {color: '#66676d', fontSize: 10},
});
