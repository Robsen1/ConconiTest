package at.fhooe.mc.conconii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class DataManager extends BroadcastReceiver {
    private ArrayList<ActualData> mDataList = new ArrayList<>();
    private static DataManager mgr = null;

    private Location mLastLocation = null;
    private Intent mIntent = null;
    private Location mActualLocation = null;
    private float mDistance = 0;

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

    public int getActualHeartRate() {
        //TODO:
        //ble implementation
        return 0;
    }

    public float getActualDistance() {
        //extract location out of intent

        if (mIntent != null && mIntent.getSerializableExtra("GPS_Data") != null) {//check whether parcelable or not
            mActualLocation = (Location) mIntent.getExtras().getParcelable("GPS_DATA");
        }
        //calculate distance
        if (mLastLocation != null) {
            mDistance += mActualLocation.distanceTo(mLastLocation);
        }
        //store actualLocation in mLastLocation
        mLastLocation = mActualLocation;
        return mDistance;
    }

    public float getActualSpeed() {
        if (mIntent != null && mIntent.getSerializableExtra("GPS_Data") != null) {//check whether parcelable or not
            mActualLocation = (Location) mIntent.getExtras().getParcelable("GPS_DATA");
        }

        return mActualLocation.getSpeed();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;

    }
}
