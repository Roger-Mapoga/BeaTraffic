package co.za.gmapssolutions.beatraffic.domain;

import android.location.Address;
import org.json.JSONException;
import org.json.JSONObject;

public class BeaTrafficAddress {
    public static JSONObject addressToJson(Address address) throws JSONException {
        JSONObject jsonAddress = new JSONObject();
        jsonAddress.put("streetName",address.getAddressLine(0));
        jsonAddress.put("postalCode",address.getAddressLine(1));
        jsonAddress.put("city",address.getLocality());
        jsonAddress.put("country",address.getCountryName());
        jsonAddress.put("provence",address.getAdminArea());
        jsonAddress.put("road",address.getThoroughfare());
        if(address.hasLatitude())
        jsonAddress.put("latitude",address.getLatitude());
        if(address.hasLongitude())
        jsonAddress.put("longitude",address.getLongitude());

        return jsonAddress;
    }
}
