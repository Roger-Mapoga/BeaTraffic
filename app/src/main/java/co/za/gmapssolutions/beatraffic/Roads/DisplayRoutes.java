package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import co.za.gmapssolutions.beatraffic.BeaTrafficViewModel;
import co.za.gmapssolutions.beatraffic.R;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import co.za.gmapssolutions.beatraffic.services.location.ILocationConsumer;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class DisplayRoutes implements ILocationConsumer {
    private final String TAG = DisplayRoutes.class.getSimpleName();
    private final Context context;
    private final MapView map;
    private final MyLocation myLocation;
    private final BeaTrafficViewModel viewModel;

    public DisplayRoutes(Context context, MapView map, MyLocation myLocation, BeaTrafficViewModel viewModel){
        this.context = context;
        this.map = map;
        this.myLocation =myLocation;
        this.viewModel = viewModel;
    }
    public void show(Road[] road,Context context){
        for (Road value : road) {
            Polyline roadOverlay = RoadManager.buildRoadOverlay(value);
            map.getOverlays().add(roadOverlay);
            for (int i = 0; i < value.mNodes.size(); i++) {
                RoadNode node = value.mNodes.get(i);
                Marker nodeMarker = new Marker(map,context);
                nodeMarker.setPosition(node.mLocation);
//                nodeMarker.setIcon(null);
                nodeMarker.setVisible(false);
                nodeMarker.setTitle("Step " + i);
                map.getOverlays().add(nodeMarker);
                nodeMarker.setSnippet(node.mInstructions);
                nodeMarker.setSubDescription(Road.getLengthDurationText(context, node.mLength, node.mDuration));
                Drawable icon = context.getResources().getDrawable(R.drawable.osm_ic_center_map, context.getResources().newTheme());
                nodeMarker.setImage(icon);
//                if(node.mLength > 0) {
//                    distance[d] += node.mLength;
//                    //Log.d(TAG,"Distance: "+node.mLength + " Size: "+ distance.length);
//                }
//          node.mManeuverType
            }
            //d++;
        }
    }
    public void setMarker(GeoPoint point, String message){
        Marker startMarker = new Marker(map);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(context.getResources().getDrawable(R.drawable.marker_default,context.getResources().newTheme()));
        startMarker.setTitle(message);
        if(point != null)
            startMarker.setPosition(point);
        map.getOverlays().add(startMarker);
    }
    public void setStartPointIcon(GeoPoint startPoint){
        Location mLocation = new Location("");
        mLocation.setLatitude(startPoint.getLatitude());
        mLocation.setLongitude(startPoint.getLongitude());
        myLocation.setMyLocation(mLocation);
        map.getOverlays().add(myLocation);
        map.invalidate();
    }
    @Override
    public void updateLocation(Location location) {
        if (location != null) {
            map.getOverlays().clear();
            List<GeoPoint> currGeoPoint = new ArrayList<>();
            myLocation.setMyLocation(location);
            map.getOverlays().add(myLocation);
            if (viewModel.getRoutes().getValue() != null) {
                show(viewModel.getRoutes().getValue(), context);
                setMarker(viewModel.getEndPoint().getValue(), "End point");
            }
            if (viewModel.getRoute().getValue() == null) {
                map.getController().animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
            } else if (viewModel.getRoute().getValue().getBtnRouteStateValue().equals("start")) {
                currGeoPoint.add(new GeoPoint(location.getLatitude(), location.getLongitude()));
                currGeoPoint.add(viewModel.getEndPoint().getValue());
                map.zoomToBoundingBox(viewModel.getBoundingBox(currGeoPoint), true, 250);
            } else if (viewModel.getRoute().getValue().getBtnRouteStateValue().equals("cancel")) {
                currGeoPoint.add(new GeoPoint(location.getLatitude(), location.getLongitude()));
                map.zoomToBoundingBox(viewModel.getBoundingBox(currGeoPoint), true, 10, 18.0, 10L);
            }
            map.postInvalidate();
        }
    }
}
