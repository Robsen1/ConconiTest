package at.fhooe.mc.conconii;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity {
    private static MainActivity instance; //singleton
    private static final int STORE_PERIOD = 50; //interval for storing the data in meters
    public static boolean testFinished = false;
    private float mDistance = 0; //the actual distance since the start in meters
    private boolean mImageSet=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        DataManager.getInstance(); //initial Singleton creation
        startService(new Intent(getApplicationContext(), GpsService.class));
        startService(new Intent(getApplicationContext(), BluetoothService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: start fragment and put button logic into it


        Button b = (Button) findViewById(R.id.mainActivity_button_quit);
        b.setOnClickListener(new View.OnClickListener() {
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

        TextView log1 = (TextView) findViewById(R.id.mainActivity_text_distance);
        TextView log2 = (TextView) findViewById(R.id.mainActivity_text_speed);
        TextView log3 = (TextView) findViewById(R.id.mainActivity_text_heartRate);

        //update values
        mDistance += mgr.getActualDistance(); //increase distance
        log1.setText(String.valueOf(mDistance));
        log2.setText(String.valueOf(mgr.getActualSpeed()));
        log3.setText(String.valueOf(mgr.getActualHeartRate()));

        //add a measurement point to the ArrayList
        //TODO: fix issue that data is not added in the given period
        int intDistance = (int) mDistance;
        if (intDistance % STORE_PERIOD == 0 && intDistance != 0) {
            mgr.addData(new ActualData());
            log1.setText("addData:" + mDistance);
        }
    }

    public void changeHeartVisualisation() {
        ImageView v = (ImageView) findViewById(R.id.mainActivity_image_HeartRate);
        if (mImageSet) {
            v.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            mImageSet=false;
        } else {
            v.setImageResource(R.drawable.ic_favorite_48pt_3x);
            mImageSet=true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}