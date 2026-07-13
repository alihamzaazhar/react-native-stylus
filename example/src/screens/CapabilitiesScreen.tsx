import React, {useEffect, useState} from 'react';
import {Stylus, type StylusCapabilities, type StylusPlatformFeatures} from 'react-native-stylus';
import {Card, Json, LabScreen} from '../components/Lab';
export function CapabilitiesScreen() {
  const [capabilities,setCapabilities]=useState<StylusCapabilities>(); const [platform,setPlatform]=useState<StylusPlatformFeatures>();
  useEffect(()=>{void Stylus.getCapabilities().then(setCapabilities); void Stylus.getPlatformFeatures().then(setPlatform); return Stylus.addDeviceChangeListener(devices=>setCapabilities(old=>old?{...old,devices,stylusSupported:devices.length>0}:old));},[]);
  return <LabScreen eyebrow="HARDWARE + PLATFORM" title="What this device supports"><Card><Json value={platform}/></Card><Card><Json value={capabilities}/></Card></LabScreen>;
}
