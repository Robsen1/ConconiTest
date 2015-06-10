package at.fhooe.mc.conconii;

import android.location.Location;

/**
 * Created by Robsen & Gix
 */
public class ActualData {
    private int mHeartRate = 0;
    private float mDistance = 0f;
    private float mSpeed = 0f;

    private static float mTotalDistance = 0f;
    private static Location mLastLocation = null; //wenn ned spiecherbar --> clone();


    public ActualData() {
        this.mHeartRate = getActualHeartRate();
        this.mDistance = getActualDistance();
        this.mSpeed = getActualSpeed();
    }

    public int getHeartRate() {
        return mHeartRate;
    }

    public float getDistance() {
        return mDistance;
    }

    public float getSpeed() {
        return mSpeed;
    }

    static int getActualHeartRate() {
        return 0;

    }

    static float getActualDistance() {
        //get last location by mLastLocation
        //get actual location from gps
        //calculate and store in mTotalDistance
        //store actual in mLastLocation
        return 0;
    }

    static float getActualSpeed() {
        return 0;
    }
}
