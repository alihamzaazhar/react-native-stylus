import React, {useState} from 'react';
import {Text} from 'react-native';
import {StylusCanvas, type StylusInputEvent} from 'react-native-stylus';
import {Card, Json, LabScreen} from '../components/Lab'; import {styles} from '../styles';
export function MotionDataScreen(){const [event,setEvent]=useState<StylusInputEvent>(); return <LabScreen eyebrow="RAW POINTER PIPELINE" title="Inspect every stylus axis"><Text style={styles.body}>Hover or draw below. Historical and predicted samples are delivered independently.</Text><StylusCanvas style={styles.canvas} color="#b54b2a" strokeWidth={8} onStylusEvent={setEvent}/><Card><Json value={event}/></Card></LabScreen>;}
