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
 */
public class GpsService extends Service implements LocationListener, Runnable {


    //TEST GPS:
    //speed measurement is really nice
    //distance measurement has to be tested again with navi
    //5 meters update interval is optimal value, so each second an intent is received

    private static final String TAG = "GpsService";
    private LocationManager mLocationManager = null;
    protected boolean isGpsEnabled = false;
    private Thread mGpsThread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,   // 0.5s
                5, this);
        Log.i(TAG, "onCreate()");
        startThread();
    }

    private void startThread() {
        mGpsThread = new Thread(this);
        mGpsThread.start();
        if (mGpsThread.isAlive()) {
            Log.i(TAG, "Thread started");
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Intent update = new Intent(this, DataManager.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("GPS_DATA",location);
        update.putExtras(bundle);
        sendBroadcast(update);
        Log.i(TAG, "Location update sent : " + location);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        isGpsEnabled = true;

    }

    @Override
    public void onProviderDisabled(String provider) {
        //TODO:
        // activate gps

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

    }


    @Override
    public void run() {
        while(!MainActivity.testFinished)
        {
            //Do nothing
        }
        mLocationManager.removeUpdates(this);
        Log.i(TAG, "Thread stopped");
        DataManager.getInstance().finalize();
        stopSelf();
    }
}
