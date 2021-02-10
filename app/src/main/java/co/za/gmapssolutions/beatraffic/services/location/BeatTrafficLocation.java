package co.za.gmapssolutions.beatraffic.services.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class BeatTrafficLocation implements LocationListener{
    private  final String TAG = BeatTrafficLocation.class.getSimpleName();
    private long mLastGps = 0;
    private final LocationManager locationManager;
    private final Set<String> locationSources = new HashSet<>();
    private final Context context;
    private final ILocationConsumer iLocationConsumer;
    public BeatTrafficLocation(Context context,ILocationConsumer iLocationConsumer){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;
        locationSources.add(LocationManager.GPS_PROVIDER);
        locationSources.add(LocationManager.NETWORK_PROVIDER);
        this.iLocationConsumer = iLocationConsumer;
    }
    @SuppressLint("MissingPermission")
    public void enableLocation(){
        for(String provider : locationManager.getProviders(true)){
//            if(!shouldIgnore(provider,System.currentTimeMillis()))
            {
                if (locationSources.contains(provider)) {
                    try {
                        locationManager.requestLocationUpdates(provider, 4000, 10, this);
                    } catch (Throwable e) {
                        Log.e(TAG, "unable to update location from provider " + provider, e);
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location){
        if (shouldIgnore(location.getProvider(), System.currentTimeMillis()))
            return;
//        Intent local = new Intent();
//        local.setAction("co.za.gmapssolutions.beatraffic.services.location");
//        local.putExtra("location", location);
//        context.sendBroadcast(local);
        iLocationConsumer.updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    private boolean shouldIgnore(final String pProvider, final long pTime) {
        long gpsWaitTime = 20000;
        if (LocationManager.GPS_PROVIDER.equals(pProvider)) {
            mLastGps = pTime;
        } else {
            return pTime < mLastGps + gpsWaitTime;
        }
        return false;
    }
}
