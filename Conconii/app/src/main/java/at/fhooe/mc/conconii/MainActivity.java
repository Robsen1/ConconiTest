package at.fhooe.mc.conconii;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity implements Observer {
    private static final String TAG = "MainActivity";
    private static final int STORE_PERIOD = 50; //interval for storing the data in meters
    public static String EXTRAS_DEVICE_NAME = "conconii.ble.device.name";
    public static String EXTRAS_DEVICE_ADDRESS = "conconii.ble.device.address";
    public static boolean mTestFinished = false;
    private float mDistance = 0; //the actual distance since the start in meters
    private boolean mImageSet = true;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_GET_DEVICE = 2;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mDeviceAddress = null;
    private String mDeviceName = null;
    private boolean mIsDeviceConnected = false;

    private BluetoothService mBluetoothService = null;
    private boolean mIsBleServiceBound=false;
    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "BluetoothService connected");
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            mBluetoothService.initialize();
            mBluetoothService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };
    private GpsService mGpsService = null;
    private boolean mIsGpsEnabled = false;
    private boolean mIsGpsServiceBound=false;
    private ServiceConnection mGpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "GpsService connected");
            mGpsService = ((GpsService.LocalBinder) service).getService();
            mGpsService.requestUpdates();
            if (!mGpsService.isEnabled())
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");

        DataManager.getInstance().registerReceiver(getApplicationContext());
        DataManager.getInstance().attach(this);

        final Button restart = (Button) findViewById(R.id.mainActivity_button_quit);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
                if (!layout.isShown()) {
                    layout.setVisibility(View.VISIBLE);
                    mDistance = 0;
                    restart.setText("SCAN FOR DEVICES");
                    TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
                    countdown.setText("START");

                } else {
                    if (mIsBleServiceBound) {
                        unbindService(mBluetoothServiceConnection);
                        mIsBleServiceBound = false;
                    }
                    startScan();
                }
            }
        });
        Button tutorial = (Button) findViewById(R.id.startscreen_button_tutorial);
        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TutorialActivity.class));
            }
        });
        TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
        countdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown();
            }
        });

        bindService(new Intent(this, GpsService.class), mGpsServiceConnection, BIND_AUTO_CREATE);
        mIsGpsServiceBound=true;
        enableBluetoothAndStartScan();
    }


    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
        Button quit = (Button) findViewById(R.id.mainActivity_button_quit);
        layout.setVisibility(View.VISIBLE);
        quit.setText("SCAN FOR DEVICES");
    }

    private void startCountdown() {
        final TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
        int seconds = 10;
        seconds++;
        CountDownTimer timer = new CountDownTimer(seconds * 1000, 900) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished / 1000) - 1;
                if (time != 0)
                    countdown.setText(String.valueOf(time));
                else
                    countdown.setText("GO!");
            }

            @Override
            public void onFinish() {
                LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
                layout.setVisibility(View.GONE);
                Button quit= (Button) findViewById(R.id.mainActivity_button_quit);
                quit.setText("RESTART");
            }
        };
        timer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            startScan();
        }
        if (requestCode == REQUEST_GET_DEVICE && resultCode == Activity.RESULT_OK) {
            mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
            Log.i(TAG, "Attempting to bind BluetoothService");
            bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection, BIND_AUTO_CREATE);
            mIsBleServiceBound=true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        startActivityForResult(new Intent(MainActivity.this, ScanActivity.class), REQUEST_GET_DEVICE);
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
            if (intDistance % STORE_PERIOD > 0 && intDistance % STORE_PERIOD < 10) {
                mgr.addData(new ActualData());
                log1.setText("addData:" + mDistance);
            }
        }
        if (msg.equals(BluetoothService.ACTION_HEART_RATE_UPDATE)) {
            TextView log3 = (TextView) findViewById(R.id.mainActivity_text_heartRate);

            log3.setText(String.valueOf(mgr.getActualHeartRate()));
            changeHeartVisualisation();
        }

        if (msg.equals(BluetoothService.ACTION_GATT_CONNECTED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_BLEconnected);
            status.setColorFilter(Color.GREEN);
            mIsDeviceConnected = true;
        }

        if (msg.equals(BluetoothService.ACTION_GATT_DISCONNECTED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_BLEconnected);
            status.setColorFilter(Color.RED);
            mIsDeviceConnected = false;
        }

        if (msg.equals(GpsService.ACTION_PROVIDER_ENABLED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_GPSconnected);
            status.setColorFilter(Color.GREEN);
        }

        if (msg.equals(GpsService.ACTION_PROVIDER_DISABLED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_GPSconnected);
            status.setColorFilter(Color.RED);
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
        //cleanup
        if (mIsGpsServiceBound) {
            unbindService(mGpsServiceConnection);
        }
        if (mIsBleServiceBound)
            unbindService(mBluetoothServiceConnection);
        DataManager.getInstance().unregisterReceiver(getApplicationContext());
        DataManager.getInstance().detach(this);
        DataManager.getInstance().finalize();
    }

    @Override
    public void update() {
        //get new values
    }

    @Override
    public void update(String msg) {
        if (msg.equals(BluetoothService.ACTION_INVALID_DEVICE)) {
            Toast.makeText(this, "You have to use a device which is able to display the Heart Rate...", Toast.LENGTH_LONG);
            return;
        }
        updateUI(msg);


    }

    private boolean enableBluetoothAndStartScan() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT).show();
        }

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else startScan();
        return true;
    }

}