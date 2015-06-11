package at.fhooe.mc.conconii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class DataManager extends BroadcastReceiver {
    private static final String TAG = "DataManager";
    private ArrayList<ActualData> mDataList = new ArrayList<>();
    private static DataManager mgr = null;

    private Location mLastLocation = null;
    private Intent mIntent = null;
    private Location mActualLocation = null;
    private float mDistance = 0;

    //singelton pattern
    //Problem:
    //nach jedem on receive wird verliert mgr seine daten?!!!!?!

    public static DataManager getInstance() {
        if (mgr == null) {
            mgr = new DataManager();
        }
        return mgr;
    }

    public DataManager() {
       mgr=this;
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

        if (mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
            Log.i(TAG, "Parcelable received");
        }
        //calculate distance
        if (mLastLocation != null) {
            mDistance += mActualLocation.distanceTo(mLastLocation);
        }
        Log.i(TAG, "mLastLocation: " + mLastLocation);
        //store actualLocation in mLastLocation
        mLastLocation = new Location(mActualLocation);
        return mDistance;
    }

    public float getActualSpeed() {
        if (mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
        }

        return mActualLocation.getSpeed();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;

        try {
            MainActivity.getInstance().updateUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeLog(String msg) {

    }
//        File log = new File("sdcard/log.txt");
//        if(!log.exists()){
//            try {
//                log.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(log,true));
//            writer.append(msg);
//            writer.newLine();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
