package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import co.za.gmapssolutions.beatraffic.R;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

public class DisplayRoutes {
    private Context context;
    private MapView map;
    public DisplayRoutes(Context context,MapView map){
        this.context = context;
        this.map = map;
    }
    public void show(Road[] road){
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
//                if(node.mLength > 0) {
//                    distance[d] += node.mLength;
//                    //Log.d(TAG,"Distance: "+node.mLength + " Size: "+ distance.length);
//                }
//          node.mManeuverType
            }
            //d++;
        }
    }
}
