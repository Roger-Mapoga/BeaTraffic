package co.za.gmapssolutions.beatraffic.services.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class LocationReceiver extends ResultReceiver {
    private final Context context;
    private final IMapController mapController;
    private final MapView map;
    private GeoPoint startPoint;
    private final MyLocation myLocation;
    BroadcastReceiver broadcastReceiver;

    //    private final AutoStart detectedActivity;
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
            startPoint = new GeoPoint(resultData.getDouble("currentLatitude"),resultData.getDouble("currentLongitude"));
            IntentFilter filter = new IntentFilter();
            filter.addAction("co.za.gmapssolutions.beatraffic.services.UserActivityType");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent != null) {
                        myLocation.setUserActivity(intent.getIntExtra("UserActivityType", -1));
                        myLocation.setUserConfidence(intent.getIntExtra("UserActivityConfidence", -1));
                    }
                }
            };
            context.registerReceiver(broadcastReceiver,filter);


            myLocation.setMyLocation(resultData.getParcelable("loc"));
            mapController.animateTo(startPoint);

           // Log.d("TAG", "onReceiveResult: "+resultData.getString());
           // myLocation.draw(new Canvas(),map.getProjection());
            //-26.20132  //28.04044
//           startPoint.setLatitude(-26.20132);
//           startPoint.setLongitude(28.04044);
//            mapController.setCenter(startPoint);
//
//            Marker startMarker = new Marker(map);
//            startMarker.setPosition(startPoint);
//            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
////
//            startMarker.setIcon(context.getResources().getDrawable(R.drawable.marker_default,context.getResources().newTheme()));
//            startMarker.setTitle("Start point - "+startPoint.getLongitude() +" : "+startPoint.getLatitude() );
//
//            startMarker.setDraggable(true);
//            startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer(map));
//            map.getOverlays().add(startMarker);
           // map.invalidate();
            map.postInvalidate();
            map.getOverlays().add(myLocation);
        }
        super.onReceiveResult(resultCode, resultData);
    }

    public GeoPoint getStartPoint(){
        return startPoint;
    }
}

