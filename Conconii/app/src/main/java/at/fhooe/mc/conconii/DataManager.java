package at.fhooe.mc.conconii;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class DataManager extends BroadcastReceiver{
    private ArrayList<ActualData> mDataList = new ArrayList<>();
    private static DataManager mgr = null;

    private float mTotalDistance = 0f;
    private Location mLastLocation = null; //wenn nicht speicherbar --> clone();

    private DataManager() {

    }

    //singelton pattern
    public static DataManager getManager() {
        if (mgr == null) {
            mgr = new DataManager();
        }
        return mgr;
    }

    public ArrayList<ActualData> getDataList() {
        return mDataList;
    }

    public void addData(ActualData newData) {
        mDataList.add(newData);
    }

    public int getActualHeartRate(){
        //BluetoothGattCharacteristic to int
        return 0;
    }

    public float getActualDistance(){
        //Location to float
        //get GPS data
        //get last location by mLastLocation
        //get actual location from gps
        //calculate and store in mTotalDistance
        //store actual in mLastLocation
        return 0;
    }

    public float getActualSpeed() {
        // get GPS data

        return 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {


    }
}
