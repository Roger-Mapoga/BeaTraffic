package co.za.gmapssolutions.beatraffic.restClient;

import android.location.Address;
import android.util.Log;
import co.za.gmapssolutions.beatraffic.domain.User;
import org.json.JSONArray;
import org.osmdroid.bonuspack.routing.Road;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static co.za.gmapssolutions.beatraffic.domain.BeaTrafficAddress.*;
import static co.za.gmapssolutions.beatraffic.domain.User.userToJson;


public class KafkaProducerRestClient implements Runnable{
    private String TAG = KafkaProducerRestClient.class.getSimpleName();
    private URL url;
    private HttpURLConnection con;
    private User user;
    private Address departure,destination;
    private JSONArray jsonArray;
    private Road[] routes;
    public KafkaProducerRestClient(URL url, User user, Address departure, Address destination, Road[] routes, JSONArray jsonArray){
        this.url = url;
        this.user = user;
        this.departure = departure;
        this.destination = destination;
        this.routes = routes;
        this.jsonArray = jsonArray;
    }

    public void run(){
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
//            Log.i(TAG,routesToJson(user.getId(),routes).getJSONObject(1).toString());
            jsonArray.put(userToJson(user));
            jsonArray.put(departureAddressToJson(user.getId(),departure));
            jsonArray.put(destinationAddressToJson(user.getId(),destination));
            jsonArray.put(routesToJson(user.getId(),routes));
//            Log.i(TAG,routesToJson(user.getId(),routes).toString());

            Log.i(TAG,jsonArray.toString());
            //Log.i(TAG,destinationAddressToJson(user.getId(),destination).toString());
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonArray.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            System.out.println(con.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
