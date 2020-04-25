package co.za.gmapssolutions.beatraffic.nominatim;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public class ReverseGeocoderNominatim implements Runnable {
    private String TAG = ReverseGeocoderNominatim.class.getSimpleName();
    private Context context;
    private String destination;
    private GeocoderNominatim geocoder;
    private List<Address> endPoint;
    private Handler handler;
    private Message msg;
    Bundle bundle = new Bundle();
    public ReverseGeocoderNominatim(Context context,Handler handler, GeocoderNominatim geocoder, String destination){
        this.context = context;
        this.handler = handler;
        this.geocoder = geocoder;
        this.destination = destination;
    }

    @Override
    public void run() {
        try {
            msg=handler.obtainMessage();
            endPoint = geocoder.getFromLocationName(destination,1);
            Log.v(TAG,"Destination : " + endPoint.toString());
            bundle.putString("get-destination","success");
            msg.setData(bundle);
            handler.sendMessage(msg);
            //Toast.makeText(context,endPoint.toString(),Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.v(TAG,e.getMessage());
        }
    }
    public List<Address> getGeocoder(){
        return endPoint;
    }
    public GeoPoint getDestination(){
        return endPoint == null ? null : new GeoPoint(endPoint.iterator().next().getLatitude(),endPoint.iterator().next().getLongitude());
    }
}
