import React, {useState} from 'react';
import {Text, View} from 'react-native';
import {
  createAnnotationDocument,
  serializeAnnotationDocument,
  Stylus,
  StylusAutosaveController,
  StylusCanvas,
  type StylusAnnotationDocument,
  type StylusDocumentStorage,
  type StylusStroke,
} from 'react-native-stylus';
import {Action, Card, LabScreen} from '../components/Lab';
import {styles} from '../styles';

let memoryValue: string | null = null;
const memoryStorage: StylusDocumentStorage = {
  load: async () => memoryValue,
  save: async (_key, value) => { memoryValue = value; },
};

export function AnnotationScreen() {
  const [document, setDocument] = useState<StylusAnnotationDocument>(() => createAnnotationDocument({type: 'pdf', uri: 'content://example/document.pdf', page: 1, width: 1080, height: 1440}));
  const [autosave] = useState(() => new StylusAutosaveController(memoryStorage, 'annotation-demo', 300));
  const [message, setMessage] = useState('Draw over the page placeholder.');
  const update = (strokes: StylusStroke[]) => {
    const next = {...document, strokes};
    setDocument(next); autosave.schedule(next);
  };
  const copy = () => {
    const value = serializeAnnotationDocument(document);
    Stylus.setClipboardText(value, 'Stylus annotation');
    setMessage(`${value.length} characters copied`);
  };
  return <LabScreen eyebrow="EDITABLE OVERLAY" title="Annotate images and PDF pages">
    <Text style={styles.body}>The source renderer belongs behind this transparent canvas. Ink remains editable and uses source-document coordinates.</Text>
    <Card><View style={{height: 380, backgroundColor: '#fffdf5', borderWidth: 1, borderColor: '#c8bfae'}}><Text style={styles.eyebrow}>PDF PAGE {document.target.page}</Text><StylusCanvas style={{flex: 1}} strokes={document.strokes} fingerDrawingEnabled color="#b54b2a" strokeWidth={8} onStrokesChange={update}/></View></Card>
    <Action label="Copy editable annotation" onPress={copy}/>
    <Action label="Flush autosave" onPress={()=>autosave.flush(document).then(()=>setMessage('Autosave flushed'))} alt/>
    <Text style={styles.metric}>{message}</Text>
  </LabScreen>;
}
