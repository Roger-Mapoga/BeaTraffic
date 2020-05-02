package co.za.gmapssolutions.beatraffic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.util.Log;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import com.google.android.gms.location.DetectedActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

public class AutoStart extends BroadcastReceiver {
    private String TAG = AutoStart.class.getSimpleName();
    private ThreadPoolExecutor threadPoolExecutor;
    private URL url;
    private Address destination,departure;
    public AutoStart(ThreadPoolExecutor threadPoolExecutor, URL url, Address departure,Address destination){
        this.threadPoolExecutor = threadPoolExecutor;
        this.url = url;
        this.destination = destination;
        this.departure = departure;
    }
    public void onReceive(Context context, Intent intent) {
       // context.startService(new Intent(context, LocationService.class));
        if(intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)){
            int type = intent.getIntExtra("type", -1);
            int confidence = intent.getIntExtra("confidence", 0);

            Toast.makeText(context,"Activity type: " + type+" , confidence : " + confidence,Toast.LENGTH_LONG).show();

            Log.i(TAG,"Action : "+ type +" , confidence : "+confidence);
            if(DetectedActivity.STILL == type){
                User user = new User(1,"car");
                KafkaProducerRestClient producerRestClient = new KafkaProducerRestClient(url, user,departure,destination,new JSONArray());
                threadPoolExecutor.submit(producerRestClient);
            }
        }
//        if (ActivityTransitionResult.hasResult(intent)) {
//            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
//            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
//
//                // chronological sequence of events....
//                if(event.getActivityType() == DetectedActivity.UNKNOWN) {
//                   // context.startService(new Intent(context, LocationService.class));
//                    Log.d(TAG, "Activity detected ."+event.getActivityType());
//                }
//            }
//        }

    }
}