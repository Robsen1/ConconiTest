package at.fhooe.mc.conconii;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity {
    private static MainActivity instance; //singleton
    private static final int STORE_PERIOD = 50; //interval for storing the data in meters
    public static boolean testFinished = false;
    private float mDistance = 0; //the actual distance since the start in meters
    private boolean mImageSet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        DataManager.getInstance(); //initial Singleton creation

        View startscreenView = findViewById(R.id.startscreen_backLayout_vertical);

        // hide the quit button
        Button quitButton = (Button) findViewById(R.id.mainActivity_button_quit);
        quitButton.setVisibility(View.GONE);

        //startscreen spinner, seek and button

        //spinner with the device names of the scanned dvices (from DataManager - scannedDevices ArrayList<BluetoothDevice>)
        Spinner sensorSpinner = (Spinner) findViewById(R.id.startscreen_heartRateSensors_spinner);
        ArrayList<String> deviceNames = new ArrayList<>();

        for (int i = 0; i < DataManager.getInstance().getScannedDevices().toArray().length; i++) {
            BluetoothDevice device = (BluetoothDevice) DataManager.getInstance().getScannedDevices().get(i);
            deviceNames.add(device.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, deviceNames);
        sensorSpinner.setAdapter(spinnerAdapter);
        sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice chosenDevice = (BluetoothDevice) DataManager.getInstance().getScannedDevices().remove(position);
                DataManager.getInstance().getScannedDevices().add(0, chosenDevice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        SeekBar startSpeedSeekBar = (SeekBar) findViewById(R.id.startscreen_startspeed_seekBar);
        Button startButton = (Button) findViewById(R.id.startscreen_button_start);

        //TODO: only for testing purposes
        startService(new Intent(getApplicationContext(), GpsService.class));
        startService(new Intent(getApplicationContext(), BluetoothService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: start fragment and put button logic into it
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
        ImageView bleStatus = (ImageView) findViewById(R.id.mainActivity_image_BLEconnected);
        ImageView gpsStatus = (ImageView) findViewById(R.id.mainActivity_image_GPSconnected);

        //update values
        mDistance += mgr.getActualDistance(); //increase distance
        log1.setText(String.valueOf(mDistance));
        log2.setText(String.valueOf(mgr.getActualSpeed()));
        log3.setText(String.valueOf(mgr.getActualHeartRate()));

        //display connection state
        int color=getConnectionStateColor(mgr.getBleConnectionState());
        if (color != -1) {
            bleStatus.setColorFilter(color);
        }
        color=getConnectionStateColor(mgr.getGpsConnectionState());
        if(color!=-1){
            gpsStatus.setColorFilter(color);
        }


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


    public int getConnectionStateColor(int state) {
        int color = -1;
        switch (state) {
            case 0:
                color = Color.argb(255, 255, 0, 0); //disconnected red
                break;
            case 1: color = Color.argb(255, 255, 255, 0); //connecting yellow
                break;
            case 2:
                color = Color.argb(255, 0, 255, 0); //connected green
                break;
            case 3:color = Color.argb(255, 0, 0, 255); //disconnecting blue
                break;
        }
        return color;
    }
}