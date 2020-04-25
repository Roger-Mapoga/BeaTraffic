package co.za.gmapssolutions.beatraffic.services.location;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;


public class LocationService extends IntentService {
    private static final String TAG = LocationService.class.getSimpleName();
    private LocationManager locationManager;
    private BeatTrafficLocation listener;
    private int SUCCESS = 1;
    public LocationService(){
        super("LocationService");
    }
    @Override
    protected void onHandleIntent(final Intent locationIntent) {
        try {
            ResultReceiver locationReceiver = locationIntent.getParcelableExtra("currentLocation");
            Bundle bundle = new Bundle();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new BeatTrafficLocation();

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);

            bundle.putDouble("currentLatitude",locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude());
            bundle.putDouble("currentLongitude",locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());

            Log.v(TAG,"Last known location : " + locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).toString());
            locationReceiver.send(SUCCESS,bundle);

        }catch (SecurityException e){
            Log.v(TAG,e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "beatTrafficLocation service done", Toast.LENGTH_SHORT).show();
    }
}

