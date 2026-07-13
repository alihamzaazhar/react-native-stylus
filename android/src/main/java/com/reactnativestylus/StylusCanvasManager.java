package com.reactnativestylus;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ViewManagerDelegate;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.viewmanagers.StylusCanvasManagerDelegate;
import com.facebook.react.viewmanagers.StylusCanvasManagerInterface;
import java.util.Map;

public final class StylusCanvasManager extends SimpleViewManager<StylusCanvasView> implements StylusCanvasManagerInterface<StylusCanvasView> {
  public static final String NAME = "StylusCanvas";
  private final ViewManagerDelegate<StylusCanvasView> delegate = new StylusCanvasManagerDelegate<>(this);
  @NonNull @Override public String getName() { return NAME; }
  @Override protected ViewManagerDelegate<StylusCanvasView> getDelegate() { return delegate; }
  @NonNull @Override protected StylusCanvasView createViewInstance(@NonNull ThemedReactContext context) { return new StylusCanvasView(context); }

  @ReactProp(name="color") @Override public void setColor(StylusCanvasView v, String x) { v.setColor(x); }
  @ReactProp(name="strokeWidth", defaultFloat=6f) @Override public void setStrokeWidth(StylusCanvasView v, float x) { v.setStrokeWidth(x); }
  @ReactProp(name="opacity", defaultFloat=1f) @Override public void setOpacity(StylusCanvasView v, float x) { v.setOpacity(x); }
  @ReactProp(name="tool") @Override public void setTool(StylusCanvasView v, String x) { v.setTool(x); }
  @ReactProp(name="brush") @Override public void setBrush(StylusCanvasView v, String x) { v.setBrush(x); }
  @ReactProp(name="brushDynamicsJson") @Override public void setBrushDynamicsJson(StylusCanvasView v, String x) { v.setBrushDynamicsJson(x); }
  @ReactProp(name="eraserMode") @Override public void setEraserMode(StylusCanvasView v, String x) { v.setEraserMode(x); }
  @ReactProp(name="tiltEnabled", defaultBoolean=true) @Override public void setTiltEnabled(StylusCanvasView v, boolean x) { v.setTiltEnabled(x); }
  @ReactProp(name="directionEnabled", defaultBoolean=true) @Override public void setDirectionEnabled(StylusCanvasView v, boolean x) { v.setDirectionEnabled(x); }
  @ReactProp(name="brushPreviewEnabled", defaultBoolean=true) @Override public void setBrushPreviewEnabled(StylusCanvasView v, boolean x) { v.setBrushPreviewEnabled(x); }
  @ReactProp(name="pointerIcon") @Override public void setPointerIcon(StylusCanvasView v, String x) { v.setPointerIconName(x); }
  @ReactProp(name="pressureEnabled", defaultBoolean=true) @Override public void setPressureEnabled(StylusCanvasView v, boolean x) { v.setPressureEnabled(x); }
  @ReactProp(name="predictionEnabled", defaultBoolean=true) @Override public void setPredictionEnabled(StylusCanvasView v, boolean x) { v.setPredictionEnabled(x); }
  @ReactProp(name="fingerDrawingEnabled", defaultBoolean=false) @Override public void setFingerDrawingEnabled(StylusCanvasView v, boolean x) { v.setFingerDrawingEnabled(x); }
  @ReactProp(name="hoverEnabled", defaultBoolean=true) @Override public void setHoverEnabled(StylusCanvasView v, boolean x) { v.setHoverEnabled(x); }
  @ReactProp(name="clearToken", defaultInt=0) @Override public void setClearToken(StylusCanvasView v, int x) { v.setClearToken(x); }
  @ReactProp(name="undoToken", defaultInt=0) @Override public void setUndoToken(StylusCanvasView v, int x) { v.setUndoToken(x); }
  @ReactProp(name="redoToken", defaultInt=0) @Override public void setRedoToken(StylusCanvasView v, int x) { v.setRedoToken(x); }
  @ReactProp(name="strokesJson") @Override public void setStrokesJson(StylusCanvasView v, String x) { v.setStrokesJson(x); }

  @Override public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.<String, Object>builder()
      .put("topStylusEvent", MapBuilder.of("registrationName", "onStylusEvent"))
      .put("topStrokesChanged", MapBuilder.of("registrationName", "onStrokesChanged")).build();
  }
}
