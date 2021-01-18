package co.za.gmapssolutions.beatraffic;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;

import java.util.List;

public class BeaTrafficViewModel extends ViewModel {
    private String TAG = BeaTrafficViewModel.class.getSimpleName();
    public boolean isNewlyCreated = true;
//    public Road[] routes;
//    public Double mapZoomLevel;
    public String requestedRoutes = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.routes";
    private MutableLiveData<Road[]> routes;
    public String userMapZoomLevel = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.mapZoomLevel";
    private MutableLiveData<Double> mapZoomLevel;
    public String userStartPoint = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.startPoint";
    private MutableLiveData<GeoPoint> startPoint;
    public String userEndPoint = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.endPoint";
    private MutableLiveData<GeoPoint> endPoint;
//    public String userShowBottomSheet = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.showBottomSheet";
//    private final MutableLiveData<Boolean> showBottomSheet = new MutableLiveData<>(false);
    public String userBottomSheetState = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.bottomSheetState";
    private MutableLiveData<Integer> bottomSheetState;
//    private MutableLiveData<BoundingBox> boundingBox;
    public String userBtnStartDrive = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.btnStartDrive";
    private MutableLiveData<String> btnStartDrive;
    public MutableLiveData<String> getBtnStartDriveState(){
        if(btnStartDrive == null)
            btnStartDrive = new MutableLiveData<>();
        return btnStartDrive;
    }
    public MutableLiveData<Integer> getBottomSheetState(){
        if(bottomSheetState == null)
            bottomSheetState = new MutableLiveData<>(BottomSheetBehavior.STATE_HIDDEN);
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
        try {
            return BoundingBox.fromGeoPoints(geoPointList);
        }catch (IllegalArgumentException e){
            final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
            return new BoundingBox(tileSystem.getMaxLatitude(),
                    tileSystem.getMaxLongitude(),
                    tileSystem.getMinLatitude(),
                    tileSystem.getMinLongitude()
            );
        }
    }
    public double getTravelDuration(double durationInSec){
        if(durationInSec/60 > 60){
            return ( durationInSec/ 60) / 60;
        }
        return durationInSec / 60;
    }


}
