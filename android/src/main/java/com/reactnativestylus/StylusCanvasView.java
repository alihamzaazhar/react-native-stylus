package com.reactnativestylus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.PointerIcon;
import android.view.ScaleGestureDetector;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

final class StylusCanvasView extends FrameLayout {
  private static final class Stroke {
    String id = UUID.randomUUID().toString();
    int color; float width, opacity; String tool, brush;
    float minimumWidth, maximumWidth, pressureGamma;
    float velocitySensitivity, tiltSensitivity, directionSensitivity, smoothing;
    boolean pressureEnabled, tiltEnabled, directionEnabled;
    final List<StylusPoint> points = new ArrayList<>();
  }

  private final List<Stroke> strokes = new ArrayList<>();
  private final List<Stroke> redo = new ArrayList<>();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
  private final MotionEventPredictor predictor;
  private final ScaleGestureDetector scaleDetector;
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
  private String pointerIconResource;
  private float pointerIconHotspotX, pointerIconHotspotY;
  private float hoverX, hoverY, hoverPressure, hoverTilt;
  private boolean hovering;
  private float viewportScale = 1f, viewportOffsetX, viewportOffsetY, viewportRotation;
  private boolean viewportGesturesEnabled;
  private float lastPanX, lastPanY;
  private String selectionMode = "none";
  private final Set<String> selectedStrokeIds = new HashSet<>();
  private final List<PointF> lasso = new ArrayList<>();
  private int lastSelectionTransformToken;
  private int clearToken, undoToken, redoToken, resetViewportToken, deleteSelectionToken, duplicateSelectionToken;

  StylusCanvasView(Context context) {
    super(context);
    setWillNotDraw(false);
    setBackgroundColor(Color.TRANSPARENT);
    setFocusable(true);
    predictor = MotionEventPredictor.newInstance(this);
    scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
      @Override public boolean onScale(ScaleGestureDetector detector) {
        PointF anchor = viewToDocument(detector.getFocusX(), detector.getFocusY());
        viewportScale = clamp(viewportScale * detector.getScaleFactor(), 0.1f, 12f);
        PointF withoutOffset = documentToView(anchor.x, anchor.y, false);
        viewportOffsetX = detector.getFocusX() - withoutOffset.x;
        viewportOffsetY = detector.getFocusY() - withoutOffset.y;
        emitViewport(); invalidate(); return true;
      }
    });
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
  void setViewportScale(float value) { viewportScale = clamp(value, 0.1f, 12f); invalidate(); }
  void setViewportOffsetX(float value) { viewportOffsetX = value; invalidate(); }
  void setViewportOffsetY(float value) { viewportOffsetY = value; invalidate(); }
  void setViewportRotation(float value) { viewportRotation = value; invalidate(); }
  void setViewportGesturesEnabled(boolean value) { viewportGesturesEnabled = value; }
  void setResetViewportToken(int value) { if (value != resetViewportToken) { resetViewportToken = value; viewportScale = 1f; viewportOffsetX = viewportOffsetY = viewportRotation = 0f; emitViewport(); invalidate(); } }
  void setSelectionMode(String value) { selectionMode = value == null ? "none" : value; }
  void setSelectedStrokeIdsJson(String value) {
    if (value == null) return;
    try { selectedStrokeIds.clear(); JSONArray ids = new JSONArray(value); for (int i = 0; i < ids.length(); i++) selectedStrokeIds.add(ids.getString(i)); invalidate(); } catch (Exception ignored) { }
  }
  void setSelectionTransformJson(String value) {
    if (value == null) return;
    try {
      JSONObject transform = new JSONObject(value); int token = transform.optInt("token");
      if (token == lastSelectionTransformToken) return; lastSelectionTransformToken = token;
      transformSelection((float) transform.optDouble("translateX"), (float) transform.optDouble("translateY"),
        (float) transform.optDouble("scaleX", 1), (float) transform.optDouble("scaleY", 1), (float) transform.optDouble("rotation"));
    } catch (Exception ignored) { }
  }
  void setDeleteSelectionToken(int value) { if (value != deleteSelectionToken) { deleteSelectionToken = value; deleteSelection(); } }
  void setDuplicateSelectionToken(int value) { if (value != duplicateSelectionToken) { duplicateSelectionToken = value; duplicateSelection(); } }
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
  void setPointerIconResource(String value) { pointerIconResource = value; applyPointerIconResource(); }
  void setPointerIconHotspotX(float value) { pointerIconHotspotX = value; applyPointerIconResource(); }
  void setPointerIconHotspotY(float value) { pointerIconHotspotY = value; applyPointerIconResource(); }
  private void applyPointerIconResource() {
    if (Build.VERSION.SDK_INT < 24 || pointerIconResource == null || pointerIconResource.isEmpty()) return;
    int resource = getResources().getIdentifier(pointerIconResource, "drawable", getContext().getPackageName());
    if (resource == 0) resource = getResources().getIdentifier(pointerIconResource, "mipmap", getContext().getPackageName());
    if (resource == 0) return;
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
    if (bitmap == null) return;
    float x = clamp(pointerIconHotspotX, 0, Math.max(0, bitmap.getWidth() - 1));
    float y = clamp(pointerIconHotspotY, 0, Math.max(0, bitmap.getHeight() - 1));
    setPointerIcon(PointerIcon.create(bitmap, x, y));
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
        stroke.tool = source.optString("tool", "pen"); stroke.brush = source.optString("brush", "pressurePen");
        JSONObject dynamics = source.optJSONObject("brushDynamics");
        stroke.minimumWidth = dynamics == null ? 0.5f : Math.max(0.1f, (float) dynamics.optDouble("minimumWidth", 0.5));
        stroke.maximumWidth = dynamics == null ? 100f : Math.max(stroke.minimumWidth, (float) dynamics.optDouble("maximumWidth", 100));
        stroke.pressureGamma = dynamics == null ? 1f : Math.max(0.05f, (float) dynamics.optDouble("pressureGamma", 1));
        stroke.velocitySensitivity = dynamics == null ? 0f : clamp((float) dynamics.optDouble("velocitySensitivity", 0), 0f, 1f);
        stroke.tiltSensitivity = dynamics == null ? 1f : clamp((float) dynamics.optDouble("tiltSensitivity", 1), 0f, 2f);
        stroke.directionSensitivity = dynamics == null ? 1f : clamp((float) dynamics.optDouble("directionSensitivity", 1), 0f, 2f);
        stroke.smoothing = dynamics == null ? 0f : clamp((float) dynamics.optDouble("smoothing", 0), 0f, 0.95f);
        stroke.pressureEnabled = source.optBoolean("pressureEnabled", true);
        stroke.tiltEnabled = source.optBoolean("tiltEnabled", true);
        stroke.directionEnabled = source.optBoolean("directionEnabled", true);
        JSONArray points = source.optJSONArray("points");
        if (points != null) for (int p = 0; p < points.length(); p++) stroke.points.add(new StylusPoint(points.getJSONObject(p)));
        strokes.add(stroke);
      }
      reconcileSelection();
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
    if (!stylus && viewportGesturesEnabled) return handleViewportGesture(event);
    if ("lasso".equals(selectionMode)) return handleLasso(event);
    if ("eyedropper".equals(selectionMode)) return handleEyedropper(event);
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
      if ("eraser".equals(current.tool)) { PointF point = viewToDocument(event.getX(), event.getY()); eraseAt(point.x, point.y, current.width * 2f / viewportScale); }
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

  private boolean handleViewportGesture(MotionEvent event) {
    scaleDetector.onTouchEvent(event);
    int action = event.getActionMasked();
    if (action == MotionEvent.ACTION_DOWN) { lastPanX = event.getX(); lastPanY = event.getY(); getParent().requestDisallowInterceptTouchEvent(true); }
    else if (action == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1 && !scaleDetector.isInProgress()) {
      viewportOffsetX += event.getX() - lastPanX; viewportOffsetY += event.getY() - lastPanY;
      lastPanX = event.getX(); lastPanY = event.getY(); emitViewport(); invalidate();
    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) getParent().requestDisallowInterceptTouchEvent(false);
    return true;
  }

  private boolean handleLasso(MotionEvent event) {
    int action = event.getActionMasked(); PointF point = viewToDocument(event.getX(), event.getY());
    if (action == MotionEvent.ACTION_DOWN) { lasso.clear(); lasso.add(point); getParent().requestDisallowInterceptTouchEvent(true); }
    else if (action == MotionEvent.ACTION_MOVE) lasso.add(point);
    else if (action == MotionEvent.ACTION_UP) { lasso.add(point); selectLasso(); lasso.clear(); getParent().requestDisallowInterceptTouchEvent(false); }
    else if (action == MotionEvent.ACTION_CANCEL) { lasso.clear(); getParent().requestDisallowInterceptTouchEvent(false); }
    invalidate(); return true;
  }

  private boolean handleEyedropper(MotionEvent event) {
    if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return true;
    PointF sample = viewToDocument(event.getX(), event.getY());
    Stroke nearest = null; float nearestDistance = Float.MAX_VALUE;
    for (Stroke stroke : strokes) {
      if ("eraser".equals(stroke.tool)) continue;
      for (StylusPoint point : stroke.points) {
        float dx = point.x - sample.x, dy = point.y - sample.y;
        float distance = dx * dx + dy * dy;
        if (distance < nearestDistance) { nearestDistance = distance; nearest = stroke; }
      }
    }
    if (nearest != null) {
      WritableMap body = Arguments.createMap();
      body.putString("color", String.format("#%06X", nearest.color & 0xFFFFFF));
      emit("topColorPicked", body);
    }
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
    stroke.color = color; stroke.width = strokeWidth / viewportScale; stroke.opacity = opacity;
    stroke.brush = brush; stroke.minimumWidth = minimumWidth / viewportScale; stroke.maximumWidth = maximumWidth / viewportScale;
    stroke.pressureGamma = pressureGamma; stroke.velocitySensitivity = velocitySensitivity;
    stroke.tiltSensitivity = tiltSensitivity; stroke.directionSensitivity = directionSensitivity; stroke.smoothing = smoothing;
    stroke.pressureEnabled = pressureEnabled; stroke.tiltEnabled = tiltEnabled; stroke.directionEnabled = directionEnabled;
    boolean hardwareEraser = event.getToolType(event.getActionIndex()) == MotionEvent.TOOL_TYPE_ERASER;
    stroke.tool = hardwareEraser ? "eraser" : tool;
    current = stroke;
    strokes.add(stroke);
    appendCurrent(event);
  }

  private void appendHistory(MotionEvent event) {
    for (int h = 0; h < event.getHistorySize(); h++) current.points.add(documentPoint(new StylusPoint(event, 0, h, false)));
  }

  private void appendCurrent(MotionEvent event) { current.points.add(documentPoint(new StylusPoint(event, event.getActionIndex(), -1, false))); }

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
    copyStrokeStyle(source, segment);
    segment.points.addAll(points); output.add(segment);
  }

  private static void copyStrokeStyle(Stroke source, Stroke target) {
    target.color = source.color; target.width = source.width; target.opacity = source.opacity;
    target.tool = source.tool; target.brush = source.brush;
    target.minimumWidth = source.minimumWidth; target.maximumWidth = source.maximumWidth;
    target.pressureGamma = source.pressureGamma; target.velocitySensitivity = source.velocitySensitivity;
    target.tiltSensitivity = source.tiltSensitivity; target.directionSensitivity = source.directionSensitivity;
    target.smoothing = source.smoothing; target.pressureEnabled = source.pressureEnabled;
    target.tiltEnabled = source.tiltEnabled; target.directionEnabled = source.directionEnabled;
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.save();
    canvas.translate(viewportOffsetX, viewportOffsetY); canvas.rotate(viewportRotation); canvas.scale(viewportScale, viewportScale);
    for (Stroke stroke : strokes) {
      if (stroke == current && inkStroke != null) continue;
      if ("eraser".equals(stroke.tool) || stroke.points.size() < 2) continue;
      paint.setColor(stroke.color);
      paint.setAlpha(Math.round(255 * stroke.opacity * ("highlighter".equals(stroke.tool) ? 0.45f : 1f)));
      float renderedWidth = stroke.width;
      for (int i = 1; i < stroke.points.size(); i++) {
        StylusPoint a = stroke.points.get(i - 1), b = stroke.points.get(i);
        float pressure = stroke.pressureEnabled
          ? 0.75f + 0.5f * (float) Math.pow(clamp((a.pressure + b.pressure) * 0.5f, 0f, 1f), stroke.pressureGamma)
          : 1f;
        float tiltFactor = stroke.tiltEnabled && "calligraphy".equals(stroke.brush) ? 1f + Math.abs(b.tilt) * stroke.tiltSensitivity : 1f;
        float directionFactor = stroke.directionEnabled && "calligraphy".equals(stroke.brush) ? 1f - stroke.directionSensitivity * 0.45f + stroke.directionSensitivity * 0.45f * Math.abs((float) Math.cos(b.orientation)) : 1f;
        float brushFactor = "marker".equals(stroke.brush) ? 1.3f : 1f;
        float delta = Math.max(1f, b.timestamp - a.timestamp);
        float velocity = (float) Math.hypot(b.x - a.x, b.y - a.y) / delta;
        float velocityFactor = 1f - stroke.velocitySensitivity * Math.min(0.8f, velocity / 4f);
        float targetWidth = clamp(stroke.width * pressure * tiltFactor * directionFactor * brushFactor * velocityFactor, stroke.minimumWidth, stroke.maximumWidth);
        renderedWidth = i == 1 ? targetWidth : renderedWidth * stroke.smoothing + targetWidth * (1f - stroke.smoothing);
        paint.setStrokeWidth(renderedWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
      }
    }
    drawSelection(canvas);
    canvas.restore();
    drawLasso(canvas);
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
      PointF document = viewToDocument(point.x, point.y); JSONObject documentJson = new JSONObject(); documentJson.put("x", document.x); documentJson.put("y", document.y); payload.put("documentPoint", documentJson);
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

  private void emitViewport() {
    WritableMap body = Arguments.createMap(); body.putDouble("scale", viewportScale); body.putDouble("offsetX", viewportOffsetX);
    body.putDouble("offsetY", viewportOffsetY); body.putDouble("rotation", viewportRotation); emit("topViewportChanged", body);
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
        item.put("opacity", stroke.opacity); item.put("tool", stroke.tool); item.put("brush", stroke.brush);
        item.put("pressureEnabled", stroke.pressureEnabled); item.put("tiltEnabled", stroke.tiltEnabled); item.put("directionEnabled", stroke.directionEnabled);
        JSONObject dynamics = new JSONObject(); dynamics.put("minimumWidth", stroke.minimumWidth); dynamics.put("maximumWidth", stroke.maximumWidth);
        dynamics.put("pressureGamma", stroke.pressureGamma); dynamics.put("velocitySensitivity", stroke.velocitySensitivity);
        dynamics.put("tiltSensitivity", stroke.tiltSensitivity); dynamics.put("directionSensitivity", stroke.directionSensitivity);
        dynamics.put("smoothing", stroke.smoothing); item.put("brushDynamics", dynamics);
        JSONArray points = new JSONArray(); for (StylusPoint point : stroke.points) points.put(point.json());
        item.put("points", points); array.put(item);
      } catch (Exception ignored) { }
    }
    return array;
  }

  private StylusPoint documentPoint(StylusPoint source) {
    PointF point = viewToDocument(source.x, source.y);
    return new StylusPoint(source, point.x, point.y);
  }

  private PointF viewToDocument(float x, float y) {
    float radians = (float) Math.toRadians(-viewportRotation);
    float translatedX = x - viewportOffsetX, translatedY = y - viewportOffsetY;
    return new PointF((translatedX * (float) Math.cos(radians) - translatedY * (float) Math.sin(radians)) / viewportScale,
      (translatedX * (float) Math.sin(radians) + translatedY * (float) Math.cos(radians)) / viewportScale);
  }

  private PointF documentToView(float x, float y, boolean includeOffset) {
    float scaledX = x * viewportScale, scaledY = y * viewportScale;
    float radians = (float) Math.toRadians(viewportRotation);
    return new PointF(scaledX * (float) Math.cos(radians) - scaledY * (float) Math.sin(radians) + (includeOffset ? viewportOffsetX : 0),
      scaledX * (float) Math.sin(radians) + scaledY * (float) Math.cos(radians) + (includeOffset ? viewportOffsetY : 0));
  }

  private void selectLasso() {
    selectedStrokeIds.clear();
    if (lasso.size() >= 3) for (Stroke stroke : strokes) {
      for (StylusPoint point : stroke.points) if (insideLasso(point.x, point.y)) { selectedStrokeIds.add(stroke.id); break; }
    }
    emitSelection();
  }

  private boolean insideLasso(float x, float y) {
    boolean inside = false;
    for (int i = 0, previous = lasso.size() - 1; i < lasso.size(); previous = i++) {
      PointF a = lasso.get(i), b = lasso.get(previous);
      if ((a.y > y) != (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) inside = !inside;
    }
    return inside;
  }

  private float[] selectionBounds() {
    float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
    for (Stroke stroke : strokes) if (selectedStrokeIds.contains(stroke.id)) for (StylusPoint point : stroke.points) {
      minX = Math.min(minX, point.x); minY = Math.min(minY, point.y); maxX = Math.max(maxX, point.x); maxY = Math.max(maxY, point.y);
    }
    return minX == Float.MAX_VALUE ? null : new float[]{minX, minY, maxX, maxY};
  }

  private void transformSelection(float translateX, float translateY, float scaleX, float scaleY, float rotation) {
    float[] bounds = selectionBounds(); if (bounds == null) return;
    float originX = (bounds[0] + bounds[2]) / 2f, originY = (bounds[1] + bounds[3]) / 2f;
    float cosine = (float) Math.cos(rotation), sine = (float) Math.sin(rotation);
    for (Stroke stroke : strokes) if (selectedStrokeIds.contains(stroke.id)) for (int i = 0; i < stroke.points.size(); i++) {
      StylusPoint point = stroke.points.get(i); float x = (point.x - originX) * scaleX, y = (point.y - originY) * scaleY;
      stroke.points.set(i, new StylusPoint(point, x * cosine - y * sine + originX + translateX, x * sine + y * cosine + originY + translateY));
    }
    invalidate(); emitStrokes(); emitSelection();
  }

  private void deleteSelection() {
    if (selectedStrokeIds.isEmpty()) return;
    for (int i = strokes.size() - 1; i >= 0; i--) if (selectedStrokeIds.contains(strokes.get(i).id)) strokes.remove(i);
    selectedStrokeIds.clear(); invalidate(); emitStrokes(); emitSelection();
  }

  private void duplicateSelection() {
    Set<String> sourceIds = new HashSet<>(selectedStrokeIds);
    List<Stroke> copies = new ArrayList<>(); selectedStrokeIds.clear();
    for (Stroke source : strokes) {
      if (!sourceIds.contains(source.id)) continue;
      Stroke copy = new Stroke(); copyStrokeStyle(source, copy);
      for (StylusPoint point : source.points) copy.points.add(new StylusPoint(point, point.x + 16 / viewportScale, point.y + 16 / viewportScale));
      copies.add(copy);
    }
    for (Stroke copy : copies) selectedStrokeIds.add(copy.id);
    strokes.addAll(copies); invalidate(); emitStrokes(); emitSelection();
  }

  private void drawSelection(Canvas canvas) {
    float[] bounds = selectionBounds(); if (bounds == null) return;
    paint.setStyle(Paint.Style.STROKE); paint.setColor(Color.rgb(21, 115, 104)); paint.setAlpha(220); paint.setStrokeWidth(2f / viewportScale);
    canvas.drawRect(bounds[0] - 6 / viewportScale, bounds[1] - 6 / viewportScale, bounds[2] + 6 / viewportScale, bounds[3] + 6 / viewportScale, paint);
  }

  private void drawLasso(Canvas canvas) {
    if (lasso.size() < 2) return; Path path = new Path();
    PointF first = documentToView(lasso.get(0).x, lasso.get(0).y, true); path.moveTo(first.x, first.y);
    for (int i = 1; i < lasso.size(); i++) { PointF point = documentToView(lasso.get(i).x, lasso.get(i).y, true); path.lineTo(point.x, point.y); }
    paint.setStyle(Paint.Style.STROKE); paint.setColor(Color.rgb(21, 115, 104)); paint.setAlpha(220); paint.setStrokeWidth(2f); canvas.drawPath(path, paint);
  }

  private void emitSelection() {
    WritableMap body = Arguments.createMap(); JSONArray ids = new JSONArray(); for (String id : selectedStrokeIds) ids.put(id);
    body.putString("strokeIdsJson", ids.toString()); String boundsJson = ""; float[] bounds = selectionBounds();
    if (bounds != null) try { JSONObject value = new JSONObject(); value.put("x", bounds[0]); value.put("y", bounds[1]); value.put("width", bounds[2] - bounds[0]); value.put("height", bounds[3] - bounds[1]); boundsJson = value.toString(); } catch (Exception ignored) { }
    body.putString("boundsJson", boundsJson); emit("topSelectionChanged", body);
  }

  private void reconcileSelection() {
    Set<String> available = new HashSet<>(); for (Stroke stroke : strokes) available.add(stroke.id);
    if (selectedStrokeIds.retainAll(available)) emitSelection();
  }

  private void clear() { strokes.clear(); redo.clear(); selectedStrokeIds.clear(); current = null; inkView.cancelUnfinishedStrokes(); inkStroke = null; invalidate(); emitStrokes(); emitSelection(); }
  private void undo() { if (!strokes.isEmpty()) { redo.add(strokes.remove(strokes.size() - 1)); reconcileSelection(); invalidate(); emitStrokes(); } }
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
