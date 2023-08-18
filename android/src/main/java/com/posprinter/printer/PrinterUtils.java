package com.posprinter.printer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.posprinter.interfaces.IDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrinterUtils {
  public static void register(Context activity, BroadcastReceiver receiver, IntentFilter intentFilter) {
    try {
      activity.unregisterReceiver(receiver);
    } catch (Exception e) {
      e.printStackTrace();
    }

    activity.registerReceiver(receiver, intentFilter);
  }

  public static void requestAccessLocationPermission(Activity activity) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
      int permissionCheck = ContextCompat.checkSelfPermission(activity, "Manifest.permission.ACCESS_FINE_LOCATION");
      permissionCheck += ContextCompat.checkSelfPermission(activity, "Manifest.permission.ACCESS_COARSE_LOCATION");
      permissionCheck += ContextCompat.checkSelfPermission(activity, "Manifest.permission.BLUETOOTH_SCAN");
      permissionCheck += ContextCompat.checkSelfPermission(activity, "Manifest.permission.BLUETOOTH_CONNECT");
      if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        String[] permission = new String[]{
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.BLUETOOTH_CONNECT
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          activity.requestPermissions(permission, 1001);
        }
      }
    }
  }

  public static Map<String, IDevice> toMap(List<IDevice> devices) {
    Map<String, IDevice> map = new HashMap<>();

    for (IDevice device : devices) {
      map.put(device.getDisplayName(), device);
    }

    return map;
  }
}
