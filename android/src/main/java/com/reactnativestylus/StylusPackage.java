package com.reactnativestylus;

import androidx.annotation.NonNull;
import com.facebook.react.BaseReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.uimanager.ViewManager;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public final class StylusPackage extends BaseReactPackage {
  @Override public NativeModule getModule(@NonNull String name, @NonNull ReactApplicationContext context) {
    return StylusModule.NAME.equals(name) ? new StylusModule(context) : null;
  }

  @NonNull @Override public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return () -> Collections.singletonMap(StylusModule.NAME,
      new ReactModuleInfo(StylusModule.NAME, StylusModule.NAME, false, false, false, true));
  }

  @NonNull @Override public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext context) {
    return Arrays.asList(new StylusCanvasManager(), new StylusHandwritingInputManager(), new StylusHandwritingDelegatorManager(), new StylusDropZoneManager());
  }
}
