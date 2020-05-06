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

public class ReverseGeoCoderNominatim implements Runnable {
    private String TAG = ReverseGeoCoderNominatim.class.getSimpleName();
    private Context context;
    private String destination;
    private GeocoderNominatim geoCoder;
    private List<Address> endPoint;
    private GeoPoint startPoint;
    private List<Address> departure;
    private Handler handler;
    private Message msg;
    Bundle bundle = new Bundle();
    public ReverseGeoCoderNominatim(Context context, Handler handler, GeocoderNominatim geoCoder,GeoPoint startPoint, String destination){
        this.context = context;
        this.handler = handler;
        this.geoCoder = geoCoder;
        this.destination = destination;
        this.startPoint = startPoint;
    }

    @Override
    public void run() {
        try {
            msg=handler.obtainMessage();
            departure = geoCoder.getFromLocation(startPoint.getLatitude(),startPoint.getLongitude(),1);
            endPoint = geoCoder.getFromLocationName(destination,1);
            if(!endPoint.isEmpty()) {
                Log.v(TAG, "Destination : " + endPoint);
                bundle.putString("get-destination", "success");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }else{
                bundle.putString("get-destination-error","error");
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.v(TAG,"Destination error");
            }
            //Toast.makeText(context,endPoint.toString(),Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.v(TAG,"Nominatim exception" + e.getMessage());
        }
    }
    public Address getDepartureAddress(){
        return departure.iterator().next();
    }
    public Address getDestinationAddress(){
        return endPoint.iterator().next();
    }
    public GeoPoint getDestination(){
        return endPoint == null ? null : new GeoPoint(endPoint.iterator().next().getLatitude(),endPoint.iterator().next().getLongitude());
    }
}
