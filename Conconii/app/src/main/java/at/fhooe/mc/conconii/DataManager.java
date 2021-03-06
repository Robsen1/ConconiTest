package at.fhooe.mc.conconii;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

public class DataManager extends BroadcastReceiver {
    private static final String TAG = "DataManager";
    private ArrayList<ActualData> mDataList = new ArrayList<>(); //static list for measurement points
    private static DataManager mgr = null; //singleton
    private ArrayList<BluetoothDevice> scannedDevices = new ArrayList<>();

    private Location mLastLocation = null;
    private Location mActualLocation = null;
    private int mHeartRate = 0;
    private float mSpeed = 0;
    private Intent mIntent = null;


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
            MainActivity.testFinished = false; //only for testing purposes
        }
        return DataManager.mgr;
    }

    /**
     * public constructor because otherwise the broadcast receiver can't be registered statically int the manifest
     */

    public DataManager() {
        //do stuff once
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
     * Shows the state of the Bluetooth connection.
     * -1 = no changes
     * 0 = disconnected
     * 1 = connecting
     * 2 = connected
     * 3 = disconnecting
     *
     * @return the actual state
     */
    public int getBleConnectionState() {
        //check the received intent for its type
        if (mIntent != null && mIntent.getIntExtra("BLE_CONN", -1) != -1) {
            return mIntent.getIntExtra("BLE_CONN", -1);
        }
        return -1;
    }

    /**
     * Shows the state of the GPS connection
     * -1 = no changes
     * 0 = disconnected
     * 1 = connecting
     * 2 = connected
     *
     * @return the actual state
     */
    public int getGpsConnectionState() {
        if (GpsService.gpsIsConnected)
            return 2;
        else return 0;
    }

    /**
     * Getter method for the actual heart rate.
     *
     * @return The last heart rate received by an intent, -1 if no data is received
     */
    public int getActualHeartRate() {
        //check the received intent for its type
        if (mIntent != null && mIntent.getIntExtra("BLE_DATA", -1) != -1) {
            Log.i(TAG, "Integer received: " + mIntent.getIntExtra("BLE_DATA", -1));
            mgr.mHeartRate = mIntent.getIntExtra("BLE_DATA", -1);
            MainActivity.getInstance().changeHeartVisualisation();
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
        if (mIntent != null && mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mgr.mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
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
        if (mIntent != null && mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mgr.mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
        }
        //calculate actual speed in km/h
        if (mActualLocation != null)
            mgr.mSpeed = mActualLocation.getSpeed() * 3.6f;
        return mgr.mSpeed;
    }

    public synchronized ArrayList getScannedDevices() {
        return scannedDevices;
    }

    public synchronized void addScannedDevice(BluetoothDevice device) {
        mgr.scannedDevices.add(device);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //make the received intent globally usable
        if (mgr != null) {
            mgr.mIntent = intent; //why is mIntent = intent not the same??!!??!?!?!?
        }
        //update the UI
        try {
            MainActivity.getInstance().updateUI();
        } catch (Exception e) {
            //just ignore
        }
    }

    /**
     * This method is for resetting the Singleton
     */
    public void finalize() {
        DataManager.mgr = null;
    }

/*
    public void writeLog(String msg) {

    }
        File log = new File("sdcard/log.txt");
        if(!log.exists()){
            try {
                log.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(log,true));
            writer.append(msg);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
