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
    //constants
    private static final String TAG = "GpsService";
    public static final String ACTION_LOCATION_UPDATE = "blablba";
    public static final String EXTRA_GPS_DATA = "blub";
    public static final String ACTION_PROVIDER_ENABLED = "bubububbs";
    public static final String ACTION_PROVIDER_DISABLED = "gashjfkld";
    public static final String ACTION_GPS_FIXED = "fixed bla";

    //variables
    private LocationManager mLocationManager = null;
    private IBinder mBinder = new LocalBinder();
    private boolean mFirstUpdate =true;

    //lifecycle methods

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mLocationManager.removeUpdates(this);
        return super.onUnbind(intent);
    }

    //listener methods

    @Override
    public void onLocationChanged(Location location) {
        if(mFirstUpdate){
            Intent i = new Intent(ACTION_GPS_FIXED);
            sendBroadcast(i);
            mFirstUpdate =false;
        }
        //build and send intent
        Intent update = new Intent(ACTION_LOCATION_UPDATE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_GPS_DATA, location);
        update.putExtras(bundle);
        sendBroadcast(update);
        Log.i(TAG, "Location update sent : " + location);
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

    //controller methods

    /**
     * Is called to check whether the GPS-provider is enabled or not. Is called
     * by the {@link android.content.ServiceConnection} callback.
     * Additionally sends a broadcast for each result.
     * @return True if enabled, false otherwise
     */
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

    /**
     * Used to request location updates in a hardcoded interval of 5 meters.
     * Is called by the {@link android.content.ServiceConnection} callback.
     */
    public void requestUpdates() {
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,   //get update anytime
                5, this);//get update about each 5 meters}
    }



    //inner classes
    /**
     * This class is for returning an instance of the service
     */
    public class LocalBinder extends Binder {
        GpsService getService() {
            return GpsService.this;
        }
    }
}
