package at.fhooe.mc.conconii;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    private static final int STORE_PERIOD = 50; //in meters
    private static boolean testFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager mgr=DataManager.getManager();
        while (!testFinished) {
            //set text in gui per mgr getters

            if ((int) mgr.getActualDistance() % STORE_PERIOD == 0) {
                mgr.addData(new ActualData());
            }
        }
        startS


    }
}
