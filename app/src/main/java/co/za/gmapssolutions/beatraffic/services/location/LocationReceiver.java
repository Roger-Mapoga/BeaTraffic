package co.za.gmapssolutions.beatraffic.services.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;

public class LocationReceiver extends ResultReceiver{
    private final String TAG = LocationReceiver.class.getSimpleName();
    private final Context context;
    private final IMapController mapController;
    private final MapView map;
    private final MyLocation myLocation;
    BroadcastReceiver broadcastReceiver;
    public LocationReceiver(Handler handler, Context context, MapView map, IMapController mapController,MyLocation myLocation) {
        super(handler);
        this.context = context;
        this.mapController = mapController;
        this.map = map;
        this.myLocation = myLocation;
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        int SUCCESS = 1;
        if(SUCCESS == resultCode){
            Log.i(TAG, "onReceiveResult: ");
            IntentFilter filter = new IntentFilter();
            filter.addAction("co.za.gmapssolutions.beatraffic.services.UserActivityType");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent != null) {
                        Log.d(TAG, "userActivity: "+intent.getIntExtra("UserActivityType", -1));

                        myLocation.setUserActivity(intent.getIntExtra("UserActivityType", -1));
                        myLocation.setUserConfidence(intent.getIntExtra("UserActivityConfidence", -1));
                    }
                }
            };
            context.registerReceiver(broadcastReceiver,filter);
        }
        super.onReceiveResult(resultCode, resultData);
    }
}

