package com.reactnativestylus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.PointerIcon;
import android.widget.FrameLayout;
import androidx.ink.authoring.InProgressStrokeId;
import androidx.ink.authoring.InProgressStrokesFinishedListener;
import androidx.ink.authoring.InProgressStrokesView;
import androidx.ink.brush.Brush;
import androidx.ink.brush.BrushFamily;
import androidx.ink.brush.StockBrushes;
import androidx.input.motionprediction.MotionEventPredictor;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

final class StylusCanvasView extends FrameLayout {
  private static final class Stroke {
    String id = UUID.randomUUID().toString();
    int color; float width, opacity; String tool;
    final List<StylusPoint> points = new ArrayList<>();
  }

  private final List<Stroke> strokes = new ArrayList<>();
  private final List<Stroke> redo = new ArrayList<>();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
  private final MotionEventPredictor predictor;
  private final InProgressStrokesView inkView;
  private InProgressStrokeId inkStroke;
  private Stroke current;
  private int color = Color.BLACK;
  private float strokeWidth = 6f, opacity = 1f;
  private String tool = "pen";
  private String brush = "pressurePen";
  private String eraserMode = "wholeStroke";
  private float minimumWidth = 0.5f, maximumWidth = 100f, pressureGamma = 1f;
  private float velocitySensitivity, tiltSensitivity = 1f, directionSensitivity = 1f, smoothing;
  private long previousEventTime;
  private boolean pressureEnabled = true, predictionEnabled = true, fingerDrawingEnabled, hoverEnabled = true;
  private boolean tiltEnabled = true, directionEnabled = true, brushPreviewEnabled = true;
  private float hoverX, hoverY, hoverPressure, hoverTilt;
  private boolean hovering;
  private int clearToken, undoToken, redoToken;

  StylusCanvasView(Context context) {
    super(context);
    setWillNotDraw(false);
    setBackgroundColor(Color.TRANSPARENT);
    setFocusable(true);
    predictor = MotionEventPredictor.newInstance(this);
    inkView = new InProgressStrokesView(context);
    inkView.setClickable(false);
    inkView.setFocusable(false);
    inkView.setUseHighLatencyRenderHelper(false);
    inkView.addFinishedStrokesListener(new InProgressStrokesFinishedListener() {
      @Override public void onStrokesFinished(java.util.Map<InProgressStrokeId, androidx.ink.strokes.Stroke> finished) {
        inkView.removeFinishedStrokes(finished.keySet());
        invalidate();
      }
    });
    addView(inkView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    inkView.eagerInit();
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStyle(Paint.Style.STROKE);
  }

  void setColor(String value) { try { color = Color.parseColor(value); } catch (Exception ignored) { } }
  void setStrokeWidth(float value) { strokeWidth = Math.max(0.5f, value); }
  void setOpacity(float value) { opacity = Math.max(0f, Math.min(1f, value)); }
  void setTool(String value) { tool = value == null ? "pen" : value; }
  void setBrush(String value) { brush = value == null ? "pressurePen" : value; }
  void setEraserMode(String value) { eraserMode = value == null ? "wholeStroke" : value; }
  void setBrushDynamicsJson(String value) {
    if (value == null) return;
    try {
      JSONObject config = new JSONObject(value);
      minimumWidth = Math.max(0.1f, (float) config.optDouble("minimumWidth", 0.5));
      maximumWidth = Math.max(minimumWidth, (float) config.optDouble("maximumWidth", 100));
      pressureGamma = Math.max(0.05f, (float) config.optDouble("pressureGamma", 1));
      velocitySensitivity = clamp((float) config.optDouble("velocitySensitivity", 0), 0f, 1f);
      tiltSensitivity = clamp((float) config.optDouble("tiltSensitivity", 1), 0f, 2f);
      directionSensitivity = clamp((float) config.optDouble("directionSensitivity", 1), 0f, 2f);
      smoothing = clamp((float) config.optDouble("smoothing", 0), 0f, 0.95f);
    } catch (Exception ignored) { }
  }
  void setTiltEnabled(boolean value) { tiltEnabled = value; }
  void setDirectionEnabled(boolean value) { directionEnabled = value; }
  void setBrushPreviewEnabled(boolean value) { brushPreviewEnabled = value; }
  void setPointerIconName(String value) {
    if (Build.VERSION.SDK_INT < 24) return;
    int type = PointerIcon.TYPE_DEFAULT;
    if ("crosshair".equals(value)) type = PointerIcon.TYPE_CROSSHAIR;
    else if ("hand".equals(value)) type = PointerIcon.TYPE_HAND;
    else if ("text".equals(value)) type = PointerIcon.TYPE_TEXT;
    else if ("none".equals(value)) type = PointerIcon.TYPE_NULL;
    setPointerIcon(PointerIcon.getSystemIcon(getContext(), type));
  }
  void setPressureEnabled(boolean value) { pressureEnabled = value; }
  void setPredictionEnabled(boolean value) { predictionEnabled = value; }
  void setFingerDrawingEnabled(boolean value) { fingerDrawingEnabled = value; }
  void setHoverEnabled(boolean value) { hoverEnabled = value; }
  void setClearToken(int value) { if (value != clearToken) { clearToken = value; clear(); } }
  void setUndoToken(int value) { if (value != undoToken) { undoToken = value; undo(); } }
  void setRedoToken(int value) { if (value != redoToken) { redoToken = value; redo(); } }
  void setStrokesJson(String value) {
    if (value == null) return;
    try {
      JSONArray input = new JSONArray(value);
      strokes.clear(); redo.clear(); current = null;
      for (int i = 0; i < input.length(); i++) {
        JSONObject source = input.getJSONObject(i); Stroke stroke = new Stroke();
        stroke.id = source.optString("id", stroke.id); stroke.color = Color.parseColor(source.optString("color", "#000000"));
        stroke.width = (float) source.optDouble("width", 6); stroke.opacity = (float) source.optDouble("opacity", 1);
        stroke.tool = source.optString("tool", "pen"); JSONArray points = source.optJSONArray("points");
        if (points != null) for (int p = 0; p < points.length(); p++) stroke.points.add(new StylusPoint(points.getJSONObject(p)));
        strokes.add(stroke);
      }
      invalidate();
    } catch (Exception ignored) { }
  }

  @Override public boolean onHoverEvent(MotionEvent event) {
    if (!hoverEnabled || !isStylus(event)) return super.onHoverEvent(event);
    hoverX = event.getX(); hoverY = event.getY(); hoverPressure = event.getPressure();
    hoverTilt = event.getAxisValue(MotionEvent.AXIS_TILT);
    hovering = event.getActionMasked() != MotionEvent.ACTION_HOVER_EXIT;
    emitInput(event, actionName(event.getActionMasked()), false);
    invalidate();
    return true;
  }

  @Override public boolean onGenericMotionEvent(MotionEvent event) {
    int action = event.getActionMasked();
    if (isStylus(event) && (action == MotionEvent.ACTION_BUTTON_PRESS || action == MotionEvent.ACTION_BUTTON_RELEASE)) {
      emitInput(event, actionName(action), false);
      return true;
    }
    return super.onGenericMotionEvent(event);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    boolean stylus = isStylus(event);
    if (!stylus && !fingerDrawingEnabled) return false;
    predictor.record(event);
    boolean canceled = action == MotionEvent.ACTION_CANCEL ||
      (Build.VERSION.SDK_INT >= 33 && (event.getFlags() & MotionEvent.FLAG_CANCELED) != 0);

    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
      getParent().requestDisallowInterceptTouchEvent(true);
      beginStroke(event);
      if (!"eraser".equals(current.tool)) {
        inkStroke = inkView.startStroke(event, event.getActionIndex(), createInkBrush());
      }
    } else if (action == MotionEvent.ACTION_MOVE && current != null) {
      if (inkStroke != null) inkView.addToStroke(event, 0, inkStroke);
      appendHistory(event);
      appendCurrent(event);
      if ("eraser".equals(current.tool)) eraseAt(event.getX(), event.getY(), current.width * 2f);
    } else if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) && current != null) {
      if (inkStroke != null) {
        if (canceled) inkView.cancelStroke(inkStroke, event);
        else inkView.finishStroke(event, event.getActionIndex(), inkStroke);
        inkStroke = null;
      }
      appendCurrent(event);
      if (canceled) strokes.remove(current);
      current = null;
      redo.clear();
      emitStrokes();
      getParent().requestDisallowInterceptTouchEvent(false);
    } else if (action == MotionEvent.ACTION_CANCEL) {
      if (inkStroke != null) { inkView.cancelStroke(inkStroke, event); inkStroke = null; }
      if (current != null) strokes.remove(current);
      current = null;
      emitStrokes();
      getParent().requestDisallowInterceptTouchEvent(false);
    }
    emitInput(event, actionName(action), canceled);
    invalidate();
    return true;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    return true;
  }

  private Brush createInkBrush() {
    BrushFamily family;
    if ("marker".equals(brush)) family = StockBrushes.marker();
    else if ("highlighter".equals(brush) || "highlighter".equals(tool)) family = StockBrushes.highlighter();
    else family = StockBrushes.pressurePen();
    int alpha = Math.round(255f * opacity * ("highlighter".equals(tool) ? 0.45f : 1f));
    int brushColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    return Brush.createWithColorIntArgb(family, brushColor, strokeWidth, 0.1f);
  }

  private void beginStroke(MotionEvent event) {
    Stroke stroke = new Stroke();
    stroke.color = color; stroke.width = strokeWidth; stroke.opacity = opacity;
    boolean hardwareEraser = event.getToolType(event.getActionIndex()) == MotionEvent.TOOL_TYPE_ERASER;
    stroke.tool = hardwareEraser ? "eraser" : tool;
    current = stroke;
    strokes.add(stroke);
    appendCurrent(event);
  }

  private void appendHistory(MotionEvent event) {
    for (int h = 0; h < event.getHistorySize(); h++) current.points.add(new StylusPoint(event, 0, h, false));
  }

  private void appendCurrent(MotionEvent event) { current.points.add(new StylusPoint(event, 0, -1, false)); }

  private void eraseAt(float x, float y, float radius) {
    if ("partial".equals(eraserMode)) { erasePartial(x, y, radius); return; }
    for (int s = strokes.size() - 1; s >= 0; s--) {
      Stroke stroke = strokes.get(s);
      if (stroke == current) continue;
      for (StylusPoint point : stroke.points) {
        float dx = point.x - x, dy = point.y - y;
        if (dx * dx + dy * dy <= radius * radius) { strokes.remove(s); break; }
      }
    }
  }

  private void erasePartial(float x, float y, float radius) {
    float squaredRadius = radius * radius;
    List<Stroke> replacements = new ArrayList<>();
    for (int s = strokes.size() - 1; s >= 0; s--) {
      Stroke source = strokes.get(s);
      if (source == current || "eraser".equals(source.tool)) continue;
      List<StylusPoint> segment = new ArrayList<>();
      boolean changed = false;
      for (StylusPoint point : source.points) {
        boolean erased = (point.x - x) * (point.x - x) + (point.y - y) * (point.y - y) <= squaredRadius;
        if (erased) {
          changed = true;
          addSegment(source, segment, replacements);
          segment = new ArrayList<>();
        } else segment.add(point);
      }
      if (changed) {
        addSegment(source, segment, replacements);
        strokes.remove(s);
      }
    }
    strokes.addAll(replacements);
  }

  private static void addSegment(Stroke source, List<StylusPoint> points, List<Stroke> output) {
    if (points.size() < 2) return;
    Stroke segment = new Stroke();
    segment.color = source.color; segment.width = source.width; segment.opacity = source.opacity; segment.tool = source.tool;
    segment.points.addAll(points); output.add(segment);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (Stroke stroke : strokes) {
      if (stroke == current && inkStroke != null) continue;
      if ("eraser".equals(stroke.tool) || stroke.points.size() < 2) continue;
      paint.setColor(stroke.color);
      paint.setAlpha(Math.round(255 * stroke.opacity * ("highlighter".equals(stroke.tool) ? 0.45f : 1f)));
      for (int i = 1; i < stroke.points.size(); i++) {
        StylusPoint a = stroke.points.get(i - 1), b = stroke.points.get(i);
        float pressure = pressureEnabled ? Math.max(0.01f, (float) Math.pow(Math.max(0f, (a.pressure + b.pressure) * 0.5f), pressureGamma)) : 1f;
        float tiltFactor = tiltEnabled && "calligraphy".equals(brush) ? 1f + Math.abs(b.tilt) * tiltSensitivity : 1f;
        float directionFactor = directionEnabled && "calligraphy".equals(brush) ? 1f - directionSensitivity * 0.45f + directionSensitivity * 0.45f * Math.abs((float) Math.cos(b.orientation)) : 1f;
        float brushFactor = "marker".equals(brush) ? 1.3f : 1f;
        float delta = Math.max(1f, b.timestamp - a.timestamp);
        float velocity = (float) Math.hypot(b.x - a.x, b.y - a.y) / delta;
        float velocityFactor = 1f - velocitySensitivity * Math.min(0.8f, velocity / 4f);
        float targetWidth = clamp(stroke.width * pressure * tiltFactor * directionFactor * brushFactor * velocityFactor, minimumWidth, maximumWidth);
        float previousWidth = paint.getStrokeWidth();
        paint.setStrokeWidth(previousWidth * smoothing + targetWidth * (1f - smoothing));
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
      }
    }
    if (hovering && brushPreviewEnabled) {
      paint.setStyle(Paint.Style.STROKE); paint.setColor(color); paint.setAlpha(170);
      paint.setStrokeWidth(2f); float factor = tiltEnabled ? 1f + hoverTilt * 0.5f : 1f;
      canvas.drawCircle(hoverX, hoverY, Math.max(3f, strokeWidth * Math.max(0.2f, hoverPressure) * factor), paint);
    }
  }

  private void emitInput(MotionEvent event, String action, boolean canceled) {
    try {
      JSONObject payload = new JSONObject();
      StylusPoint point = new StylusPoint(event, event.getActionIndex(), -1, false);
      JSONArray history = new JSONArray();
      for (int h = 0; h < event.getHistorySize(); h++) history.put(new StylusPoint(event, 0, h, false).json());
      JSONObject predicted = null;
      if (predictionEnabled && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
        MotionEvent prediction = predictor.predict();
        if (prediction != null) { predicted = new StylusPoint(prediction, 0, -1, true).json(); prediction.recycle(); }
      }
      payload.put("action", action); payload.put("point", point.json()); payload.put("history", history);
      payload.put("predicted", predicted == null ? JSONObject.NULL : predicted);
      payload.put("canceled", canceled); payload.put("palmRejected", canceled && point.toolType == MotionEvent.TOOL_TYPE_FINGER);
      JSONObject diagnostics = new JSONObject();
      long interval = previousEventTime == 0 ? 0 : Math.max(1, event.getEventTime() - previousEventTime);
      diagnostics.put("eventAgeMs", Math.max(0, SystemClock.uptimeMillis() - event.getEventTime()));
      diagnostics.put("historicalSampleCount", event.getHistorySize());
      diagnostics.put("predictedSampleAvailable", predicted != null);
      diagnostics.put("estimatedSampleRateHz", interval == 0 ? 0 : 1000f * (event.getHistorySize() + 1) / interval);
      payload.put("diagnostics", diagnostics); previousEventTime = event.getEventTime();
      WritableMap body = Arguments.createMap(); body.putString("payload", payload.toString());
      emit("topStylusEvent", body);
    } catch (Exception ignored) { }
  }

  private void emitStrokes() {
    WritableMap body = Arguments.createMap();
    body.putString("strokesJson", strokesJson().toString());
    body.putInt("strokeCount", strokes.size()); body.putBoolean("canUndo", !strokes.isEmpty()); body.putBoolean("canRedo", !redo.isEmpty());
    emit("topStrokesChanged", body);
  }

  @SuppressWarnings("deprecation") private void emit(String name, WritableMap body) {
    ((com.facebook.react.bridge.ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), name, body);
  }

  private JSONArray strokesJson() {
    JSONArray array = new JSONArray();
    for (Stroke stroke : strokes) {
      try {
        JSONObject item = new JSONObject(); item.put("id", stroke.id);
        item.put("color", String.format("#%08X", stroke.color)); item.put("width", stroke.width);
        item.put("opacity", stroke.opacity); item.put("tool", stroke.tool);
        JSONArray points = new JSONArray(); for (StylusPoint point : stroke.points) points.put(point.json());
        item.put("points", points); array.put(item);
      } catch (Exception ignored) { }
    }
    return array;
  }

  private void clear() { strokes.clear(); redo.clear(); current = null; inkView.cancelUnfinishedStrokes(); inkStroke = null; invalidate(); emitStrokes(); }
  private void undo() { if (!strokes.isEmpty()) { redo.add(strokes.remove(strokes.size() - 1)); invalidate(); emitStrokes(); } }
  private void redo() { if (!redo.isEmpty()) { strokes.add(redo.remove(redo.size() - 1)); invalidate(); emitStrokes(); } }

  private static boolean isStylus(MotionEvent event) {
    return event.isFromSource(InputDevice.SOURCE_STYLUS) || event.getToolType(event.getActionIndex()) == MotionEvent.TOOL_TYPE_STYLUS || event.getToolType(event.getActionIndex()) == MotionEvent.TOOL_TYPE_ERASER;
  }

  private static float clamp(float value, float minimum, float maximum) { return Math.max(minimum, Math.min(maximum, value)); }

  private static String actionName(int action) {
    switch (action) {
      case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN: return "down";
      case MotionEvent.ACTION_MOVE: return "move";
      case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP: return "up";
      case MotionEvent.ACTION_CANCEL: return "cancel";
      case MotionEvent.ACTION_HOVER_ENTER: return "hoverEnter";
      case MotionEvent.ACTION_HOVER_MOVE: return "hoverMove";
      case MotionEvent.ACTION_HOVER_EXIT: return "hoverExit";
      case MotionEvent.ACTION_BUTTON_PRESS: return "buttonPress";
      case MotionEvent.ACTION_BUTTON_RELEASE: return "buttonRelease";
      default: return "move";
    }
  }
}
