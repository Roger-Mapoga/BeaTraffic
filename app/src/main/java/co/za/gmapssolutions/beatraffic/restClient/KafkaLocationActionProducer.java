package co.za.gmapssolutions.beatraffic.restClient;

import android.location.Location;
import co.za.gmapssolutions.beatraffic.domain.User;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

public class KafkaLocationActionProducer implements Runnable {
    private ArrayBlockingQueue<Location> locations;
    private URL url;
    private HttpURLConnection con;
    private JSONObject jsonObject;
    private User user;
    public KafkaLocationActionProducer(URL url, ArrayBlockingQueue<Location> locations, User user, JSONObject jsonObject){
        this.url = url;
        this.locations = locations;
        this.user = user;
        this.jsonObject = jsonObject;
    }
    @Override
    public void run() {
        while (locations.size() > 0) {
            Location location = locations.poll();
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);
                jsonObject.put("User", user);
                jsonObject.put("Latitude", location.getLatitude());
                jsonObject.put("Longitude", location.getLongitude());
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                System.out.println(con.getResponseCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
