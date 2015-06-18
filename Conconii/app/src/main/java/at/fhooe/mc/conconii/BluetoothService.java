package at.fhooe.mc.conconii;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.IBinder;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Robsen & Gix
 * This is the service for creating a ble connection and for receiving datafrom the GATT serve.
 * This service works as a GATT client. Received data is sent to the DataManger singleton.
 * This service runs in his own thread.
 */
public class BluetoothService extends Service implements Runnable {
    private static final String TAG = "BluetoothService";
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothAdapter.LeScanCallback mBleCallback;
    private BluetoothGattCallback mGattCallback;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice chosenDevice;

    private Thread mBleThread = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Log.i(TAG, "onCreate()");
        startThread();
    }

    private boolean setBluetooth(boolean enable) {
        boolean isEnabled = mBluetoothAdapter.isEnabled();

        if (enable && !isEnabled) {
            return mBluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return mBluetoothAdapter.disable();
        }

        // necessary for compiler
        return false;
    }

    /**
     * starts a new Thread and write log if successful
     */
    private void startThread() {
        mBleThread = new Thread(this);
        mBleThread.start();
        if (mBleThread.isAlive()) {
            Log.i(TAG, "Thread started");
        }
    }

    @Override
    public void run() {
        setBluetooth(true);

        BluetoothAdapter.LeScanCallback mBleCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                DataManager.getInstance().addScannedDevice(device);
            }
        };
        mBluetoothAdapter.startLeScan(mBleCallback);

        // TODO: the chosen device have to be at index 0
        chosenDevice = (BluetoothDevice) DataManager.getInstance().getScannedDevices().get(0);
        BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                gatt.discoverServices();
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                List<BluetoothGattService> services = gatt.getServices();

                Iterator<BluetoothGattService> iterService = services.iterator();
                BluetoothGattService service = iterService.next();
                while (iterService.hasNext()) {

                    // check if its a heart rate sensor -> 6157 (0x180D)
                    if (service.getType() == 6157) {
                        List<BluetoothGattCharacteristic> charas = service.getCharacteristics();

                        Iterator<BluetoothGattCharacteristic> iterChara = charas.iterator();
                        BluetoothGattCharacteristic chara = iterChara.next();

                        // check whether it is the heart rate measurement characteristic 10807
                        if (chara.getInstanceId() == 10807) {

                        }


                    }
                }


            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

        };
        chosenDevice.connectGatt(this, true, mGattCallback);


        while (!MainActivity.testFinished) {

            Intent intent = new Intent(this, DataManager.class);
            intent.putExtra("BLE_DATA", 0);
            sendBroadcast(intent);
            Log.i(TAG, "HeartRate update sent");
        }
        Log.i(TAG, "Thread stopped");
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
