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

    //singelton pattern

    public static synchronized DataManager getInstance() {
        if (DataManager.mgr == null) {
            DataManager.mgr = new DataManager();
            Log.i(TAG, "Singleton creation");
            MainActivity.testFinished = false; //only for testing
        }
        return DataManager.mgr;
    }

    public DataManager() {
    }

    public ArrayList<ActualData> getDataList() {
        return mDataList;
    }

    public void addData(ActualData newData) {
        mDataList.add(newData);
    }

    public int getActualHeartRate() {
        if (mIntent != null && mIntent.getIntExtra("BLE_DATA", -1) != -1) {
            Log.i(TAG, "Integer received: " + mIntent.getIntExtra("BLE_DATA", -1));
            return mIntent.getIntExtra("BLE_DATA", -1);


        }
        return -1;
    }

    public float getActualDistance() {
        //extract location out of intent
        float distance = 0;
        if (mIntent != null && mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
            Log.i(TAG, "Parcelable received: " + mActualLocation);

            //calculate distance
            if (mLastLocation != null) {
                distance = mLastLocation.distanceTo(mActualLocation);
//                if (distance > 7.0f) {
//                    distance = 3.0f;
//                }
            }
        }

        //store actualLocation in mLastLocation
        if (mActualLocation != null) {
            mLastLocation = new Location(mActualLocation);
        }
        return distance;
    }

    public float getActualSpeed() {
        if (mIntent != null && mIntent.getExtras().getParcelable("GPS_DATA") != null) {//check whether parcelable or not
            mActualLocation = mIntent.getExtras().getParcelable("GPS_DATA");
        }
        if (mActualLocation != null)
            return mActualLocation.getSpeed() * 3.6f;
        else return 0.0f;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mgr != null) {
            mgr.mIntent = intent;
        }

        try {
            MainActivity.getInstance().updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void finalize() {
        DataManager.mgr = null;
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
