package com.posprinter;

import com.posprinter.interfaces.IDevice;
import com.posprinter.interfaces.IDeviceDiscoverer;

public abstract class DiscoveryCallbacks {
  public abstract void onStatusChanged(IDeviceDiscoverer discoverer);

  public abstract void onDeviceDiscovered(IDevice device);
}
