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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * The MainActivity displays the actual distance,speed and heart rate.
 */
public class MainActivity extends Activity implements Observer {
    //constants
    private static final String TAG = "MainActivity";
    private static final int STORE_PERIOD = 50;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_GET_DEVICE = 2;
    public static final String EXTRAS_DEVICE_NAME = "conconii.ble.device.name";
    public static final String EXTRAS_DEVICE_ADDRESS = "conconii.ble.device.address";

    //options
    protected static int mStartspeed = 6;

    //flags
    private boolean mTestIsRunning = false;
    private boolean mImageSet = true;
    private boolean mIsDeviceConnected = false;
    private boolean mBleServiceIsBound = false;
    private boolean mIsGpsEnabled = false;
    private boolean mGpsServiceIsBound = false;

    //service connections
    private String mDeviceAddress = null;
    private String mDeviceName = null;
    private BluetoothService mBluetoothService = null;
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

    //lifecycle related methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataManager.getInstance().registerReceiver(getApplicationContext());
        DataManager.getInstance().attach(this);

        createStartscreenUI();

        bindGpsService(true);
        enableBluetoothAndStartScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mTestIsRunning) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
            Button quit = (Button) findViewById(R.id.mainActivity_button_quit);
            layout.setVisibility(View.VISIBLE);
            quit.setText("SCAN FOR DEVICES");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            startScanActivity();
        }
        if (requestCode == REQUEST_GET_DEVICE && resultCode == Activity.RESULT_OK) {
            mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
            bindBleService(true);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cleanup
        bindGpsService(false);
        bindBleService(false);
        DataManager.getInstance().unregisterReceiver(getApplicationContext());
        DataManager.getInstance().detach(this);
        DataManager.getInstance().finalize();
    }

    //bluetooth related methods
    private boolean enableBluetoothAndStartScan() {
        BluetoothManager bluetoothManager=null;
        BluetoothAdapter bluetoothAdapter=null;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT).show();
        }

        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else startScanActivity();
        return true;
    }

    private void bindBleService(boolean bind) {
        if (bind) {
            bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection, BIND_AUTO_CREATE);
            mBleServiceIsBound = true;
        } else if (mBleServiceIsBound) {
            unbindService(mBluetoothServiceConnection);
        }
    }

    private void startScanActivity() {
        startActivityForResult(new Intent(MainActivity.this, ScanActivity.class), REQUEST_GET_DEVICE);
    }

    //gps related methods
    private void bindGpsService(boolean bind) {
        if (bind) {
            bindService(new Intent(this, GpsService.class), mGpsServiceConnection, BIND_AUTO_CREATE);
            mGpsServiceIsBound = true;
        } else if (mGpsServiceIsBound) {
            unbindService(mGpsServiceConnection);
            mGpsServiceIsBound = false;
        }
    }

    //UI related methods
    private void createStartscreenUI() {
        final Button finish = (Button) findViewById(R.id.mainActivity_button_quit);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
                if (!layout.isShown()) {
                    mTestIsRunning = false;
                    layout.setVisibility(View.VISIBLE);
                    finish.setText("SCAN FOR DEVICES");
                    TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
                    countdown.setText("START");
                    startActivity(new Intent(MainActivity.this, EvaluationActivity.class));

                } else {
                    if (mBleServiceIsBound) {
                        unbindService(mBluetoothServiceConnection);
                        mBleServiceIsBound = false;
                    }
                    startScanActivity();
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

        SeekBar bar = (SeekBar) findViewById(R.id.startscreen_seekBar_startspeed);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView test = (TextView) findViewById(R.id.startscreen_text_startspeed_value);
                test.setText(String.valueOf(progress + 6));
                mStartspeed = progress + 6;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void startCountdown() {
        final TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
        int seconds = 3;
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
                Button quit = (Button) findViewById(R.id.mainActivity_button_quit);
                quit.setText("FINISH");
                mTestIsRunning = true;
            }
        };
        timer.start();
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
            TextView distance = (TextView) findViewById(R.id.mainActivity_text_distance);
            TextView actualSpeed = (TextView) findViewById(R.id.mainActivity_text_speedActual);
            TextView neededSpeed = (TextView) findViewById(R.id.mainActivity_text_speedActual);
            NumberFormat distanceFormat = new DecimalFormat("0.00");
            NumberFormat speedFormat = new DecimalFormat("0.0");
            float neSpe=mStartspeed + mgr.getActualDistance() / 200 * 0.5f;

            neededSpeed.setText(speedFormat.format(neSpe));
            distance.setText(distanceFormat.format(mgr.getActualDistance()/1000));
            actualSpeed.setText(speedFormat.format(mgr.getActualSpeed()));

            //add a measurement point to the ArrayList
            int intDistance = (int) mgr.getActualDistance();
            if (intDistance % STORE_PERIOD > 0 && intDistance % STORE_PERIOD < 10) {
                mgr.addData(new ActualData());
            }
        }
        if (msg.equals(BluetoothService.ACTION_HEART_RATE_UPDATE)) {
            TextView rate = (TextView) findViewById(R.id.mainActivity_text_heartRate);

            rate.setText(String.valueOf(mgr.getActualHeartRate()));
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

        if (msg.equals(GpsService.ACTION_GPS_FIXED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_GPSconnected);
            status.setColorFilter(Color.GREEN);
        }

        if (msg.equals(GpsService.ACTION_PROVIDER_DISABLED)) {
            ImageView status = (ImageView) findViewById(R.id.mainActivity_image_GPSconnected);
            status.setColorFilter(Color.RED);
        }
    }

    //Observer Pattern related methods
    @Override
    public void update() {
        //get new values
    }

    @Override
    public void update(String msg) {
        if (msg.equals(BluetoothService.ACTION_INVALID_DEVICE)) {
            Toast.makeText(this, "Please use a device which is able to display the Heart Rate...", Toast.LENGTH_LONG);
            return;
        }
        updateUI(msg);


    }



}