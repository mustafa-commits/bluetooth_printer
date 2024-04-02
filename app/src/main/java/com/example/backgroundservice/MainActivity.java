package com.example.backgroundservice;

import static com.example.backgroundservice.PrintPic.mergeBitmapsVertically;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothHelper bluetoothHelper;

    PrintBluetooth printBT = new PrintBluetooth();

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private ArrayAdapter<String> mAdapter;
    ListView deviceListView;
    Button showDevicesButton;
    Button printbtn;

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothHelper = new BluetoothHelper(this);
        //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)); intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent);
        deviceListView = findViewById(R.id.deviceListView);
        showDevicesButton = findViewById(R.id.showDevicesButton);
        printbtn = findViewById(R.id.printButton);
        editText = findViewById(R.id.textField);
        showDevicesButton.setOnClickListener(v -> showDevices());

        // Register BroadcastReceiver for Bluetooth state changes
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setAdapter(mAdapter);


        // PrintBluetooth.printer_id=printerID.getText().toString();
        printBT.context = this;
        printBT.findBT(this, mBluetoothAdapter, mDeviceList, mAdapter);


        printbtn.setOnClickListener(e -> {
            try {

                if (!printBT.isConnected()) {
                    BluetoothDevice device = bluetoothHelper.getSavedDeviceFromPrefs();
                    PrintBluetooth.printer_id = device.getName();
                    Toast.makeText(this, device.getName() + " " + device.getAddress(), Toast.LENGTH_SHORT).show();
                    printBT.openBT2(device.getAddress());
                }
                Bitmap header = BitmapFactory.decodeResource(getResources(), R.drawable.socity_logo);
                header = Utils.scale(header, 0.8f);

                Bitmap footerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.footer_printer);
                footerBitmap = Utils.scale(footerBitmap, 0.8f);

                if (editText.getText().toString().equals("")) {
                    printBT.printQrCode(mergeBitmapsVertically(
                            header,
                            PrintBluetooth.drawText("\n  الاسم : \n  تيست\n  الومز : \n  ١٢٣٤\n", 380, 25),
                            BarcodeGenerator.getBitmap("1234", 50, 380, 250),
                            footerBitmap
                    ));
                } else {
                    printBT.printQrCode(
                            PrintBluetooth.drawText(" " + editText.getText().toString(), 380, 25)
                    );
                }

                //printBT.printQrCode(drawText(arabic, 400, 25));
                //printBT.closeBT();


            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister BroadcastReceiver
        unregisterReceiver(mReceiver);
    }

    public void showDevices() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled, prompt the user to enable it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return;
            }
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> deviceList = new ArrayList<>();

        for (BluetoothDevice device : pairedDevices) {
            deviceList.add(device.getName() + "\n" + device.getAddress());
            mDeviceList.add(device);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);

        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = (String) parent.getItemAtPosition(position);
            String[] parts = deviceInfo.split("\n");
            String deviceName = parts[0];
            String deviceAddress = parts[1];
            bluetoothHelper.saveDeviceToPrefs(deviceName, deviceAddress);
            PrintBluetooth.printer_id = deviceName;
            if (!printBT.isConnected()) {
                printBT.openBT2(deviceAddress);
            }
            deviceListView.setVisibility(ListView.GONE);
        });

        deviceListView.setVisibility(ListView.VISIBLE);
    }

    // BroadcastReceiver to listen for Bluetooth state changes
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    Log.d("TAG", "onReceive: status changed");
                    // Bluetooth has been turned on connect to printer
                    BluetoothDevice device = bluetoothHelper.getSavedDeviceFromPrefs();
                    printBT.openBT2(device.getAddress());

                }
            }
        }
    };


}