package com.example.bluetoothscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> devices = new ArrayList<>();
    ArrayList arraylistforencountereddevices = new ArrayList();
    //ArrayList nullSort = new ArrayList();
    private static final String SHARED_PREFS_FILE = "/data/data/com.example.bluetoothscan/shared_prefs";
    //allows to use bluetooth
    BluetoothAdapter BA;
    TinyDB tinydb;
    ListView listOfDDevices;
    String myName;
    String deviceInfo;
    private BroadcastReceiver blueReceiver;
    //String myName = "Bismillah";


    public void turnBluetoothOff (View view){
        BA.disable();
        if (BA.isEnabled()){
            Toast.makeText(getApplicationContext(), "Bluetooth could not be turned off", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_LONG).show();
    }

    public void turnBluetoothOn (View view){
        if (BA.isEnabled()){
            Toast.makeText(getApplicationContext(), "Bluetooth is already On", Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(i);
        //BA.enable();
        Toast.makeText(getApplicationContext(), "Bluetooth turned On", Toast.LENGTH_LONG).show();
    }

    public void findDiscoverableDevices (View view){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(i);
    }

    public void setmyDevice (View view){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(i);
        EditText mEdit   = (EditText)findViewById(R.id.editText);
        myName = mEdit.getText().toString();
        String str1 = "ID is set to: ";
        BA.setName(myName);
        StringBuilder sb = new StringBuilder();
        sb.append(str1);
        sb.append(myName);
        String concatenatedText = sb.toString();
        Toast.makeText(getApplicationContext(), concatenatedText, Toast.LENGTH_LONG).show();
    }

    public void findDevices (View view) throws IOException {
        listOfDDevices = (ListView) findViewById(R.id.listViewDetected);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        BA.startDiscovery();
        //final ArrayList<String> devices = new ArrayList<>();
        final ArrayAdapter<String> theAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, devices);
        listOfDDevices.setAdapter(theAdapter);

        blueReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        BA.startDiscovery();
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        devices.clear();
                        theAdapter.notifyDataSetChanged();
                    }
                }
                else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Boolean repeated = false;
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    SimpleDateFormat s = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
                    String format = s.format(new Date());
                    deviceInfo = device.getName() + "\n" + device.getAddress() + " "+ format;
                    for (String x : devices) {
                        if (x.equals(deviceInfo)) {
                            repeated = true;
                            break;
                        }
                    }
                    if (!repeated)
                        devices.add(deviceInfo);
                    theAdapter.notifyDataSetChanged();
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                    devices.clear();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(blueReceiver, filter);

        //save
        //TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList nullSort = new ArrayList();
        //nullSort = tinydb.getListString("All Detected");
        //String nulls= Integer.toString(devices.size());
        for (int counter = 0; counter < devices.size(); counter++) {
            String a = devices.get(counter);
            if (!a.contains("null")) {
                nullSort.add(devices.get(counter));
            }
        }
        Toast.makeText(getApplicationContext(), Integer.toString(nullSort.size()), Toast.LENGTH_LONG).show();
        tinydb.putListString ("All Detected", nullSort);

    }

    public void viewPairedDevices (View view) {
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        ListView listOfDevices = (ListView) findViewById(R.id.listViewPaired);
        ArrayList arraylistforpaireddevices = new ArrayList();
        for (BluetoothDevice bluetoothDevice : pairedDevices){
            arraylistforpaireddevices.add(bluetoothDevice.getName());
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arraylistforpaireddevices);
        listOfDevices.setAdapter(arrayAdapter);
    }

    public void viewEncounteredDevices (View view){
        ListView listOfDevices = (ListView) findViewById(R.id.listViewPaired);
        arraylistforencountereddevices.addAll(tinydb.getListString("All Detected"));
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arraylistforencountereddevices);
        listOfDevices.setAdapter(arrayAdapter);
        tinydb.putListString ("All Detected", arraylistforencountereddevices);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //need to declare Bluetooth adapter here
        BA = BluetoothAdapter.getDefaultAdapter() ;
        tinydb = new TinyDB(this);
        EditText mEdit   = (EditText)findViewById(R.id.editText);
    }
}
