package at.fhooe.mc.conconii;

/**
 * Created by Robsen & Gix
 */
public class ActualData {
    private int mHeartRate = 0;
    private float mDistance = 0f;
    private float mSpeed = 0f;

    public ActualData() {
        DataManager mgr=DataManager.getInstance();
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
