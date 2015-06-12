package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Robsen & Gix
 * This is the service for receiving gps data which is sent to the DataManger
 * This service runs in his own thread and listens for Location updates
 */
public class GpsService extends Service implements LocationListener, Runnable {

    private static final String TAG = "GpsService";
    private LocationManager mLocationManager = null;
    private Thread mGpsThread = null;

    @Override
    public void onCreate() {
        super.onCreate();

        //start requesting location updates
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,   //get update anytime
                5, this);//get update about each 5 meters
        Log.i(TAG, "onCreate()");
        startThread();
    }

    /**
     * starts a new Thread and write log if successful
     */
    private void startThread() {
        mGpsThread = new Thread(this);
        mGpsThread.start();
        if (mGpsThread.isAlive()) {
            Log.i(TAG, "Thread started");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //build and send intent
        Intent update = new Intent(this, DataManager.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("GPS_DATA",location);
        update.putExtras(bundle);
        sendBroadcast(update);
        Log.i(TAG, "Location update sent : " + location);
    }

    @Override
    public void run() {
        while(!MainActivity.testFinished)
        {
            //do nothing just listen for location updates
        }
        mLocationManager.removeUpdates(this); //stop sending location updates
        Log.i(TAG, "Thread stopped");
        DataManager.getInstance().finalize(); //only for testing purposes
        stopSelf();
    }

    //TESTED GPS:
    //speed measurement is really nice
    //distance measurement has to be tested again with navi
    //5 meters update interval is an optimal value, so about each second an intent is received

    //TODO: check whether gps is enabled or not

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
