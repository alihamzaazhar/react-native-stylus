import React, {useState} from 'react';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {
  Pressable,
  StatusBar,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';
import {
  StylusCanvas,
  type StylusCanvasProps,
  type StylusStroke,
} from 'react-native-stylus';
import type {RootStackParamList} from '../navigation';

type Props = NativeStackScreenProps<RootStackParamList, 'Studio'>;
type Brush = NonNullable<StylusCanvasProps['brush']>;
type Tool = NonNullable<StylusCanvasProps['tool']>;

const brushOptions: Array<{brush: Brush; label: string; sampleStyle: object}> = [
  {brush: 'pressurePen', label: 'Pen', sampleStyle: {width: 16, height: 52}},
  {brush: 'calligraphy', label: 'Nib', sampleStyle: {width: 24, height: 46}},
  {brush: 'marker', label: 'Marker', sampleStyle: {width: 32, height: 42}},
  {brush: 'highlighter', label: 'Hi-lite', sampleStyle: {width: 38, height: 36}},
];

const widthOptions = [
  {value: 3, sampleStyle: {height: 3}},
  {value: 7, sampleStyle: {height: 7}},
  {value: 12, sampleStyle: {height: 12}},
  {value: 18, sampleStyle: {height: 18}},
  {value: 26, sampleStyle: {height: 26}},
];

const opacityOptions = [0.25, 0.5, 0.75, 1];
const colorOptions = [
  {value: '#ef3b2d', style: {backgroundColor: '#ef3b2d'}},
  {value: '#ffc83d', style: {backgroundColor: '#ffc83d'}},
  {value: '#f4ea36', style: {backgroundColor: '#f4ea36'}},
  {value: '#38d9c5', style: {backgroundColor: '#38d9c5'}},
  {value: '#3c9df5', style: {backgroundColor: '#3c9df5'}},
  {value: '#345bff', style: {backgroundColor: '#345bff'}},
  {value: '#a23df0', style: {backgroundColor: '#a23df0'}},
  {value: '#f2f2f4', style: {backgroundColor: '#f2f2f4'}},
];

function IconButton({label, onPress, selected, disabled}: {label: string; onPress: () => void; selected?: boolean; disabled?: boolean}) {
  return <Pressable accessibilityRole="button" accessibilityLabel={label} disabled={disabled} onPress={onPress} style={[styles.iconButton, selected && styles.iconButtonSelected, disabled && styles.disabled]}>
    <Text style={[styles.iconLabel, selected && styles.iconLabelSelected]}>{label}</Text>
  </Pressable>;
}

export function NoteStudioScreen({navigation}: Props) {
  const [title, setTitle] = useState('Untitled note');
  const [tool, setTool] = useState<Tool>('pen');
  const [brush, setBrush] = useState<Brush>('pressurePen');
  const [color, setColor] = useState('#f2f2f4');
  const [strokeWidth, setStrokeWidth] = useState(12);
  const [opacity, setOpacity] = useState(1);
  const [pressureWidth, setPressureWidth] = useState(true);
  const [panelOpen, setPanelOpen] = useState(true);
  const [clearToken, setClearToken] = useState(0);
  const [undoToken, setUndoToken] = useState(0);
  const [redoToken, setRedoToken] = useState(0);
  const [strokeCount, setStrokeCount] = useState(0);
  const [history, setHistory] = useState({canUndo: false, canRedo: false});

  const selectBrush = (nextBrush: Brush) => {
    setBrush(nextBrush);
    setTool(nextBrush === 'highlighter' ? 'highlighter' : 'pen');
  };

  const handleStrokesChange = (strokes: StylusStroke[], state: {canUndo: boolean; canRedo: boolean}) => {
    setStrokeCount(strokes.length);
    setHistory(state);
  };

  return <SafeAreaView style={styles.screen} edges={['top', 'bottom']}>
    <StatusBar barStyle="light-content" backgroundColor="#080808" />
    <View style={styles.topBar}>
      <IconButton label="Back" onPress={navigation.goBack} />
      <TextInput accessibilityLabel="Note title" value={title} onChangeText={setTitle} placeholder="Title" placeholderTextColor="#5f6064" style={styles.titleInput} />
      <Text style={styles.pageBadge}>1 / 1</Text>
      <IconButton label="Clear" onPress={() => setClearToken(value => value + 1)} disabled={strokeCount === 0} />
    </View>

    <View style={styles.workspace}>
      <StylusCanvas
        style={styles.canvas}
        tool={tool}
        brush={brush}
        color={color}
        strokeWidth={strokeWidth}
        opacity={opacity}
        pressureEnabled={pressureWidth}
        tiltEnabled
        directionEnabled
        predictionEnabled
        hoverEnabled
        brushPreviewEnabled
        fingerDrawingEnabled
        eraserMode="partial"
        clearToken={clearToken}
        undoToken={undoToken}
        redoToken={redoToken}
        brushDynamics={{
          minimumWidth: pressureWidth ? Math.max(1, strokeWidth * 0.15) : strokeWidth,
          maximumWidth: strokeWidth,
          pressureGamma: 0.8,
          velocitySensitivity: 0.2,
          tiltSensitivity: brush === 'calligraphy' ? 1.4 : 0.5,
          directionSensitivity: brush === 'calligraphy' ? 1.2 : 0.2,
          smoothing: 0.4,
        }}
        onStrokesChange={handleStrokesChange}
      />

      <View style={styles.canvasMeta} pointerEvents="none">
        <Text style={styles.canvasMetaText}>{strokeCount} strokes</Text>
      </View>

      <Pressable accessibilityRole="button" accessibilityLabel="Toggle brush settings" onPress={() => setPanelOpen(value => !value)} style={styles.floatingPen}>
        <Text style={styles.floatingPenText}>PEN</Text>
      </Pressable>

      {panelOpen && <View style={styles.brushPanel}>
        <View style={styles.panelHeader}>
          <View>
            <Text style={styles.panelEyebrow}>BRUSH LAB</Text>
            <Text style={styles.panelTitle}>Ink settings</Text>
          </View>
          <IconButton label="Close" onPress={() => setPanelOpen(false)} />
        </View>

        <View style={styles.brushRow}>
          {brushOptions.map(option => <Pressable key={option.brush} accessibilityRole="button" accessibilityLabel={`${option.label} brush`} onPress={() => selectBrush(option.brush)} style={[styles.brushOption, brush === option.brush && styles.brushOptionSelected]}>
            <View style={[styles.brushSample, option.sampleStyle]} />
            <Text style={styles.brushLabel}>{option.label}</Text>
          </Pressable>)}
        </View>

        <View style={styles.controlBlock}>
          <View style={styles.controlHeading}>
            <Text style={styles.controlLabel}>Thickness</Text>
            <Text style={styles.controlValue}>{strokeWidth}px</Text>
          </View>
          <View style={styles.optionTrack}>
            {widthOptions.map(option => <Pressable key={option.value} accessibilityRole="button" accessibilityLabel={`${option.value} pixel thickness`} onPress={() => setStrokeWidth(option.value)} style={[styles.widthOption, strokeWidth === option.value && styles.optionSelected]}>
              <View style={[styles.widthSample, option.sampleStyle]} />
            </Pressable>)}
          </View>
        </View>

        <View style={styles.controlBlock}>
          <View style={styles.controlHeading}>
            <Text style={styles.controlLabel}>Opacity</Text>
            <Text style={styles.controlValue}>{Math.round(opacity * 100)}%</Text>
          </View>
          <View style={styles.optionTrack}>
            {opacityOptions.map(value => <Pressable key={value} accessibilityRole="button" accessibilityLabel={`${Math.round(value * 100)} percent opacity`} onPress={() => setOpacity(value)} style={[styles.opacityOption, opacity === value && styles.optionSelected]}>
              <View style={[styles.opacitySample, {opacity: value}]} />
            </Pressable>)}
          </View>
        </View>

        <View style={styles.toggleRow}>
          <View>
            <Text style={styles.controlLabel}>Pressure width</Text>
            <Text style={styles.helperText}>Vary the stroke with S Pen pressure</Text>
          </View>
          <Switch value={pressureWidth} onValueChange={setPressureWidth} trackColor={{false: '#55565c', true: '#5a55d6'}} thumbColor="#f4f4f5" />
        </View>

        <View style={styles.colorRow}>
          {colorOptions.map(option => <Pressable key={option.value} accessibilityRole="button" accessibilityLabel={`Use ${option.value} ink`} onPress={() => setColor(option.value)} style={[styles.colorSwatch, option.style, color === option.value && styles.colorSelected]} />)}
        </View>
      </View>}
    </View>

    <View style={styles.toolbar}>
      <IconButton label="Pen" selected={tool === 'pen'} onPress={() => {setTool('pen'); setPanelOpen(true);}} />
      <IconButton label="Mark" selected={tool === 'highlighter'} onPress={() => {setTool('highlighter'); setBrush('highlighter'); setPanelOpen(true);}} />
      <IconButton label="Erase" selected={tool === 'eraser'} onPress={() => setTool('eraser')} />
      <View style={styles.toolbarDivider} />
      <IconButton label="Undo" disabled={!history.canUndo} onPress={() => setUndoToken(value => value + 1)} />
      <IconButton label="Redo" disabled={!history.canRedo} onPress={() => setRedoToken(value => value + 1)} />
    </View>
  </SafeAreaView>;
}

const styles = StyleSheet.create({
  screen: {flex: 1, backgroundColor: '#080808'},
  topBar: {height: 64, paddingHorizontal: 10, flexDirection: 'row', alignItems: 'center', gap: 8, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: '#2c2d31'},
  titleInput: {flex: 1, color: '#f3f3f5', fontSize: 22, fontWeight: '700', paddingVertical: 8},
  pageBadge: {color: '#8d8e93', fontSize: 12, fontVariant: ['tabular-nums']},
  workspace: {flex: 1, backgroundColor: '#020202', overflow: 'hidden'},
  canvas: {flex: 1, backgroundColor: '#020202'},
  canvasMeta: {position: 'absolute', right: 14, top: 14, backgroundColor: '#202125cc', borderRadius: 12, paddingHorizontal: 10, paddingVertical: 6},
  canvasMetaText: {color: '#a9aab0', fontSize: 11, fontWeight: '700'},
  floatingPen: {position: 'absolute', right: 16, bottom: 20, width: 58, height: 58, borderRadius: 29, alignItems: 'center', justifyContent: 'center', backgroundColor: '#37309a', borderWidth: 1, borderColor: '#5d55d6'},
  floatingPenText: {color: '#d8d5ff', fontSize: 11, fontWeight: '900', letterSpacing: 1},
  brushPanel: {position: 'absolute', left: 18, right: 92, bottom: 18, borderRadius: 28, padding: 18, gap: 14, backgroundColor: '#2a2b2f', borderWidth: 1, borderColor: '#3b3c42', shadowColor: '#000', shadowOpacity: 0.45, shadowRadius: 20, shadowOffset: {width: 0, height: 12}, elevation: 18},
  panelHeader: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'},
  panelEyebrow: {color: '#8d88ff', fontSize: 10, fontWeight: '900', letterSpacing: 1.8},
  panelTitle: {color: '#f5f5f6', fontSize: 20, fontWeight: '800', marginTop: 2},
  brushRow: {flexDirection: 'row', gap: 8},
  brushOption: {flex: 1, minHeight: 86, borderRadius: 16, alignItems: 'center', justifyContent: 'flex-end', padding: 8, backgroundColor: '#222327', borderWidth: 1, borderColor: '#393a40'},
  brushOptionSelected: {backgroundColor: '#3a3a40', borderColor: '#8e89ff'},
  brushSample: {borderRadius: 8, backgroundColor: '#d8d8dc', marginBottom: 7, transform: [{rotate: '-8deg'}]},
  brushLabel: {color: '#bfc0c5', fontSize: 10, fontWeight: '700'},
  controlBlock: {gap: 8},
  controlHeading: {flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'},
  controlLabel: {color: '#f0f0f2', fontSize: 13, fontWeight: '700'},
  controlValue: {color: '#aaaab0', fontSize: 12, fontVariant: ['tabular-nums']},
  optionTrack: {height: 44, flexDirection: 'row', alignItems: 'center', gap: 8, padding: 4, borderRadius: 14, backgroundColor: '#202125'},
  widthOption: {flex: 1, height: 36, alignItems: 'center', justifyContent: 'center', borderRadius: 10},
  widthSample: {width: 38, borderRadius: 13, backgroundColor: '#e7e7e9'},
  opacityOption: {flex: 1, height: 36, alignItems: 'center', justifyContent: 'center', borderRadius: 10},
  opacitySample: {width: 28, height: 28, borderRadius: 14, backgroundColor: '#f4f4f5'},
  optionSelected: {backgroundColor: '#494a51'},
  toggleRow: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'},
  helperText: {color: '#85868c', fontSize: 10, marginTop: 2},
  colorRow: {flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'},
  colorSwatch: {width: 30, height: 30, borderRadius: 15, borderWidth: 2, borderColor: 'transparent'},
  colorSelected: {borderColor: '#fff', transform: [{scale: 1.16}]},
  toolbar: {height: 70, paddingHorizontal: 14, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-around', backgroundColor: '#202124', borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: '#3b3c40'},
  toolbarDivider: {width: 1, height: 32, backgroundColor: '#404146'},
  iconButton: {minWidth: 46, height: 46, borderRadius: 23, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 8},
  iconButtonSelected: {backgroundColor: '#4a4b51'},
  iconLabel: {color: '#d5d5d8', fontSize: 11, fontWeight: '800'},
  iconLabelSelected: {color: '#fff'},
  disabled: {opacity: 0.3},
});
