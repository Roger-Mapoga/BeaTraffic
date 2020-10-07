package co.za.gmapssolutions.beatraffic.services.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class LocationReceiver extends ResultReceiver {
    private int SUCCESS = 1;
    private Context context;
    private IMapController mapController;
    private MapView map;
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
            startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
            map.getOverlays().add(startMarker);
            map.invalidate();

        }
        super.onReceiveResult(resultCode, resultData);
    }
    public GeoPoint getStartPoint(){
        return startPoint;
    }

    private class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {

        ArrayList<GeoPoint> mTrace;
        Polyline mPolyline;

        OnMarkerDragListenerDrawer() {
            mTrace = new ArrayList<GeoPoint>(100);
            mPolyline = new Polyline();
            mPolyline.setColor(0xAA0000FF);
            mPolyline.setWidth(2.0f);
            mPolyline.setGeodesic(true);
            map.getOverlays().add(mPolyline);
        }

        @Override public void onMarkerDrag(Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        @Override public void onMarkerDragEnd(Marker marker) {
            mTrace.add(marker.getPosition());
            mPolyline.setPoints(mTrace);
            map.invalidate();
        }

        @Override public void onMarkerDragStart(Marker marker) {
            //mTrace.add(marker.getPosition());
        }
    }
}

