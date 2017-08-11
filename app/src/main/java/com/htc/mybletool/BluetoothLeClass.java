package com.htc.mybletool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lidong_Zhou on 2017/8/11.
 */
public class BluetoothLeClass extends Activity {
    private final static String TAG = "BluetoothLeClass";
    private Activity  mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> data;
    private HashMap<String, Object> item;
    private ListView  mBleListView;

    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;

    private final static int REQUEST_CODE = 1;

    public BluetoothLeClass(Activity mContext) {
        this.mContext = mContext;
    }

    public void initBluetoothLeClass(Activity mContext) {
        this.mContext = mContext;
    }

    public BluetoothAdapter getluetoothAdapter() {

        return  mBluetoothAdapter;
    }

    public  void initBleScanList(ListView  mBleListView){
        this.mBleListView = mBleListView;
        data = new ArrayList<HashMap<String,Object>>();
        adapter = new SimpleAdapter(mContext, data, R.layout.blescanlistviewitem,
                new String[]{"name", "address","uuid"}, new int[]{R.id.remote_name, R.id.remote_address,R.id.remote_uuid});
        mBleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,  " "+data.get(i).get("name"));
                stopScan();
                connectBleDevice(data.get(i).get("address").toString());
            }
        });
    }

    /************************************* enable bluetooth start    **********************************/
    public  void enableBluetooth() {

        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "你的手机不支持BLE", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Log.e(TAG, "initialize Bluetooth, has BLE system");
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "你的手机不支持BLE",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.e(TAG,  "mBluetoothAdapter = " + mBluetoothAdapter);
        }

        // 打开蓝牙
        mBluetoothAdapter.enable();
        Log.e(TAG,  "mBluetoothAdapter.enable");



    }
    /************************************* enable bluetooth end  **********************************/

    /************************************* start scan            **********************************/

    public void statScan() {
        if (mBluetoothAdapter != null) {

            BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner != null)
                mBluetoothLeScanner.startScan(mLecallback);
        }
    }
    public void stopScan() {
        if (mBluetoothAdapter != null) {

            BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner != null)
                mBluetoothLeScanner.stopScan(mLecallback);
        }
    }


    public ScanCallback mLecallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG, "onScanResult"+result.getDevice().getAddress()
                    +"  "+result.getDevice().getName()
                    +" "+result.getDevice().getUuids());
            for(HashMap<String, Object> mItem : data){
                if (mItem.get("address").equals(result.getDevice().getAddress()))
                    return;

            }

            item = new HashMap<String, Object>();
            item.put("name",result.getDevice().getName());
            item.put("address",result.getDevice().getAddress());
            item.put("uuid",result.getDevice().getUuids());
            data.add(item);
            mBleListView.setAdapter(adapter);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


/****************************************end  scan**********************************************/


/*************************************Connect remote device start**********************************/
    public boolean connectBleDevice(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG,
                    "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            try {
                Log.e(TAG, "Connected");

            } catch (Exception e) {
                e.printStackTrace();
            }



        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG, "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
/*************************************Connect remote device end  **********************************/

}
