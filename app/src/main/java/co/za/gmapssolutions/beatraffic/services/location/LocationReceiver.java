package co.za.gmapssolutions.beatraffic.services.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import co.za.gmapssolutions.beatraffic.R;
import co.za.gmapssolutions.beatraffic.map.OnMarkerDragListenerDrawer;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationReceiver extends ResultReceiver {
    private int SUCCESS = 1;
    private final Context context;
    private final IMapController mapController;
    private final MapView map;
    private GeoPoint startPoint;
    public LocationReceiver(Handler handler, Context context, MapView map, IMapController mapController){
        super(handler);
        this.context = context;
        this.mapController = mapController;
        this.map = map;
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(SUCCESS == resultCode){
            startPoint = new GeoPoint(resultData.getDouble("currentLatitude"),resultData.getDouble("currentLongitude"));

            mapController.setCenter(startPoint);

            Marker startMarker = new Marker(map);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            startMarker.setIcon(context.getResources().getDrawable(R.drawable.marker_default,context.getResources().newTheme()));
            startMarker.setTitle("Start point");

            startMarker.setDraggable(true);
            startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer(map));
            map.getOverlays().add(startMarker);
            map.invalidate();

        }
        super.onReceiveResult(resultCode, resultData);
    }
    public GeoPoint getStartPoint(){
        return startPoint;
    }
}

