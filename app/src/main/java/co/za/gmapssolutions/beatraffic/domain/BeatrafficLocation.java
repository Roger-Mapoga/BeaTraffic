package co.za.gmapssolutions.beatraffic.domain;

import android.location.Location;
import org.json.JSONException;
import org.json.JSONObject;

public class BeatrafficLocation {
    private final User user;
    private final Location location;
    public BeatrafficLocation(User user, Location location){
        this.user = user;
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }
    public static JSONObject locationToJson(BeatrafficLocation location) throws JSONException {
        JSONObject jsonUserLocation =  new JSONObject();
        jsonUserLocation.put("id",location.getUser().getId());
        jsonUserLocation.put("type",location.getUser().getType());
        jsonUserLocation.put("latitude",location.getLocation().getLatitude());
        jsonUserLocation.put("longitude",location.getLocation().getLongitude());
        return jsonUserLocation;
    }
}
