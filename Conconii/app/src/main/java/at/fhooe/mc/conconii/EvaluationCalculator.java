package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Created by Robsn & Gix
 */
public class EvaluationCalculator {
    private ArrayList<ActualData> mData = new ArrayList<>();
    private ArrayList<ActualData> mFinalData = new ArrayList<>();

    EvaluationCalculator(ArrayList<ActualData> _data) {
        mData = _data;
    }

    /**
     * saves a average value of every speed step
     */
    private void calcFinalData() {
        float s = mData.get(0).getTargetSpeed();
        int n = 0; // element counter
        int heartRateASum = 0;
        float speedSum = 0f;

        for (int i = 0; i < mData.size(); i++) {

            // simple filter of invalid values - can be extended
            if (mData.get(i).getHeartRate() != 0 && mData.get(i).getSpeed() > 0) {
                n++;
                heartRateASum += mData.get(i).getHeartRate();
                speedSum += mData.get(i).getSpeed();
            }

            // if next step - save point and increase s at 0.5
            if (s != mData.get(i).getTargetSpeed()) {
                mFinalData.add(new ActualData(heartRateASum / n, speedSum / n, mData.get(i).getDistance()));
                s += 0.5f;
                //reset all variables
                n = 0;
                heartRateASum = 0;
                speedSum = 0f;
            }
        }
    }


    //getter
    public ArrayList<ActualData> getFinalData() {
        return mFinalData;
    }
}
