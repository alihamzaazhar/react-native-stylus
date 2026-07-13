package com.reactnativestylus;

import android.graphics.Color;
import android.text.InputType;
import androidx.annotation.NonNull;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewManagerDelegate;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.viewmanagers.StylusHandwritingInputManagerDelegate;
import com.facebook.react.viewmanagers.StylusHandwritingInputManagerInterface;
import java.util.Map;

public final class StylusHandwritingInputManager extends SimpleViewManager<StylusHandwritingInputView> implements StylusHandwritingInputManagerInterface<StylusHandwritingInputView> {
  public static final String NAME = "StylusHandwritingInput";
  private final ViewManagerDelegate<StylusHandwritingInputView> delegate = new StylusHandwritingInputManagerDelegate<>(this);
  @NonNull @Override public String getName() { return NAME; }
  @Override protected ViewManagerDelegate<StylusHandwritingInputView> getDelegate() { return delegate; }
  @NonNull @Override protected StylusHandwritingInputView createViewInstance(@NonNull ThemedReactContext context) { return new StylusHandwritingInputView(context); }

  @ReactProp(name="text") @Override public void setText(StylusHandwritingInputView v, String x) { v.setTextValue(x); }
  @ReactProp(name="hint") @Override public void setHint(StylusHandwritingInputView v, String x) { v.setHint(x); }
  @ReactProp(name="textColor") @Override public void setTextColor(StylusHandwritingInputView v, String x) { if (x != null) v.setTextColor(Color.parseColor(x)); }
  @ReactProp(name="hintColor") @Override public void setHintColor(StylusHandwritingInputView v, String x) { if (x != null) v.setHintTextColor(Color.parseColor(x)); }
  @ReactProp(name="textSize", defaultFloat=16f) @Override public void setTextSize(StylusHandwritingInputView v, float x) { v.setTextSize(x); }
  @ReactProp(name="multiline", defaultBoolean=false) @Override public void setMultiline(StylusHandwritingInputView v, boolean x) { v.setSingleLine(!x); v.setInputType(InputType.TYPE_CLASS_TEXT | (x ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0)); }
  @ReactProp(name="autoHandwritingEnabled", defaultBoolean=true) @Override public void setAutoHandwritingEnabled(StylusHandwritingInputView v, boolean x) { v.setAutoHandwriting(x); }
  @ReactProp(name="handwritingDelegate", defaultBoolean=false) @Override public void setHandwritingDelegate(StylusHandwritingInputView v, boolean x) { v.setHandwritingDelegateValue(x); }
  @ReactProp(name="handwritingBoundsLeft", defaultFloat=0f) @Override public void setHandwritingBoundsLeft(StylusHandwritingInputView v, float x) { v.setBoundsLeft(x); }
  @ReactProp(name="handwritingBoundsTop", defaultFloat=0f) @Override public void setHandwritingBoundsTop(StylusHandwritingInputView v, float x) { v.setBoundsTop(x); }
  @ReactProp(name="handwritingBoundsRight", defaultFloat=0f) @Override public void setHandwritingBoundsRight(StylusHandwritingInputView v, float x) { v.setBoundsRight(x); }
  @ReactProp(name="handwritingBoundsBottom", defaultFloat=0f) @Override public void setHandwritingBoundsBottom(StylusHandwritingInputView v, float x) { v.setBoundsBottom(x); }
  @ReactProp(name="selectAllOnFocus", defaultBoolean=false) @Override public void setSelectAllOnFocus(StylusHandwritingInputView v, boolean x) { v.setSelectAllOnFocus(x); }
  @ReactProp(name="hoverFocusEnabled", defaultBoolean=false) @Override public void setHoverFocusEnabled(StylusHandwritingInputView v, boolean x) { v.setHoverFocusEnabled(x); }

  @Override public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.<String, Object>builder()
      .put("topTextChanged", MapBuilder.of("registrationName", "onTextChanged"))
      .put("topHandwritingStatus", MapBuilder.of("registrationName", "onHandwritingStatus")).build();
  }
}
