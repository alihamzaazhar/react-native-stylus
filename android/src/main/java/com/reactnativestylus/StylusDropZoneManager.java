package com.reactnativestylus;

import android.view.View;
import androidx.annotation.NonNull;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ViewManagerDelegate;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.viewmanagers.StylusDropZoneManagerDelegate;
import com.facebook.react.viewmanagers.StylusDropZoneManagerInterface;
import java.util.Map;

public final class StylusDropZoneManager extends ViewGroupManager<StylusDropZoneView> implements StylusDropZoneManagerInterface<StylusDropZoneView> {
  public static final String NAME = "StylusDropZone";
  private final ViewManagerDelegate<StylusDropZoneView> delegate = new StylusDropZoneManagerDelegate<>(this);
  @NonNull @Override public String getName() { return NAME; }
  @Override protected ViewManagerDelegate<StylusDropZoneView> getDelegate() { return delegate; }
  @NonNull @Override protected StylusDropZoneView createViewInstance(@NonNull ThemedReactContext context) { return new StylusDropZoneView(context); }
  @Override public void addView(StylusDropZoneView parent, View child, int index) { parent.addView(child, index); }
  @Override public int getChildCount(StylusDropZoneView parent) { return parent.getChildCount(); }
  @Override public View getChildAt(StylusDropZoneView parent, int index) { return parent.getChildAt(index); }
  @Override public void removeViewAt(StylusDropZoneView parent, int index) { parent.removeViewAt(index); }
  @ReactProp(name="payload") @Override public void setPayload(StylusDropZoneView v, String x) { v.setPayload(x); }
  @ReactProp(name="draggable", defaultBoolean=false) @Override public void setDraggable(StylusDropZoneView v, boolean x) { v.setDraggable(x); }
  @ReactProp(name="dropEnabled", defaultBoolean=true) @Override public void setDropEnabled(StylusDropZoneView v, boolean x) { v.setDropEnabled(x); }
  @Override public Map<String, Object> getExportedCustomDirectEventTypeConstants() { return MapBuilder.of("topStylusDrag", MapBuilder.of("registrationName", "onStylusDrag")); }
}
