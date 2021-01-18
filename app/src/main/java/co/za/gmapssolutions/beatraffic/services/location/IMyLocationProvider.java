package co.za.gmapssolutions.beatraffic.services.location;

import org.osmdroid.util.GeoPoint;

public interface IMyLocationProvider {
    void enableLocation();
    GeoPoint getLastKnownLocation();
}
