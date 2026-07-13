package com.reactnativestylus;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Build;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

final class StylusDropZoneView extends FrameLayout {
  private String payload = "";
  private boolean draggable;
  private boolean dropEnabled = true;

  StylusDropZoneView(Context context) {
    super(context);
    setOnLongClickListener(view -> startDrag());
    setOnDragListener((view, event) -> handleDrag(event));
  }

  void setPayload(String value) { payload = value == null ? "" : value; }
  void setDraggable(boolean value) { draggable = value; setLongClickable(value); }
  void setDropEnabled(boolean value) { dropEnabled = value; }

  private boolean startDrag() {
    if (!draggable) return false;
    ClipData data = ClipData.newPlainText("react-native-stylus", payload);
    DragShadowBuilder shadow = new DragShadowBuilder(this);
    if (Build.VERSION.SDK_INT >= 24) startDragAndDrop(data, shadow, null, View.DRAG_FLAG_GLOBAL);
    else startDrag(data, shadow, null, 0);
    emit("start", 0, 0, payload, "text/plain");
    return true;
  }

  private boolean handleDrag(DragEvent event) {
    if (!dropEnabled && event.getAction() != DragEvent.ACTION_DRAG_ENDED) return false;
    String action;
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED: action = "started"; break;
      case DragEvent.ACTION_DRAG_ENTERED: action = "entered"; break;
      case DragEvent.ACTION_DRAG_LOCATION: action = "location"; break;
      case DragEvent.ACTION_DRAG_EXITED: action = "exited"; break;
      case DragEvent.ACTION_DROP: action = "drop"; break;
      default: action = "ended";
    }
    ClipData data = event.getClipData();
    String text = data != null && data.getItemCount() > 0 ? String.valueOf(data.getItemAt(0).coerceToText(getContext())) : "";
    ClipDescription description = event.getClipDescription();
    String mime = description != null && description.getMimeTypeCount() > 0 ? description.getMimeType(0) : "";
    emit(action, event.getX(), event.getY(), text, mime);
    return true;
  }

  @SuppressWarnings("deprecation") private void emit(String action, float x, float y, String value, String mime) {
    WritableMap event = Arguments.createMap();
    event.putString("action", action); event.putDouble("x", x); event.putDouble("y", y);
    event.putString("payload", value); event.putString("mimeType", mime);
    ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topStylusDrag", event);
  }
}
