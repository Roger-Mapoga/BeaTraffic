package co.za.gmapssolutions.beatraffic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import co.za.gmapssolutions.beatraffic.transition.Constants;


public class AutoStart extends BroadcastReceiver {
    private String TAG = AutoStart.class.getSimpleName();
    int type;
    int confidence;
    @Override
    public void onReceive(Context context, Intent intent) {
       // context.startService(new Intent(context, LocationService.class));
        if(intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)){
            type = intent.getIntExtra("type", -1);
            confidence = intent.getIntExtra("confidence", 0);

            Toast.makeText(context,"Activity type: " + type+" , confidence : " + confidence,Toast.LENGTH_LONG).show();
            Log.d(TAG, "onReceive: sending location to server ");
        }else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            context.startService(new Intent(context, LocationService.class));
        }
    }
    public int getType(){
        return type;
    }
    public int getConfidence(){
        return confidence;
    }
}