package co.za.gmapssolutions.beatraffic.restClient;

import co.za.gmapssolutions.beatraffic.domain.Location;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class KafkaProducerRestClient implements Runnable{
    private URL url;
    private HttpURLConnection con;
    private Location location;
    private JSONObject jsonObject;
    public KafkaProducerRestClient(URL url,Location location,JSONObject jsonObject){
        this.url = url;
        this.location = location;
        this.jsonObject = jsonObject;
    }

    public void run(){
        // "{id : 1, type : 'car',streetName : 'kutlwano', longitude : 21.0, latitude : 21.0}";
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

               jsonObject.put("id",location.getId());
               jsonObject.put("type",location.getType());
               jsonObject.put("streetName",location.getStreetName());
               jsonObject.put("longitude",location.getLongitude());
               jsonObject.put("latitude",location.getLatitude());

               try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
               }

            System.out.println(con.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
