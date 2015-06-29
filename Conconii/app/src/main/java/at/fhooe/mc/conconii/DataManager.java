package at.fhooe.mc.conconii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

/**
 * The DataManager must be used to get data from any sensor.
 * This class is implemented as Singleton and it's main job is to provide and store
 * data. In addition to this core tasks the manager also stores the list of measurement points.
 * The manager as a broadcast receiver gets each intent and decides which data was received and stores
 * some data until a new intent is received.
 *
 * @author Robsen & Gix
 */

public class DataManager extends Observable {
    //static fields
    private static final String TAG = "DataManager";
    private static DataManager mgr = null; //singleton field
    private final ArrayList<ActualData> mDataList = new ArrayList<>();

    //member variables
    private Intent mIntent = null;
    private Location mLastLocation = null;
    private Location mActualLocation = null;
    private int mHeartRate = 0;
    private float mSpeed = 0;
    private float mDistance = 0;

    //flags
    private boolean mReceiverIsRegistered = false;


    //Singleton methods

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
     * Blocks the constructor because of singleton implementation
     */
    private DataManager() {
        //do stuff once
    }

    //Data related methods

    /**
     * Getter method for the list of measured points
     *
     * @return List of measured points
     */
    public ArrayList<ActualData> getDataList() {
        return mgr.mDataList;
    }

    /**
     * Method for adding a measurement point to the list
     *
     * @param newData The actual data
     */
    public void addData(ActualData newData) {
        mgr.mDataList.add(newData);
    }

    /**
     * Getter method for the actual heart rate.
     *
     * @return The last heart rate received by an {@link Intent}.
     */
    public int getActualHeartRate() {
        //check the received intent for its type
        if (mgr.mIntent == null || !mgr.mIntent.getAction().equals(BluetoothService.ACTION_HEART_RATE_UPDATE)) {
            return mgr.mHeartRate;
        }
        if (mgr.mIntent.getIntExtra(BluetoothService.EXTRA_BLE_DATA, -1) != -1) {
            mgr.mHeartRate = mIntent.getIntExtra(BluetoothService.EXTRA_BLE_DATA, -1);
        }
        return mgr.mHeartRate;
    }

    /**
     * Getter method for the actual distance since start
     *
     * @return The distance since start
     */
    public float getActualDistance() {
        //check the actual intent for its type
        if (mgr.mIntent != null && mgr.mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA) != null) {//check whether parcelable or not
            mgr.mActualLocation = mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA);

            //calculate new distance in meters
            if (mgr.mLastLocation != null) {
                mgr.mDistance += mgr.mLastLocation.distanceTo(mgr.mActualLocation);
            }
        }
        //store last location
        if (mgr.mActualLocation != null) {
            mgr.mLastLocation = new Location(mgr.mActualLocation);
        }
        return mgr.mDistance;
    }

    /**
     * Getter method for the actual speed.
     *
     * @return The last speed through Location object.
     */
    public float getActualSpeed() {
        //check the actual intent for its type
        if (mgr.mIntent != null && mgr.mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA) != null) {//check whether parcelable or not
            mgr.mActualLocation = mgr.mIntent.getExtras().getParcelable(GpsService.EXTRA_GPS_DATA);
        }
        //calculate actual speed in km/h
        if (mgr.mActualLocation != null)
            mgr.mSpeed = mgr.mActualLocation.getSpeed() * 3.6f;
        return mgr.mSpeed;
    }

    //BroadcastReceiver

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

    /**
     * Registers the receiver to the given context.
     *
     * @param context The Context to register
     */
    public void registerReceiver(Context context) {
        context.registerReceiver(mDataReceiver, createIntentFilter());
        mgr.mReceiverIsRegistered = true;
    }

    /**
     * Unregisters the receiver from the given context.
     *
     * @param context The registered Context
     */
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(mDataReceiver);
        mReceiverIsRegistered = false;
    }

    /**
     * Creates the intent filter for receiving Intents
     *
     * @return The intent filter
     */
    private IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_HEART_RATE_UPDATE);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_INVALID_DEVICE);
        intentFilter.addAction(GpsService.ACTION_LOCATION_UPDATE);
        intentFilter.addAction(GpsService.ACTION_PROVIDER_ENABLED);
        intentFilter.addAction(GpsService.ACTION_PROVIDER_DISABLED);
        intentFilter.addAction(GpsService.ACTION_GPS_FIXED);
        return intentFilter;
    }

    /**
     * Clears the singleton instance and unregisters the receiver if registered.
     *
     * @param context the context to unregister
     * @see #unregisterReceiver(Context)
     */
    void clear(Context context) {
        if (mReceiverIsRegistered) {
            unregisterReceiver(context);
        }
        mgr = null;
    }

    /**
     * Resets the singleton
     * Uses the {@link #clear(Context)} method
     *
     * @param context The context for re-registering the receiver.
     */
    public void reset(Context context) {
        clear(context);
        getInstance();
        DataManager.mgr.registerReceiver(context);
        notifyAllObservers();
    }
}
