import React, {useRef, useState} from 'react';
import {PanResponder, Pressable, StyleSheet, Text, View} from 'react-native';

interface StudioSliderProps {
  label: string;
  value: number;
  minimumValue: number;
  maximumValue: number;
  step?: number;
  valueLabel?: string;
  onValueChange: (value: number) => void;
}

export function StudioSlider({label, value, minimumValue, maximumValue, step = 1, valueLabel, onValueChange}: StudioSliderProps) {
  const [width, setWidth] = useState(1);
  const update = (x: number) => {
    const ratio = Math.max(0, Math.min(1, x / width));
    const raw = minimumValue + ratio * (maximumValue - minimumValue);
    onValueChange(Math.round(raw / step) * step);
  };
  const panResponder = useRef(PanResponder.create({
    onStartShouldSetPanResponder: () => true,
    onMoveShouldSetPanResponder: () => true,
    onPanResponderGrant: event => update(event.nativeEvent.locationX),
    onPanResponderMove: event => update(event.nativeEvent.locationX),
  })).current;
  const ratio = (value - minimumValue) / (maximumValue - minimumValue);

  return <View style={styles.block}>
    <View style={styles.heading}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{valueLabel ?? value}</Text></View>
    <Pressable accessibilityRole="adjustable" accessibilityLabel={label} accessibilityValue={{min: minimumValue, max: maximumValue, now: value}} onLayout={event => setWidth(event.nativeEvent.layout.width)} style={styles.track} {...panResponder.panHandlers}>
      <View style={[styles.fill, {width: `${ratio * 100}%`}]} />
      <View style={[styles.thumb, {left: `${ratio * 100}%`}]} />
    </Pressable>
  </View>;
}

const styles = StyleSheet.create({
  block: {gap: 8},
  heading: {flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'},
  label: {color: '#f0f0f2', fontSize: 13, fontWeight: '700'},
  value: {color: '#aaaab0', fontSize: 12, fontVariant: ['tabular-nums']},
  track: {height: 34, borderRadius: 17, justifyContent: 'center', backgroundColor: '#1d1e22', overflow: 'visible'},
  fill: {position: 'absolute', left: 0, height: 5, borderRadius: 3, backgroundColor: '#817bff'},
  thumb: {position: 'absolute', width: 22, height: 22, marginLeft: -11, borderRadius: 11, backgroundColor: '#f4f4f5', borderWidth: 3, borderColor: '#817bff'},
});
