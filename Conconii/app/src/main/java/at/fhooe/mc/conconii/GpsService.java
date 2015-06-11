package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import java.io.Serializable;

/**
 * Created by Robsen & Gix
 */
public class GpsService extends Service implements LocationListener,Runnable{

    private LocationManager mLocationManager = null;
    protected boolean isGpsEnabled=false;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500,   // 0.5s
                0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Intent update = new Intent(this,DataManager.class);
        update.putExtra("GPS_Data",location);
        sendBroadcast(update);
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
        isGpsEnabled=true;

    }

    @Override
    public void onProviderDisabled(String provider) {
        //TODO:
        // activate gps

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void run() {
        //endlosschleife?
    }
}
