package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import co.za.gmapssolutions.beatraffic.R;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class RoadFetcher implements Runnable {
    private static String TAG = RoadFetcher.class.getSimpleName();
    private Context context;
    private GeoPoint startPoint;
    private GeoPoint endPoint;
    private MapView map;
    private RoadManager roadManager;
    private  Handler handler;
    private Message msg;
    Bundle bundle = new Bundle();
    public RoadFetcher(Context context,Handler handler, MapView map, RoadManager roadManager,GeoPoint startPoint, GeoPoint endPoint){
        this.context = context;
        this.handler = handler;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.map = map;
        this.roadManager = roadManager;
    }
    @Override
    public void run(){
        roadManager = new OSRMRoadManager(context);
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(startPoint);
        routePoints.add(endPoint);

        msg = handler.obtainMessage();

        Road[]road = roadManager.getRoads(routePoints);

        Drawable nodeIcon = context.getResources().getDrawable(R.drawable.marker_cluster, context.getResources().newTheme());
        for(int a =0; a < road.length;a++) {
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road[a]);

            map.getOverlays().add(roadOverlay);
            for (int i = 0; i < road[a].mNodes.size(); i++) {
                RoadNode node = road[a].mNodes.get(i);
                Marker nodeMarker = new Marker(map);
                nodeMarker.setPosition(node.mLocation);
                nodeMarker.setIcon(nodeIcon);
                nodeMarker.setTitle("Step " + i);
                map.getOverlays().add(nodeMarker);
                nodeMarker.setSnippet(node.mInstructions);
                nodeMarker.setSubDescription(Road.getLengthDurationText(context, node.mLength, node.mDuration));
                Drawable icon = context.getResources().getDrawable(R.drawable.osm_ic_center_map, context.getResources().newTheme());
                nodeMarker.setImage(icon);

//          node.mManeuverType
            }
        }
        map.invalidate();
        bundle.putString("get-roads","done");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
