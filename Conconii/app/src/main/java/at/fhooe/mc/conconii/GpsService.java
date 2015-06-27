package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Robsen & Gix
 * This is the service for receiving gps data which is sent to the DataManger
 * This service runs in his own thread and listens for Location updates
 */
public class GpsService extends Service implements LocationListener {

    private static final String TAG = "GpsService";
    public static final String ACTION_LOCATION_UPDATE = "blablba";
    public static final String EXTRA_GPS_DATA = "blub";
    public static final String ACTION_PROVIDER_ENABLED = "bububuiufbbs";
    public static final String ACTION_PROVIDER_DISABLED = "gashjfkld";
    public static boolean gpsIsConnected = false;
    private LocationManager mLocationManager = null;
    private IBinder mBinder = new LocalBinder();


    @Override
    public void onLocationChanged(Location location) {
        gpsIsConnected = true;
        //build and send intent
        Intent update = new Intent(ACTION_LOCATION_UPDATE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_GPS_DATA, location);
        update.putExtras(bundle);
        sendBroadcast(update);
        Log.i(TAG, "Location update sent : " + location);
    }

    //TODO: check whether gps is enabled or not

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            Intent i = new Intent(ACTION_PROVIDER_ENABLED);
            sendBroadcast(i);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            Intent i = new Intent(ACTION_PROVIDER_DISABLED);
            sendBroadcast(i);
        }
    }

    public boolean isEnabled() {
        //start requesting location updates
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            sendBroadcast(new Intent(ACTION_PROVIDER_ENABLED));
            return true;
        } else {
            sendBroadcast(new Intent(ACTION_PROVIDER_DISABLED));
            return false;
        }
    }

    public void requestUpdates() {
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,   //get update anytime
                5, this);//get update about each 5 meters}
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mLocationManager.removeUpdates(this);
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        GpsService getService() {
            return GpsService.this;
        }
    }
}
