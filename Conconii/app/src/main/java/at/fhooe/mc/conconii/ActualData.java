package at.fhooe.mc.conconii;


/**
 * Created by Robsen & Gix
 * This class is a simple data container class for storing the measurement points.
 */
public class ActualData {
    private int mHeartRate = 0;
    private float mDistance = 0f;
    private float mSpeed = 0f;
    private float mTargetSpeed = 0f;

    public ActualData() {
        //store all current values
        DataManager mgr = DataManager.getInstance();
        this.mHeartRate = mgr.getActualHeartRate();
        this.mDistance = mgr.getActualDistance();
        this.mSpeed = mgr.getActualSpeed();
        //if the startspeed is 6km/h!--------------------!
        this.mTargetSpeed = 6 + mDistance / 200 * 0.5f;
    }

    public ActualData(int _heartRate,float _speed, float _distance) {
        this.mHeartRate = _heartRate;
        this.mDistance = _distance;
        this.mSpeed = _speed;
        //if the startspeed is 6km/h!--------------------!
        this.mTargetSpeed = 6 + mDistance / 200 * 0.5f;
    }

    //Getter methods
    public int getHeartRate() {
        return mHeartRate;
    }

    public float getDistance() {
        return mDistance;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public float getTargetSpeed() {
        return mTargetSpeed;
    }
}
