package com.posprinter;

import com.posprinter.interfaces.IDevice;
import com.posprinter.interfaces.IPrintingService;

public abstract class DeviceConnectionStatusCallbacks {
  public abstract void onStatusChanged(IDevice device, IPrintingService service);
}
