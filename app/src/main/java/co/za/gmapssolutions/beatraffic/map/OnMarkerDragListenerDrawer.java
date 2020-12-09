package co.za.gmapssolutions.beatraffic.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener{
    ArrayList<GeoPoint> mTrace;
    Polyline mPolyline;
    MapView map;
   public OnMarkerDragListenerDrawer(MapView map) {
        mTrace = new ArrayList<GeoPoint>(100);
        mPolyline = new Polyline();
        mPolyline.setColor(0xAA0000FF);
        mPolyline.setWidth(2.0f);
        mPolyline.setGeodesic(true);
        this.map = map;
        this.map.getOverlays().add(mPolyline);
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
