package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

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
    private final IMapController mapController;
    public RoadFetcher(Context context, Handler handler, MapView map, IMapController mapController, RoadManager roadManager, GeoPoint startPoint,
                       GeoPoint endPoint, DisplayRoutes displayRoutes){
        this.context = context;
        this.handler = handler;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.map = map;
        this.mapController = mapController;
        this.roadManager = roadManager;
        this.displayRoutes = displayRoutes;
    }
    @Override
    public void run(){
        //roadManager = new OSRMRoadManager(context);
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(startPoint);
        routePoints.add(endPoint);
        map.getOverlays().clear();

        displayRoutes.setStartPointIcon(startPoint);
        //setMarker(startPoint,"Start point");
        displayRoutes.setMarker(endPoint,"End point");

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
        bundle.putString("get-roads","done");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
    public Road[] getRoutes() {
        return road;
    }

}
