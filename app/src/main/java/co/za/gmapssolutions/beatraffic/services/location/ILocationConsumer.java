package co.za.gmapssolutions.beatraffic.services.location;

import android.location.Location;

public interface ILocationConsumer {
    void updateLocation(Location location);
}
