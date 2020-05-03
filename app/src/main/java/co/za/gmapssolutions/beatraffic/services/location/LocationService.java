package co.za.gmapssolutions.beatraffic.services.location;

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


public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private LocationManager locationManager;
    private BeatTrafficLocation listener;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private IBinder mBinder = new LocationService.LocalBinder();
    private Task<Void> task;
    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }
    private int SUCCESS = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectActivity.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesHandler();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            ResultReceiver locationReceiver = intent.getParcelableExtra("currentLocation");
            Bundle bundle = new Bundle();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new BeatTrafficLocation();

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);

            bundle.putDouble("currentLatitude",locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude());
            bundle.putDouble("currentLongitude",locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());

            Log.v(TAG,"Last known location : " + locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString());
            locationReceiver.send(SUCCESS,bundle);

        }catch (SecurityException e){
            Log.v(TAG,e.getMessage());
        }


        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
//        Toast.makeText(this,"Location service stopped",Toast.LENGTH_LONG).show();
        // I want to restart this service again in one hour
//        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarm.set(
//                alarm.RTC_WAKEUP,
//                System.currentTimeMillis() + (1000 * 60),
//                PendingIntent.getService(this, 0, new Intent(this, LocationService.class), 0)
//        );
    }
//    @Override
//    protected void onHandleIntent(final Intent locationIntent) {

//    }

}

