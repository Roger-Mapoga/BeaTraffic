package co.za.gmapssolutions.beatraffic.Roads;

import android.util.Log;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RoadManager {
    private static final String TAG = RoadManager.class.getSimpleName();
    private String url,postUrl;
    public RoadManager(String url,String postUrl){
        this.url = url;
        this.postUrl = postUrl;
    }
    public List<GeoPoint> getRoads(ArrayList<GeoPoint> routePoints ){
//        ArrayList<GeoPoint> polyline = new ArrayList<GeoPoint>(len/3);
        List<GeoPoint> polyline = new ArrayList<GeoPoint>();

        //Road[] roads = new Road[1];
        //Road road = new Road();
        try {
            URL url = new URL(this.url);
            URL postUrl = new URL(this.postUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            HttpURLConnection postCon = (HttpURLConnection) postUrl.openConnection();
            RestClient restClient = new RestClient(con);
            RestClient postRestClient = new RestClient(postCon);
//            Log.d(TAG,waypoints);

            if(postRestClient.post(routePoints.toString().replaceAll("\\[|\\]", " ")) == HttpURLConnection.HTTP_OK) {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                int response = restClient.get();
                if (response == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonObject = new JSONObject(restClient.getData());

                    System.out.println(TAG + ": " + jsonObject);
                    int len = jsonObject.getJSONArray("coordinates").getJSONArray(0).length();
                    System.out.println(TAG + len);
                    for (int x = 0; x < jsonObject.getJSONArray("coordinates").getJSONArray(0).length(); x++) {
                        Object test = jsonObject.getJSONArray("coordinates").getJSONArray(0).get(x);

                        String[] temp = test.toString().replaceAll("\\[|\\]", " ").split(",");
                        GeoPoint geoPoint = new GeoPoint(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
                        Log.d(TAG, "getRoads: "+geoPoint);
                        polyline.add(geoPoint);
                    }
//                    road.mStatus = Road.STATUS_OK;
//                    road.mRouteHigh = polyline;
//                    road.mBoundingBox = BoundingBox.fromGeoPoints(polyline);
//                    roads[0] = road;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }
    public static Polyline buildRoadOverlay(Road road, int color, float width){ //0x800000FF, 5.0f
        Polyline roadOverlay = new Polyline();
        roadOverlay.setColor(color);
        roadOverlay.setWidth(width);
        if (road != null) {
            ArrayList<GeoPoint> polyline = road.mRouteHigh;
            roadOverlay.setGeodesic(true);
            roadOverlay.setPoints(polyline);
        }
        return roadOverlay;
    }

}
