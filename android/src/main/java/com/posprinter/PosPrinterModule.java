package com.posprinter;

import androidx.annotation.NonNull;

import com.android.print.sdk.Barcode;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.posprinter.bluetooth.BluetoothDiscoverer;
import com.posprinter.constants.DeviceConnectionStatus;
import com.posprinter.interfaces.IDevice;
import com.posprinter.interfaces.IDeviceDiscoverer;
import com.posprinter.interfaces.IPrintingService;
import com.posprinter.printer.Printer;
import com.posprinter.printer.PrinterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReactModule(name = PosPrinterModule.NAME)
public class PosPrinterModule extends ReactContextBaseJavaModule {
  public static final String NAME = "PosPrinter";

  private List<IDeviceDiscoverer> deviceDiscoverers = new ArrayList<>();
  private Map<String, IDevice> deviceMap = new HashMap<>();
  private Map<String, IDevice> bondedDeviceMap = new HashMap<>();
  private IPrintingService selectedPrinterService = null;
  private Printer printer = new Printer();

  public PosPrinterModule(ReactApplicationContext reactContext) {
    super(reactContext);

    deviceDiscoverers.add(new BluetoothDiscoverer(this.getReactApplicationContext()));
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void init(Promise promise) {
    try {
      PrinterUtils.requestAccessLocationPermission(this.getCurrentActivity());
      promise.resolve(null);
    } catch (Exception e) {
      e.printStackTrace();
      promise.reject("FAILED TO INITIALIZE", e.getMessage());
    }
  }

  @ReactMethod
  public void getDevices(Promise promise) {
    Map<String, IDevice> bondedDeviceMap = new HashMap<>();

    for (IDeviceDiscoverer discoverer : this.deviceDiscoverers) {
      for (IDevice device : discoverer.getDevices()) {
        if (!bondedDeviceMap.containsKey(device.getIdentifier()))
          bondedDeviceMap.put(device.getIdentifier(), device);
      }
    }

    WritableArray arrDevices = Arguments.createArray();

    for (IDevice device : bondedDeviceMap.values()) {
      arrDevices.pushMap(fromDevice(device));
    }

    promise.resolve(arrDevices);
  }

  @ReactMethod
  public void scanDevices() {
    DiscoveryCallbacks callbacks = new DiscoveryCallbacks() {
      @Override
      public void onStatusChanged(IDeviceDiscoverer discoverer) {
      }

      @Override
      public void onDeviceDiscovered(IDevice device) {
        if (device.getDisplayName() != null) {
          deviceMap.put(device.getIdentifier(), device);
          sendDevicesEvent();
        }
      }
    };

    for (IDeviceDiscoverer discoverer : this.deviceDiscoverers) {
      discoverer.setCallbacks(callbacks);
      discoverer.startScanningDevices();
    }
  }

  @ReactMethod
  public void stopScanningDevices() {
    for (IDeviceDiscoverer discoverer : this.deviceDiscoverers) {
      discoverer.stopScanningDevices();
    }
  }

  @ReactMethod
  public void connectDevice(String identifier, int timeout, final Promise promise) {
    if (this.bondedDeviceMap.keySet().size() < 1) {
      for (IDeviceDiscoverer discoverer : this.deviceDiscoverers) {
        for (IDevice device : discoverer.getDevices()) {
          if (!this.bondedDeviceMap.containsKey(device.getIdentifier()))
            this.bondedDeviceMap.put(device.getIdentifier(), device);
        }
      }
    }

    if (!this.bondedDeviceMap.containsKey(identifier)) {
      promise.reject("NO DEVICE FOUND", "Unable to find the device" + identifier);
      return;
    }

    IDevice device = this.bondedDeviceMap.get(identifier);
    IPrintingService service = null;
    selectedPrinterService = null;

    try {
      service = device.startService(new DeviceConnectionStatusCallbacks() {
        @Override
        public void onStatusChanged(IDevice device, IPrintingService service) {
          if (service.getStatus() == DeviceConnectionStatus.CONNECTED) {
            selectedPrinterService = service;
            promise.resolve(fromDevice(device));
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      promise.reject("FAILED TO CONNECT DEVICE", e.getMessage());
    }
  }

  @ReactMethod
  public void addCommands(ReadableArray array, Promise promise) {
    if (selectedPrinterService == null) {
      promise.reject("PRINTER IS UNAVAILABLE", "Printer is unavailable");
      return;
    }

    if (selectedPrinterService.getStatus() != DeviceConnectionStatus.CONNECTED) {
      promise.reject("PRINTER IS NOT CONNECTED", "Printer is not connected");
      return;
    }

    printer.setService(selectedPrinterService);
    printer.init();

    if (!printer.isConnected()) {
      promise.reject("PRINTER IS NOT CONNECTED", "Printer is not connected");
      return;
    }

    for (int i = 0; i < array.size(); i++) {
      ReadableMap map = array.getMap(i);
      String type = map.getString("type");

      switch (type) {
        case "setFont":
          int width = map.getInt("width");
          int height = map.getInt("height");
          int bold = map.getInt("bold");
          int underline = map.getInt("underline");
          printer.setFont(width, height, bold, underline);
          break;
        case "setPrinter":
          int command = map.getInt("command");
          int value = map.getInt("value");
          printer.setPrinter(command, value);
          break;
        case "printText":
          String text = map.getString("text");
          printer.printText(text);
          break;
        case "printImageFromStorage":
          String url = map.getString("url");
          int dstWidth = map.getInt("width");
          int dstHeight = map.getInt("height");
          printer.printImageFromStorage(url, dstWidth, dstHeight);
          break;
        case "setCharacterMultiple":
          int x1 = map.getInt("x");
          int y1 = map.getInt("y");
          printer.setCharacterMultiple(x1, y1);
          break;
        case "setLeftMargin":
          int x2 = map.getInt("x");
          int y2 = map.getInt("y");
          printer.setLeftMargin(x2, y2);
          break;
        case "printBarCode":
          Byte barcodeType = (byte) map.getInt("barcodeType");
          int param1 = map.getInt("param1");
          int param2 = map.getInt("param2");
          int param3 = map.getInt("param3");
          String content = map.getString("content");
          Barcode barcode = new Barcode(barcodeType, param1, param2, param3, content);
          printer.printBarCode(barcode);
          break;
      }
    }

    promise.resolve("DONE");
  }

  private WritableMap fromDevice(IDevice device) {
    WritableMap map = Arguments.createMap();

    map.putString("name", device.getDisplayName());
    map.putString("identifier", device.getIdentifier());

    return map;
  }

  private void sendDevicesEvent() {
    WritableArray array = Arguments.createArray();

    for (IDevice device : deviceMap.values()) {
      array.pushMap(fromDevice(device));
    }

    this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("available_bluetooth_devices", array);
  }
}
