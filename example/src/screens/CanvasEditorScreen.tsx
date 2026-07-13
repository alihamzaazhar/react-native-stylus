import React, {useState} from 'react';
import {Text, View} from 'react-native';
import {createStylusDocument, Stylus, StylusCanvas, type StylusSelectionState, type StylusStroke, type StylusViewport} from 'react-native-stylus';
import {Action, Card, LabScreen} from '../components/Lab';
import {styles} from '../styles';

export function CanvasEditorScreen() {
  const [strokes, setStrokes] = useState<StylusStroke[]>([]);
  const [viewport, setViewport] = useState<StylusViewport>({scale: 1, offsetX: 0, offsetY: 0, rotation: 0});
  const [selection, setSelection] = useState<StylusSelectionState>({strokeIds: [], bounds: null});
  const [lasso, setLasso] = useState(false);
  const [transformToken, setTransformToken] = useState(0);
  const [transform, setTransform] = useState({token: 0});
  const [deleteToken, setDeleteToken] = useState(0), [duplicateToken, setDuplicateToken] = useState(0), [resetToken, setResetToken] = useState(0);
  const [message, setMessage] = useState('Stylus draws. One finger pans and two fingers zoom.');
  const applyTransform = (next: {translateX?: number; translateY?: number; scaleX?: number; scaleY?: number; rotation?: number}) => {
    const token = transformToken + 1; setTransformToken(token); setTransform({token, ...next});
  };
  const exportPng = async () => {
    const document = {...createStylusDocument({width: 1080, height: 1440}), strokes};
    setMessage(await Stylus.exportDocumentPng(document, 'canvas-editor.png'));
  };
  return <LabScreen eyebrow="NATIVE EDITOR" title="Viewport and lasso selection">
    <Text style={styles.body}>Switch to lasso, draw around strokes with the stylus, then transform the native selection.</Text>
    <View style={styles.row}><Action label={lasso ? 'Use pen' : 'Use lasso'} onPress={()=>setLasso(value=>!value)}/><Action label="Reset view" onPress={()=>setResetToken(value=>value+1)} alt/></View>
    <StylusCanvas style={styles.canvas} strokes={strokes} onStrokesChange={setStrokes} viewport={viewport} onViewportChange={setViewport} viewportGesturesEnabled resetViewportToken={resetToken} selectionMode={lasso?'lasso':'none'} selectedStrokeIds={selection.strokeIds} onSelectionChange={setSelection} selectionTransform={transform} deleteSelectionToken={deleteToken} duplicateSelectionToken={duplicateToken} color="#173b32" strokeWidth={8}/>
    <View style={styles.row}><Action label="Move" onPress={()=>applyTransform({translateX:24,translateY:16})}/><Action label="Scale" onPress={()=>applyTransform({scaleX:1.15,scaleY:1.15})}/><Action label="Rotate" onPress={()=>applyTransform({rotation:Math.PI/16})}/></View>
    <View style={styles.row}><Action label="Duplicate" onPress={()=>setDuplicateToken(value=>value+1)} alt/><Action label="Delete" onPress={()=>setDeleteToken(value=>value+1)} alt/><Action label="Export PNG" onPress={exportPng} alt/></View>
    <Card><Text style={styles.metric}>zoom={viewport.scale.toFixed(2)} selected={selection.strokeIds.length}{`\n`}{message}</Text></Card>
  </LabScreen>;
}
