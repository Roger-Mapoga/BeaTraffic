package co.za.gmapssolutions.beatraffic;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import co.za.gmapssolutions.beatraffic.adapter.RouteDetail;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public class BeaTrafficViewModel extends ViewModel {
    private String TAG = BeaTrafficViewModel.class.getSimpleName();
    public boolean isNewlyCreated = true;
    public String requestedRoutes = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.routes";
    private MutableLiveData<Road[]> routes;
    public String userMapZoomLevel = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.mapZoomLevel";
    private MutableLiveData<Double> mapZoomLevel;
    public String userStartPoint = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.startPoint";
    private MutableLiveData<GeoPoint> startPoint;
    public String userEndPoint = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.endPoint";
    private MutableLiveData<GeoPoint> endPoint;
    public String bottomSheetHidden = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.bottomSheetHidden";
    private MutableLiveData<Boolean> userBottomSheetHidden;
    public String userBottomSheetState = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.bottomSheetState";
    private MutableLiveData<Integer> bottomSheetState;
    public String userRoute = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.userRoute";
    private MutableLiveData<RouteDetail> route;

    //    public String
    public MutableLiveData<Boolean> getBottomSheetHidden(){
        if(userBottomSheetHidden == null)
            userBottomSheetHidden = new MutableLiveData<>();
        return userBottomSheetHidden;
    }
    public MutableLiveData<RouteDetail> getRoute(){
        if(route == null)
            route = new MutableLiveData<>();
        return route;
    }
    public MutableLiveData<Integer> getBottomSheetState(){
        if(bottomSheetState == null)
            bottomSheetState = new MutableLiveData<>();
        return bottomSheetState;
    }
    public MutableLiveData<GeoPoint> getStartPoint(){
        if(startPoint == null)
            startPoint = new MutableLiveData<>();
        return startPoint;
    }
    public MutableLiveData<GeoPoint> getEndPoint(){
        if(endPoint == null)
            endPoint = new MutableLiveData<>();
        return endPoint;
    }
    public MutableLiveData<Road[]> getRoutes(){
        if(routes == null){
            routes = new MutableLiveData<>();
        }
        return routes;
    }
    public MutableLiveData<Double> getMapZoomLevel(){
        if(mapZoomLevel == null){
            mapZoomLevel = new MutableLiveData<>();
        }
        return mapZoomLevel;
    }
    //
    public BoundingBox getBoundingBox(List<GeoPoint> geoPointList){
        return BoundingBox.fromGeoPoints(geoPointList);
    }
    public double getTravelDuration(double durationInSec){
        if(durationInSec/60 > 60){
            return ( durationInSec/ 60) / 60;
        }
        return durationInSec / 60;
    }


}
