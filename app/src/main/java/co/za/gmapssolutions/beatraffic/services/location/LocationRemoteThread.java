package co.za.gmapssolutions.beatraffic.services.location;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import co.za.gmapssolutions.beatraffic.domain.BeatrafficLocation;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import com.google.android.gms.location.DetectedActivity;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static co.za.gmapssolutions.beatraffic.domain.BeatrafficLocation.locationToJson;
import static co.za.gmapssolutions.beatraffic.transition.Constants.BROADCAST_DETECTED_ACTIVITY;
import static co.za.gmapssolutions.beatraffic.transition.Constants.CONFIDENCE;

public class LocationRemoteThread implements Runnable, LocationListener {
    private final String TAG = LocationRemoteThread.class.getSimpleName();
    private long mLastGps = 0;
    private final RestClient restClient;
    private Location location = null;
    private final Context context;
    private final CountDownLatch latch;
    private final User user;
    private int UserActivityType = -1;
    private int UserActivityTypeConfidence = -1;
    @SuppressLint("MissingPermission")
    public LocationRemoteThread(Context context, User user, RestClient restClient, CountDownLatch latch){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.restClient = restClient;
        this.context = context;
        this.user = user;
        this.latch = latch;
        Set<String> locationSources = new HashSet<>();
        locationSources.add(LocationManager.GPS_PROVIDER);
        locationSources.add(LocationManager.NETWORK_PROVIDER);
        for(String provider : locationManager.getProviders(true)){
//            if(!shouldIgnore(provider,System.currentTimeMillis()))
            {
                if (locationSources.contains(provider)) {
                    try {
                        locationManager.requestLocationUpdates(provider, 4000, 10, this);
                        location = new Location(provider);
                    } catch (Throwable e) {
                        Log.e(TAG, "unable to update location from provider " + provider, e);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        IntentFilter filter = new IntentFilter(BROADCAST_DETECTED_ACTIVITY);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null) {
                    UserActivityType = intent.getIntExtra("type", -1);
                    UserActivityTypeConfidence = intent.getIntExtra("confidence", -1);
                    Log.i(TAG, "type: "+UserActivityType +" , confidence: " + UserActivityTypeConfidence);
                    if(UserActivityType == DetectedActivity.IN_VEHICLE && UserActivityTypeConfidence >= CONFIDENCE) {
                        Log.i(TAG, "Thread is running" + UserActivityType);
                        try {
                            BeatrafficLocation l = new BeatrafficLocation(user, location);
                            HttpURLConnection con = restClient.post(locationToJson(l).toString());
                            if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
                                Thread.sleep( 1000);
                            }
                        } catch (IOException | InterruptedException | JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Log.i(TAG, "Thread is running");
                    }
                }
            }
        };
        context.registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (shouldIgnore(location.getProvider(), System.currentTimeMillis()))
            return;
//        Intent local = new Intent();
//        local.setAction("co.za.gmapssolutions.beatraffic.services.location");
//        local.putExtra("location", location);
//        context.sendBroadcast(local);
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
