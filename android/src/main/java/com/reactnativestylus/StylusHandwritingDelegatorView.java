package com.reactnativestylus;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.PointerIcon;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

final class StylusHandwritingDelegatorView extends TextView {
  private String delegationId;

  StylusHandwritingDelegatorView(Context context) {
    super(context);
    setPadding(20, 14, 20, 14);
    setTextColor(Color.DKGRAY);
    setBackgroundColor(Color.WHITE);
    setFocusable(true);
    setClickable(true);
    setContentDescription("Handwriting input");
    if (Build.VERSION.SDK_INT >= 24) setPointerIcon(PointerIcon.getSystemIcon(context, PointerIcon.TYPE_TEXT));
    setOnClickListener(view -> activateDelegate(false));
    if (Build.VERSION.SDK_INT >= 34) setHandwritingDelegatorCallback(() -> activateDelegate(true));
  }

  void setDelegationId(String value) { delegationId = value; }
  void setAllowedDelegatePackage(String value) { if (Build.VERSION.SDK_INT >= 34) setAllowedHandwritingDelegatePackage(value); }

  private void activateDelegate(boolean handwriting) {
    StylusHandwritingInputView delegate = StylusHandwritingInputView.findDelegate(delegationId);
    boolean found = delegate != null;
    if (delegate != null) {
      delegate.setVisibility(VISIBLE);
      delegate.requestFocus();
      InputMethodManager input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      input.showSoftInput(delegate, InputMethodManager.SHOW_IMPLICIT);
    }
    WritableMap event = Arguments.createMap();
    event.putBoolean("supported", Build.VERSION.SDK_INT >= 34);
    event.putBoolean("delegateFound", found);
    event.putBoolean("handwriting", handwriting);
    emit("topDelegationActivated", event);
  }

  @SuppressWarnings("deprecation") private void emit(String name, WritableMap event) {
    ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), name, event);
  }
}
