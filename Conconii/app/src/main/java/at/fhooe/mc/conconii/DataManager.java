package at.fhooe.mc.conconii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by Robsen & Gix
 */
public class DataManager extends BroadcastReceiver {
    private ArrayList<ActualData> mDataList = new ArrayList<>();
    private static DataManager mgr = null;

    private float mTotalDistance = 0f;
    private MyLocation mLastLocation = null;
    private Intent mIntent = null;
    private MyLocation mActualLocation = null;

    //singelton pattern
    public static DataManager getManager() {
        if (mgr == null) {
            mgr = new DataManager();
            //TODO:
            //register Receiver Dynamically!
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
        //check intent for having Integer Extra
        return 0;
    }

    public float getActualDistance() {
        //extract location out of intent
        if (mIntent.getSerializableExtra("GPS_Data") != null) {//check whether serializable or not
            mActualLocation = (MyLocation) mIntent.getExtras().getSerializable("GPS_DATA");
        }
        //TODO:
        //Location to float
        //get GPS data
        //calculate and store in mTotalDistance
        //store actualLocation in mLastLocation
        return 0;
    }

    public float getActualSpeed() {
        // same as getActualDistance

        return 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;

    }
}
