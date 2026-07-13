import React, {type PropsWithChildren} from 'react';
import {Pressable, ScrollView, Text, View} from 'react-native';
import {styles} from '../styles';
export function LabScreen({title, eyebrow, children}: PropsWithChildren<{title: string; eyebrow: string}>) {
  return <ScrollView style={styles.screen} contentContainerStyle={styles.content}><Text style={styles.eyebrow}>{eyebrow}</Text><Text style={styles.title}>{title}</Text>{children}</ScrollView>;
}
export function Card({children}: PropsWithChildren) { return <View style={styles.card}>{children}</View>; }
export function Action({label, onPress, alt}: {label: string; onPress: () => void; alt?: boolean}) {
  return <Pressable onPress={onPress} style={[styles.button, alt && styles.buttonAlt]}><Text style={styles.buttonText}>{label}</Text></Pressable>;
}
export function Json({value}: {value: unknown}) { return <Text selectable style={styles.metric}>{JSON.stringify(value, null, 2)}</Text>; }
