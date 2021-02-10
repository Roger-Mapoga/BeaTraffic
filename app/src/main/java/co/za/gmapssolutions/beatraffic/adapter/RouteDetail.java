package co.za.gmapssolutions.beatraffic.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class RouteDetail implements Parcelable {
    private String routeDetails;
    private String btnRouteStateValue;
    private int id;
    public RouteDetail(int id){
        this.id = id;
    }

    protected RouteDetail(Parcel in) {
        routeDetails = in.readString();
        btnRouteStateValue = in.readString();
        id = in.readInt();
    }

    public static final Creator<RouteDetail> CREATOR = new Creator<RouteDetail>() {
        @Override
        public RouteDetail createFromParcel(Parcel in) {
            return new RouteDetail(in);
        }

        @Override
        public RouteDetail[] newArray(int size) {
            return new RouteDetail[size];
        }
    };

    public int getId(){ return id;}
    public void setId(int id){
        this.id = id;
    }
    public String getRouteDetails() {
        return routeDetails;
    }
    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }

    public String getBtnRouteStateValue() {
        return btnRouteStateValue;
    }

    public void setBtnRouteStateValue(String btnAltRouteStateValue) {
        this.btnRouteStateValue = btnAltRouteStateValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(routeDetails);
        parcel.writeString(btnRouteStateValue);
        parcel.writeInt(id);
    }
}
