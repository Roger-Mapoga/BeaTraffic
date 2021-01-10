package co.za.gmapssolutions.beatraffic;

import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class APITests {
    private static final String TAG = APITests.class.getSimpleName();

//    @Test
    public void testForecastJsonParse(){
        try {
            //http://192.168.8.102:8080/location
            URL url = new URL("http://192.168.8.105:8080/location");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            RestClient restClient = new RestClient(url);
            int response = restClient.get();
            if( response == HttpURLConnection.HTTP_OK){
                try {
                    JSONArray jsonArray = new JSONArray(restClient.getData());
                    for(int i =0; i<jsonArray.length();i++){
                        System.out.println(TAG +": "+jsonArray.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println(TAG +": "+"Error " +response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void routesApiTest()  {
        String urlString = "http://192.168.8.105:8080/routes";
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            RestClient restClient = new RestClient(url);
            int response = restClient.get();
            if( response == HttpURLConnection.HTTP_OK){
                JSONObject jsonObject = new JSONObject(restClient.getData());
                System.out.println(TAG +": "+jsonObject);
                System.out.println(TAG + jsonObject.getJSONArray("coordinates").getJSONArray(0));
                int len = jsonObject.getJSONArray("coordinates").getJSONArray(0).length();
                System.out.println(TAG + len);
                ArrayList<GeoPoint> polyline = new ArrayList<GeoPoint>(len/3);
                for(int x =0;x<jsonObject.getJSONArray("coordinates").getJSONArray(0).length();x++){
                    Object test = jsonObject.getJSONArray("coordinates").getJSONArray(0).get(x);

                    String [] temp = test.toString().replaceAll("\\[|\\]"," ") .split(",");
                    GeoPoint geoPoint = new GeoPoint(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
                    polyline.add(geoPoint);
                    System.out.println(geoPoint);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
