package com.posprinter.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.posprinter.DiscoveryCallbacks;
import com.posprinter.constants.DiscoveryStatus;
import com.posprinter.interfaces.IDevice;
import com.posprinter.interfaces.IDeviceDiscoverer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothDiscoverer implements IDeviceDiscoverer {
  private DiscoveryCallbacks callbacks;
  private Map<String, BluetoothDevice> bluetoothDeviceDict = new HashMap<>();
  private BluetoothDiscoverer ref = this;
  private Context context;
  private BluetoothAdapter bluetoothAdapter;
  int status = DiscoveryStatus.NOT_READY;

  public BluetoothDiscoverer(Context activity) {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    this.context = activity;
  }

  @Override
  public String getName() {
    return "Bluetooth";
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setCallbacks(DiscoveryCallbacks callbacks) {
    this.callbacks = callbacks;
  }

  @Override
  public List<IDevice> getDevices() {
    stopDiscovery();

    List<IDevice> devicesForPrinting = new ArrayList<IDevice>();

    if (bluetoothAdapter == null) {
      return devicesForPrinting;
    }

    Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
    if (pairedDevice.size() > 0) {
      for (BluetoothDevice device : pairedDevice) {
        String deviceIdentifier = BluetoothUtils.getIdentifier(device);
        bluetoothDeviceDict.put(deviceIdentifier, device);
      }
    }

    if (bluetoothDeviceDict.keySet().size() > 0) {
      for (String deviceIdentifier : bluetoothDeviceDict.keySet()) {
        devicesForPrinting.add(convertToBluetoothDevice(bluetoothDeviceDict.get(deviceIdentifier)));
      }
    }

    return devicesForPrinting;
  }

  @Override
  public void startScanningDevices() {

  }

  @Override
  public void stopScanningDevices() {

  }

  private void stopDiscovery() {
    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }
  }

  private IDevice convertToBluetoothDevice(BluetoothDevice device) {
    return new BluetoothDeviceForPrinting(device, context);
  }
}
