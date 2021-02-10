package co.za.gmapssolutions.beatraffic.restClient;

import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import co.za.gmapssolutions.beatraffic.domain.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.bonuspack.routing.Road;

import java.io.IOException;

import static co.za.gmapssolutions.beatraffic.domain.BeaTrafficAddress.*;
import static co.za.gmapssolutions.beatraffic.domain.User.userToJson;


public class KafkaProducerRestClient implements Runnable{
    private final String TAG = KafkaProducerRestClient.class.getSimpleName();
    private final RestClient restClient;
    private final Handler handler;
    private final Bundle bundle = new Bundle();
    private final User user;
    private final Address departure;
    private final Address destination;
    private final JSONArray jsonArray;
    private final Road[] routes;
    public KafkaProducerRestClient(RestClient restClient, Handler handler, User user, Address departure, Address destination, Road[] routes, JSONArray jsonArray){
        this.restClient = restClient;
        this.handler = handler;
        this.user = user;
        this.departure = departure;
        this.destination = destination;
        this.routes = routes;
        this.jsonArray = jsonArray;
    }

    public void run(){
        try {
            Log.i(TAG,"Starting ");
            jsonArray.put(userToJson(user));
            jsonArray.put(departureAddressToJson(user.getId(),departure));
            jsonArray.put(destinationAddressToJson(user.getId(),destination));
            jsonArray.put(routesToJson(user.getId(),routes));
            int response = restClient.post(jsonArray.toString()).getResponseCode();
            Log.i(TAG,jsonArray.toString());
            Message msg = handler.obtainMessage();
            bundle.putInt("http-post-status",response);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i(TAG,"Response: "+response);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
