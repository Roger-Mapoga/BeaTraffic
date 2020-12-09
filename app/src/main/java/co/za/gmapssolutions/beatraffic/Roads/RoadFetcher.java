package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import co.za.gmapssolutions.beatraffic.R;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class RoadFetcher implements Runnable {
    private static final String TAG = RoadFetcher.class.getSimpleName();
    private final Context context;
    private final GeoPoint startPoint;
    private final GeoPoint endPoint;
    private final MapView map;
    private final RoadManager roadManager;
    private final Handler handler;
    private Road[] road = new Road[2];
    private final Bundle bundle = new Bundle();
    public RoadFetcher(Context context, Handler handler, MapView map, RoadManager roadManager, GeoPoint startPoint,
                       GeoPoint endPoint){
        this.context = context;
        this.handler = handler;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.map = map;
        this.roadManager = roadManager;
    }
    @Override
    public void run(){
        //roadManager = new OSRMRoadManager(context);
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(startPoint);
        routePoints.add(endPoint);
        map.getOverlays().clear();
        setMarker(startPoint,"Start point");
        setMarker(endPoint,"End point");

        Message msg = handler.obtainMessage();
        roadManager.addRequestOption("alternatives=10");
        Road[]road = roadManager.getRoads(routePoints);
//        Drawable nodeIcon = context.getResources().getDrawable(R.drawable., context.getResources().newTheme());
        int d = 0;
        double [] distance = new double[road.length];
        for (Road value : road) {
            Polyline roadOverlay = RoadManager.buildRoadOverlay(value);
            map.getOverlays().add(roadOverlay);

            for (int i = 0; i < value.mNodes.size(); i++) {
                RoadNode node = value.mNodes.get(i);
                Marker nodeMarker = new Marker(map);
                nodeMarker.setPosition(node.mLocation);
//                nodeMarker.setIcon(null);
                nodeMarker.setVisible(false);
                nodeMarker.setTitle("Step " + i);
                map.getOverlays().add(nodeMarker);
                nodeMarker.setSnippet(node.mInstructions);
                nodeMarker.setSubDescription(Road.getLengthDurationText(context, node.mLength, node.mDuration));
                Drawable icon = context.getResources().getDrawable(R.drawable.osm_ic_center_map, context.getResources().newTheme());
                nodeMarker.setImage(icon);
                if(node.mLength > 0) {
                    distance[d] += node.mLength;
                    //Log.d(TAG,"Distance: "+node.mLength + " Size: "+ distance.length);
                }
//          node.mManeuverType
            }
            d++;
        }
        map.invalidate();
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
        startMarker.setPosition(point);
        map.getOverlays().add(startMarker);
    }
}
