package at.fhooe.mc.conconii;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity {
    private static MainActivity instance; //singleton
    private static final int STORE_PERIOD = 50; //interval for storing the data in meters
    public static boolean testFinished = false;
    private float mDistance = 0; //the actual distance since the start in meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        DataManager.getInstance(); //initial Singleton creation
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: start fragment and put button logic into it

        Button b = (Button) findViewById(R.id.test_button_start);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), GpsService.class));
                startService(new Intent(getApplicationContext(), BluetoothService.class));
            }
        });
        Button s = (Button) findViewById(R.id.test_button_stop);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFinished = true;
                mDistance = 0;//only for testing
                System.runFinalization();
                System.exit(0);
            }
        });
    }


    /**
     * called to get the MainActivity's actual object.
     *
     * @return the MainActivity's instance
     */
    public synchronized static MainActivity getInstance() {
        return MainActivity.instance;
    }

    /**
     * Called to update the user interface.
     * Updated values are the distance,speed and heart rate.
     * The update interval depends on the caller, each call is an update with the actual data.
     */
    public void updateUI() {
        DataManager mgr = DataManager.getInstance();
        if (mgr == null) return; //possible to delete?

        TextView log1 = (TextView) findViewById(R.id.test_text_log_distance);
        TextView log2 = (TextView) findViewById(R.id.test_text_log_speed);
        TextView log3 = (TextView) findViewById(R.id.test_text_log_rate);

        //update values
        mDistance += mgr.getActualDistance(); //increase distance
        log1.setText(String.valueOf(mDistance) + " [m]");
        log2.setText(String.valueOf(mgr.getActualSpeed()) + " [km/h]");
        log3.setText(String.valueOf(mgr.getActualHeartRate()) + " [bpm]");

        //add a measurement point to the ArrayList
        //TODO: fix issue that data is not added in the given period
        int intDistance = (int) mDistance;
        if (intDistance % STORE_PERIOD == 0 && intDistance != 0) {
            mgr.addData(new ActualData());
            log1.setText("addData:" + mDistance);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}