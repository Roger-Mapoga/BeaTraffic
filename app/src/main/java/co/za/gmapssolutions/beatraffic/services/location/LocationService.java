package co.za.gmapssolutions.beatraffic.services.location;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import co.za.gmapssolutions.beatraffic.MainActivity;
import co.za.gmapssolutions.beatraffic.R;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import co.za.gmapssolutions.beatraffic.transition.DetectActivity;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import java.util.Objects;


public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private Task<Void> task;
    private int SUCCESS;
    private Location location;
    private WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    enum ServiceState {
        STARTED,
        STOPPED
    }
    enum Actions {
        START,
        STOP
    }



    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }
    private final IBinder mBinder = new LocationService.LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        Intent mIntentService = new Intent(this, DetectActivity.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesHandler();
        Notification notification = createNotification();
        startForeground(1,notification);
        SUCCESS = 1;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
           try {
               ResultReceiver locationReceiver = intent.getParcelableExtra("currentLocation");
//               BeatTrafficLocation listener = intent.getParcelableExtra("listener");
               Bundle bundle = new Bundle();
               assert locationReceiver != null;
               locationReceiver.send(SUCCESS, bundle);
//               assert listener != null;
//               boolean enabled = listener.enableLocation();
               Log.d(TAG, "onLocationChanged: ");
//               if(enabled){
               startService();

//                       Log.d(TAG, "updateLocation: " + location);
//                       bundle.putDouble("currentLatitude", location.getLatitude());
//                       bundle.putDouble("currentLongitude", location.getLongitude());
//                       bundle.putParcelable("loc", location);
////                       Log.v(TAG, "Last known location: " + listener.getLastKnownLocation());

               //               }
//               if (!shouldIgnore(provider, System.currentTimeMillis()))
////                       if(locationManager.getLastKnownLocation(provider) != null)
//               {
//                   bundle.putDouble("currentLatitude", locationManager.getLastKnownLocation(provider).getLatitude());
//                   bundle.putDouble("currentLongitude", locationManager.getLastKnownLocation(provider).getLongitude());
//                   bundle.putParcelable("loc",locationManager.getLastKnownLocation(provider));
//                   Log.v(TAG, "Last known location : (" + provider + ") " + locationManager.getLastKnownLocation(provider));
//                   assert locationReceiver != null;
//                   locationReceiver.send(SUCCESS, bundle);
//               }
           } catch (SecurityException e) {
               Log.v(TAG, Objects.requireNonNull(e.getMessage()));
           }
        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.
        //stopSelf();
        return START_STICKY;
    }
    private void setServiceState(Context context,ServiceState serviceState){
        //getSharedPreferences()
    }
    @SuppressLint("WakelockTimeout")
    private void startService(){
        if(isServiceStarted) return;
        Log.d(TAG, "Starting the foreground service task");
        isServiceStarted = true;
        setServiceState(this, ServiceState.STARTED);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"LocationService::lock");
        wakeLock.acquire();
    }
    private void stopService(){
        Log.d(TAG, "Stopping the foreground service");
        try{
            if(wakeLock.isHeld()){
                wakeLock.release();
            }
            stopForeground(true);
            stopSelf();
        }catch (Exception e){
            Log.d(TAG, "Service stopped without being started: " + e.getMessage());
        }
        isServiceStarted = false;
        setServiceState(this,ServiceState.STOPPED);
    }
    private Notification createNotification(){
        String notificationChannelId = "ENDLESS SERVICE CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId,
                    "Endless Service notifications channel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Endless Service channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel)  ;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this,notificationChannelId);
        else
            builder = new Notification.Builder(this);
        return builder
                .setContentTitle("Endless Service")
                .setContentText("This is your favorite endless service working")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
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

        task.addOnSuccessListener(result -> Toast.makeText(getApplicationContext(),
                "Successfully requested activity updates",
                Toast.LENGTH_SHORT)
                .show());

        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                "Requesting activity updates failed to start",
                Toast.LENGTH_SHORT)
                .show());
    }

    public void removeActivityUpdatesHandler() {
        task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(result -> Toast.makeText(getApplicationContext(),
                "Removed activity updates successfully!",
                Toast.LENGTH_SHORT)
                .show());

        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
        Log.d(TAG, "The service has been created");
//        Toast.makeText(this,"Location service stopped",Toast.LENGTH_LONG).show();
      //   I want to restart this service again in 5 minutes
//        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarm.set(AlarmManager.RTC_WAKEUP,
//                System.currentTimeMillis() + (1000 * 60),
//                PendingIntent.getService(this, 0, new Intent(this, LocationService.class), 0)
//        );
    }

}

