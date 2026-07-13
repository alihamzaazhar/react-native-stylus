package com.reactnativestylus;

import android.graphics.Color;
import androidx.annotation.NonNull;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewManagerDelegate;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.viewmanagers.StylusHandwritingDelegatorManagerDelegate;
import com.facebook.react.viewmanagers.StylusHandwritingDelegatorManagerInterface;
import java.util.Map;

public final class StylusHandwritingDelegatorManager extends SimpleViewManager<StylusHandwritingDelegatorView> implements StylusHandwritingDelegatorManagerInterface<StylusHandwritingDelegatorView> {
  public static final String NAME = "StylusHandwritingDelegator";
  private final ViewManagerDelegate<StylusHandwritingDelegatorView> delegate = new StylusHandwritingDelegatorManagerDelegate<>(this);
  @NonNull @Override public String getName() { return NAME; }
  @Override protected ViewManagerDelegate<StylusHandwritingDelegatorView> getDelegate() { return delegate; }
  @NonNull @Override protected StylusHandwritingDelegatorView createViewInstance(@NonNull ThemedReactContext context) { return new StylusHandwritingDelegatorView(context); }
  @ReactProp(name="label") @Override public void setLabel(StylusHandwritingDelegatorView v, String x) { v.setText(x); }
  @ReactProp(name="textColor") @Override public void setTextColor(StylusHandwritingDelegatorView v, String x) { if (x != null) v.setTextColor(Color.parseColor(x)); }
  @ReactProp(name="textSize", defaultFloat=16f) @Override public void setTextSize(StylusHandwritingDelegatorView v, float x) { v.setTextSize(x); }
  @ReactProp(name="delegationId") @Override public void setDelegationId(StylusHandwritingDelegatorView v, String x) { v.setDelegationId(x); }
  @ReactProp(name="allowedDelegatePackage") @Override public void setAllowedDelegatePackage(StylusHandwritingDelegatorView v, String x) { v.setAllowedDelegatePackage(x); }
  @Override public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of("topDelegationActivated", MapBuilder.of("registrationName", "onDelegationActivated"));
  }
}
