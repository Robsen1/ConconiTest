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
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
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

//testing MAC: 6C:EC:EB:00:E1:5F
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); //descriptor
    private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"); //characteristics
    private static final UUID HEART_RATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"); //service

    public static final String ACTION_HEART_RATE_UPDATE = "conconii.ble.heart.rate.update";
    public static final String ACTION_GATT_CONNECTED = "conconii.ble.gatt.connected";
    public static final String EXTRA_BLE_DATA = "conconii.ble.extra.data";
    public static final String ACTION_GATT_DISCONNECTED = "conconii.ble.gatt.disconnected";
    public static final String ACTION_INVALID_DEVICE = "conconii.ble.invalid.device";

    private BluetoothManager mBluetoothManager=null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private String mBluetoothDeviceAddress = null;
    private final IBinder mBinder = new LocalBinder();


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.i(TAG, "GATT connected");
                Intent i = new Intent(ACTION_GATT_CONNECTED);
                sendBroadcast(i);
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.discoverServices();
                Log.i(TAG, "GATT disconnected");
                Intent i = new Intent(ACTION_GATT_DISCONNECTED);
                sendBroadcast(i);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, services.size() + " services discovered");
            BluetoothGattCharacteristic chara = null;
            Iterator<BluetoothGattService> iterator = services.iterator();
            do {
                BluetoothGattService serv = iterator.next();
                if (serv.getUuid().equals(HEART_RATE)) {
                    chara = serv.getCharacteristic(HEART_RATE_MEASUREMENT);
                }

            } while (iterator.hasNext() && chara == null);

            if (chara != null && chara.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION) != null) {
                Log.i(TAG, "BLE Heart-Rate-Profile available");
                //set notifications for server and client
                gatt.setCharacteristicNotification(chara, true);
                BluetoothGattDescriptor desc = chara.getDescriptors().get(0);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(desc);
            } else {
                Log.i(TAG, "BLE Heart-Rate-Profile NOT available");
                Intent i= new Intent(ACTION_INVALID_DEVICE);
                sendBroadcast(i);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            int flag = characteristic.getProperties();
            int format;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            int heartRate = characteristic.getIntValue(format, 1);
            //send data to DataManager
            if (heartRate != 0) {
                Intent i = new Intent(ACTION_HEART_RATE_UPDATE);
                i.putExtra(EXTRA_BLE_DATA, heartRate);
                sendBroadcast(i);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        Log.i(TAG,"BluetoothService initialized");
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to isEnabled.");
            return false;
        }
        // We want to directly isEnabled to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        return true;
    }


    @Override
    public boolean onUnbind(Intent intent) {

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }
}
