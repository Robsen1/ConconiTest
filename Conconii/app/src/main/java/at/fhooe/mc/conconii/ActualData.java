package at.fhooe.mc.conconii;

import android.location.Location;

/**
 * Created by Robsen & Gix
 */
public class ActualData {
    private int mHeartRate = 0;
    private float mDistance = 0f;
    private float mSpeed = 0f;

    public ActualData() {
        DataManager mgr= DataManager.getManager();
        this.mHeartRate = mgr.getActualHeartRate();
        this.mDistance = mgr.getActualDistance();
        this.mSpeed = mgr.getActualSpeed();
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
}
