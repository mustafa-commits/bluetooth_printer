package com.example.backgroundservice;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PrintBluetooth extends AppCompatActivity {

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    byte FONT_TYPE;

    public static String printer_id;

    BluetoothHelper bluetoothHelper;

    public PrintBluetooth() {
    }

    public void findBT(Context context, BluetoothAdapter bluetoothAdapter, ArrayList<BluetoothDevice> deviceList, ArrayAdapter<String> adapter) {

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled, prompt the user to enable it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return;
            }

// Get a list of connected devices for a specific profile (A2DP in this case)
            mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.A2DP) {
                        List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();

                        for (BluetoothDevice device : connectedDevices) {
                            // Check if the connected device is the specific printer device
                            if (device.getAddress().equals("00:12:5B:00:03:79")) {
                                // The device is connected to the printer
                                Log.d("TAG", "Device is connected to the printer");

                            }
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    try {
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            Toast.makeText(getApplicationContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                        }
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            startActivityForResult(enableBluetooth, 0);
                        }
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                Log.d("TAG", "findBT: " + device.getName());
                                if (device.getName().equals(printer_id)) {
                                    mmDevice = device;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, BluetoothProfile.A2DP);

        }
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.d("TAG", "findBT: " + device.getName());
                    if (device.getName().equals(printer_id)) {
                        mmDevice = device;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void findBT2() {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.d("TAG", "findBT: " + device.getName());
                    if (device.getName().equals(printer_id)) {
                        mmDevice = device;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Context context;

    // tries to open a connection to the bluetooth printer device
    public void openBT(Context context, String address) {

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request the missing permissions.
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    return;
                }
                ((Activity) context).startActivityForResult(enableBtIntent, 1);
            }

// Get a list of connected devices for a specific profile (A2DP in this case)
//            mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
//                @Override
//                public void onServiceConnected(int profile, BluetoothProfile proxy) {
//
//                    if (profile == BluetoothProfile.A2DP) {
//                        List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();
//
//                        for (BluetoothDevice device : connectedDevices) {
//                            // Check if the connected device is the specific printer device
//                            if (device.getAddress().equals(address)) {
//                                Toast.makeText(context,"connected",Toast.LENGTH_SHORT).show();
//                            }
//                            Log.e("TAG", "not equal: "+device.getAddress()+" address "+address);
//
//                        }
//                    }
//                }
//
//                @Override
//                public void onServiceDisconnected(int profile) {
//                    Toast.makeText(context, "onServiceDisconnected", Toast.LENGTH_LONG).show();
//                    try {
//                        int connectionState = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
//
//                        if (connectionState != BluetoothProfile.STATE_CONNECTED && !isConnected()) {
//                            // Standard SerialPortService ID
//                            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//
//                            if (mmDevice != null) {
//                                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
//                                mmSocket.connect();
//                                mmOutputStream = mmSocket.getOutputStream();
//                                mmInputStream = mmSocket.getInputStream();
//                                beginListenForData();
//                            } else Log.e("TAG", "Bluetooth device is null1");
//                        }
//                    } catch (Exception e) {
//                        Toast.makeText(context,"not connected",Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
//                }
//            }, BluetoothProfile.A2DP);
//        }
//        try {
//            int connectionState = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
//
//            if (connectionState != BluetoothProfile.STATE_CONNECTED) {
//                // Standard SerialPortService ID
//                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//
//                if (mmDevice != null) {
//                    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
//                    mmSocket.connect();
//                    mmOutputStream = mmSocket.getOutputStream();
//                    mmInputStream = mmSocket.getInputStream();
//                    beginListenForData();
//                } else Log.e("TAG", "Bluetooth device is null2");
//            }
//        } catch (Exception e) {
//            Log.d("TAG", "onCreate: not connected");
//            e.printStackTrace();
//        }

        }
    }

    public void openBT2(String address) {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmDevice = mBluetoothAdapter.getRemoteDevice(address);
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            beginListenForData();
            Toast.makeText(context, "Connected to device", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to connect to device", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void printQrCode(Bitmap qRBit) {
        try {
            PrintPic printPic1 = PrintPic.getInstance();
            printPic1.init(qRBit);
            byte[] bitmapdata2 = printPic1.printDraw();
            mmOutputStream.write(bitmapdata2);
            byte[] command = new byte[]{0x1B, 0x4A, (byte) 5};
            mmOutputStream.write(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (mmSocket != null) {
            return mmSocket.isConnected();
        }
        return false;
    }


    /*
     * after opening a connection to bluetooth printer device,
     * we have to listen and check if a data were sent to be printed.
     */
    public void beginListenForData() {
        try {
            final Handler handler = new Handler();
            // this is the ASCII code for a newline character
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );
                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                        readBufferPosition = 0;
                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
//                                                myLabel.setText(data);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            Log.e("TAG", "Error reading data. Reconnecting...");
                            try {
                                reconnect();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            //stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    this will update data printer name in ModelUser
    // close the connection to bluetooth printer.
    public void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reconnect() throws IOException {
        closeBT(); // Close the existing connection

        // Attempt to reconnect (you may want to add a delay here if needed)

        BluetoothDevice device = bluetoothHelper.getSavedDeviceFromPrefs();
        openBT(getApplicationContext(), device.getAddress());
    }

    public static Bitmap drawText(String text, int textWidth, int textSize) {

        //text dimension
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        // textWidth = (int) paint.measureText(text);
        StaticLayout mTextLayout = new StaticLayout(text, paint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        // bitmap setup
        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);


        //background
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        c.drawPaint(p);

        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();
        return b;


    }

}