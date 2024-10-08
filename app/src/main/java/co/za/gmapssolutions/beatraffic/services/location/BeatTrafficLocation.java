package co.za.gmapssolutions.beatraffic.services.location;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class BeatTrafficLocation implements LocationListener{

    private  final String TAG = BeatTrafficLocation.class.getSimpleName();
    private Location location;

    @Override
    public void onLocationChanged(Location location){
        this.location = location;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getCurrentLocation(){
        return location;
    }
}
