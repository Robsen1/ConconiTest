/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.fhooe.mc.conconii;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 *
 * @author Robsen & Gix
 */
public class ScanActivity extends ListActivity implements View.OnClickListener {
    //constants
    private static final String TAG = "ScanActivity";
    private static final long SCAN_PERIOD = 10000;

    //variables
    private DeviceListAdapter mDeviceListAdapter = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button mRefresh = null;
    private Handler mHandler;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothDevice mDevice = null;

    //flags
    private boolean mScanning;

    //lifecycle related methods

    /**
     * Creates layout and initializes Bluetooth.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceListAdapter = new DeviceListAdapter();
        mHandler = new Handler();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        setContentView(R.layout.activity_scan);
    }

    /**
     * Starts scanning and initializes list.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mRefresh = (Button) findViewById(R.id.scanActivty_button_refresh);
        mRefresh.setOnClickListener(this);
        setListAdapter(mDeviceListAdapter);
        scanForDevices(true);
    }

    /**
     * Stops scanning and cleans up.
     */
    @Override
    protected void onPause() {
        super.onPause();
        scanForDevices(false);
        mDeviceListAdapter.clear();
    }

    //listener methods

    /**
     * Used to stop scanning and setting result for {@link MainActivity#onActivityResult(int, int, Intent)}.
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mDevice = mDeviceListAdapter.getDevice(position);
        if (mDevice == null) return;
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mDevice.getAddress());
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, mDevice.getName());

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Restarts scanning.
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (!mScanning) {
            scanForDevices(true);
        }
    }

    //UI methods

    /**
     * Sets the visibility of the text.
     *
     * @param enable true -> visible /
     *               false -> gone
     */
    private void setTextVisibility(boolean enable) {
        TextView text = (TextView) findViewById(R.id.scanActivity_text_searching);
        if (enable) {
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }

    }

    /**
     * Rotates the Image in a predefined time interval using {@link CountDownTimer}.
     */
    private void rotateImage() {
        final ImageView refresh = (ImageView) findViewById(R.id.scanActivity_image_refresh);
        CountDownTimer timer = new CountDownTimer(SCAN_PERIOD, 10) {
            int i = 1;

            @Override
            public void onTick(long millisUntilFinished) {
                refresh.setRotation(1 * i++);
                refresh.invalidate();
            }

            @Override
            public void onFinish() {
                refresh.setRotation(0);
                refresh.invalidate();
            }
        };
        timer.start();
    }

    //bluetooth methods

    /**
     * This method is used for starting and stopping the scan.
     * It also calls the {@link #setTextVisibility(boolean)} method.
     *
     * @param enable If true, the scan is started and {@link #rotateImage()} is called.
     *               If false, the scan is stopped.
     */
    private void scanForDevices(final boolean enable) {
        setTextVisibility(enable);
        if (enable) {
            rotateImage();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    setTextVisibility(false);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDeviceListAdapter.addDevice(device);
                            mDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    //inner classes

    /**
     * Adapter for holding devices found through scanning.
     */
    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mDevices;
        private LayoutInflater mInflator;

        public DeviceListAdapter() {
            super();
            mDevices = new ArrayList<BluetoothDevice>();
            mInflator = ScanActivity.this.getLayoutInflater();
        }

        /**
         * Adds a device to ArrayList if the list doesn't contain it.
         *
         * @param device The device to add
         */
        public void addDevice(BluetoothDevice device) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
            }
        }

        /**
         * Getter for the device chosen by the user.
         *
         * @param position The clicked position.
         * @return The device
         */
        public BluetoothDevice getDevice(int position) {
            return mDevices.get(position);
        }

        /**
         * Clears the list
         */
        public void clear() {
            mDevices.clear();
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mDevices.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown Device");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    /**
     * Class for holding the View.
     */
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}