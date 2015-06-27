package at.fhooe.mc.conconii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 * The DataManager must be used to get data from any sensor.
 * This class is implemented as Singleton and it's main job is to provide and store
 * data. In addition to this core tasks the manager also stores the list of measurement points.
 * The manager as a broadcast receiver gets each intent and decides which data was received and stores
 * some data until a new intent is received.
 */


//TODO: App chrashes at unregister receiver

public class DataManager extends Observable {
    private static final String TAG = "DataManager";
    private static DataManager mgr = null; //singleton
    private ArrayList<ActualData> mDataList = new ArrayList<>(); //static list for measurement points
    private Location mLastLocation = null;
    private Location mActualLocation = null;
    private int mHeartRate = 0;
    private float mSpeed = 0;
    private Intent mIntent = null;
    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //make the received intent globally usable
            if (mgr != null) {
                mgr.mIntent = intent;
            }
            //update the UI
            Log.i(TAG, "Received Intent, going to update Observers");
            notifyAllObservers(intent.getAction());
        }
    };

    private DataManager() {
        //do stuff once
    }

    /**
     * This method is used to get the DataManager instance.
     * if once returned no new instance will be created.
     *
     * @return DataManager instance
     */
    public static synchronized DataManager getInstance() {
        if (DataManager.mgr == null) {
            DataManager.mgr = new DataManager();
            Log.i(TAG, "Singleton creation");
        }
        return DataManager.mgr;
    }

    /**
     * Getter method for the list of measured points
     *
     * @return List of measured points
     */
    public ArrayList<ActualData> getDataList() {
        return mDataList;
    }

    /**
     * Method for adding a measurement point to the list
     *
     * @param newData
     */
    public void addData(ActualData newData) {
        mDataList.add(newData);
    }

    /**
     * Getter method for the actual heart rate.
     *
     * @return The last heart rate received by an intent, -1 if no data is received
     */
    public int getActualHeartRate() {
        //check the received intent for its type
        if (mIntent == null || !mIntent.getAction().equals(BluetoothService.ACTION_HEART_RATE_UPDATE)) {
            return mHeartRate;
        }
        if (mIntent.getIntExtra(BluetoothService.EXTRA_BLE_DATA, -1) != -1) {
            Log.i(TAG, "Integer received: " + mIntent.getIntExtra(BluetoothService.EXTRA_BLE_DATA, -1));
            mgr.mHeartRate = mIntent.getIntExtra(BluetoothService.EXTRA_BLE_DATA, -1);
        }
        return mHeartRate;
    }

    /**
     * Getter method for the actual distance between the last intent and the actual one
     *
     * @return The distance between the last location and the current, 0 if no data is received
     */
    public float getActualDistance() {
        float distance = 0;
        //check the actual intent for its type
        if (mIntent != null && mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA) != null) {//check whether parcelable or not
            mgr.mActualLocation = mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA);
            Log.i(TAG, "Parcelable received: " + mActualLocation);

            //calculate new distance in meters
            if (mLastLocation != null) {
                distance = mLastLocation.distanceTo(mActualLocation);
            }
        }
        //store last location
        if (mgr.mActualLocation != null) {
            mgr.mLastLocation = new Location(mActualLocation);
        }
        return distance;
    }

    /**
     * Getter method for the actual speed.
     *
     * @return The speed if available through Location object, 0.0 if not
     */
    public float getActualSpeed() {
        //check the actual intent for its type
        if (mIntent != null && mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA) != null) {//check whether parcelable or not
            mgr.mActualLocation = mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA);
        }
        //calculate actual speed in km/h
        if (mActualLocation != null)
            mgr.mSpeed = mActualLocation.getSpeed() * 3.6f;
        return mgr.mSpeed;
    }

    public void registerReceiver(Context context) {
        context.registerReceiver(mDataReceiver, createIntentFilter());
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(mDataReceiver);
    }

    private IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_HEART_RATE_UPDATE);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_INVALID_DEVICE);
        intentFilter.addAction(GpsService.ACTION_LOCATION_UPDATE);
        intentFilter.addAction(GpsService.ACTION_PROVIDER_ENABLED);
        intentFilter.addAction(GpsService.ACTION_PROVIDER_DISABLED);
        return intentFilter;
    }

    /**
     * This method is for resetting the Singleton
     */
    public void finalize() {
        DataManager.mgr = null;
    }


}
