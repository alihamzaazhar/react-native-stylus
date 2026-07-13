import React from 'react';
import {StatusBar} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import type {RootStackParamList} from './src/navigation';
import {HomeScreen} from './src/screens/HomeScreen';
import {CapabilitiesScreen} from './src/screens/CapabilitiesScreen';
import {MotionDataScreen} from './src/screens/MotionDataScreen';
import {DrawingScreen} from './src/screens/DrawingScreen';
import {HandwritingScreen} from './src/screens/HandwritingScreen';
import {HoverScreen} from './src/screens/HoverScreen';
import {PalmScreen} from './src/screens/PalmScreen';
import {InteractionScreen} from './src/screens/InteractionScreen';
import {ImmersiveScreen} from './src/screens/ImmersiveScreen';
import {DocumentEditingScreen} from './src/screens/DocumentEditingScreen';
import {AnnotationScreen} from './src/screens/AnnotationScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return <SafeAreaProvider><StatusBar barStyle="dark-content" backgroundColor="#f4f0e7" />
    <NavigationContainer><Stack.Navigator screenOptions={{headerStyle: {backgroundColor: '#f4f0e7'}, headerTintColor: '#17211b', contentStyle: {backgroundColor: '#f4f0e7'}}}>
      <Stack.Screen name="Home" component={HomeScreen} options={{title: 'Stylus Lab'}} />
      <Stack.Screen name="Capabilities" component={CapabilitiesScreen} />
      <Stack.Screen name="MotionData" component={MotionDataScreen} options={{title: 'MotionEvent data'}} />
      <Stack.Screen name="Drawing" component={DrawingScreen} options={{title: 'Ink and brushes'}} />
      <Stack.Screen name="DocumentEditing" component={DocumentEditingScreen} options={{title: 'Document editing'}} />
      <Stack.Screen name="Annotation" component={AnnotationScreen} options={{title: 'Image and PDF annotation'}} />
      <Stack.Screen name="Handwriting" component={HandwritingScreen} />
      <Stack.Screen name="Hover" component={HoverScreen} options={{title: 'Hover and cursors'}} />
      <Stack.Screen name="Palm" component={PalmScreen} options={{title: 'Palm rejection'}} />
      <Stack.Screen name="Interaction" component={InteractionScreen} options={{title: 'Input and navigation'}} />
      <Stack.Screen name="Immersive" component={ImmersiveScreen} options={{title: 'Immersive and large screens'}} />
    </Stack.Navigator></NavigationContainer>
  </SafeAreaProvider>;
}
