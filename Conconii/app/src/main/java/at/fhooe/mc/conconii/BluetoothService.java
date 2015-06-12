package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Robsen & Gix
 */
public class BluetoothService extends Service implements Runnable {
    private static final String TAG = "BluetoothService";
    private Thread mBleThread = null;

//TODO:
    //BLE implementation


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        startThread();

    }

    private void startThread() {
        mBleThread = new Thread(this);
        mBleThread.start();
        if (mBleThread.isAlive()) {
            Log.i(TAG, "Thread started");
        }
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

    @Override
    public void run() {

        //only for testing simulated heart rate
        int hr=0;
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
}
