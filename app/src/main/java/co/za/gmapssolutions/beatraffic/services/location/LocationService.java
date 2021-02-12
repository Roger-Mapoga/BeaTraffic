package co.za.gmapssolutions.beatraffic.services.location;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import co.za.gmapssolutions.beatraffic.MainActivity;
import co.za.gmapssolutions.beatraffic.R;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.executor.DefaultExecutorSupplier;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.security.SecurePreferences;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import co.za.gmapssolutions.beatraffic.transition.DetectActivity;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.*;


public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private Task<Void> task;
    private int SUCCESS;
    private WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private BeatTrafficLocation location;
    enum ServiceState {
        STARTED,
        STOPPED
    }
    enum Actions {
        START,
        STOP
    }

    String HOST = "http://192.168.8.106";
    RestClient restClient;
    LocationRemoteThread locationRemoteThread;
    ThreadPoolExecutor backGroundThreadPoolExecutor;
    ExecutorService executorService;
    CountDownLatch latch;
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

        DefaultExecutorSupplier defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
        backGroundThreadPoolExecutor = defaultExecutorSupplier.forBackgroundTasks();
        executorService = Executors.newSingleThreadExecutor();
        latch = new CountDownLatch(1);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
           try {
               ResultReceiver locationReceiver = intent.getParcelableExtra("locationReceiver");
               Bundle bundle = new Bundle();
               assert locationReceiver != null;
               locationReceiver.send(SUCCESS, bundle);

              try {
                   URL locUrl = new URL(HOST + ":8080/beatraffic");
                  restClient = new RestClient(locUrl);
                  SecurePreferences preferences = new SecurePreferences(this, "user-info",
                          "YourSecurityKey", true);
                  User user = new User(Long.parseLong(preferences.getString("userId")),preferences.getString("carType"));
                  locationRemoteThread = new LocationRemoteThread(this,user,restClient,latch);
               } catch (MalformedURLException e) {
                   e.printStackTrace();
               }
////
               startService();

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
        if(isServiceStarted) {
            Log.d(TAG, "startService");
            return;
        }
        Log.d(TAG, "Starting the foreground service task");
        isServiceStarted = true;
        setServiceState(this, ServiceState.STARTED);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"LocationService::lock");
        wakeLock.acquire();

//        while(isServiceStarted){
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                if(!executorService.isShutdown()){
//                    Log.i(TAG, "Shutdown reauested");
//                    shutdown();
//                }
//            }));
        backGroundThreadPoolExecutor.submit(locationRemoteThread);

//            try {
//                Log.d(TAG, "latch await");
//                latch.await();
//                Log.d(TAG, "thread stopped");
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }finally {
//                shutdown();
//                Log.i(TAG, "Service closed successfully");
//            }
//        }
    }
    private void shutdown(){
        if(!executorService.isShutdown()){
            Log.d(TAG, "shutting down");
            executorService.shutdown();
            try{
                if(!executorService.awaitTermination(2000, TimeUnit.MICROSECONDS)){
                    Log.w(TAG, "Executor did not terminate in the specified time.");
                    Log.w(TAG,"Executor was abruptly shutdown shut down. "+executorService.shutdownNow().size()
                            +" tasks will not be executed");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
//            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
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
    public void onTaskRemoved(Intent rootIntend){
        Intent restartServiceIntent = new Intent(this,LocationService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(this,1,restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        this.getSystemService(Context.ALARM_SERVICE);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime()+(15*1000),restartServicePendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
        Log.d(TAG, "The service has been destroyed");
        stopService();
        latch.countDown();
//        Toast.makeText(this,"Location service stopped",Toast.LENGTH_LONG).show();
      //   I want to restart this service again in 5 minutes
//        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarm.set(AlarmManager.RTC_WAKEUP,
//                System.currentTimeMillis() + (1000 * 60),
//                PendingIntent.getService(this, 0, new Intent(this, LocationService.class), 0)
//        );
    }

}

