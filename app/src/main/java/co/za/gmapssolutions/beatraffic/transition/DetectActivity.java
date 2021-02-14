package co.za.gmapssolutions.beatraffic.transition;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import static co.za.gmapssolutions.beatraffic.transition.Constants.BROADCAST_DETECTED_ACTIVITY;

public class DetectActivity extends IntentService {
    protected static final String TAG = DetectActivity.class.getSimpleName();
    public DetectActivity(){
        super(TAG);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();
        for (DetectedActivity activity : detectedActivities) {
            broadcastActivity(activity);
        }
    }
    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
//        Log.i(TAG, "broadcastActivity: "+activity.getType());
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        sendBroadcast(intent);
    }
}
