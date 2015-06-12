package at.fhooe.mc.conconii;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    private static MainActivity sin;
    private static final int STORE_PERIOD = 50; //in meters
    public static boolean testFinished = false;
    private float mDistance=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataManager.getInstance();
        sin = this;


    }

    @Override
    protected void onResume() {
        super.onResume();

        //start fragment
        //TODO:
        //place buttons in fragment
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
                mDistance=0;//only for testing
            }
        });
    }

    public synchronized static MainActivity getInstance() {
        return MainActivity.sin;
    }

    //gets updated when DataManager is receiving and intent --> actualization delay==heartrate+locationchange
    public void updateUI() {
        //set text in gui per mgr getters
        DataManager mgr = DataManager.getInstance();
        if (mgr == null) return;

        mDistance+=mgr.getActualDistance();
        TextView log1 = (TextView) findViewById(R.id.test_text_log_distance);
        log1.setText(String.valueOf(mDistance) + " [m]");
        TextView log2 = (TextView) findViewById(R.id.test_text_log_speed);
        log2.setText(String.valueOf(mgr.getActualSpeed()) + " [km/h]");
        TextView log3 = (TextView) findViewById(R.id.test_text_log_rate);
        log3.setText(String.valueOf(mgr.getActualHeartRate()) + " [bpm]");

        //addData not working yet
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