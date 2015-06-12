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

    public static synchronized DataManager getInstance() {
        if(DataManager.mgr==null){
            DataManager.mgr=new DataManager();
            Log.i(TAG,"reset singleton");
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
            mDistance += (float)((int)(mActualLocation.distanceTo(mLastLocation)*100)/100);
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
        DataManager.mgr.mIntent = intent;

        try {
            MainActivity.getInstance().updateUI();
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
