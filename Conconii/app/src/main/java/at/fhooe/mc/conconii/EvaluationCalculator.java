package at.fhooe.mc.conconii;

import java.util.ArrayList;

/**
 * Created by Robsn & Gix
 * This class is used to create a list of final data which can be used to display
 * in {@link EvaluationActivity}.
 */
class EvaluationCalculator {
    private final ArrayList<ActualData> mData;
    private final ArrayList<ActualData> mFinalData;

    EvaluationCalculator(ArrayList<ActualData> data) {
        mFinalData = new ArrayList<>();
        mData = data;
        if (mData.size() != 0)
            calcFinalData();
    }

    /**
     * Calculates the final Data used to Display in the {@link EvaluationActivity}.
     * Stores an average value of every speed step.
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


    /**
     * Getter for the list of final data.
     *
     * @return The final data list
     */
    public ArrayList<ActualData> getFinalData() {
        return mFinalData;
    }
}
