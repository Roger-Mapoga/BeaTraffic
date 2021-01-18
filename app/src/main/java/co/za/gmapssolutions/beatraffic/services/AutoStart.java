package co.za.gmapssolutions.beatraffic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import com.google.android.gms.location.DetectedActivity;


public class AutoStart extends BroadcastReceiver {
    private final String TAG = AutoStart.class.getSimpleName();
    private String activityType;
    @Override
    public void onReceive(Context context, Intent intent) {
       // context.startService(new Intent(context, LocationService.class));
        if(intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)){
            int type = intent.getIntExtra("type", -1);
            int confidence = intent.getIntExtra("confidence", 0);
            switch (type){
                case DetectedActivity.IN_VEHICLE:{
                    activityType = "IN_VEHICLE";
                }
                case DetectedActivity.STILL:{
                    activityType = "STILL";
                }
                case DetectedActivity.WALKING:{
                    activityType = "WALKING";
                }
                case DetectedActivity.ON_BICYCLE:{
                    activityType = "ON_BICYCLE";
                }
                case DetectedActivity.ON_FOOT:{
                    activityType = "ON_FOOT";
                }
                case DetectedActivity.RUNNING:{
                    activityType = "RUNNING";
                }
                case DetectedActivity.TILTING:{
                    activityType = "TILTING";
                }
                case DetectedActivity.UNKNOWN:{
                    activityType = "UNKNOWN";
                }
            }
            Intent local = new Intent();
            local.setAction("co.za.gmapssolutions.beatraffic.services.UserActivityType");
            local.putExtra("UserActivityType", type);
            local.putExtra("UserActivityTypeString",activityType);
            local.putExtra("UserActivityConfidence", confidence);
            context.sendBroadcast(local);
            Toast.makeText(context,"Activity type: " + activityType+" , confidence: " + confidence,Toast.LENGTH_LONG).show();
//            Log.d(TAG, "onReceive: sending location to server ");
        }else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            context.startService(new Intent(context, LocationService.class));
        }
    }
}