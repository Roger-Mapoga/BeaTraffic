package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.R;
import org.json.JSONArray;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class DisplayForecast implements Runnable {
    private static final String TAG = DisplayForecast.class.getSimpleName();

    private final Context context;
    private final String forecast;
    private final MapView map;
    public DisplayForecast(Context context, MapView map, String forecast){
        this.context = context;
        this.forecast = forecast;
        this.map = map;
    }

    @Override
    public void run() {
        Marker nodeMarker = new Marker(map);
        Drawable nodeIcon = context.getResources().getDrawable(R.drawable.marker_default, context.getResources().newTheme());
        ArrayList<GeoPoint> trafficPoints = new ArrayList<>();
        Polyline trafficOverlay = new Polyline();
        trafficOverlay.setColor(Color.RED);
        trafficOverlay.setWidth(5.0f);
        try {
            JSONArray jsonArray = new JSONArray(forecast);
            for(int i =0; i<jsonArray.length();i++){
                GeoPoint mLocation = new GeoPoint(jsonArray.getJSONObject(i).getDouble("latitude"),
                        jsonArray.getJSONObject(i).getDouble("longitude"));
                trafficPoints.add(mLocation);
//                trafficPoints.clear();
                //nodeMarker.setPosition(mLocation);
//                nodeMarker.setIcon(nodeIcon);
//                map.getOverlays().add(nodeMarker);
//                Marker marker = new Marker(map);
//                marker.setPosition(mLocation);
//                marker.setIcon(nodeIcon);
//                marker.setTitle("Traffic point: "+mLocation.getLatitude() +" : "+mLocation.getLongitude());
//                marker.setDraggable(true);
//                marker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer(map));
//                map.getOverlays().add(marker);

                Log.d(TAG ,"Forecast: "+mLocation.getLatitude() +" : "+mLocation.getLongitude());
            }
            trafficOverlay.setPoints(trafficPoints);

            map.getOverlays().add(trafficOverlay);


            map.invalidate();

//            Road trafficRoad = new Road(trafficPoints);
//            Polyline roadOverlay = RoadManager.buildRoadOverlay(trafficRoad,0xF00000FF,5.0f);
//            map.getOverlays().add(roadOverlay);
//            map.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Forecasted: " + forecast);
        Toast.makeText(context,"Forecasted: " +forecast,Toast.LENGTH_LONG).show();
    }
}
