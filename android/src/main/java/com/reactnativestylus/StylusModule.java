package com.reactnativestylus;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Build;
import android.view.InputDevice;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.annotations.ReactModule;
import org.json.JSONArray;
import org.json.JSONObject;

@ReactModule(name = StylusModule.NAME)
public final class StylusModule extends NativeStylusModuleSpec implements InputManager.InputDeviceListener {
  public static final String NAME = "StylusModule";
  private final InputManager inputManager;
  private boolean observing;

  public StylusModule(ReactApplicationContext context) {
    super(context);
    inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
    inputManager.registerInputDeviceListener(this, null);
    observing = true;
  }

  @NonNull @Override public String getName() { return NAME; }

  @Override public void getStylusDevices(Promise promise) { promise.resolve(devicesJson().toString()); }

  @Override public void isStylusSupported(Promise promise) { promise.resolve(devicesJson().length() > 0); }

  @Override public void getCapabilities(Promise promise) {
    try {
      JSONObject result = new JSONObject();
      JSONArray devices = devicesJson();
      result.put("androidApiLevel", Build.VERSION.SDK_INT);
      result.put("stylusSupported", devices.length() > 0);
      result.put("motionPredictionSupported", Build.VERSION.SDK_INT >= 19);
      result.put("canceledFlagSupported", Build.VERSION.SDK_INT >= 33);
      result.put("lowLatencyFrontBufferSupported", Build.VERSION.SDK_INT >= 29);
      result.put("devices", devices);
      promise.resolve(result.toString());
    } catch (Exception error) { promise.reject("stylus_capabilities", error); }
  }

  @Override public void invalidate() {
    if (observing) inputManager.unregisterInputDeviceListener(this);
    observing = false;
    super.invalidate();
  }

  @Override public void onInputDeviceAdded(int deviceId) { emitOnDevicesChanged(devicesJson().toString()); }
  @Override public void onInputDeviceRemoved(int deviceId) { emitOnDevicesChanged(devicesJson().toString()); }
  @Override public void onInputDeviceChanged(int deviceId) { emitOnDevicesChanged(devicesJson().toString()); }

  private JSONArray devicesJson() {
    JSONArray result = new JSONArray();
    for (int id : inputManager.getInputDeviceIds()) {
      InputDevice device = inputManager.getInputDevice(id);
      if (device == null || !device.supportsSource(InputDevice.SOURCE_STYLUS)) continue;
      JSONObject item = new JSONObject();
      try {
        item.put("id", id);
        item.put("name", device.getName());
        item.put("descriptor", device.getDescriptor());
        item.put("vendorId", Build.VERSION.SDK_INT >= 19 ? device.getVendorId() : 0);
        item.put("productId", Build.VERSION.SDK_INT >= 19 ? device.getProductId() : 0);
        item.put("external", device.isExternal());
        item.put("sources", device.getSources());
        item.put("hasPressure", hasAxis(device, MotionEvent.AXIS_PRESSURE));
        item.put("hasTilt", hasAxis(device, MotionEvent.AXIS_TILT));
        item.put("hasOrientation", hasAxis(device, MotionEvent.AXIS_ORIENTATION));
        item.put("hasDistance", hasAxis(device, MotionEvent.AXIS_DISTANCE));
        item.put("hasHover", device.supportsSource(InputDevice.SOURCE_STYLUS));
        item.put("hasEraser", true);
        result.put(item);
      } catch (Exception ignored) { }
    }
    return result;
  }

  private static boolean hasAxis(InputDevice device, int axis) {
    return device.getMotionRange(axis, InputDevice.SOURCE_STYLUS) != null;
  }
}
