package com.posprinter.interfaces;

import com.posprinter.DeviceConnectionStatusCallbacks;

public interface IDevice {
  public abstract String getIdentifier();

  public abstract String getDisplayName();

  public abstract IPrintingService startService(DeviceConnectionStatusCallbacks callbacks) throws Exception;
}
