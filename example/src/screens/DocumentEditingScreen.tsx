import React, {useState} from 'react';
import {Text, View} from 'react-native';
import {
  createStylusDocument,
  createStylusLayer,
  duplicateStrokes,
  exportStylusDocumentToSvg,
  serializeStylusDocument,
  StylusCanvas,
  transformStrokes,
  type StylusDocument,
  type StylusStroke,
} from 'react-native-stylus';
import {Action, Card, LabScreen} from '../components/Lab';
import {styles} from '../styles';

export function DocumentEditingScreen() {
  const [document, setDocument] = useState<StylusDocument>(() => createStylusDocument({metadata: {title: 'Field notes'}}));
  const [message, setMessage] = useState('Draw a stroke, then apply document operations.');

  const updateStrokes = (strokes: StylusStroke[]) => setDocument(current => ({...current, strokes: strokes.map(stroke => ({...stroke, layerId: current.activeLayerId}))}));
  const duplicate = () => setDocument(current => ({...current, strokes: [...current.strokes, ...duplicateStrokes(current.strokes)]}));
  const move = () => setDocument(current => ({...current, strokes: transformStrokes(current.strokes, {translateX: 24, translateY: 24, rotation: 0.05})}));
  const addLayer = () => setDocument(current => {
    const layer = createStylusLayer(`Layer ${current.layers.length + 1}`);
    return {...current, layers: [...current.layers, layer], activeLayerId: layer.id};
  });

  return <LabScreen eyebrow="DOCUMENT MODEL" title="Edit reusable ink documents">
    <Text style={styles.body}>This screen uses the versioned document API. Drawings can be assigned to layers, transformed, duplicated, serialized, and exported as SVG.</Text>
    <Card>
      <View style={{height: 320, backgroundColor: '#fffdf5', borderRadius: 16, overflow: 'hidden'}}>
        <StylusCanvas style={{flex: 1}} strokes={document.strokes} fingerDrawingEnabled color="#173b32" strokeWidth={7} onStrokesChange={updateStrokes} />
      </View>
    </Card>
    <Card>
      <Text style={styles.body}>Active layer: {document.layers.find(layer => layer.id === document.activeLayerId)?.name}</Text>
      <Text style={styles.body}>Strokes: {document.strokes.length} · Layers: {document.layers.length}</Text>
      <Action label="Add layer" onPress={addLayer} />
      <Action label="Duplicate strokes" onPress={duplicate} alt />
      <Action label="Move and rotate" onPress={move} alt />
      <Action label="Measure JSON export" onPress={() => setMessage(`${serializeStylusDocument(document).length} JSON characters`)} alt />
      <Action label="Measure SVG export" onPress={() => setMessage(`${exportStylusDocumentToSvg(document).length} SVG characters`)} alt />
      <Text style={styles.metric}>{message}</Text>
    </Card>
  </LabScreen>;
}
