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
  @ReactProp(name="viewportScale", defaultFloat=1f) @Override public void setViewportScale(StylusCanvasView v, float x) { v.setViewportScale(x); }
  @ReactProp(name="viewportOffsetX", defaultFloat=0f) @Override public void setViewportOffsetX(StylusCanvasView v, float x) { v.setViewportOffsetX(x); }
  @ReactProp(name="viewportOffsetY", defaultFloat=0f) @Override public void setViewportOffsetY(StylusCanvasView v, float x) { v.setViewportOffsetY(x); }
  @ReactProp(name="viewportRotation", defaultFloat=0f) @Override public void setViewportRotation(StylusCanvasView v, float x) { v.setViewportRotation(x); }
  @ReactProp(name="viewportGesturesEnabled", defaultBoolean=false) @Override public void setViewportGesturesEnabled(StylusCanvasView v, boolean x) { v.setViewportGesturesEnabled(x); }
  @ReactProp(name="resetViewportToken", defaultInt=0) @Override public void setResetViewportToken(StylusCanvasView v, int x) { v.setResetViewportToken(x); }
  @ReactProp(name="selectionMode") @Override public void setSelectionMode(StylusCanvasView v, String x) { v.setSelectionMode(x); }
  @ReactProp(name="selectedStrokeIdsJson") @Override public void setSelectedStrokeIdsJson(StylusCanvasView v, String x) { v.setSelectedStrokeIdsJson(x); }
  @ReactProp(name="selectionTransformJson") @Override public void setSelectionTransformJson(StylusCanvasView v, String x) { v.setSelectionTransformJson(x); }
  @ReactProp(name="deleteSelectionToken", defaultInt=0) @Override public void setDeleteSelectionToken(StylusCanvasView v, int x) { v.setDeleteSelectionToken(x); }
  @ReactProp(name="duplicateSelectionToken", defaultInt=0) @Override public void setDuplicateSelectionToken(StylusCanvasView v, int x) { v.setDuplicateSelectionToken(x); }
  @ReactProp(name="tiltEnabled", defaultBoolean=true) @Override public void setTiltEnabled(StylusCanvasView v, boolean x) { v.setTiltEnabled(x); }
  @ReactProp(name="directionEnabled", defaultBoolean=true) @Override public void setDirectionEnabled(StylusCanvasView v, boolean x) { v.setDirectionEnabled(x); }
  @ReactProp(name="brushPreviewEnabled", defaultBoolean=true) @Override public void setBrushPreviewEnabled(StylusCanvasView v, boolean x) { v.setBrushPreviewEnabled(x); }
  @ReactProp(name="pointerIcon") @Override public void setPointerIcon(StylusCanvasView v, String x) { v.setPointerIconName(x); }
  @ReactProp(name="pointerIconResource") @Override public void setPointerIconResource(StylusCanvasView v, String x) { v.setPointerIconResource(x); }
  @ReactProp(name="pointerIconHotspotX", defaultFloat=0f) @Override public void setPointerIconHotspotX(StylusCanvasView v, float x) { v.setPointerIconHotspotX(x); }
  @ReactProp(name="pointerIconHotspotY", defaultFloat=0f) @Override public void setPointerIconHotspotY(StylusCanvasView v, float x) { v.setPointerIconHotspotY(x); }
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
      .put("topStrokesChanged", MapBuilder.of("registrationName", "onStrokesChanged"))
      .put("topViewportChanged", MapBuilder.of("registrationName", "onViewportChanged"))
      .put("topSelectionChanged", MapBuilder.of("registrationName", "onSelectionChanged")).build();
  }
}
