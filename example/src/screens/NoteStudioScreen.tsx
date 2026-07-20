import React, { useState } from 'react';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import {
  Pressable,
  ScrollView,
  StatusBar,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import {
  StylusCanvas,
  type StylusCanvasProps,
  type StylusStroke,
} from 'react-native-stylus';
import type { RootStackParamList } from '../navigation';
import { StudioColorControls } from '../components/StudioColorControls';
import { StudioSlider } from '../components/StudioSlider';

type Props = NativeStackScreenProps<RootStackParamList, 'Studio'>;
type Brush = NonNullable<StylusCanvasProps['brush']>;
type Tool = NonNullable<StylusCanvasProps['tool']>;

const brushOptions: Array<{
  id: string;
  brush: Brush;
  label: string;
  tool: Tool;
  width: number;
  dynamics: NonNullable<StylusCanvasProps['brushDynamics']>;
  sampleStyle: object;
}> = [
  {
    id: 'ballpoint',
    brush: 'pressurePen',
    label: 'Ballpoint',
    tool: 'pen',
    width: 5,
    dynamics: {minimumWidth: 4, maximumWidth: 6, pressureGamma: 1, smoothing: 0.5},
    sampleStyle: { width: 16, height: 52 },
  },
  {
    id: 'fountain',
    brush: 'calligraphy',
    label: 'Fountain',
    tool: 'pen',
    width: 10,
    dynamics: {minimumWidth: 3, maximumWidth: 18, pressureGamma: 0.75, tiltSensitivity: 1.3, directionSensitivity: 1.4, smoothing: 0.35},
    sampleStyle: { width: 24, height: 46 },
  },
  {
    id: 'pencil',
    brush: 'pressurePen',
    label: 'Pencil',
    tool: 'pen',
    width: 7,
    dynamics: {minimumWidth: 1, maximumWidth: 12, pressureGamma: 1.2, velocitySensitivity: 0.2, tiltSensitivity: 1.6, smoothing: 0.25},
    sampleStyle: { width: 18, height: 48 },
  },
  {
    id: 'marker',
    brush: 'marker',
    label: 'Marker',
    tool: 'pen',
    width: 18,
    dynamics: {minimumWidth: 14, maximumWidth: 24, pressureGamma: 1, smoothing: 0.6},
    sampleStyle: { width: 32, height: 42 },
  },
  {
    id: 'brush',
    brush: 'calligraphy',
    label: 'Brush',
    tool: 'pen',
    width: 14,
    dynamics: {minimumWidth: 2, maximumWidth: 28, pressureGamma: 0.65, tiltSensitivity: 1.8, directionSensitivity: 1.6, smoothing: 0.2},
    sampleStyle: { width: 30, height: 48 },
  },
  {
    id: 'highlighter',
    brush: 'highlighter',
    label: 'Highlighter',
    tool: 'highlighter',
    width: 24,
    dynamics: {minimumWidth: 24, maximumWidth: 24, smoothing: 0.7},
    sampleStyle: { width: 38, height: 36 },
  },
];

function IconButton({
  label,
  onPress,
  selected,
  disabled,
}: {
  label: string;
  onPress: () => void;
  selected?: boolean;
  disabled?: boolean;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={label}
      disabled={disabled}
      onPress={onPress}
      style={[
        styles.iconButton,
        selected && styles.iconButtonSelected,
        disabled && styles.disabled,
      ]}
    >
      <Text style={[styles.iconLabel, selected && styles.iconLabelSelected]}>
        {label}
      </Text>
    </Pressable>
  );
}

export function NoteStudioScreen({ navigation }: Props) {
  const [title, setTitle] = useState('Untitled note');
  const [tool, setTool] = useState<Tool>('pen');
  const [profileId, setProfileId] = useState('fountain');
  const [brush, setBrush] = useState<Brush>('pressurePen');
  const [brushDynamics, setBrushDynamics] = useState<NonNullable<StylusCanvasProps['brushDynamics']>>(brushOptions[1].dynamics);
  const [color, setColor] = useState('#f2f2f4');
  const [strokeWidth, setStrokeWidth] = useState(12);
  const [opacity, setOpacity] = useState(1);
  const [pressureWidth, setPressureWidth] = useState(true);
  const [tiltResponse, setTiltResponse] = useState(true);
  const [recentPenColors, setRecentPenColors] = useState(['#F2F2F4', '#3C9DF5', '#EF3B2D']);
  const [recentHighlighterColors, setRecentHighlighterColors] = useState(['#F4EA36', '#38D9C5', '#FFC83D']);
  const [favoriteColors, setFavoriteColors] = useState<string[]>([]);
  const [interactionMode, setInteractionMode] = useState<NonNullable<StylusCanvasProps['selectionMode']>>('none');
  const [panelOpen, setPanelOpen] = useState(true);
  const [clearToken, setClearToken] = useState(0);
  const [undoToken, setUndoToken] = useState(0);
  const [redoToken, setRedoToken] = useState(0);
  const [strokeCount, setStrokeCount] = useState(0);
  const [history, setHistory] = useState({ canUndo: false, canRedo: false });

  const selectBrush = (profile: typeof brushOptions[number]) => {
    setProfileId(profile.id);
    setBrush(profile.brush);
    setTool(profile.tool);
    setStrokeWidth(profile.width);
    setBrushDynamics(profile.dynamics);
    if (profile.tool === 'highlighter') setColor(recentHighlighterColors[0]);
  };

  const selectColor = (nextColor: string) => {
    const normalized = nextColor.toUpperCase();
    setColor(normalized);
    const update = (values: string[]) => [normalized, ...values.filter(value => value !== normalized)].slice(0, 8);
    if (tool === 'highlighter') setRecentHighlighterColors(update); else setRecentPenColors(update);
  };

  const toggleFavorite = (value: string) => {
    setFavoriteColors(current => current.includes(value) ? current.filter(item => item !== value) : [value, ...current].slice(0, 8));
  };

  const handleStrokesChange = (
    strokes: StylusStroke[],
    state: { canUndo: boolean; canRedo: boolean },
  ) => {
    setStrokeCount(strokes.length);
    setHistory(state);
  };

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor="#080808" />
      <View style={styles.topBar}>
        <IconButton label="Back" onPress={navigation.goBack} />
        <TextInput
          accessibilityLabel="Note title"
          value={title}
          onChangeText={setTitle}
          placeholder="Title"
          placeholderTextColor="#5f6064"
          style={styles.titleInput}
        />
        <Text style={styles.pageBadge}>1 / 1</Text>
        <IconButton
          label="Clear"
          onPress={() => setClearToken(value => value + 1)}
          disabled={strokeCount === 0}
        />
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
          tiltEnabled={tiltResponse}
          directionEnabled
          predictionEnabled
          hoverEnabled
          brushPreviewEnabled
          fingerDrawingEnabled
          eraserMode="partial"
          clearToken={clearToken}
          undoToken={undoToken}
          redoToken={redoToken}
          selectionMode={interactionMode}
          brushDynamics={{
            ...brushDynamics,
            minimumWidth: pressureWidth ? Math.max(1, strokeWidth * 0.35) : strokeWidth,
            maximumWidth: pressureWidth ? strokeWidth * 1.25 : strokeWidth,
          }}
          onStrokesChange={handleStrokesChange}
          onColorPick={picked => {selectColor(picked); setInteractionMode('none'); setPanelOpen(true);}}
        />

        <View style={styles.canvasMeta} pointerEvents="none">
          <Text style={styles.canvasMetaText}>{strokeCount} strokes</Text>
        </View>

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Toggle brush settings"
          onPress={() => setPanelOpen(value => !value)}
          style={styles.floatingPen}
        >
          <Text style={styles.floatingPenText}>PEN</Text>
        </Pressable>

        {panelOpen && (
          <View style={styles.brushPanel}>
            <ScrollView contentContainerStyle={styles.panelContent} showsVerticalScrollIndicator={false}>
            <View style={styles.panelHeader}>
              <View>
                <Text style={styles.panelEyebrow}>BRUSH LAB</Text>
                <Text style={styles.panelTitle}>Ink settings</Text>
              </View>
              <IconButton label="Close" onPress={() => setPanelOpen(false)} />
            </View>

            <View style={styles.brushRow}>
              {brushOptions.map(option => (
                <Pressable
                  key={option.id}
                  accessibilityRole="button"
                  accessibilityLabel={`${option.label} brush`}
                  onPress={() => selectBrush(option)}
                  style={[
                    styles.brushOption,
                    profileId === option.id && styles.brushOptionSelected,
                  ]}
                >
                  <View style={[styles.brushSample, option.sampleStyle]} />
                  <Text style={styles.brushLabel}>{option.label}</Text>
                </Pressable>
              ))}
            </View>

            <StudioSlider label="Thickness" value={strokeWidth} minimumValue={1} maximumValue={64} step={1} valueLabel={`${strokeWidth}px`} onValueChange={setStrokeWidth} />
            <StudioSlider label="Opacity" value={Math.round(opacity * 100)} minimumValue={5} maximumValue={100} step={1} valueLabel={`${Math.round(opacity * 100)}%`} onValueChange={value => setOpacity(value / 100)} />

            <View style={styles.toggleRow}>
              <View>
                <Text style={styles.controlLabel}>Pressure width</Text>
                <Text style={styles.helperText}>
                  Vary the stroke with S Pen pressure
                </Text>
              </View>
              <Switch
                value={pressureWidth}
                onValueChange={setPressureWidth}
                trackColor={{ false: '#55565c', true: '#5a55d6' }}
                thumbColor="#f4f4f5"
              />
            </View>

            <View style={styles.toggleRow}>
              <View><Text style={styles.controlLabel}>Tilt response</Text><Text style={styles.helperText}>Use angle for expressive brushes</Text></View>
              <Switch value={tiltResponse} onValueChange={setTiltResponse} trackColor={{ false: '#55565c', true: '#5a55d6' }} thumbColor="#f4f4f5" />
            </View>
            <StudioColorControls color={color} recentColors={tool === 'highlighter' ? recentHighlighterColors : recentPenColors} favoriteColors={favoriteColors} onColorChange={selectColor} onToggleFavorite={toggleFavorite} onEyedropper={() => {setInteractionMode('eyedropper'); setPanelOpen(false);}} />
            </ScrollView>
          </View>
        )}
      </View>

      <View style={styles.toolbar}>
        <IconButton
          label="Pen"
          selected={tool === 'pen'}
          onPress={() => {
            setTool('pen');
            setPanelOpen(true);
          }}
        />
        <IconButton
          label="Mark"
          selected={tool === 'highlighter'}
          onPress={() => {
            setTool('highlighter');
            setBrush('highlighter');
            setPanelOpen(true);
          }}
        />
        <IconButton
          label="Erase"
          selected={tool === 'eraser'}
          onPress={() => setTool('eraser')}
        />
        <View style={styles.toolbarDivider} />
        <IconButton
          label="Undo"
          disabled={!history.canUndo}
          onPress={() => setUndoToken(value => value + 1)}
        />
        <IconButton
          label="Redo"
          disabled={!history.canRedo}
          onPress={() => setRedoToken(value => value + 1)}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#080808' },
  topBar: {
    height: 64,
    paddingHorizontal: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#2c2d31',
  },
  titleInput: {
    flex: 1,
    color: '#f3f3f5',
    fontSize: 22,
    fontWeight: '700',
    paddingVertical: 8,
  },
  pageBadge: { color: '#8d8e93', fontSize: 12, fontVariant: ['tabular-nums'] },
  workspace: { flex: 1, backgroundColor: '#020202', overflow: 'hidden' },
  canvas: { flex: 1, backgroundColor: '#020202' },
  canvasMeta: {
    position: 'absolute',
    right: 14,
    top: 14,
    backgroundColor: '#202125cc',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  canvasMetaText: { color: '#a9aab0', fontSize: 11, fontWeight: '700' },
  floatingPen: {
    position: 'absolute',
    right: 16,
    bottom: 20,
    width: 58,
    height: 58,
    borderRadius: 29,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#37309a',
    borderWidth: 1,
    borderColor: '#5d55d6',
  },
  floatingPenText: {
    color: '#d8d5ff',
    fontSize: 11,
    fontWeight: '900',
    letterSpacing: 1,
  },
  brushPanel: {
    position: 'absolute',
    left: 18,
    right: 92,
    bottom: 18,
    maxHeight: '88%',
    borderRadius: 28,
    padding: 6,
    backgroundColor: '#2a2b2f',
    borderWidth: 1,
    borderColor: '#3b3c42',
    shadowColor: '#000',
    shadowOpacity: 0.45,
    shadowRadius: 20,
    shadowOffset: { width: 0, height: 12 },
    elevation: 18,
  },
  panelContent: {padding: 12, gap: 14},
  panelHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  panelEyebrow: {
    color: '#8d88ff',
    fontSize: 10,
    fontWeight: '900',
    letterSpacing: 1.8,
  },
  panelTitle: {
    color: '#f5f5f6',
    fontSize: 20,
    fontWeight: '800',
    marginTop: 2,
  },
  brushRow: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  brushOption: {
    flexBasis: '30%',
    flexGrow: 1,
    minHeight: 86,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: 8,
    backgroundColor: '#222327',
    borderWidth: 1,
    borderColor: '#393a40',
  },
  brushOptionSelected: { backgroundColor: '#3a3a40', borderColor: '#8e89ff' },
  brushSample: {
    borderRadius: 8,
    backgroundColor: '#d8d8dc',
    marginBottom: 7,
    transform: [{ rotate: '-8deg' }],
  },
  brushLabel: { color: '#bfc0c5', fontSize: 10, fontWeight: '700' },
  controlBlock: { gap: 8 },
  controlHeading: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  controlLabel: { color: '#f0f0f2', fontSize: 13, fontWeight: '700' },
  controlValue: {
    color: '#aaaab0',
    fontSize: 12,
    fontVariant: ['tabular-nums'],
  },
  optionTrack: {
    height: 44,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    padding: 4,
    borderRadius: 14,
    backgroundColor: '#202125',
  },
  widthOption: {
    flex: 1,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
  },
  widthSample: { width: 38, borderRadius: 13, backgroundColor: '#e7e7e9' },
  opacityOption: {
    flex: 1,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
  },
  opacitySample: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#f4f4f5',
  },
  optionSelected: { backgroundColor: '#494a51' },
  toggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  helperText: { color: '#85868c', fontSize: 10, marginTop: 2 },
  colorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  colorSwatch: {
    width: 30,
    height: 30,
    borderRadius: 15,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  colorSelected: { borderColor: '#fff', transform: [{ scale: 1.16 }] },
  toolbar: {
    height: 70,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    backgroundColor: '#202124',
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: '#3b3c40',
  },
  toolbarDivider: { width: 1, height: 32, backgroundColor: '#404146' },
  iconButton: {
    minWidth: 46,
    height: 46,
    borderRadius: 23,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 8,
  },
  iconButtonSelected: { backgroundColor: '#4a4b51' },
  iconLabel: { color: '#d5d5d8', fontSize: 11, fontWeight: '800' },
  iconLabelSelected: { color: '#fff' },
  disabled: { opacity: 0.3 },
});
