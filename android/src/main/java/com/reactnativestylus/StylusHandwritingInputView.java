package com.reactnativestylus;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.widget.EditText;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

final class StylusHandwritingInputView extends EditText {
  private boolean updatingText;
  private boolean hoverFocusEnabled;
  private float boundsLeft, boundsTop, boundsRight, boundsBottom;

  StylusHandwritingInputView(Context context) {
    super(context);
    setPadding(20, 14, 20, 14);
    setBackgroundColor(Color.WHITE);
    if (Build.VERSION.SDK_INT >= 24) setPointerIcon(PointerIcon.getSystemIcon(context, PointerIcon.TYPE_TEXT));
    addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!updatingText) emitText(s.toString());
      }
      @Override public void afterTextChanged(Editable s) { }
    });
    setOnFocusChangeListener((view, focused) -> emitStatus(focused));
  }

  void setTextValue(String value) {
    String next = value == null ? "" : value;
    if (next.contentEquals(getText())) return;
    updatingText = true; setText(next); setSelection(next.length()); updatingText = false;
  }

  void setAutoHandwriting(boolean enabled) { if (Build.VERSION.SDK_INT >= 33) setAutoHandwritingEnabled(enabled); }
  void setHandwritingDelegateValue(boolean enabled) { if (Build.VERSION.SDK_INT >= 34) setIsHandwritingDelegate(enabled); }
  void setHoverFocusEnabled(boolean enabled) { hoverFocusEnabled = enabled; }
  void setBoundsLeft(float value) { boundsLeft = value; applyBounds(); }
  void setBoundsTop(float value) { boundsTop = value; applyBounds(); }
  void setBoundsRight(float value) { boundsRight = value; applyBounds(); }
  void setBoundsBottom(float value) { boundsBottom = value; applyBounds(); }

  private void applyBounds() {
    if (Build.VERSION.SDK_INT >= 34) {
      float density = getResources().getDisplayMetrics().density;
      setHandwritingBoundsOffsets(boundsLeft * density, boundsTop * density, boundsRight * density, boundsBottom * density);
    }
  }

  @Override public boolean onHoverEvent(MotionEvent event) {
    if (hoverFocusEnabled && event.isFromSource(InputDevice.SOURCE_STYLUS) && event.getActionMasked() == MotionEvent.ACTION_HOVER_ENTER) requestFocus();
    return super.onHoverEvent(event);
  }

  @SuppressWarnings("deprecation") private void emitText(String text) {
    WritableMap event = Arguments.createMap(); event.putString("text", text);
    ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topTextChanged", event);
  }

  @SuppressWarnings("deprecation") private void emitStatus(boolean focused) {
    WritableMap event = Arguments.createMap(); event.putBoolean("supported", Build.VERSION.SDK_INT >= 34); event.putBoolean("focused", focused);
    ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topHandwritingStatus", event);
  }
}
