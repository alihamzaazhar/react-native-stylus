import React from 'react';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {Text} from 'react-native';
import type {RootStackParamList} from '../navigation';
import {Action, Card, LabScreen} from '../components/Lab';
import {styles} from '../styles';
type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;
const tests: Array<[keyof RootStackParamList, string, string]> = [
  ['Capabilities','Platform capabilities','Devices, Android versions, Ink and handwriting support'],
  ['MotionData','MotionEvent data','Pressure, tilt, orientation, distance and prediction'],
  ['Drawing','Ink and brushes','Drawing, sketching, annotation, tools and persistence'],
  ['DocumentEditing','Document editing','Layers, transforms, shapes, selection and export'],
  ['Handwriting','Handwriting','Android 14 IME handwriting and bounds'],
  ['Hover','Hover and cursors','Preview, focus and cursor behavior'],
  ['Palm','Palm rejection','Canceled touches and stray-mark removal'],
  ['Interaction','Input and navigation','Selection, navigation and drag/drop guidance'],
  ['Immersive','Immersive layouts','Fullscreen and large-screen behavior'],
];
export function HomeScreen({navigation}: Props) { return <LabScreen eyebrow="RN 0.86 · FABRIC · TURBOMODULE" title="Android stylus capability lab">
  <Text style={styles.body}>Every screen isolates one documented Android stylus behavior. Results come from this app and device, not Samsung-specific APIs.</Text>
  {tests.map(([route,title,description]) => <Card key={route}><Text style={{fontSize:18,fontWeight:'800'}}>{title}</Text><Text style={styles.body}>{description}</Text><Action label="Open test" onPress={() => navigation.navigate(route)} /></Card>)}
  </LabScreen>; }
