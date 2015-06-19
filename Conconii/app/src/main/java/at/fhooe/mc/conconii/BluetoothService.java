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
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by Robsen & Gix
 * This is the service for creating a ble connection and for receiving datafrom the GATT serve.
 * This service works as a GATT client. Received data is sent to the DataManger singleton.
 * This service runs in his own thread.
 */
public class BluetoothService extends Service implements Runnable {
    private static final String TAG = "BluetoothService";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); //descriptor
    private static final UUID HEART_RATE_MEASUREMENT= UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"); //characteristics
    private static final UUID HEART_RATE= UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"); //service

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt=null;

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        Log.i(TAG, "onCreate()");
        startThread();
    }

    /**
     * This Method toggles the Bluetooth status.
     * @param enable true to enable Bluetooth, false to disable
     * @return true if success, false otherwise
     */
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
        Thread thread = new Thread(this);
        thread.start();
        if (thread.isAlive()) {
            Log.i(TAG, "Thread started");
        }
    }

    @Override
    public void run() {
        //enable Bluetooth
        setBluetooth(true);

        //define LeScanCallback (deprecated version because no lollipop testing devices available)
        final BluetoothAdapter.LeScanCallback bleCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (device != null && !DataManager.getInstance().getScannedDevices().contains(device)) {
                    DataManager.getInstance().addScannedDevice(device);
                    Log.i(TAG, "Device: " + device.getName() + " added");
                } else mBluetoothAdapter.stopLeScan(this);
                //TODO: stop scan as user chooses device (choosing puts device at pos 0 and stores UUID)
            }
        };
        //start scan with predefined callback
        mBluetoothAdapter.startLeScan(bleCallback);

        long m1 = System.currentTimeMillis() + 3000;//for testing purposes only
        while (System.currentTimeMillis() < m1) ;

        BluetoothDevice chosenDevice = (BluetoothDevice) DataManager.getInstance().getScannedDevices().get(0);
        Log.i(TAG, "chosenDevice: " + chosenDevice);

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(status==BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothGatt = gatt;
                    gatt.discoverServices();
                    Log.i(TAG, "GATT connected");
                }
                Intent i = new Intent(BluetoothService.this,DataManager.class);
                i.putExtra("BLE_CONN",newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                List<BluetoothGattService> services = gatt.getServices();
                Log.i(TAG, services.size()+" services discovered");
                BluetoothGattCharacteristic chara=null;
                Iterator<BluetoothGattService> iterator=services.iterator();
                do{
                    BluetoothGattService serv=iterator.next();
                    if(serv.getUuid().equals(HEART_RATE)){
                        chara=serv.getCharacteristic(HEART_RATE_MEASUREMENT);
                    }

                }while(iterator.hasNext() && chara==null);

                 if(chara!=null && chara.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION)!=null) {
                     Log.i(TAG,"ble heart rate profile available");

                     //set notifications for server and client
                     gatt.setCharacteristicNotification(chara, true);
                     BluetoothGattDescriptor desc = chara.getDescriptors().get(0);
                     desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                     gatt.writeDescriptor(desc);
                 }
                else{
                     Log.i(TAG, "ble heart rate profile NOT available");
                     Toast.makeText(MainActivity.getInstance(),R.string.BLE_device_error_msg,Toast.LENGTH_LONG);
                 }
            }

            //TODO: distinguish between FORMAT
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                    characteristic) {
                int heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1); //hardcoded
                //send data to DataManager
                Intent i = new Intent(BluetoothService.this, DataManager.class);
                i.putExtra("BLE_DATA", heartRate);
                sendBroadcast(i);
            }
        };

        //establish Gatt connection
        chosenDevice.connectGatt(this, true, gattCallback);

        while (!MainActivity.testFinished) {
            //do nothing
        }

        //stop everything
        mBluetoothGatt.close();
        mBluetoothGatt=null;
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
