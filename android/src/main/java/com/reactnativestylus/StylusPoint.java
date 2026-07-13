package com.reactnativestylus;

import android.view.MotionEvent;
import org.json.JSONObject;

final class StylusPoint {
  final float x, y, pressure, tilt, orientation, distance, size, touchMajor, touchMinor;
  final long timestamp;
  final int pointerId, toolType, buttons;
  final boolean predicted, historical;

  StylusPoint(JSONObject value) {
    x = (float) value.optDouble("x"); y = (float) value.optDouble("y");
    pressure = (float) value.optDouble("pressure"); tilt = (float) value.optDouble("tilt");
    orientation = (float) value.optDouble("orientation"); distance = (float) value.optDouble("distance");
    size = (float) value.optDouble("size"); touchMajor = (float) value.optDouble("touchMajor");
    touchMinor = (float) value.optDouble("touchMinor"); timestamp = value.optLong("timestamp");
    pointerId = value.optInt("pointerId"); buttons = value.optInt("buttons");
    toolType = toolValue(value.optString("toolType")); predicted = value.optBoolean("predicted");
    historical = value.optBoolean("historical");
  }

  StylusPoint(MotionEvent event, int pointer, int history, boolean predicted) {
    historical = history >= 0;
    this.predicted = predicted;
    x = historical ? event.getHistoricalX(pointer, history) : event.getX(pointer);
    y = historical ? event.getHistoricalY(pointer, history) : event.getY(pointer);
    pressure = historical ? event.getHistoricalPressure(pointer, history) : event.getPressure(pointer);
    size = historical ? event.getHistoricalSize(pointer, history) : event.getSize(pointer);
    touchMajor = historical ? event.getHistoricalTouchMajor(pointer, history) : event.getTouchMajor(pointer);
    touchMinor = historical ? event.getHistoricalTouchMinor(pointer, history) : event.getTouchMinor(pointer);
    tilt = axis(event, MotionEvent.AXIS_TILT, pointer, history);
    orientation = axis(event, MotionEvent.AXIS_ORIENTATION, pointer, history);
    distance = axis(event, MotionEvent.AXIS_DISTANCE, pointer, history);
    timestamp = historical ? event.getHistoricalEventTime(history) : event.getEventTime();
    pointerId = event.getPointerId(pointer);
    toolType = event.getToolType(pointer);
    buttons = event.getButtonState();
  }

  StylusPoint(StylusPoint source, float x, float y) {
    this.x = x; this.y = y; pressure = source.pressure; tilt = source.tilt;
    orientation = source.orientation; distance = source.distance; size = source.size;
    touchMajor = source.touchMajor; touchMinor = source.touchMinor; timestamp = source.timestamp;
    pointerId = source.pointerId; toolType = source.toolType; buttons = source.buttons;
    predicted = source.predicted; historical = source.historical;
  }

  private static float axis(MotionEvent e, int axis, int pointer, int history) {
    return history >= 0 ? e.getHistoricalAxisValue(axis, pointer, history) : e.getAxisValue(axis, pointer);
  }

  JSONObject json() {
    JSONObject value = new JSONObject();
    try {
      value.put("x", x); value.put("y", y); value.put("pressure", pressure);
      value.put("tilt", tilt); value.put("orientation", orientation); value.put("distance", distance);
      value.put("size", size); value.put("touchMajor", touchMajor); value.put("touchMinor", touchMinor);
      value.put("timestamp", timestamp); value.put("pointerId", pointerId);
      value.put("toolType", toolName(toolType)); value.put("buttons", buttons);
      value.put("predicted", predicted); value.put("historical", historical);
    } catch (Exception ignored) { }
    return value;
  }

  static String toolName(int type) {
    if (type == MotionEvent.TOOL_TYPE_STYLUS) return "stylus";
    if (type == MotionEvent.TOOL_TYPE_ERASER) return "eraser";
    if (type == MotionEvent.TOOL_TYPE_FINGER) return "finger";
    if (type == MotionEvent.TOOL_TYPE_MOUSE) return "mouse";
    return "unknown";
  }

  private static int toolValue(String type) {
    if ("stylus".equals(type)) return MotionEvent.TOOL_TYPE_STYLUS;
    if ("eraser".equals(type)) return MotionEvent.TOOL_TYPE_ERASER;
    if ("finger".equals(type)) return MotionEvent.TOOL_TYPE_FINGER;
    if ("mouse".equals(type)) return MotionEvent.TOOL_TYPE_MOUSE;
    return MotionEvent.TOOL_TYPE_UNKNOWN;
  }
}
