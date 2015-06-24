package at.fhooe.mc.conconii;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


//TODO: bind to services and übergeb MAC


/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity implements View.OnClickListener, Observer {
    private static final String TAG = "MainActivity";
    private static final int STORE_PERIOD = 50; //interval for storing the data in meters
    public static final String EXTRAS_DEVICE_NAME = "bibibi";
    public static final String EXTRAS_DEVICE_ADDRESS ="bobobo" ;
    public static boolean mTestFinished = false;
    private float mDistance = 0; //the actual distance since the start in meters
    private boolean mImageSet = true;
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothService mBluetoothService = null;
    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            mBluetoothService.initialize();
            //scanning...
            //TODO: scanning activity
            //connectGatt...
            mBluetoothService.connect("MAC");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };
    private ServiceConnection mGpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //TODO: GPS connection
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate()");
        Button quitButton = (Button) findViewById(R.id.mainActivity_button_quit);
        quitButton.setOnClickListener(this);

        if(enableBluetooth()){
            DataManager.getInstance().registerReceiver(getApplicationContext());
            //bindService(new Intent(this,GpsService.class),mGpsServiceConnection,BIND_AUTO_CREATE);
            startActivity(new Intent(this,StartscreenActivity.class));
            bindService(new Intent(this, DataManager.class), mBluetoothServiceConnection, BIND_AUTO_CREATE);
        }
        else finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called to update the user interface.
     * Updated values are the distance,speed and heart rate.
     * The update interval depends on the caller, each call is an update with the actual data.
     */
    public void updateUI(String msg) {
        DataManager mgr = DataManager.getInstance();
        if (mgr == null) return;

        //update values
        if (msg.equals(GpsService.ACTION_LOCATION_UPDATE)) {
            TextView log1 = (TextView) findViewById(R.id.mainActivity_text_distance);
            TextView log2 = (TextView) findViewById(R.id.mainActivity_text_speed);

            mDistance += mgr.getActualDistance(); //increase distance
            log1.setText(String.valueOf(mDistance));
            log2.setText(String.valueOf(mgr.getActualSpeed()));

            //add a measurement point to the ArrayList
            int intDistance = (int) mDistance;
            if (intDistance % STORE_PERIOD > 0 && intDistance % STORE_PERIOD < 7) {
                mgr.addData(new ActualData());
                log1.setText("addData:" + mDistance);
            }
        }
        if (msg.equals(BluetoothService.ACTION_HEART_RATE_UPDATE)) {
            TextView log3 = (TextView) findViewById(R.id.mainActivity_text_heartRate);

            log3.setText(String.valueOf(mgr.getActualHeartRate()));
            changeHeartVisualisation();
        }
    }

    public void changeHeartVisualisation() {
        ImageView v = (ImageView) findViewById(R.id.mainActivity_image_HeartRate);
        if (mImageSet) {
            v.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            mImageSet = false;
        } else {
            v.setImageResource(R.drawable.ic_favorite_48pt_3x);
            mImageSet = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.getInstance().unregisterReceiver(getApplicationContext());
        DataManager.getInstance().finalize();
    }


    @Override
    public void onClick(View v) {
        mTestFinished = true;
    }

    @Override
    public void update() {
        //get new values
    }

    @Override
    public void update(String msg) {
        updateUI(msg);

        if (msg.equals(BluetoothService.ACTION_GATT_CONNECTED)) {
            //updateConnectionStatus - red or green
        }
    }

    private boolean enableBluetooth() {
        //TODO: put this method into Bluetoothscan

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return true;
    }

}