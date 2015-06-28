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
 * The MainActivity displays two major layouts: the startScreen and the mainActivity layout.
 */
public class MainActivity extends Activity implements Observer {

    //constants
    private static final String TAG = "MainActivity";
    public static final String EXTRAS_DEVICE_NAME = "conconii.ble.device.name";
    public static final String EXTRAS_DEVICE_ADDRESS = "conconii.ble.device.address";
    private static final int STORE_PERIOD = 50; //in meters
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_GET_DEVICE = 2;
    private static final String ACTION_RESET_LOCATION ="conconii.gps.reset" ;

    //options
    protected static int mStartspeed = 6; //modified by seekbar

    //flags
    private boolean mTestIsRunning = false;
    private boolean mImageSet = true;
    private boolean mIsDeviceConnected = false; //reserved for future uses
    private boolean mBleServiceIsBound = false;
    private boolean mIsGpsEnabled = false;  //reserved for future uses
    private boolean mGpsServiceIsBound = false;

    //service connections
    private String mDeviceAddress = null;
    private String mDeviceName = null; //reserved for future uses
    private BluetoothService mBluetoothService = null;
    private final ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
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
    private final ServiceConnection mGpsServiceConnection = new ServiceConnection() {
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
        //initialize singleton
        DataManager.getInstance().registerReceiver(getApplicationContext());
        //build UI
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
        if (!mTestIsRunning) {
            //cleanup
            bindGpsService(false);
            bindBleService(false);
            DataManager.getInstance().detach(this);
            DataManager.getInstance().clear(getApplicationContext());
        }
    }

    //bluetooth related methods

    /**
     * Once this method is called, Bluetooth is going to be activated by the user.
     * If BT is already enabled, the ScanActivity gets started.
     * If BT is not enabled the ScanActivity gets started through the {@link #onActivityResult(int, int, Intent)} Callback.
     *
     * @return False is returned if errors occurred.
     */
    private boolean enableBluetoothAndStartScan() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter=bluetoothManager.getAdapter();
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

    /**
     * This method is used universally to bind or unbind from the BluetoothService.
     * Further workflow is specified trough the {@link #mBluetoothServiceConnection}.
     *
     * @param bind If True, {@link #bindService(Intent, ServiceConnection, int)} is called,
     *             {@link #unbindService(ServiceConnection)} is called otherwise.
     */
    private void bindBleService(boolean bind) {
        if (bind) {
            if (!mBleServiceIsBound)
                bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection, BIND_AUTO_CREATE);
            mBleServiceIsBound = true;
        } else if (mBleServiceIsBound) {
            unbindService(mBluetoothServiceConnection);
            mBleServiceIsBound = false;
        }
    }

    /**
     * This method is just for shortening the call of the ScanActivity.
     * {@link #startActivityForResult(Intent, int)} is invoked.
     */
    private void startScanActivity() {
        startActivityForResult(new Intent(MainActivity.this, ScanActivity.class), REQUEST_GET_DEVICE);
    }

    //gps related methods

    /**
     * This method is used universally to bind or unbind from the GpsService.
     * Further workflow is specified trough the {@link #mGpsServiceConnection}.
     *
     * @param bind If True, {@link #bindService(Intent, ServiceConnection, int)} is called,
     *             {@link #unbindService(ServiceConnection)} is called otherwise.
     */
    private void bindGpsService(boolean bind) {
        if (bind) {
            if (!mGpsServiceIsBound) {
                bindService(new Intent(this, GpsService.class), mGpsServiceConnection, BIND_AUTO_CREATE);
                mGpsServiceIsBound = true;
            }
        } else if (mGpsServiceIsBound) {
            unbindService(mGpsServiceConnection);
            mGpsServiceIsBound = false;
        }
    }

    //UI related methods

    /**
     * This method should only be called once for initializing the behavior of the UserInterface.
     * It only specifies the behaviour of Elements in the startScreen Layout.
     *
     */
    private void createStartscreenUI() {
        final Button finish = (Button) findViewById(R.id.mainActivity_button_quit);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.startscreen_backLayout_vertical);
                if (!layout.isShown()) { //test is running
                    mTestIsRunning = false;
                    layout.setVisibility(View.VISIBLE);
                    finish.setText("SCAN FOR DEVICES");
                    TextView countdown = (TextView) findViewById(R.id.startscreen_text_countdown);
                    countdown.setText("START");
                    DataManager.getInstance().reset(getApplicationContext());
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
                TextView speed = (TextView) findViewById(R.id.startscreen_text_startspeed_value);
                speed.setText(String.valueOf(progress + 6));
                mStartspeed = progress + 6;
                TextView startspeed= (TextView) findViewById(R.id.mainActivity_text_speedNeeded);
                NumberFormat speedFormat = new DecimalFormat("0.0");
                startspeed.setText("/"+speedFormat.format(mStartspeed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * At Call, the countdown starts with a predefined interval of 900ms.
     * The Starting Value is hardcoded with 3000ms.
     * The method is also capable for changing the TextView.
     * On finish the MainActivity is attached to the Observable.
     * @see CountDownTimer
     */
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
                DataManager.getInstance().clear(MainActivity.this.getApplicationContext());
                DataManager.getInstance().attach(MainActivity.this); //observer
            }
        };
        timer.start();
    }

    /**
     * On Call, the fancy Heart image changes.
     * (The heart beats at each call :D)
     */
    private void changeHeartVisualisation() {
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
     * This method is called by {@link #update(String)}.
     * Depending on the param, different components of the mainActivity layout are updated with values
     * out of the {@link DataManager} singleton class.
     *
     * @param msg The {@link Intent} action codes.
     */
    private void updateUI(String msg) {
        DataManager mgr = DataManager.getInstance();
        if (mgr == null) return;

        //update values
        if (msg.equals(GpsService.ACTION_LOCATION_UPDATE) ||msg.equals(ACTION_RESET_LOCATION)) {
            TextView distance = (TextView) findViewById(R.id.mainActivity_text_distance);
            TextView actualSpeed = (TextView) findViewById(R.id.mainActivity_text_speedActual);
            TextView neededSpeed = (TextView) findViewById(R.id.mainActivity_text_speedNeeded);
            NumberFormat distanceFormat = new DecimalFormat("0");
            NumberFormat speedFormat = new DecimalFormat("0.0");
            float neSpe = mStartspeed + (int)mgr.getActualDistance() / 200 * 0.5f;

            neededSpeed.setText("/"+speedFormat.format(neSpe));
            distance.setText(distanceFormat.format(mgr.getActualDistance()));
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
        updateUI(ACTION_RESET_LOCATION );
    }

    @Override
    public void update(String msg) {
        if (msg.equals(BluetoothService.ACTION_INVALID_DEVICE)) {
            Toast.makeText(this, "Please use a device which is able to display the Heart Rate...", Toast.LENGTH_LONG).show();
            return;
        }
        updateUI(msg);
    }
}