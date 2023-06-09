package com.posprinter.interfaces;

import com.posprinter.DeviceConnectionStatusCallbacks;

public interface IPrintingService {
  public byte[] read();

  public int write(byte[] data);

  public void setCallbacks(DeviceConnectionStatusCallbacks callbacks);

  public int getStatus();
}
