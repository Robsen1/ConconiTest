package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Robsen & Gix
 * This is the service for creating a ble connection and for receiving datafrom the GATT serve.
 * This service works as a GATT client. Received data is sent to the DataManger singleton.
 * This service runs in his own thread.
 */
public class BluetoothService extends Service implements Runnable {
    private static final String TAG = "BluetoothService";
    private Thread mBleThread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        startThread();
    }

    /**
     * starts a new Thread and write log if successful
     *
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

        //TODO: implement all the BLE logic

        //for testing purposes a implementatin which simulates heart rate updates
        int hr=0; //heart rate
        while (!MainActivity.testFinished) {
            try {
                Thread.sleep(1000); //heart beat
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this,DataManager.class);
            intent.putExtra("BLE_DATA",hr++);
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
