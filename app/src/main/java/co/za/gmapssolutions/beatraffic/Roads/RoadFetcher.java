package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import co.za.gmapssolutions.beatraffic.R;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class RoadFetcher implements Runnable {
    private static final String TAG = RoadFetcher.class.getSimpleName();
    private final Context context;
    private final GeoPoint startPoint;
    private final GeoPoint endPoint;
    private final MapView map;
    private final RoadManager roadManager;
    private final Handler handler;
    private Road[] road = new Road[10];
    private final Bundle bundle = new Bundle();
    private final DisplayRoutes displayRoutes;
    private final MyLocation myLocation;
    private final IMapController mapController;
    public RoadFetcher(Context context, Handler handler, MapView map, IMapController mapController, RoadManager roadManager, GeoPoint startPoint,
                       GeoPoint endPoint, MyLocation myLocation, DisplayRoutes displayRoutes){
        this.context = context;
        this.handler = handler;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.map = map;
        this.mapController = mapController;
        this.roadManager = roadManager;
        this.displayRoutes = displayRoutes;
        this.myLocation = myLocation;
    }
    @Override
    public void run(){
        //roadManager = new OSRMRoadManager(context);
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(startPoint);
        routePoints.add(endPoint);
        map.getOverlays().clear();
        Location mLocation = new Location("");
        mLocation.setLatitude(startPoint.getLatitude());
        mLocation.setLongitude(startPoint.getLongitude());
        myLocation.setMyLocation(mLocation);

        //setMarker(startPoint,"Start point");
        setMarker(endPoint,"End point");

        Message msg = handler.obtainMessage();
        roadManager.addRequestOption("alternatives=10");
        road = roadManager.getRoads(routePoints);

//        Drawable nodeIcon = context.getResources().getDrawable(R.drawable., context.getResources().newTheme());
//        int d = 0;
//        double [] distance = new double[road.length];
        displayRoutes.show(road);
//        mapController.setZoom(9.3f);
//        map.invalidate();
        map.postInvalidate();
        map.getOverlays().add(myLocation);
        bundle.putString("get-roads","done");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
    public Road[] getRoutes() {
        return road;
    }
    private void setMarker(GeoPoint point,String message){
        Marker startMarker = new Marker(map);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(context.getResources().getDrawable(R.drawable.marker_default,context.getResources().newTheme()));
        startMarker.setTitle(message);
        if(point != null)
        startMarker.setPosition(point);
        map.getOverlays().add(startMarker);
    }
}
