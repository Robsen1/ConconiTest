package at.fhooe.mc.conconii;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by Robsen & Gix
 */
public class GpsService extends Service implements LocationListener{

    private LocationManager mLocationManager=null;
    private Location mLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                500,   // 0.5s
                0, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
           //endlosschleife
            //mLocation intent


        return START_STICKY;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;

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

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
