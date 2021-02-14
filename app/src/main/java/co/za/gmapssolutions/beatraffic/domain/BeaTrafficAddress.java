package co.za.gmapssolutions.beatraffic.domain;

import android.location.Address;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;

public class BeaTrafficAddress {
    public static JSONObject departureAddressToJson(Long userId,Address address) throws JSONException {
        JSONObject jsonAddress = new JSONObject();
        jsonAddress.put("userId",userId);
        jsonAddress.put("streetName",address.getAddressLine(0));
        jsonAddress.put("postalCode",address.getAddressLine(1));
        if(address.getLocality() != null)
        jsonAddress.put("city",address.getLocality());
        else
        jsonAddress.put("city","not provided");
        jsonAddress.put("country",address.getCountryName());
        jsonAddress.put("provence",address.getAdminArea());
        jsonAddress.put("road",address.getThoroughfare());
        if(address.hasLatitude())
        jsonAddress.put("latitude",address.getLatitude());
        if(address.hasLongitude())
        jsonAddress.put("longitude",address.getLongitude());

        return jsonAddress;
    }
    public static JSONObject destinationAddressToJson(Long userId,Address address) throws JSONException {
        JSONObject jsonAddress = new JSONObject();
        jsonAddress.put("userId",userId);
        jsonAddress.put("postalCode",address.getPostalCode());
        if(address.getLocality() != null)
            jsonAddress.put("city",address.getLocality());
        else
            jsonAddress.put("city","not provided");
        jsonAddress.put("country",address.getCountryName());
        jsonAddress.put("provence",address.getAdminArea());
        if(address.hasLatitude())
            jsonAddress.put("latitude",address.getLatitude());
        if(address.hasLongitude())
            jsonAddress.put("longitude",address.getLongitude());
        return jsonAddress;
    }
    public static JSONArray routesToJson(Long userId, Road[] routes) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        int count = 0;
        jsonObject.put("userId",userId);
        for(Road route : routes) {
            jsonObject.put("route-to-destination-"+count, route.mRouteHigh);
            count++;
        }
        jsonArray.put(jsonObject);
        return jsonArray;
    }
}
