package com.example.backgroundservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

public class BluetoothHelper {

    private static final String PREFS_NAME = "BluetoothPrefs";
    private static final String DEVICE_NAME_KEY = "DeviceName";
    private static final String DEVICE_ADDRESS_KEY = "DeviceAddress";

    private final Context context;

    public BluetoothHelper(Context context) {
        this.context = context;
    }

    public void saveDeviceToPrefs(String deviceName, String deviceAddress) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DEVICE_NAME_KEY, deviceName);
        editor.putString(DEVICE_ADDRESS_KEY, deviceAddress);
        editor.apply();
    }

    public BluetoothDevice getSavedDeviceFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceName = prefs.getString(DEVICE_NAME_KEY, "");
        String deviceAddress = prefs.getString(DEVICE_ADDRESS_KEY, "");

        if (!deviceName.isEmpty() && !deviceAddress.isEmpty()) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            return bluetoothAdapter.getRemoteDevice(deviceAddress);
        }

        return null;
    }
}

