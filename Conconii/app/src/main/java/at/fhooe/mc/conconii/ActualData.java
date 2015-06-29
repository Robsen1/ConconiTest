package at.fhooe.mc.conconii;


/**
 * This class is a simple data container class for storing the measurement points.
 *
 * @author Robsen & Gix
 */
class ActualData {
    private int mHeartRate = 0;
    private float mDistance = 0f;
    private float mSpeed = 0f;
    private float mTargetSpeed = 0f;

    /**
     * Creates an object with data corresponding to the state at call.
     */
    public ActualData() {
        //store all current values
        DataManager mgr = DataManager.getInstance();
        this.mHeartRate = mgr.getActualHeartRate();
        this.mDistance = mgr.getActualDistance();
        this.mSpeed = mgr.getActualSpeed();
        this.mTargetSpeed = MainActivity.mStartspeed + mDistance / 200 * 0.5f;
    }

    /**
     * Creates an object with data corresponding to params.
     *
     * @param heartRate in beats per minute
     * @param speed in kilometers per hour
     * @param distance in meters
     */
    public ActualData(int heartRate, float speed, float distance) {
        this.mHeartRate = heartRate;
        this.mDistance = distance;
        this.mSpeed = speed;
        this.mTargetSpeed = MainActivity.mStartspeed + mDistance / 200 * 0.5f;
    }

    /**
     * Gets the current heart rate.
     * @return heart rate in bpm
     */
    public int getHeartRate() {
        return mHeartRate;
    }

    /**
     * Gets the current distance.
     * @return distance in meters
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     * Gets the current speed.
     * @return speed in km/h
     */
    public float getSpeed() {
        return mSpeed;
    }

    /**
     * Gets the required speed at current time.
     * @return speed in km/h
     */
    public float getTargetSpeed() {
        return mTargetSpeed;
    }
}
