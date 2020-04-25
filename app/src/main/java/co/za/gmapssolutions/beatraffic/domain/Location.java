package co.za.gmapssolutions.beatraffic.domain;

public class Location {
    private long id;
    private String type;
    private String streetName;
    private double longitude;
    private double latitude;
    public Location(long id,String type,String streetName,double longitude,double latitude){
        this.id = id;
        this.type = type;
        this.streetName = streetName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStreetName() {
        return streetName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
