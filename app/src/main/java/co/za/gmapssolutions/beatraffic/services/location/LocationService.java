package co.za.gmapssolutions.beatraffic.services.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import co.za.gmapssolutions.beatraffic.transition.DetectActivity;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private LocationManager locationManager;
    private BeatTrafficLocation listener;
    private Intent mIntentService;
    private final Set<String> locationSources = new HashSet<>();
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private final IBinder mBinder = new LocationService.LocalBinder();
    private Task<Void> task;
    private int SUCCESS;
    private long mLastGps = 0;
    private long gpsWaitTime =20000;
    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectActivity.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesHandler();
        locationSources.add(LocationManager.GPS_PROVIDER);
        locationSources.add(LocationManager.NETWORK_PROVIDER);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new BeatTrafficLocation();
        SUCCESS = 1;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
           try {
                   ResultReceiver locationReceiver = intent.getParcelableExtra("currentLocation");
                   Bundle bundle = new Bundle();
//                   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
//                    locationManager.getAllProviders();
//                    locationManager.getProviders(true);
               for(String provider : locationManager.getProviders(true)){
                   if(locationSources.contains(provider)){
                       if (!shouldIgnore(provider, System.currentTimeMillis())) {
                           locationManager.requestLocationUpdates(provider, 4000, 0, listener);
                           bundle.putDouble("currentLatitude", locationManager.getLastKnownLocation(provider).getLatitude());
                           bundle.putDouble("currentLongitude", locationManager.getLastKnownLocation(provider).getLongitude());
                           bundle.putParcelable("loc",locationManager.getLastKnownLocation(provider));
                           Log.v(TAG, "Last known location : (" + provider + ") " + locationManager.getLastKnownLocation(provider).toString());
                           assert locationReceiver != null;
                           locationReceiver.send(SUCCESS, bundle);
                       }
                   }
               }
           } catch (SecurityException e) {
               Log.v(TAG, Objects.requireNonNull(e.getMessage()));
           }
        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.
        //stopSelf();
        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    protected boolean shouldIgnore(final String pProvider, final long pTime) {

        if (LocationManager.GPS_PROVIDER.equals(pProvider)) {
            mLastGps = pTime;
        } else {
            if (pTime < mLastGps + gpsWaitTime) {
                return true;
            }
        }

        return false;
    }
    public void requestActivityUpdatesHandler() {
        task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "Successfully requested activity updates",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Requesting activity updates failed to start",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public void removeActivityUpdatesHandler() {
        task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "Removed activity updates successfully!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
//        Toast.makeText(this,"Location service stopped",Toast.LENGTH_LONG).show();
      //   I want to restart this service again in 5 minutes
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(alarm.RTC_WAKEUP,
                System.currentTimeMillis() + (1000 * 60),
                PendingIntent.getService(this, 0, new Intent(this, LocationService.class), 0)
        );
    }

}

