package at.fhooe.mc.conconii;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    private static MainActivity sin;
    private static final int STORE_PERIOD = 5; //in meters
    public static boolean testFinished = false;

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
        Button b = (Button) findViewById(R.id.test_button_start);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gpsService = new Intent(getApplicationContext(), GpsService.class);
                startService(gpsService);
            }
        });
        Button s = (Button) findViewById(R.id.test_button_stop);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFinished = true;
                finish();
            }
        });

//        MainActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    public synchronized static MainActivity getInstance() {
        return MainActivity.sin;
    }

    public void updateUI() {
        //set text in gui per mgr getters
        DataManager mgr = DataManager.getInstance();
        float distance = mgr.getActualDistance();
        TextView log = (TextView) findViewById(R.id.test_text_log);
        log.setText(String.valueOf(distance));
        if (((int)distance) % STORE_PERIOD == 0 && (int)distance!=0) {
            mgr.addData(new ActualData());
            log.setText("addData");
        }
    }


}